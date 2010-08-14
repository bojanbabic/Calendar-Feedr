package com.metabroadcast.mashups.feedr.core;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheFactory;
import net.sf.jsr107cache.CacheManager;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.repackaged.org.json.JSONArray;
import com.google.appengine.repackaged.org.json.JSONObject;
import com.google.gdata.data.DateTime;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.calendar.CalendarEntry;
import com.google.gdata.data.calendar.CalendarEventEntry;
import com.google.gdata.data.calendar.CalendarFeed;
import com.google.gdata.data.extensions.When;
import com.metabroadcast.mashups.feedr.google.calendar.CalendarClient;
import com.metabroadcast.mashups.feedr.google.calendar.CalendarUtil;
import com.metabroadcast.mashups.feedr.manager.EntityManager;
import com.metabroadcast.mashups.feedr.model.Media;
import com.metabroadcast.mashups.feedr.model.AuthenticatedUser;
import com.metabroadcast.mashups.feedr.model.ProcessedMediaLog;
import com.metabroadcast.mashups.feedr.model.TaskExecution;
import com.metabroadcast.mashups.feedr.util.JCacheUtil;

public class FeedrTask {
	private static final Logger logger = Logger.getLogger(FeedrTask.class
			.getName());
	private Cache cache;
	CalendarClient calendarClient;
	EntityManager manager;
	private static final String URL = "http://atlasapi.org/2.0/items.json?transmissionTime-after=now&transportType=link&limit=40";

	public FeedrTask() {
		manager = new EntityManager();
		try{
			CacheFactory cacheFactory = CacheManager.getInstance().getCacheFactory();
			cache = cacheFactory.createCache(Collections.emptyMap());
		} catch (CacheException ce) {
			logger.warning("Cache exception:"+ce);
		}
	}

	public void core() throws Exception {

		List<Media> newCalendarEvents = getLatestMedia();
		String userCacheKey = JCacheUtil.getKeyDailyKeyLabel("user_processed");
		List<String> processedUsers = new ArrayList<String>();
		
		if (cache.containsKey(userCacheKey)){
			processedUsers = (List<String>) cache.get(userCacheKey);
		}
		
		if (newCalendarEvents == null || newCalendarEvents.size() == 0){
			logger.info("No media returned from query:"+URL);
		}
		List<AuthenticatedUser> authorizedUsers = manager.getAuthenticatedUsers();
		if (authorizedUsers == null || authorizedUsers.size() == 0){
			logger.info("No authorized users.");
			return;
		}
		// for every registered user update events in calendar
		for (AuthenticatedUser user : authorizedUsers) {
			logger.info("processing user:"+user.getUser().getEmail());
			List<Media> newEntries = removeDuplicates(newCalendarEvents);
			String userMail = user.getUser().getEmail();
			if (processedUsers.contains(userMail)){
				logger.info("skipping already processed user:"+userMail);
				continue;
			}
			if (user.getToken() == null){
				logger.info("user not authenticated.");
			}
			// retrieve calendar
			
			String calendarId = user.getCalendarUid();
			if (calendarId == null){
				logger.warning("Can't find user calendar uid. trying seeking by name");
				CalendarEntry fCalendar = calendarClient.getCalendarByName(CalendarUtil.CALENDAR_FEEDR_TITLE);
				if (fCalendar == null){
					logger.warning("Can't retrieve calendar name. Skipping user:"+user.getUser().getEmail());
					continue;
				}
				calendarId  = CalendarUtil.getCalendarUid(fCalendar.getId());
			}

			
			updateEvents(user, newEntries, calendarId);
			// On successful job remembers processed user
			processedUsers.add(userMail);
			Key key = KeyFactory.createKey(TaskExecution.class
					.getSimpleName(), userMail);
			TaskExecution te = new TaskExecution(userMail, new Date(System.currentTimeMillis()));
			te.setKey(key);
			manager.saveTaskExecution(te);
		}
		cache.put(userCacheKey, processedUsers);
	}

	public void updateEvents(AuthenticatedUser authUser,
			List<Media> newCalendarEntries, String calendarId) throws Exception {
		calendarClient = new CalendarClient(authUser.getToken());
		calendarClient.initCalendarClient(authUser.getToken());

		for (Media mediaEntry : newCalendarEntries) {
			
			logger.info("new entry:"+mediaEntry);
			for (String scheduleDate : mediaEntry.getOcurringDates()) {
				DateTime now = new DateTime(System.currentTimeMillis());
				
				CalendarEventEntry entry = CalendarUtil.createCalendarEventEntry(scheduleDate, mediaEntry);
				DateTime startTime = null;
				if (entry.getTimes() != null){
					startTime = entry.getTimes().get(0).getStartTime();
				}
				// create events only for upcoming event that are not added to
				// users calendar
				if ( startTime == null || startTime.compareTo(now) < 0) {
					logger.info("outdated event or bad date:"+startTime);
					continue;
				}
				boolean eventAlreadyAdded = false;
				List<ProcessedMediaLog> pmLog = manager.getMediaForUser(authUser.getUser().getEmail(), mediaEntry.hashCode());
				if (pmLog == null || pmLog.size() > 0){
					eventAlreadyAdded = true;
				}
				if (eventAlreadyAdded) {
					logger.info("repeated event entry.Skipping:"+mediaEntry + " for scheduled date:"+scheduleDate);
					continue;
				}
				// update database with user and calendar hashcode
				logger.info("creating event. title:"+entry.getTitle().getPlainText()+", content:"+entry.getContent().toString()+", time:"+entry.getTimes().get(0).getStartTime());
				calendarClient.addMediaEvent(calendarId, entry);
				ProcessedMediaLog processedMediaLog = new ProcessedMediaLog(authUser.getUser().getEmail(), mediaEntry.hashCode(), new Date(System.currentTimeMillis()));
				manager.saveProcessedMediaLog(processedMediaLog);
				logger.info("media processed:"+mediaEntry);

			}
		}
	}

	public List<Media> getLatestMedia() {

		List<Media> mediaList = new ArrayList<Media>();
		try {
			Long now = System.currentTimeMillis();
			URL url = new URL(URL);
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					url.openStream(), "UTF-8"));
			StringBuffer json = new StringBuffer();
			String line;
			while ((line = reader.readLine()) != null) {
				json.append(line);
			}
			reader.close();
			JSONObject jsonObject = null;
			try{
				jsonObject = new JSONObject(json.toString());
			} catch (Exception e) {
				logger.warning("json error:"+e);
			}
			
			if (jsonObject != null && jsonObject.has("items")) {
				JSONArray jsonArray = jsonObject.getJSONArray("items");

				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject object = jsonArray.getJSONObject(i);
					String title = object.getString("title");
					String desc = object.getString("description");
					JSONObject locations = object.getJSONArray("locations")
							.getJSONObject(0);
					String link = locations.getString("uri");

					JSONArray recurrences = object.getJSONArray("broadcasts");
					// Media media = new Media(title, desc, link, date);
					Media media = new Media();
					// media.setKey(KeyFactory.cre)
					media.setTitle(title);
					if (desc.length() > 499) {
						media.setDesc(desc.substring(0, 496) + "...");
					} else {
						media.setDesc(desc);
					}

					media.setLink(link);

					for (int j = 0; j < recurrences.length(); j++) {
						JSONObject singleBroadcast = recurrences
								.getJSONObject(j);
						String transmissionTime = singleBroadcast
								.getString("transmission_time");
						String transmission_end_time = singleBroadcast
								.getString("transmission_end_time");
						media.getOcurringDates().add(transmissionTime);
					}

					mediaList.add(media);
					manager.saveMedia(media);
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		return mediaList;
	}

	public List<Media> removeDuplicates(List<Media> all) {

		return all;
	}

	// public static void main(String[] args){
	// FeedrTask feedrTask = new FeedrTask();
	// List<Media> medias =feedrTask.getLatestMedia();
	// for (Media media: medias){
	// logger.info(media.toString());
	// }
	//		
	// }
}

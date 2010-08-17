package com.metabroadcast.mashups.feedr.google.calendar;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

import com.google.gdata.client.calendar.CalendarService;
import com.google.gdata.data.DateTime;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.calendar.CalendarEntry;
import com.google.gdata.data.calendar.CalendarEventEntry;
import com.google.gdata.data.calendar.CalendarFeed;
import com.google.gdata.data.calendar.ColorProperty;
import com.google.gdata.data.calendar.HiddenProperty;
import com.google.gdata.data.extensions.When;
import com.google.gdata.model.gd.EventEntry;
import com.google.gdata.util.ServiceException;
import com.metabroadcast.mashups.feedr.model.Media;
import com.sun.org.apache.xerces.internal.util.URI.MalformedURIException;

public class CalendarClient {
	private static final Logger logger = Logger.getLogger(CalendarClient.class.getName());
	private static final String ALL_CALENDARS_URL = "http://www.google.com/calendar/feeds/default/allcalendars/full";
	private static final String OWN_CALENDARS_URL = "https://www.google.com/calendar/feeds/default/owncalendars/full";
	private URL feedUrl;
	CalendarFeed calendarFeed;
	CalendarService calendarService;
	private String sessionToken;
	private static final String PRIVATE_CALENDAR_URL = "https://www.google.com/calendar/feeds/CALENDAR_ID@group.calendar.google.com/private/full";

	public CalendarClient() {
		
	}

	public CalendarClient(String token) throws Exception {
		initCalendarClient(token);
	}
	public void initCalendarClient(String sessionToken) throws MalformedURLException, ServiceException, IOException{
		if (sessionToken == null){
			// TODO log this 
			return;
		}
		feedUrl = new URL(ALL_CALENDARS_URL);
		calendarService = new CalendarService("google-feedr-v1.0");
		calendarService.setAuthSubToken(sessionToken);
		calendarFeed = calendarService.getFeed(feedUrl, CalendarFeed.class);
		
	}

	public CalendarFeed getAllCalendarInfo() throws ServiceException, IOException {
		if (calendarService == null){
			return null;
		}
		CalendarFeed calendarFeed = null;
		calendarFeed = calendarService.getFeed(feedUrl, CalendarFeed.class);
		return calendarFeed;
	}
	
	public CalendarEntry getCalendarByName(String calendarName) throws ServiceException, IOException{
		
		CalendarFeed calendarFeed = getAllCalendarInfo();
		for (CalendarEntry entry: calendarFeed.getEntries()){
			logger.info("Scanning calendar entry:"+ entry.getTitle().getPlainText());
			if (entry.getTitle().getPlainText().equals(calendarName)){
				logger.info("Found existing calendar with name:"+calendarName);
				return entry;
			}
			
		}
		logger.info("Calendar with name:"+calendarName+" does not exist.");
		return null;
	}
	
	public String addNewCalendar(CalendarEntry calendarEntry) throws ServiceException, IOException{

		if (calendarService == null){
			logger.warning("Calendar service not init.");
			return null;
		}
		
		CalendarEntry existingCalendar = getCalendarByName(calendarEntry.getTitle().getPlainText());
		if (existingCalendar != null){
			logger.info("Calendar already exists.");
			String calendarUid = CalendarUtil.getCalendarUid(existingCalendar.getId());
			return calendarUid;
		}		

		URL url = new URL(OWN_CALENDARS_URL);
		CalendarEntry newcalendar = calendarService.insert(url, calendarEntry);
		
		String calendarUid = CalendarUtil.getCalendarUid(newcalendar.getId());
		return calendarUid;
	}
	
	public void addMediaEvent(String calendarId, CalendarEventEntry eventEntry) throws MalformedURLException, IOException, ServiceException{
		URL url = new URL(PRIVATE_CALENDAR_URL.replace("CALENDAR_ID", calendarId)); 
		calendarService.insert(url, eventEntry);
		
	}
	public void addMediaEvent1(String calendarId, CalendarEventEntry eventEntry) throws MalformedURLException, IOException, ServiceException{
		URL url = new URL(calendarId);
		calendarService.insert(url, eventEntry);
		
	}
	public void setPrivateMode() throws MalformedURLException{
		feedUrl = new URL(OWN_CALENDARS_URL);
	}
	
	public String getCalendarId(){
		String calendarId = null;
		for (CalendarEntry calendarEntry: calendarFeed.getEntries()){
			if (calendarEntry.getTitle().getPlainText().equals(CalendarUtil.CALENDAR_FEEDR_TITLE)){
				logger.info(""+calendarEntry.getTitle().getPlainText()+" calendar id:"+calendarEntry.getId() );
				calendarId = CalendarUtil.getCalendarUid(calendarEntry.getId());
				return calendarId;
			}
			
		}
		return calendarId;

	}

}

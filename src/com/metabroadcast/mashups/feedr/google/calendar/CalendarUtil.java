package com.metabroadcast.mashups.feedr.google.calendar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import com.google.gdata.data.DateTime;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.calendar.CalendarEntry;
import com.google.gdata.data.calendar.CalendarEventEntry;
import com.google.gdata.data.calendar.ColorProperty;
import com.google.gdata.data.calendar.HiddenProperty;
import com.google.gdata.data.extensions.When;
import com.metabroadcast.mashups.feedr.model.Media;

public class CalendarUtil {

	private static Logger logger = Logger.getLogger(CalendarUtil.class.getName());
	public static final String CALENDAR_FEEDR_TITLE = "Calendar Feedr - TV Schedule";
	public static final String CALENDAR_FEEDR_SUMMARY = "Your tv program subscription. Provided by Atlas.";

	public CalendarUtil(){
		
	}
	public CalendarEntry createCalendar(){
		CalendarEntry entry = new CalendarEntry();
		entry.setTitle(new PlainTextConstruct(CALENDAR_FEEDR_TITLE));
		entry.setSummary(new PlainTextConstruct(CALENDAR_FEEDR_SUMMARY));
		entry.setHidden(HiddenProperty.FALSE);
//		entry.setColor(new ColorProperty("#330066"));
		return entry;
	}
	
	public static CalendarEventEntry createCalendarEventEntry(String scheduleDate, Media media){
		
		// calendar sample: 17-Aug-2010 16:40:00
		DateTime dateTime = null;
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss");
		try{
			Date date = dateFormat.parse(scheduleDate);
			dateTime = new DateTime(date);
//			dateTime = DateTime.parseDate(scheduleDate);
		} catch (NumberFormatException e) {
			logger.info("bad date format:"+scheduleDate);
		} catch (ParseException pe){
			logger.info("Parse exception:"+pe);
		} catch (Exception exception){
			logger.warning("unexpected date format exception:"+exception);
		}

		logger.info("creating calendar event:");
		CalendarEventEntry entry = new CalendarEventEntry();
		entry.setTitle(new PlainTextConstruct(media.getTitle()));
		logger.info("calendar title:"+media.getTitle());
		entry.setContent(new PlainTextConstruct(media.getDesc()));
		logger.info("calendar content:"+media.getDesc());
		When when = new When();
		when.setStartTime(dateTime);
		entry.addTime(when);
		
		return entry;

	}
	
	public static String getCalendarUid(String calendarId){
		String calendarUid = null;
		calendarUid = calendarId.replaceAll("^.*calendars/", "").replaceAll("%40.*", "");
		return calendarUid;
	}
	
	public static void main(String[] args){
		String calId = CalendarUtil.getCalendarUid("http://www.google.com/calendar/feeds/default/calendars/bpdvfro47qmqdgfnkeanrtn8lc%40group.calendar.google.com");
		logger.info("calendar id:"+calId);
	}
	
	
}

package com.metabroadcast.mashups.feedr.authenticate;

import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.util.Date;
import java.util.Map;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.labs.taskqueue.Queue;
import com.google.appengine.api.labs.taskqueue.QueueFactory;
import com.google.appengine.api.labs.taskqueue.TaskOptions;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.tools.util.Logging;
import com.google.gdata.client.GoogleAuthTokenFactory.UserToken;
import com.google.gdata.client.calendar.CalendarService;
import com.google.gdata.client.http.AuthSubUtil;
import com.google.gdata.data.Feed;
import com.google.gdata.data.calendar.CalendarEntry;
import com.google.gdata.data.calendar.CalendarFeed;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceForbiddenException;
import com.metabroadcast.mashups.feedr.google.calendar.CalendarClient;
import com.metabroadcast.mashups.feedr.google.calendar.CalendarUtil;
import com.metabroadcast.mashups.feedr.manager.EntityManager;
import com.metabroadcast.mashups.feedr.manager.ManagerFactory;
import com.metabroadcast.mashups.feedr.model.AuthenticatedUser;

public class AuthenticationSub extends HttpServlet {
	private Logger logger = Logger.getLogger(AuthenticationSub.class.getName());
	private EntityManager manager = new EntityManager();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		PrivateKey privateKey = null;
		boolean newSubscriber = true;

		try {

			privateKey = AuthSubUtil.getPrivateKeyFromKeystore(
					"calendarfeedr.jks", "calfeedr", "calendarfeedr",
					"calfeedr");
			String singleUseToken = AuthSubUtil.getTokenFromReply(req
					.getQueryString());
			singleUseToken = URLDecoder.decode(singleUseToken, "UTF-8");
			String sessionToken = null;
			logger.info("single use token:" + singleUseToken + "\n");
			sessionToken = AuthSubUtil.exchangeForSessionToken(singleUseToken,
					privateKey);
			logger.info("retrieved permanent session token:"+sessionToken);
			CalendarClient calendarClient = new CalendarClient(sessionToken);

			CalendarUtil cutil = new CalendarUtil();
			CalendarEntry calendar = cutil.createCalendar();
			String calendarUid = null;
			boolean serviceForbiden = false;
			try {
				calendarUid = calendarClient.addNewCalendar(calendar);
			} catch (IOException e) {
				logger.warning("Timeout while creating new calendar.");
			} catch (ServiceForbiddenException sfe) {
				logger.warning("Service forbiden!");
				serviceForbiden = true;
			} catch (Exception e) {
				logger.warning("Unexpected exception while creating new calendar:"+e);
				e.printStackTrace();
			}
			
			if (serviceForbiden){
				resp.getWriter().print("It looks like you need to visit <a href=\"http://google.com/calendar\">Google Calendar</a> in order to init public calendars. Afterwards come back and try again.");
				return;
			}

			UserService userService = UserServiceFactory.getUserService();
			User user = userService.getCurrentUser();
//			buffer.append(privateKey).append(user.getEmail());
			if (user == null){
				logger.severe("can't authenticate user!");
				resp.sendRedirect("/");
			}

			AuthenticatedUser authUser = manager.getAuthenticatedUser(user);

			if (authUser == null || authUser.getToken() == null) {
				logger.info("Adding new authenticated user:" + user);
				authUser = new AuthenticatedUser(user, sessionToken, new Date(), null);
				Key key = KeyFactory.createKey(AuthenticatedUser.class
						.getSimpleName(), user.getEmail());
				authUser.setKey(key);
				authUser.setCalendarUid(calendarUid);
				manager.saveAuthenticatedUser(authUser);
				// add task for adding new calendar entries
				Queue queue = QueueFactory.getDefaultQueue();
				queue.add(TaskOptions.Builder.url("/updatecalendar"));
			} else {
				logger.info("Existing authenticated user:" + user.getEmail());
				newSubscriber = false;
			}

		} catch (AuthenticationException ae) {
			logger.severe("Authentication exception:" + ae + "\n");
			ae.printStackTrace();
		} catch (GeneralSecurityException gse) {
			logger.severe("General security exception:" + gse);
			gse.printStackTrace();
		} catch (Exception e) {
			logger.severe("e:" + e + "\nline:"+"\n");
			e.getStackTrace();
		}
		resp.sendRedirect("/");
	}
}

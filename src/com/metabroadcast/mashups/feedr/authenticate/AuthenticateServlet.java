package com.metabroadcast.mashups.feedr.authenticate;

import java.io.IOException;
import java.security.PrivateKey;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gdata.client.http.AuthSubUtil;
import com.metabroadcast.mashups.feedr.google.calendar.CalendarUtil;
import com.metabroadcast.mashups.feedr.manager.EntityManager;
import com.metabroadcast.mashups.feedr.manager.ManagerFactory;
import com.metabroadcast.mashups.feedr.model.AuthenticatedUser;
import com.metabroadcast.mashups.feedr.model.TaskExecution;

public class AuthenticateServlet extends HttpServlet {
	private Logger logger = Logger.getLogger(this.getClass().getName());
	private EntityManager manager = new EntityManager();
	private static final boolean testing = false;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		// USER login
		logger.info("Started init of auth");
		UserService userService = UserServiceFactory.getUserService();
		User user = userService.getCurrentUser();

		if (user == null) {
			resp.sendRedirect(userService.createLoginURL(req.getRequestURI()));
		}
		
		AuthenticatedUser authUser = manager.getAuthenticatedUser(user);
		StringBuffer buffer = new StringBuffer();
		if (authUser != null && authUser.getToken() != null) {
			TaskExecution taskExecutionedOn = manager.getLatestTaskExecution(authUser.getUser().getEmail());
			String excutionString = "";
			if (taskExecutionedOn == null){
				excutionString = "Your calendar have been queued for excution. In a while, check your calendar with name:"+CalendarUtil.CALENDAR_FEEDR_TITLE;
				
			} else {
				SimpleDateFormat format = new SimpleDateFormat("dd MM yyyy HH:mm");
				String xOn = format.format(taskExecutionedOn.getExecutedOn());
				excutionString = "Your calendar has been updated last time on:"+xOn+". Hope you enjoy service.";
			}
			
			buffer
					.append("Your calendar has been updated last time on:<sample_data>. Hope you enjoy service.");
			resp.getWriter().print(buffer.toString() + "\n");
		} else {
			// String hostedDomain = "calendarfeedr.appspot.com/";
			String nextUrl = "http://calendarfeedr.appspot.com/authsub";
			String scope = "http://www.google.com/calendar/feeds/";
			boolean secure = false;
			boolean session = true;
			String authSubUrl = AuthSubUtil.getRequestUrl(nextUrl, scope,
					secure, session);
			String authorizationUrl = "<p>CalendarFeedr needs access to your Google Calendar account.<br> "
					+ "To authorize CalendarFeedr to access your account, <a href=\""
					+ authSubUrl + "\">log in to your account</a>.</p>";
			buffer.append(authorizationUrl);
			resp.getWriter().println(buffer.toString());

		}
	}
}

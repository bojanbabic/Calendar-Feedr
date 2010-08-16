package com.metabroadcast.mashups.feedr.authenticate;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheFactory;
import net.sf.jsr107cache.CacheManager;

import com.metabroadcast.mashups.feedr.model.AuthenticatedUser;
import com.metabroadcast.mashups.feedr.model.ProcessedMediaLog;
import com.metabroadcast.mashups.feedr.model.TaskExecution;

import com.metabroadcast.mashups.feedr.manager.EntityManager;
import com.metabroadcast.mashups.feedr.util.Cnstats;
import com.metabroadcast.mashups.feedr.util.JCacheUtil;

public class DeauthenticateUser extends HttpServlet {

	EntityManager manager;
	private Cache cache;
	private Logger logger = Logger
			.getLogger(DeauthenticateUser.class.getName());

	public DeauthenticateUser() {
		manager = new EntityManager();
		try {
			CacheFactory cacheFactory = CacheManager.getInstance()
					.getCacheFactory();
			cache = cacheFactory.createCache(Collections.emptyMap());
		} catch (CacheException ce) {
			logger.warning("Cache exception:" + ce);
		}

	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		Logger logger = Logger.getLogger(DeauthenticateUser.class.getName());
		String calendarId = req.getParameter("uniqId");
		try {

			if (calendarId == null && calendarId.length() == 0) {
				logger.info("Empty calendar id. exit");
				resp.sendRedirect("/");
			}
			AuthenticatedUser currentUser = manager
					.getAuthenticatedUserByCalendarID(calendarId);
			if (currentUser == null) {
				logger
						.info("Can't retrieve user for calendar id:"
								+ calendarId);
				resp.sendRedirect("/");
			}

			List<ProcessedMediaLog> mediaLogs = manager.getLogsForUser(currentUser.getUser().getEmail());
			
			if (mediaLogs != null && mediaLogs.size() > 0){
				for (ProcessedMediaLog log: mediaLogs){
					logger.info("deleting media log:"+log);
					manager.deleteProcessedMediaLog(log);
				}
			}
			
			List<TaskExecution> tasksLogged = manager
					.getTaskExecutionForUser(currentUser.getUser().getEmail());
			if (tasksLogged != null && tasksLogged.size() > 0) {
				for (TaskExecution te : tasksLogged) {
					logger.info("deleting user task execution log:" + te);
					manager.deleteTasksExecutedForUser(te);
				}
			}
			String userCacheKey = JCacheUtil
					.getKeyDailyKeyLabel(Cnstats.CACHE_ENTRY_USER_PROCESSED);
			if (userCacheKey != null) {
				logger.info("deleting user cache entry");
				cache.remove(userCacheKey);
			}
			
			logger.info("Deleting user:" + currentUser);
			manager.deleteAuthenticatedUser(currentUser);

		} catch (Exception e) {
			logger.warning("exception occured while signing off from service:"
					+ e);
		}

		resp.sendRedirect("/");

	}
}

package com.metabroadcast.mashups.feedr.manager;

import java.util.List;
import java.util.logging.Logger;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.google.appengine.api.users.User;
import com.metabroadcast.mashups.feedr.model.Media;
import com.metabroadcast.mashups.feedr.model.AuthenticatedUser;
import com.metabroadcast.mashups.feedr.model.ProcessedMediaLog;
import com.metabroadcast.mashups.feedr.model.TaskExecution;

public class EntityManager {
	private Logger logger = Logger.getLogger(EntityManager.class.getName());
	private PersistenceManager factory = ManagerFactory.get().getPersistenceManager();
	
	public void saveMedia(Media media){
		try{
			factory.makePersistent(media);
		} finally {
		}
	}
	
	
	public void saveAuthenticatedUser(AuthenticatedUser authUser){
		try{
			logger.info("Persisting user:"+authUser);
			factory.makePersistent(authUser);
		} catch (Exception e) {
			logger.warning("Error while persistng user:"+authUser);
		} finally {
		}
	}
	
	public void saveProcessedMediaLog(ProcessedMediaLog processedMediaLog){
		try{
			logger.info("Persisting pmLog:"+processedMediaLog);
			factory.makePersistent(processedMediaLog);
		} catch (Exception e) {
			logger.warning("exception e:"+e);
		} finally{
		}
	}
	
	public AuthenticatedUser getAuthenticatedUser(User user){
		AuthenticatedUser authenticatedUser = null;
		try{
			if(user == null){
				logger.info("null user provided to auth method:");
				return null;
			}
			logger.info("trying to get info for user:"+user.getEmail());
			authenticatedUser = factory.getObjectById(AuthenticatedUser.class, user.getEmail());
		} catch (JDOObjectNotFoundException jonfe) {
			logger.info("Entity not found:"+user);
		} catch (Exception e) {
			logger.warning("Exception while getting authenticated user:"+e);
		} finally{
		}
		return authenticatedUser;
	}
	
	public List<AuthenticatedUser> getAuthenticatedUsers(){
		List<AuthenticatedUser> authenticatedUsers = null;
		try{
			authenticatedUsers = (List<AuthenticatedUser>) factory.newQuery("select from "+AuthenticatedUser.class.getName()).execute();
		} catch (Exception e) {
			logger.warning("Exception while getting authenticated userS:"+e);
		} finally{
		}
		return authenticatedUsers;
	}
	
	public List<ProcessedMediaLog> getMediaForUser(String userMail, int mediaHsh){
		List<ProcessedMediaLog> processedMedia = null;
		try{
			Query query = factory.newQuery(ProcessedMediaLog.class, 
					" userMail == userMailParam && " +
					"mediaHash == mediaHashParam ");
			query.declareParameters("String userMailParam, int mediaHashParam ");
//			query.declareParameters("int mediaHashParam ");
			
			processedMedia = (List<ProcessedMediaLog>) query.execute(userMail, mediaHsh);
			
		} catch (Exception e) {
			logger.warning("exception while fetching media for user:"+e);
		} finally{
		}
		return processedMedia;
	}
	
	public TaskExecution getLatestTaskExecution(String userMail){
		TaskExecution te = null;
		try{
			Query query = factory.newQuery(TaskExecution.class, "userMail == userMailParam");
			query.declareParameters("String userMailParam");
			query.setOrdering("executedOn desc");
			query.getFetchPlan().setFetchSize(1);
			
			List<TaskExecution> teList = (List<TaskExecution>) query.execute(userMail);
			if (teList != null && teList.size() >0){
				te = teList.get(0);
			}
		} catch (Exception e) {
			logger.warning("exception while fetching latest task execution for user"+e);
		}
		return te;
	}
	public AuthenticatedUser getAuthenticatedUserByCalendarID(String calendarId){
		AuthenticatedUser authenticatedUser = null;
		try{
			Query query = factory.newQuery(AuthenticatedUser.class, "calendarUid == calendarIDParam");
			query.declareParameters("String calendarIDParam");
			query.getFetchPlan().setFetchSize(1);
			
			List<AuthenticatedUser> authUser = (List<AuthenticatedUser>) query.execute(calendarId);
			if (authUser != null && authUser.size() > 0){
				authenticatedUser = authUser.get(0);
			}
		} catch (Exception e) {
			logger.warning("Exception while getting authenticated userS:"+e);
		} finally{
		}
		return authenticatedUser;
	}
	
	public List<TaskExecution> getTaskExecutionForUser(String userMail){
		List<TaskExecution> te = null;
		try{
			if (factory.isClosed()){
				factory = ManagerFactory.get().getPersistenceManager();
			}
			Query query = factory.newQuery(TaskExecution.class, "userMail == userMailParam");
			query.declareParameters("String userMailParam");
			
			te = (List<TaskExecution>) query.execute(userMail);
		} catch (Exception e) {
			logger.warning("exception while fetching latest task execution for user"+e);
		} finally{
		}
		return te;
	}

	public List<ProcessedMediaLog> getLogsForUser(String userMail){
		List<ProcessedMediaLog> mediaLog = null;
		try{
			Query query = factory.newQuery(ProcessedMediaLog.class, "userMail == userMailParam");
			query.declareParameters("String userMailParam");
			
			mediaLog = (List<ProcessedMediaLog>) query.execute(userMail);
		} catch (Exception e) {
			logger.warning("exception while fetching latest task execution for user"+e);
		} finally {
		}
		return mediaLog;
	}
	
	public void deleteAuthenticatedUser(AuthenticatedUser user){
		try{
			if (factory.isClosed()){
				factory = ManagerFactory.get().getPersistenceManager();
			}
			factory.deletePersistent(user);
		}finally{
		}
	}
	
	public void deleteTasksExecutedForUser(TaskExecution task){
		try{
			factory.deletePersistent(task);
		}finally{
		}
	}
	
	public void deleteProcessedMediaLog(ProcessedMediaLog mediaLog){
		try{
			factory.deletePersistent(mediaLog);
		} finally{
		}
	}
	
	public void updateAuthUserCalendarId(AuthenticatedUser authenticatedUser, String calendarId){
		try{
			AuthenticatedUser user = factory.getObjectById(AuthenticatedUser.class, authenticatedUser.getUser().getEmail());
			user.setCalendarUid(calendarId);
		} finally{
			factory.close();
		}
	}
	
	public void saveTaskExecution(TaskExecution taskExecution){
		try{
			factory.makePersistent(taskExecution);
		} finally{
			
		}
	}
}

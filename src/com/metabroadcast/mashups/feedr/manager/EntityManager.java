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
		}
	}
	
	public AuthenticatedUser getAuthenticatedUser(User user){
		AuthenticatedUser authenticatedUser = null;
		try{
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
			
			te = (TaskExecution) query.execute(userMail);
		} catch (Exception e) {
			logger.warning("exception while fetching latest task execution for user"+e);
		}
		return te;
	}
	
	public void saveTaskExecution(TaskExecution taskExecution){
		try{
			factory.makePersistent(taskExecution);
		} finally{
			
		}
	}
}

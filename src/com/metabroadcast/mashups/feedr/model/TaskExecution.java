package com.metabroadcast.mashups.feedr.model;

import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;

@PersistenceCapable
public class TaskExecution {
	
	@PrimaryKey
	@Persistent(valueStrategy=IdGeneratorStrategy.IDENTITY)
	private Key key;
	
	@Persistent
	private String userMail;
	
	@Persistent
	private Date executedOn;

	public Key getKey() {
		return key;
	}


	public void setKey(Key key) {
		this.key = key;
	}


	public String getUserMail() {
		return userMail;
	}


	public void setUserMail(String userMail) {
		this.userMail = userMail;
	}


	public Date getExecutedOn() {
		return executedOn;
	}


	public void setExecutedOn(Date executedOn) {
		this.executedOn = executedOn;
	}


	public TaskExecution(String userMail, Date executedOn) {
		super();
		this.userMail = userMail;
		this.executedOn = executedOn;
	}


	@Override
	public String toString() {
		return "TaskExecution [executedOn=" + executedOn + ", userMail="
				+ userMail + "]";
	}
	
	
	
}

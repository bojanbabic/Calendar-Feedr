package com.metabroadcast.mashups.feedr.model;

import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.users.User;

@PersistenceCapable
public class AuthenticatedUser {
	
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key;
	
	@Persistent
	private User user;
	
	@Persistent
	private String token;
	
	@Persistent
	private Date date;
	
	@Persistent
	private String calendarUid;
		
	public String getCalendarUid() {
		return calendarUid;
	}

	public void setCalendarUid(String calendarUid) {
		this.calendarUid = calendarUid;
	}

	public AuthenticatedUser() {
		// TODO Auto-generated constructor stub
	}
	

	public AuthenticatedUser(User user, String token, Date date,
			String calendarUid) {
		this.user = user;
		this.token = token;
		this.date = date;
		this.calendarUid = calendarUid;
	}

	public Key getKey() {
		return key;
	}

	public void setKey(Key key) {
		this.key = key;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}
	
}

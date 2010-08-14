package com.metabroadcast.mashups.feedr.model;

import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;

@PersistenceCapable
public class ProcessedMediaLog {
	
	@PrimaryKey
	@Persistent(valueStrategy=IdGeneratorStrategy.IDENTITY)	
	private Key key;
	@Persistent
	private String userMail;
	@Persistent
	private int mediaHash;
	@Persistent
	private Date date;
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
	public int getVideoHash() {
		return mediaHash;
	}
	public void setVideoHash(int videoHash) {
		this.mediaHash = videoHash;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public ProcessedMediaLog(String userMail, int videoHash, Date date) {
		super();
		this.userMail = userMail;
		this.mediaHash = videoHash;
		this.date = date;
	}
	
	
	

}

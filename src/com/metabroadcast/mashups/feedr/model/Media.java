package com.metabroadcast.mashups.feedr.model;

import java.util.ArrayList;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;

@PersistenceCapable
public class Media {
	
	@PrimaryKey
	@Persistent(valueStrategy= IdGeneratorStrategy.IDENTITY)
	private Key key;
	
	@Persistent
	private String title;
	
	@Persistent
	private String desc;
	
	@Persistent
	private String link;
	
	private ArrayList<String> ocurringDates = new ArrayList<String>();
	
	public Media() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((desc == null) ? 0 : desc.hashCode());
		result = prime * result + ((link == null) ? 0 : link.hashCode());
		result = prime * result
				+ ((ocurringDates == null) ? 0 : ocurringDates.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Media other = (Media) obj;
		if (desc == null) {
			if (other.desc != null)
				return false;
		} else if (!desc.equals(other.desc))
			return false;
		if (link == null) {
			if (other.link != null)
				return false;
		} else if (!link.equals(other.link))
			return false;
		if (ocurringDates == null) {
			if (other.ocurringDates != null)
				return false;
		} else if (!ocurringDates.equals(other.ocurringDates))
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Media [key=" + key + ", title=" + title + ", desc=" + desc
				+ ", link=" + link + ", ocurringDates=" + ocurringDates + "]";
	}

	public ArrayList<String> getOcurringDates() {
		return ocurringDates;
	}

	public void setOcurringDates(ArrayList<String> ocurringDates) {
		this.ocurringDates = ocurringDates;
	}

	public Key getKey() {
		return key;
	}

	public void setKey(Key key) {
		this.key = key;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}
	
	
}

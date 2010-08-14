package com.metabroadcast.mashups.feedr.manager;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;

public final class ManagerFactory {
	private final static PersistenceManagerFactory factory = JDOHelper.getPersistenceManagerFactory("transactions-optional");
	
	private ManagerFactory(){
		
	}
	
	public static PersistenceManagerFactory get(){
		return factory;
	}
}

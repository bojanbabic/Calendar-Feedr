package com.metabroadcast.mashups.feedr.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class JCacheUtil {
	
	private static String today;
	public JCacheUtil() {
		SimpleDateFormat format = new SimpleDateFormat("yyyy_mm_dd");
		today = format.format(new Date());

	}
	
	public static String getKeyDailyKeyLabel(String label){
		return today+"_"+label;
	}
}

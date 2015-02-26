package com.hotpads.datarouter.util.core;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;


public class DailyCalendarTool {

	public static final TimeZone 
		TIME_ZONE_US_EASTERN = TimeZone.getTimeZone("America/New_York");
	
	
	public static Calendar parseYYYYMMDDEastern(String in){
		return createUSEasternUSFromOneBasedYMD(
				getYear(in), getMonth(in), getDate(in));
	}
	
	public static Calendar createUSEasternUSFromOneBasedYMD(int year, int monthOneBased, int date){
		Calendar c = Calendar.getInstance(TIME_ZONE_US_EASTERN, Locale.US);
		c.set(year, monthOneBased-1, date, 0, 0, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c;
	}

	public static int getYear(String yyyymmdd){
		return Integer.valueOf(yyyymmdd.substring(0, 4));
	}
	
	public static int getMonth(String yyyymmdd){
		return Integer.valueOf(yyyymmdd.substring(4, 6));
	}
	
	public static int getDate(String yyyymmdd){
		return Integer.valueOf(yyyymmdd.substring(6, 8));
	}

	
}

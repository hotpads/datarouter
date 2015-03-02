package com.hotpads.profile.count.databean.alert;

import java.util.Date;

import com.hotpads.datarouter.util.core.DrDateTool;
import com.hotpads.datarouter.util.core.DrStringTool;

public class CounterAlertTimeRange{
	private static final String 
	ALL_HOURS = "allHours",
	ALL_DAYS = "All Days",
	WEEK_DAY = "Weekdays",
	WEEK_END = "Weekend",
	MONDAY = "Monday",
	TUESDAY = "Tuesday",
	WEDNESDAY = "Wednesday",
	THURSDAY = "Thursday",
	FRIDAY = "Friday";
	
	String alertTimeRangeStr;
	public static Boolean isInTimeRange(String alertTimeRangeStr, Date date){
		if(DrStringTool.isEmpty(alertTimeRangeStr)){
			return true;
		}
		
		String [] alertTimeRange = alertTimeRangeStr.split(";");
		if(! ALL_HOURS.equals(alertTimeRange[0])){
			String [] fromToHours = alertTimeRange[0].split("-");
			int fromHour = Integer.valueOf(fromToHours[0]);
			int toHour = Integer.valueOf(fromToHours[1]);
			int currentHourInt = DrDateTool.getHourInteger();
			if(currentHourInt < fromHour || currentHourInt > toHour){
				return false;
			}
		}
		
		if(! ALL_DAYS.equals(alertTimeRange[1])){
			if(WEEK_DAY.equals(alertTimeRange[1])){
				return DrDateTool.isWeekday(date);
			}else if(WEEK_END.equals(alertTimeRange[1])){
				return DrDateTool.isWeekend(date);
			}else{
				String [] days = alertTimeRange[1].split(",");
				for(String day : days){
					if(MONDAY.equals(day) && DrDateTool.isMonday(date)){
						return true;
					}
					if(TUESDAY.equals(day) && DrDateTool.isTuesday(date)){
						return true;
					}
					if(WEDNESDAY.equals(day) && DrDateTool.isWednesday(date)){
						return true;
					}
					if(THURSDAY.equals(day) && DrDateTool.isThursday(date)){
						return true;
					}
					if(FRIDAY.equals(day) && DrDateTool.isFriday(date)){
						return true;
					}
				}
				return false;
			}
		}
		return true;
	}
}

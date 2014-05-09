package com.hotpads.profile.count.databean.alert;

import java.util.Date;

import com.hotpads.util.core.DateTool;
import com.hotpads.util.core.StringTool;

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
		if(StringTool.isEmpty(alertTimeRangeStr)){
			return true;
		}
		
		String [] alertTimeRange = alertTimeRangeStr.split(";");
		if(! ALL_HOURS.equals(alertTimeRange[0])){
			String [] fromToHours = alertTimeRange[0].split("-");
			int fromHour = Integer.valueOf(fromToHours[0]);
			int toHour = Integer.valueOf(fromToHours[1]);
			int currentHourInt = DateTool.getHourInteger();
			if(currentHourInt < fromHour || currentHourInt > toHour){
				return false;
			}
		}
		
		if(! ALL_DAYS.equals(alertTimeRange[1])){
			if(WEEK_DAY.equals(alertTimeRange[1])){
				return DateTool.isWeekday(date);
			}else if(WEEK_END.equals(alertTimeRange[1])){
				return DateTool.isWeekend(date);
			}else{
				String [] days = alertTimeRange[1].split(",");
				for(String day : days){
					if(MONDAY.equals(day) && DateTool.isMonday(date)){
						return true;
					}
					if(TUESDAY.equals(day) && DateTool.isTuesday(date)){
						return true;
					}
					if(WEDNESDAY.equals(day) && DateTool.isWednesday(date)){
						return true;
					}
					if(THURSDAY.equals(day) && DateTool.isThursday(date)){
						return true;
					}
					if(FRIDAY.equals(day) && DateTool.isFriday(date)){
						return true;
					}
				}
				return false;
			}
		}
		return true;
	}
}

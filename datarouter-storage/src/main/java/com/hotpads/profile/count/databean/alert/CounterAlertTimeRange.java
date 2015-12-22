package com.hotpads.profile.count.databean.alert;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.hotpads.datarouter.util.core.DrDateTool;
import com.hotpads.datarouter.util.core.DrStringTool;

public class CounterAlertTimeRange{
	private static ThreadLocal<SimpleDateFormat>
			timeParser = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy.MM.dd HH:mm:ss")),
			dateFormatter = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy.MM.dd "));

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

	public static boolean isInTimeRange(String alertTimeRangeStr, Date date){
		if(DrStringTool.isEmpty(alertTimeRangeStr)){
			return true;
		}

		String [] alertTimeRange = alertTimeRangeStr.split(";");
		if(! ALL_HOURS.equals(alertTimeRange[0])){
			String [] fromToHours = alertTimeRange[0].split("-");
			String fromString = addMissingFields(fromToHours[0], date);
			String toString = addMissingFields(fromToHours[1], date);
			Date fromDate;
			Date toDate;
			try{
				fromDate = timeParser.get().parse(fromString);
				toDate = timeParser.get().parse(toString);
			}catch(ParseException e){
				throw new RuntimeException(e);
			}
			if(date.before(fromDate) || date.after(toDate)){
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

	private static String addMissingFields(String timeString, Date date){
		StringBuilder timeStringBuilder = new StringBuilder();
		timeStringBuilder.append(dateFormatter.get().format(date));
		timeStringBuilder.append(timeString);
		int colonCount = timeString.split(":").length - 1;
		for(int i = colonCount; i < 2; i++){
			timeStringBuilder.append(":00");
		}
		return timeStringBuilder.toString();
	}

	public static class CounterAlertTimeRangeTests{
		@Test
		public void testMinuteRange(){
			Date date = DrDateTool.parseUserInputDate("2015 10 09 10 20 00", null);
			Assert.assertEquals(isInTimeRange("8-11;All Days", date), true);
			Assert.assertEquals(isInTimeRange("8:20-11:24;All Days", date), true);
			Assert.assertEquals(isInTimeRange("8:20-10:13;All Days", date), false);
		}
	}
}

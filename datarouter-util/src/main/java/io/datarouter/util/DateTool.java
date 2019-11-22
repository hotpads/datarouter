/**
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.util.duration.DurationUnit;
import io.datarouter.util.duration.DurationWithCarriedUnits;

public class DateTool{
	private static final Logger logger = LoggerFactory.getLogger(DateTool.class);

	public static final int MILLISECONDS_IN_DAY = (int) Duration.ofDays(1).toMillis();
	public static final int MILLISECONDS_IN_HOUR = (int) Duration.ofHours(1).toMillis();
	public static final int MILLISECONDS_IN_MINUTE = (int) Duration.ofMinutes(1).toMillis();
	public static final int MILLISECONDS_IN_SECOND = (int) Duration.ofSeconds(1).toMillis();

	public static final int SUNDAY_INDEX = 1;
	public static final int MONDAY_INDEX = 2;
	public static final int TUESDAY_INDEX = 3;
	public static final int WEDNESDAY_INDEX = 4;
	public static final int THURSDAY_INDEX = 5;
	public static final int FRIDAY_INDEX = 6;
	public static final int SATURDAY_INDEX = 7;

	public static final List<String> DAY_ABBREVIATIONS = Arrays.asList("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat");
	public static final List<String> MONTH_ABBREVIATIONS = Arrays.asList("Jan", "Feb", "Mar", "Apr", "May", "Jun",
			"Jul", "Aug", "Sep", "Oct", "Nov", "Dec");

	public static final DateTimeFormatter JAVA_TIME_INTERNET_FORMATTER = DateTimeFormatter.ISO_INSTANT;

	private static final String ISO_FORMAT = "yyyy MM dd'T'hh mm ss'Z'";

	/**
	 * Parse the provided date string using a sequence of common date formats
	 * that users might input. If minimumYear is provided, it is used to
	 * validate the result and try alternate date formats.
	 */
	public static Date parseUserInputDate(String date, Integer minimumYear){
		if(date == null){
			return null;
		}
		date = date.replaceAll("[\\W\\s_]+", " ");

		Pattern ordinalPattern = Pattern.compile("\\d(th|nd|st)");
		String strippedDate = date;
		Matcher ordinalMatcher = ordinalPattern.matcher(strippedDate);
		while(ordinalMatcher.find()){
			int ordinalIndex = ordinalMatcher.end();
			String start = strippedDate.substring(0, ordinalIndex - 2);
			String end = strippedDate.substring(ordinalIndex);
			strippedDate = start + end;
			ordinalMatcher = ordinalPattern.matcher(strippedDate);
		}
		date = strippedDate;
		String[] commonFormats = {
				"E MMM dd hh mm ss z yyyy",
				ISO_FORMAT,
				"yyyy MM dd hh mm ss",
				"MM dd yy",
				"MMM dd yy",
				"MMMMM dd yy",
				"MMMMM yyyy",
				"yyyyMMdd",
				"yyyyMM",
				"MMMMM dd"
		};//"MM dd", "MMM dd","MMMMM dd", };

		for(String fmt : commonFormats){
			try{
				Date parsed = new SimpleDateFormat(fmt).parse(date);
				if(fmt == ISO_FORMAT){
					logger.warn("probable timezone issue input={} parsed={}", date, parsed, new Exception());
				}
				if(minimumYear != null){
					if(fmt.contains("y")){
						if(getYearInteger(parsed) < minimumYear){
							continue;
						}
						return parsed;
					}
					//year is null or not a result of parsing
					Calendar calendar = Calendar.getInstance();
					calendar.setTime(parsed);
					calendar.set(Calendar.YEAR, getYearInteger());
					parsed = calendar.getTime();
				}
				return parsed;
			}catch(ParseException pe){
				//expected
			}
		}
		return null;
	}

	public static String getNumericDate(Date date){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return sdf.format(date);
	}

	public static Calendar dateToCalendar(Date date){
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar;
	}

	public static int getCalendarField(Date date, int field){
		Calendar calendar = dateToCalendar(date);
		return calendar.get(field);
	}

	public static int getYearInteger(){
		return getYearInteger(new Date());
	}

	public static int getYearInteger(Date date){
		return getCalendarField(date, Calendar.YEAR);
	}

	public static int getDayInteger(Date date){
		return getCalendarField(date, Calendar.DAY_OF_WEEK);
	}

	public static String getDayAbbreviation(Date date){
		SimpleDateFormat sdf = new SimpleDateFormat("EEE");
		return sdf.format(date);
	}

	public static long getPeriodStart(long periodMs){
		return getPeriodStart(System.currentTimeMillis(), periodMs);
	}

	public static long getPeriodStart(long timeMs, long periodMs){
		return timeMs - timeMs % periodMs;
	}

	public static String getDateTime(Date date){
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return dateFormat.format(date);
	}

	/*---------------- time elapsed ----------------*/

	public static boolean hasPassed(Date date){
		return new Date().after(date);
	}

	public static long getMillisecondDifference(Date d1, Date d2){
		return d2.getTime() - d1.getTime();
	}

	/**
	 * note: 24 hr periods. partial days like DST switch and leap seconds are not 24 hours. use getDatesBetween
	 */
	public static double getSecondsBetween(Date d1, Date d2){
		return getPeriodsBetween(d1, d2, MILLISECONDS_IN_SECOND);
	}

	public static double getMinutesBetween(Date d1, Date d2){
		return getPeriodsBetween(d1, d2, MILLISECONDS_IN_MINUTE);
	}

	public static double getPeriodsBetween(Date d1, Date d2, long periodLengthMs){
		long msDif = Math.abs(getMillisecondDifference(d1, d2));
		return msDif / (double)periodLengthMs;
	}

	/*---------------- XsdDateTime ----------------*/

	/* as specified in RFC 3339 / ISO 8601 */
	public static String getInternetDate(Date date){
		return getInternetDate(date, 0);
	}

	public static String getInternetDate(Date date, int msCount){
		TimeZone tz = TimeZone.getTimeZone("GMT+00:00");
		StringBuilder sb = new StringBuilder();
		sb.append("yyyy-MM-dd'T'HH:mm:ss");
		if(msCount > 0){
			sb.append('.');
			for(int i = 0; i < msCount; i++){
				sb.append('S');
			}
		}
		sb.append("'Z'");
		SimpleDateFormat sdf = new SimpleDateFormat(sb.toString());
		sdf.setTimeZone(tz);
		return sdf.format(date);
	}

	public static String getInternetDate(TemporalAccessor temporalValue){
		return JAVA_TIME_INTERNET_FORMATTER.format(temporalValue);
	}

	public static String getYyyyMmDdHhMmSsMmmWithPunctuationNoSpaces(Long ms){
		return format("yyyy-MM-dd_HH:mm:ss.SSS", ms);
	}

	/**
	 * Easy selecting and searching, like for logs.
	 */
	public static String formatAlphanumeric(Long ms){
		return format("yyyy'y'MM'm'dd'd'HH'h'mm'm'ss's'SSS'ms'", ms);
	}

	public static String format(String pattern, Long ms){
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
		return LocalDateTime.ofInstant(Instant.ofEpochMilli(ms), ZoneId.systemDefault()).format(formatter);
	}

	public static final int DEFAULT_MAX_UNITS = 2;

	/**
	 * get the date in the form of "XX days, XX hours ago"
	 * only returns minutes if less than 3 hours ago, and only seconds if
	 * 	less than 1 minute ago
	 *
	 * @return "XXXXdays, XX hours ago" or "XX minutes ago" or
	 * 	"XX seconds ago" or "less than one second ago"
	 */
	public static String getAgoString(Date date){
		return getAgoString(date, DEFAULT_MAX_UNITS);
	}

	public static String getAgoString(Long dateMs){
		return getAgoString(new Date(dateMs));
	}

	public static String getAgoString(Date date, int maxUnits){
		if(date == null){
			return null;
		}
		long timeMillis = new Date().getTime() - date.getTime();
		String suffix = " ago";
		if(timeMillis < 0){
			suffix = " from now";
		}
		return getMillisAsString(Math.abs(timeMillis), maxUnits, DurationUnit.SECONDS) + suffix;
	}

	/**
	 * Translates the given milliseconds into a human readable time
	 * string to the specified precision.
	 * @param maxUnits - the desired maximum number of units in the returned string.
	 * @return a labeled,
	 */
	public static String getMillisAsString(long timeMillis, int maxUnits, DurationUnit maxPrecision){
		DurationWithCarriedUnits wud = new DurationWithCarriedUnits(timeMillis);
		return wud.toStringByMaxUnitsMaxPrecision(maxPrecision, maxUnits);
	}

	public static Date getDaysAgo(int daysAgo, Date fromDate){
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(fromDate);
		calendar.add(Calendar.DATE, -1 * daysAgo);
		return calendar.getTime();
	}

	public static Date getDaysAgo(int daysAgo){
		return getDaysAgo(daysAgo, new Date());
	}

	public static int getDatesBetween(Date oldDate, Date newDate){
		//round everything > .95 up to handle partial days due to DST and leap seconds
		double daysBetween = DateTool.getDaysBetween(oldDate, newDate);
		return (int)Math.ceil(daysBetween - .95d);
	}

	public static double getDaysBetween(Date d1, Date d2){
		return getPeriodsBetween(d1, d2, MILLISECONDS_IN_DAY);
	}

	/*---------------- day of week ----------------*/

	public static boolean isWeekday(Date date){
		int dayInteger = getDayInteger(date);
		return dayInteger == MONDAY_INDEX || dayInteger == TUESDAY_INDEX || dayInteger == WEDNESDAY_INDEX
				|| dayInteger == THURSDAY_INDEX || dayInteger == FRIDAY_INDEX;
	}

	public static boolean isWeekend(Date date){
		int dayInteger = getDayInteger(date);
		return dayInteger == SATURDAY_INDEX || dayInteger == SUNDAY_INDEX;
	}

	public static boolean isMonday(Date date){
		return MONDAY_INDEX == getDayInteger(date);
	}

	public static boolean isTuesday(Date date){
		return TUESDAY_INDEX == getDayInteger(date);
	}

	public static boolean isWednesday(Date date){
		return WEDNESDAY_INDEX == getDayInteger(date);
	}

	public static boolean isThursday(Date date){
		return THURSDAY_INDEX == getDayInteger(date);
	}

	public static boolean isFriday(Date date){
		return FRIDAY_INDEX == getDayInteger(date);
	}

	/*---------------- reverse ------------------*/

	public static Long toReverseDateLong(Date date){
		return date == null ? null : Long.MAX_VALUE - date.getTime();
	}

	public static Date fromReverseDateLong(Long dateLong){
		return dateLong == null ? null : new Date(Long.MAX_VALUE - dateLong);
	}

}

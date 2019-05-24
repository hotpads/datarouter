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

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.util.duration.DurationUnit;
import io.datarouter.util.duration.DurationWithCarriedUnits;

public final class DateTool{

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
		String[] commonFormats =
				{"E MMM dd hh mm ss z yyyy", "yyyy MM dd'T'hh mm ss'Z'", "yyyy MM dd hh mm ss", "MM dd yy", "MMM dd yy",
						"MMMMM dd yy", "MMMMM yyyy", "yyyyMMdd", "yyyyMM", "MMMMM dd"
				};//"MM dd", "MMM dd","MMMMM dd", };

		for(String fmt : commonFormats){
			try{
				Date parsed = new SimpleDateFormat(fmt).parse(date);
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
		TimeZone tz = TimeZone.getTimeZone("GMT+00:00");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
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

	/*---------------- tests ----------------*/

	public static class DateToolTests{

		private static final SimpleDateFormat df = new SimpleDateFormat("MM-dd-yyyy");

		@Test
		public void testParseCommonDate(){
			/*
			{"MM dd yy","MMM dd yy","MMMMM dd yy",
				"MM dd", "MMM dd","MMMMM dd", };*/
			testParseUserInputDate("11-05-2008", "11-05-08");
			testParseUserInputDate("11-05-2008", "11-05-2008");
			testParseUserInputDate("11-05-1982", "11-05-82");
			testParseUserInputDate("11-05-1982", "11-05-1982");
			testParseUserInputDate("11-05-2008", "Nov 5, 2008");
			testParseUserInputDate("11-05-2008", "Nov 5, 08");
			testParseUserInputDate("11-05-2008", "November 5, 2008");
			testParseUserInputDate("11-05-2008", "November 05, 2008");
			testParseUserInputDate("07-15-2011", "July 15th, 2011");
			testParseUserInputDate("01-01-2008", "1/1/2008");
			testParseUserInputDate("01-01-2008", "01/01/2008");
			testParseUserInputDate("01-05-2008", "01/05/2008");
			testParseUserInputDate("01-05-2008", "1/5/08");
			testParseUserInputDate("06-01-2010", "June 2010");
			testParseUserInputDate("06-01-2010", "201006");
			testParseUserInputDate("06-01-2010", "20100601");

			Assert.assertEquals(df.format(parseUserInputDate("July 15th",
					2000)), "07-15-" + DateTool.getYearInteger());
		}

		private void testParseUserInputDate(String expected, String original){
			Assert.assertEquals(df.format(parseUserInputDate(original, null)), expected);
		}

		@Test
		public void testAgoString(){
			Assert.assertEquals(getMillisAsString(950, 1, DurationUnit.MILLISECONDS), "950 milliseconds");
			Assert.assertEquals(getMillisAsString(950, 5, DurationUnit.MILLISECONDS), "950 milliseconds");
			Assert.assertEquals(getMillisAsString(52950, 1, DurationUnit.MILLISECONDS), "52 seconds");
			Assert.assertEquals(getMillisAsString(360051, 2, DurationUnit.MILLISECONDS), "6 minutes");
			Assert.assertEquals(getMillisAsString(225950, 2, DurationUnit.MILLISECONDS), "3 minutes, 45 seconds");
			Assert.assertEquals(getMillisAsString(10225950, 2, DurationUnit.MILLISECONDS), "2 hours, 50 minutes");
			Assert.assertEquals(getMillisAsString(240225950, 4, DurationUnit.MILLISECONDS),
					"2 days, 18 hours, 43 minutes, 45 seconds");
		}

		@Test
		public void testToReverseDateLong(){
			Date now = new Date(), zero = new Date(0L), max = new Date(Long.MAX_VALUE);
			Assert.assertEquals(toReverseDateLong(now), (Long)(Long.MAX_VALUE - now.getTime()));
			Assert.assertEquals(toReverseDateLong(zero), (Long)Long.MAX_VALUE);
			Assert.assertEquals(toReverseDateLong(max), (Long)0L);
			Assert.assertNull(toReverseDateLong(null));
		}

		@Test
		public void testFromReverseDateLong(){
			Date now = new Date(), zero = new Date(0L), max = new Date(Long.MAX_VALUE);
			Assert.assertEquals(fromReverseDateLong(Long.MAX_VALUE - now.getTime()), now);
			Assert.assertEquals(fromReverseDateLong(Long.MAX_VALUE - zero.getTime()), zero);
			Assert.assertEquals(fromReverseDateLong(Long.MAX_VALUE - max.getTime()), max);
			Assert.assertNull(fromReverseDateLong(null));
		}

		@Test
		public void testReverseDateLong(){
			Date now = new Date();
			Long nowTime = now.getTime();
			Assert.assertEquals(fromReverseDateLong(toReverseDateLong(now)), now);
			Assert.assertEquals(toReverseDateLong(fromReverseDateLong(nowTime)), nowTime);
			Assert.assertNull(fromReverseDateLong(toReverseDateLong(null)));
			Assert.assertNull(toReverseDateLong(fromReverseDateLong(null)));
		}

		@Test
		public void testGetDaysAgo(){
			Date date1 = new Date(1527182116155L);
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(1526836516155L);
			Date date2 = calendar.getTime();
			Assert.assertEquals(getDaysAgo(4, date1), date2);
		}

		@Test
		public void testGetDaysBetween(){
			Date d1 = new Date(1352059736026L);
			int daysApart = 4;
			Date d2 = new Date(d1.getTime() + MILLISECONDS_IN_DAY * daysApart);
			Assert.assertEquals(getDaysBetween(d1, d2), daysApart, 1 >> 20);
			d2 = new Date(d1.getTime() + MILLISECONDS_IN_DAY * daysApart - 4);
			Assert.assertTrue(daysApart > getDaysBetween(d1, d2));
			Assert.assertTrue(daysApart - 1 < getDaysBetween(d1, d2));
		}

		@Test
		public void testGetMinutesBetween(){
			Date d1 = new Date(1352059736026L);
			int minutesApart = 5;
			Date d2 = new Date(d1.getTime() + MILLISECONDS_IN_MINUTE * minutesApart);
			Assert.assertEquals(getMinutesBetween(d1, d2), minutesApart, 1 >> 20);
			d2 = new Date(d1.getTime() + MILLISECONDS_IN_MINUTE * minutesApart - 10);
			Assert.assertTrue(minutesApart > getMinutesBetween(d1, d2));
			Assert.assertTrue(minutesApart - 1 < getMinutesBetween(d1, d2));
		}
	}
}

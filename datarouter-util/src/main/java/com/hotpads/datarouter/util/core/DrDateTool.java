package com.hotpads.datarouter.util.core;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

import com.hotpads.util.core.date.DurationUnit;
import com.hotpads.util.core.date.DurationWithCarriedUnits;


public final class DrDateTool {

	public static final int
		MILLISECONDS_IN_WEEK = 7 * 24 * 60 * 60 * 1000,
		MILLISECONDS_IN_DAY = 24 * 60 * 60 * 1000,
		MILLISECONDS_IN_HOUR = 60 * 60 * 1000,
		MILLISECONDS_IN_MINUTE = 60 * 1000,
		MILLISECONDS_IN_SECOND = 1000;

	public static final int
		SUNDAY_INDEX = 1,
		MONDAY_INDEX = 2,
		TUESDAY_INDEX = 3,
		WEDNESDAY_INDEX = 4,
		THURSDAY_INDEX = 5,
		FRIDAY_INDEX = 6,
		SATURDAY_INDEX = 7;

	public static final List<String>
		DAY_ABBREVIATIONS = new ArrayList<String>(),
		MONTH_ABBREVIATIONS = new ArrayList<String>();

	static{
		DAY_ABBREVIATIONS.add("Sun");
		DAY_ABBREVIATIONS.add("Mon");
		DAY_ABBREVIATIONS.add("Tue");
		DAY_ABBREVIATIONS.add("Wed");
		DAY_ABBREVIATIONS.add("Thu");
		DAY_ABBREVIATIONS.add("Fri");
		DAY_ABBREVIATIONS.add("Sat");

		MONTH_ABBREVIATIONS.add("Jan");
		MONTH_ABBREVIATIONS.add("Feb");
		MONTH_ABBREVIATIONS.add("Mar");
		MONTH_ABBREVIATIONS.add("Apr");
		MONTH_ABBREVIATIONS.add("May");
		MONTH_ABBREVIATIONS.add("Jun");
		MONTH_ABBREVIATIONS.add("Jul");
		MONTH_ABBREVIATIONS.add("Aug");
		MONTH_ABBREVIATIONS.add("Sep");
		MONTH_ABBREVIATIONS.add("Oct");
		MONTH_ABBREVIATIONS.add("Nov");
		MONTH_ABBREVIATIONS.add("Dec");
	}


	/**
	 * Parse the provided date string using a sequence of common date formats
	 * that users might input. If minimumYear is provided, it is used to
	 * validate the result and try alternate date formats.
	 *
	 * @param date
	 * @param minimumYear
	 * @return
	 */
	public static Date parseUserInputDate(String date, Integer minimumYear){
		if(date==null){
			return null;
		}
		date = date.replaceAll("[\\W\\s_]+"," ");

		Pattern ordinalPattern = Pattern.compile("\\d(th|nd|st)");
		String strippedDate = date;
		Matcher ordinalMatcher = ordinalPattern.matcher(strippedDate);
		while(ordinalMatcher.find()){
			int ordinalIndex = ordinalMatcher.end();
			String start = strippedDate.substring(0,ordinalIndex-2);
			String end = strippedDate.substring(ordinalIndex);
			strippedDate = start + end;
			ordinalMatcher = ordinalPattern.matcher(strippedDate);
		}
		date = strippedDate;

		String[] commonFormats =
			{"yyyy MM dd hh mm ss","MM dd yy","MMM dd yy","MMMMM dd yy",
				"MMMMM yyyy", "yyyyMMdd", "yyyyMM", "MMMMM dd"
			};//"MM dd", "MMM dd","MMMMM dd", };

		for(String fmt : commonFormats){
			try{
				Date parsed = new SimpleDateFormat(fmt).parse(date);
				if(minimumYear != null){
					Integer year = getYearInteger(parsed);
					if(year!=null && fmt.contains("y")){
						if(getYearInteger(parsed) < minimumYear){
							continue;
						}
						return parsed;
					}
					//year is null or not a result of parsing
					Calendar c = Calendar.getInstance();
					c.setTime(parsed);
					c.set(Calendar.YEAR, getYearInteger());
					parsed = c.getTime();
				}
				return parsed;
			} catch(ParseException pe){
				//expected
			}
		}
		return null;
	}


	public static String getNumericDate(Date date){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
		return sdf.format(date);
	}

	public static Calendar dateToCalendar(Date d){
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		return c;
	}
	public static Integer getCalendarField(Date d, int field){
		Calendar c=dateToCalendar(d);
		return c.get(field);
	}

	public static Integer getYearInteger(){
		return getYearInteger(new Date());
	}

	public static Integer getHourInteger(){
		return getHourInteger(new Date());
	}

	public static Integer getYearInteger(Date d){
		return getCalendarField(d, Calendar.YEAR);
	}

	public static Integer getDayInteger(Date d){
		return getCalendarField(d, Calendar.DAY_OF_WEEK);
	}

	public static Integer getHourInteger(Date d){
		return getCalendarField(d, Calendar.HOUR_OF_DAY);
	}

	public static String getDayAbbreviation(Date date){
		SimpleDateFormat sdf = new SimpleDateFormat("EEE");
		return sdf.format(date);
	}

	public static Long getPeriodStart(long periodMs){
		return getPeriodStart(System.currentTimeMillis(), periodMs);
	}

	public static Long getPeriodStart(long timeMs, long periodMs){
		return timeMs - timeMs % periodMs;
	}


	/************************ time elapsed *********************************/

	public static long getMillisecondDifference(Date d1, Date d2){
		return d2.getTime() - d1.getTime();
	}

	/**
	 * note: 24 hr periods. partial days like DST switch and leap seconds are not 24 hours. use getDatesBetween
	 */
	public static double getSecondsBetween(Date d1, Date d2){
		return getPeriodsBetween(d1 ,d2, MILLISECONDS_IN_SECOND);
	}
	public static double getPeriodsBetween(Date d1, Date d2, long periodLengthMs){
		long msDif = Math.abs(getMillisecondDifference(d1,d2));
		return msDif / (double)periodLengthMs;
	}

	/********************* XsdDateTime *************************************/

    /* as specified in RFC 3339 / ISO 8601 */
    public static String getInternetDate(Date d){
    	TimeZone tz = TimeZone.getTimeZone("GMT+00:00");
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    	sdf.setTimeZone(tz);
    	return sdf.format(d);
    }

    public static String getYYYYMMDDHHMMSSMMMWithPunctuationNoSpaces(Long ms){
    	SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss.SSS");
    	return f.format(new Date(ms));
    }

    public static String getYYYYMMDDHHMMWithPunctuation(Date date){
    	SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    	return f.format(date);
    }

    public static String getYYYYMMDDHHMMSSWithPunctuationNoSpaces(Long ms){
    	return getYYYYMMDDHHMMSSWithPunctuationNoSpaces(new Date(ms));
    }
    public static String getYYYYMMDDHHMMSSWithPunctuationNoSpaces(Date date){
    	SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
    	return f.format(date);
    }


    public static final int DEFAULT_MAX_UNITS = 2;

    /**
     * get the date in the form of "XX days, XX hours ago"
     * only returns minutes if less than 3 hours ago, and only seconds if
     * 	less than 1 minute ago
     *
     * the inverse of getFromNowString
     *
     * @param date
     * @return "XXXXdays, XX hours ago" or "XX minutes ago" or
     * 	"XX seconds ago" or "less than one second ago"
     */
    public static String getAgoString(Date date){
    	return getAgoString(date, DEFAULT_MAX_UNITS);
    }

    /**
     * This backs hp:makeAgoStringFromLongDate() in hotpads.tld
     */
    public static String getAgoString(Long dateMs){
    	return getAgoString(new Date(dateMs));
    }

    public static String getAgoString(Date date, int maxUnits) {
    	if(date == null) { return null; }
    	long timeMillis = new Date().getTime() - date.getTime();
    	String suffix = " ago";
    	if (timeMillis < 0){ suffix = " from now"; }
    	return getMillisAsString(timeMillis, maxUnits, DurationUnit.SECONDS) + suffix;
    }

    /**
     * Translates the given milliseconds into a human readable time
	 * string to the specified precision.
     * @param timeMillis
     * @param maxUnits - the desired maximum number of units in the returned string.
     * @return a labeled,
     */
    public static String getMillisAsString(long timeMillis, int maxUnits, DurationUnit maxPrecision) {
    	DurationWithCarriedUnits wud = new DurationWithCarriedUnits(timeMillis);
    	return wud.toStringByMaxUnitsMaxPrecision(maxPrecision, maxUnits);
    }


	/*************************************************************************/

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

	public static Long toReverseDateLong(Date date) {
		return date == null ? null : Long.MAX_VALUE - date.getTime();
	}

	public static Date fromReverseDateLong(Long dateLong) {
		return dateLong == null ? null : new Date(Long.MAX_VALUE - dateLong);
	}

	/************************** tests ******************************/

	public static class Tests{

		@Test
		public void testParseCommonDate(){
			/*
			{"MM dd yy","MMM dd yy","MMMMM dd yy",
				"MM dd", "MMM dd","MMMMM dd", };*/
			SimpleDateFormat df = new SimpleDateFormat("MM-dd-yyyy");
			Assert.assertEquals("11-05-2008", df.format(parseUserInputDate("11-05-08", null)));
			Assert.assertEquals("11-05-2008", df.format(parseUserInputDate("11-05-2008", null)));
			Assert.assertEquals("11-05-1982", df.format(parseUserInputDate("11-05-82", null)));
			Assert.assertEquals("11-05-1982", df.format(parseUserInputDate("11-05-1982", null)));
			Assert.assertEquals("11-05-2008", df.format(parseUserInputDate("Nov 5, 2008", null)));
			Assert.assertEquals("11-05-2008", df.format(parseUserInputDate("Nov 5, 08", null)));
			Assert.assertEquals("11-05-2008", df.format(parseUserInputDate("November 5, 2008", null)));
			Assert.assertEquals("11-05-2008", df.format(parseUserInputDate("November 05, 2008", null)));
			Assert.assertEquals("07-15-2011", df.format(parseUserInputDate("July 15th, 2011", null)));
			Assert.assertEquals("07-15-" + DrDateTool.getYearInteger(), df.format(parseUserInputDate("July 15th",
					2000)));
			Assert.assertEquals("01-01-2008", df.format(parseUserInputDate("1/1/2008", null)));
			Assert.assertEquals("01-01-2008", df.format(parseUserInputDate("01/01/2008", null)));
			Assert.assertEquals("01-05-2008", df.format(parseUserInputDate("01/05/2008", null)));
			Assert.assertEquals("01-05-2008", df.format(parseUserInputDate("1/5/08", null)));
			Assert.assertEquals("06-01-2010", df.format(parseUserInputDate("June 2010", null)));
			Assert.assertEquals("06-01-2010", df.format(parseUserInputDate("201006", null)));
			Assert.assertEquals("06-01-2010", df.format(parseUserInputDate("20100601", null)));
		}

		@Test
		public void testAgoString(){
			Assert.assertEquals("950 milliseconds", getMillisAsString(950, 1, DurationUnit.MILLISECONDS));
			Assert.assertEquals("950 milliseconds", getMillisAsString(950, 5, DurationUnit.MILLISECONDS));
			Assert.assertEquals("52 seconds", getMillisAsString(52950, 1, DurationUnit.MILLISECONDS));
			Assert.assertEquals("6 minutes", getMillisAsString(360051, 2, DurationUnit.MILLISECONDS));
			Assert.assertEquals("3 minutes, 45 seconds", getMillisAsString(225950, 2, DurationUnit.MILLISECONDS));
			Assert.assertEquals("2 hours, 50 minutes", getMillisAsString(10225950, 2, DurationUnit.MILLISECONDS));
			Assert.assertEquals("2 days, 18 hours, 43 minutes, 45 seconds", getMillisAsString(240225950, 4,
					DurationUnit.MILLISECONDS));
		}

		@Test
		public void testToReverseDateLong(){
			Date now = new Date(), zero = new Date(0L), max = new Date(Long.MAX_VALUE);
			Assert.assertEquals((Long)(Long.MAX_VALUE - now.getTime()), toReverseDateLong(now));
			Assert.assertEquals((Long)Long.MAX_VALUE, toReverseDateLong(zero));
			Assert.assertEquals((Long)0L, toReverseDateLong(max));
			Assert.assertNull(toReverseDateLong(null));
		}

		@Test
		public void testFromReverseDateLong(){
			Date now = new Date(), zero = new Date(0L), max = new Date(Long.MAX_VALUE);
			Assert.assertEquals(now, fromReverseDateLong(Long.MAX_VALUE - now.getTime()));
			Assert.assertEquals(zero, fromReverseDateLong(Long.MAX_VALUE - zero.getTime()));
			Assert.assertEquals(max, fromReverseDateLong(Long.MAX_VALUE - max.getTime()));
			Assert.assertNull(fromReverseDateLong(null));
		}

		@Test
		public void testReverseDateLong(){
			Date now = new Date();
			Long nowTime = now.getTime();
			Assert.assertEquals(now, fromReverseDateLong(toReverseDateLong(now)));
			Assert.assertEquals(nowTime, toReverseDateLong(fromReverseDateLong(nowTime)));
			Assert.assertNull(fromReverseDateLong(toReverseDateLong(null)));
			Assert.assertNull(toReverseDateLong(fromReverseDateLong(null)));
		}

	}


}

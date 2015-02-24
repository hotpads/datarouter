package com.hotpads.util.core;

import static org.junit.Assert.assertTrue;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

import com.hotpads.util.core.collections.Pair;
import com.hotpads.util.core.date.DailyCalendarTool;
import com.hotpads.util.core.date.DurationUnit;
import com.hotpads.util.core.date.DurationWithCarriedUnits;


public final class DateTool {

	public static final double DAYS_IN_MONTH_APPROXIMATE = 30.5;//30.5 * 12 => 366 (correct on leap year)

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

	public static final int
		JAVA_YEAR_OFFSET = 1900,
		JAVA_MONTH_OFFSET = 1;

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
	
	public static final SimpleDateFormat FORMAT_yyyyMM = new SimpleDateFormat("yyyyMM");
	
	public static final String PATTERN_RFC_2822 = "EEE, dd MMM yyyy HH:mm:ss Z";

	public static String getDayAbbreviationOneBased(Integer day){
		if(day<1 || day>7){
			throw new IllegalArgumentException("day integer must be 1-7");
		}
		return DAY_ABBREVIATIONS.get(day-1);
	}

	public static String getMonthAbbreviationZeroBased(Integer month){
		if(month<0 || month>11){
			throw new IllegalArgumentException("month integer must be 0-11, was "+month);
		}
		return MONTH_ABBREVIATIONS.get(month);
	}

	public static String getMonthAbbreviationOneBased(Integer month){
		return getMonthAbbreviationZeroBased(month - 1);
	}
	public static Date parseDate(String date, String format) throws ParseException{
		return new SimpleDateFormat(format).parse(date);
	}
	public static Date parseDateMMMddHHmmyyyy(String date) throws ParseException{
		return parseDate(date,"MMM dd HH:mm yyyy");
	}
	public static Date parseDateMMMddyyyy(String date) throws ParseException{
		return parseDate(date,"MMM dd yyyy");
	}
	public static Date parseyyyyMMdd(String date) throws ParseException{
		return parseDate(date,"yyyyMMdd");
	}
	public static Date parseMMddyy(String date) throws ParseException{
		date = date.replaceAll("[\\W\\s_]+"," ");
		return parseDate(date, "MM dd yy");
	}

	/**
	 * Parse the output of java.util.Date.toString()
	 * @param date
	 * @return
	 * @throws ParseException
	 */
	public static Date parseJavaDate(String date) throws ParseException{
		return parseDate(date, "EEE MMM dd HH:mm:ss zzz yyyy");
	}


	/**
	 * Parse the dates passed in using the IfModifiedDate header format(s).
	 * @param date
	 * @return
	 * @throws ParseException
	 */
	public static Date parseModifiedSinceDate(String date) throws ParseException{
		return parseDate(date, PATTERN_RFC_2822, "EEE MMM dd HH:mm:ss zzz yyyy", "dd MMM yyyy HH:mm:ss Z");
	}


	/**
	 * @param date a string version of the date
	 * @param formats a list of valid formats to try
	 * @return a valid date if the date was not null
	 * @throws ParseException if cannot be parsed by any format
	 */
	public static Date parseDate(String date, String... formats) throws ParseException{
		if(StringTool.isEmpty(date)) return null;
		int offset = 0;
		for(String format : formats){
			try{
				SimpleDateFormat fmt = new SimpleDateFormat(format);
				return fmt.parse(date);
			}catch(ParseException e){
				offset = e.getErrorOffset();
			}
		}
		throw new ParseException("Cannot parse java date " + date, offset);
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

	public static Long getTimeNullSafe(Date d){
		if(d == null){
			return null;
		}
		return d.getTime();
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

	public static Integer getMonthInteger(){
		return getMonthIntegerOneBased(new Date());
	}

	public static Integer getMonthIntegerOneBased(){
		return getMonthIntegerOneBased(new Date());
	}

	public static Integer getDateInteger(){
		return getDateInteger(new Date());
	}

	public static Integer getHourInteger(){
		return getHourInteger(new Date());
	}

	public static Integer getMinuteInteger(){
		return getMinuteInteger(new Date());
	}

	public static Integer getSecondInteger(){
		return getSecondInteger(new Date());
	}

	public static Integer getYearInteger(Date d){
		return getCalendarField(d, Calendar.YEAR);
	}

	public static Integer getMonthIntegerOneBased(Date d){
		return getCalendarField(d, Calendar.MONTH) + 1;
	}

	public static Integer getDateInteger(Date d){
		return getCalendarField(d, Calendar.DATE);
	}

	public static Integer getDayInteger(Date d){
		return getCalendarField(d, Calendar.DAY_OF_WEEK);
	}

	public static Integer getHourInteger(Date d){
		return getCalendarField(d, Calendar.HOUR_OF_DAY);
	}

	public static Integer getMinuteInteger(Date d){
		return getCalendarField(d, Calendar.MINUTE);
	}

	public static Integer getSecondInteger(Date d){
		return getCalendarField(d, Calendar.SECOND);
	}

	public static String getSimpleDateTime(Date date){
		SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm");
		return sdf.format(date);
	}
	
	public static String getDateTime(Date date){
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return dateFormat.format(date);
	}

	public static String getSimpleDate(Date date){
		SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");
		return sdf.format(date);
	}

	public static String getNumericDate(Date date){
		SimpleDateFormat sdf = new SimpleDateFormat("M/d/yy");
		return sdf.format(date);
	}

	public static String getNumericDateFullYear(Date date){
		if(date == null){
			return null;
		}
		SimpleDateFormat sdf = new SimpleDateFormat("M/d/yyyy");
		return sdf.format(date);
	}

	public static int getDayOfWeekOneBased(int year, int monthOneBased, int dateOneBased){
		Calendar calendar = Calendar.getInstance();
		calendar.set(year, monthOneBased-1, dateOneBased);  //calendar thinks january=fucking 0
		return calendar.get(Calendar.DAY_OF_WEEK);
	}

	public static String getDayAbbreviation(Date date){
		SimpleDateFormat sdf = new SimpleDateFormat("EEE");
		return sdf.format(date);
	}
	
	public static String getDayAbbreviation(Date date, TimeZone timeZone){
		SimpleDateFormat sdf = new SimpleDateFormat("EEE");
		sdf.setTimeZone(timeZone);
		return sdf.format(date);
	}

	/**
	 * Get the start and end date of a day
	 * @param dayOffset 0 for today, -1 for yesterday, 1 for tomorrow, etc.
	 * @return Pair<Date,Date> where left is start, right is end
	 */
	public static Pair<Date, Date> getDayRange(int dayOffset, TimeZone timezone){
		return getCalendarRange(Calendar.DATE,null,dayOffset,timezone);
	}
	/**
	 * Get the start and end date of a week
	 * @param weekOffset 0 for this week, -1 for last week, 1 for next, etc.
	 * @return Pair<Date,Date> where left is start, right is end
	 */
	public static Pair<Date, Date> getWeekRange(int weekOffset, TimeZone timezone){
		return getCalendarRange(Calendar.WEEK_OF_YEAR, Calendar.DAY_OF_WEEK, weekOffset, timezone);
	}
	public static Pair<Date, Date> getAccountingWeekRange(int weekOffset, TimeZone timezone){
		Calendar c = Calendar.getInstance();
		if(timezone!=null){
			c.setTimeZone(timezone);
		}
		c.setFirstDayOfWeek(Calendar.MONDAY);
		c.set(Calendar.DAY_OF_WEEK, c.getFirstDayOfWeek());
		return getCalendarRange(Calendar.WEEK_OF_YEAR, null, weekOffset, c);
	}

	/**
	 * Get the start and end date of a month
	 * @param monthOffset 0 for this month, -1 for last month, 1 for month, etc.
	 * @return Pair<Date,Date> where left is start, right is end
	 */
	public static Pair<Date, Date> getMonthRange(int monthOffset, TimeZone timezone){
		return getCalendarRange(Calendar.MONTH, Calendar.DAY_OF_MONTH, monthOffset, timezone);
	}

	/**
	 * Get the start and end date of a time period
	 * @param period the time period this range is for,
	 * 			eg Calendar.MONTH, Calendar.WEEK_OF_YEAR, Calendar.DATE
	 * @param dayStep the day granularity of this period, null if period<=date
	 * 			eg MONTH:DAY_OF_MONTH, WEEK_OF_YEAR:DAY_OF_WEEK
	 * @param periodOffset 0 for current, -1 for last, 1 for next, etc.
	 * @param timezone
	 * @return
	 */
	private static Pair<Date,Date> getCalendarRange(
			int period, Integer dayStep, int periodOffset, TimeZone timezone){
		Calendar c = Calendar.getInstance();
		if(timezone!=null){
			c.setTimeZone(timezone);
		}
		return getCalendarRange(period,dayStep,periodOffset,c);
	}
	private static Pair<Date,Date> getCalendarRange(
			int period, Integer dayStep, int periodOffset, Calendar c){
		if(dayStep!=null){
			c.set(dayStep, 1);
		}
		c.add(period, periodOffset);

		c.set(Calendar.AM_PM, Calendar.AM);
    	c.set(Calendar.MILLISECOND, 0);
    	c.set(Calendar.SECOND, 0);
    	c.set(Calendar.MINUTE, 0);
    	c.set(Calendar.HOUR, 0);
    	Date start = c.getTime();

    	c.add(period, 1);
    	c.add(Calendar.MILLISECOND,-1);
    	Date stop = c.getTime();
		return Pair.create(start,stop);
	}

	public static Long getPeriodStart(long periodMs){
		return getPeriodStart(System.currentTimeMillis(), periodMs);
	}

	public static Long getPeriodStart(long timeMs, long periodMs){
		return (timeMs - (timeMs % periodMs));
	}

	/******************************** reverse long dates **********************************/
	
	public static Long toReverseDateLong(Date date) {
		return date == null ? null : Long.MAX_VALUE - date.getTime();
	}
	
	public static Date fromReverseDateLong(Long dateLong) {
		return dateLong == null ? null : new Date(Long.MAX_VALUE - dateLong);
	}
	
	/********************************* shifting *******************************/

	public static Date getAdjustedDate(
			Date input, int years, int months, int days,
			int hours, int minutes, int seconds, int ms){
		Calendar output = Calendar.getInstance();
		output.setFirstDayOfWeek(Calendar.SUNDAY);
		output.setTime(input);
		output.add(Calendar.YEAR, years);
		output.add(Calendar.MONTH, months);
		output.add(Calendar.DATE, days);
		output.add(Calendar.HOUR_OF_DAY, hours);
		output.add(Calendar.MINUTE, minutes);
		output.add(Calendar.SECOND, seconds);
		output.add(Calendar.MILLISECOND, ms);
		return output.getTime();
	}
	public static Date getAdjustedYears(Date input, int adjustment){
		return getAdjustedDate(input, adjustment, 0, 0, 0, 0, 0, 0);
	}
	public static Date getAdjustedMonths(Date input, int adjustment){
		return getAdjustedDate(input, 0, adjustment, 0, 0, 0, 0, 0);
	}
	public static Date getAdjustedDays(Date input, int adjustment){
		return getAdjustedDate(input, 0, 0, adjustment, 0, 0, 0, 0);
	}
	public static Date getAdjustedHours(Date input, int adjustment){
		return getAdjustedDate(input, 0, 0, 0, adjustment, 0, 0, 0);
	}
	public static Date getAdjustedMinutes(Date input, int adjustment){
		return getAdjustedDate(input, 0, 0, 0, 0, adjustment, 0, 0);
	}
	public static Date getAdjustedSeconds(Date input, int adjustment){
		return getAdjustedDate(input, 0, 0, 0, 0, 0, adjustment, 0);
	}
	public static Date getAdjustedMilliseconds(Date input, int adjustment){
		return getAdjustedDate(input, 0, 0, 0, 0, 0, 0, adjustment);
	}

	public static Date twentyFourHoursAgo() {
		Calendar twentyFourHoursAgo = Calendar.getInstance();
		twentyFourHoursAgo.add(Calendar.HOUR_OF_DAY, -24);
		return twentyFourHoursAgo.getTime();
	}
	
	public static Date getLastMillisecondOfPreviousDay(Date date){
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.add(Calendar.MILLISECOND, -1);
		
		return calendar.getTime();
	}
	
	public static Date getLastMillisecondOfCurrentDay(Date date){
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);		
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.HOUR_OF_DAY, 0);		
		calendar.add(Calendar.HOUR_OF_DAY, 24);
		calendar.add(Calendar.MILLISECOND, -1);
		
		return calendar.getTime();
	}

	public static Date getFirstMillisecondOfMonth(Date date){
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.DAY_OF_MONTH,1);
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		
		return calendar.getTime();
	}
	
	public static Date getLastMillisecondOfMonth(Date date){
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(getFirstMillisecondOfMonth(date));
		calendar.add(Calendar.MONTH,1);
		calendar.add(Calendar.MILLISECOND, -1);
		return calendar.getTime();
	}
	
	/************************ time elapsed *********************************/

	public static long getMillisecondDifference(Date d1, Date d2){
		return d2.getTime() - d1.getTime();
	}
	public static double getWeeksBetween(Date d1, Date d2){
		return getPeriodsBetween(d1, d2, MILLISECONDS_IN_WEEK);
	}
	
	/**
	 * note: 24 hr periods. partial days like DST switch and leap seconds are not 24 hours. use getDatesBetween
	 */
	public static double getDaysBetween(Date d1, Date d2){
		return getPeriodsBetween(d1, d2, MILLISECONDS_IN_DAY);
	}
	public static double getHoursBetween(Date d1, Date d2){
		return getPeriodsBetween(d1, d2, MILLISECONDS_IN_HOUR);
	}
	public static double getMinutesBetween(Date d1, Date d2){
		return getPeriodsBetween(d1 ,d2, MILLISECONDS_IN_MINUTE);
	}
	public static double getSecondsBetween(Date d1, Date d2){
		return getPeriodsBetween(d1 ,d2, MILLISECONDS_IN_SECOND);
	}
	public static double getPeriodsBetween(Date d1, Date d2, long periodLengthMs){
		long msDif = Math.abs(getMillisecondDifference(d1,d2));
		return msDif / (double)periodLengthMs;
	}
	
	public static int getDatesBetween(Date oldDate, Date newDate){
		//round everything > .95 up to handle partial days due to DST and leap seconds
		double daysBetween = DateTool.getDaysBetween(oldDate, newDate);
		return (int)Math.ceil(daysBetween-(.95d));
	}
	
	/********************* XsdDateTime *************************************/

    public static String getXsdDateTime(Date d) {
    	return getInternetDate(d);
    }

    public static String getXsdDateTime() {
    	return getInternetDate(new Date());
    }

    /* as specified in RFC 3339 / ISO 8601 */
    public static String getInternetDate(Date d){
    	TimeZone tz = TimeZone.getTimeZone("GMT+00:00");
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    	sdf.setTimeZone(tz);
    	return sdf.format(d);
    }

    public static Date parseInternetDate(String date) throws ParseException{
		if(date.length()==25 && ':'==date.charAt(22)){
			date = date.substring(0,22)+date.substring(23);
		}else if(date.length()==29 && ':'==date.charAt(26)){
			date = date.substring(0,26)+date.substring(27);
		}
		SimpleDateFormat dateParser;
		if(date.length() == 19){
			dateParser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		}else if(date.length()==20 && date.endsWith("Z")){ //GMT
			dateParser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			dateParser.setTimeZone(TimeZone.getTimeZone("GMT"));
		}else if(date.length()==24 && date.endsWith("Z")){ //GMT
			dateParser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
			dateParser.setTimeZone(TimeZone.getTimeZone("GMT"));
		}else if(date.length()>19 && date.charAt(19) == '.' && date.endsWith("Z")){//GMT
			dateParser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
			dateParser.setTimeZone(TimeZone.getTimeZone("GMT"));
		}else if(date.length()>19 && date.charAt(19) == '.'){
			dateParser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		}else{
			dateParser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		}
    	return dateParser.parse(date);
    }

    public static String getNow(){
    	return getStandardDate(Calendar.getInstance());
    }

    public static String getStandardDate(Calendar c){
    	SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz");
    	return f.format(c.getTime());
    }

    public static String getStandardDateWithoutTimezone(Date d){
    	SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    	return f.format(d);
    }
    
    public static String getStandardDateWithoutTimezone(Calendar c){
    	SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    	return f.format(c.getTime());
    }

    public static String getYYYYMMDD(){
    	return getYYYYMMDD(new Date());
    }

    public static DateFormat getYYYYMMDD_format() {
    	return new SimpleDateFormat("yyyyMMdd");
    }
    public static String getYYYYMMDD(Date date){
    	return getYYYYMMDD_format().format(date);
    }

    public static DateFormat getYYYYMMDDHH_format() {
    	return new SimpleDateFormat("yyyyMMddHH");
    }
    public static String getYYYYMMDDHH(Date date) {
    	return getYYYYMMDDHH_format().format(date);
    }

    public static DateFormat getYYYYMMDDHHMM_format() {
    	return new SimpleDateFormat("yyyyMMddHHmm");
    }
    public static String getYYYYMMDDHHMM(Date date){
    	return getYYYYMMDDHHMM_format().format(date);
    }

    public static String getYYYYMMDDHHMMSS(){
    	return getYYYYMMDDHHMMSS(new Date());
    }

    public static DateFormat getYYYYMMDDHHMMSS_format() {
    	return new SimpleDateFormat("yyyyMMddHHmmss");
    }
    public static String getYYYYMMDDHHMMSS(Date date){
    	SimpleDateFormat f = new SimpleDateFormat("yyyyMMddHHmmss");
    	return f.format(date);
    }

    public static DateFormat getYYYYMMDDHHMMSSMMM_format() {
    	return new SimpleDateFormat("yyyyMMddHHmmssSSS");
    }
    public static String getYYYYMMDDHHMMSSMMM(){
    	return getYYYYMMDDHHMMSSMMM(new Date());
    }

    public static String getYYYYMMDDHHMMSSMMM(Date date){
    	return getYYYYMMDDHHMMSSMMM_format().format(date);
    }

    public static String getYYYYMMDDHHMMSSMMMWithPunctuationNoSpaces(Long ms){
    	SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss.SSS");
    	return f.format(new Date(ms));
    }

    public static String getYYYYMMDDHHMMSSMMMWithPunctuationNoSpaces(Date date){
    	SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss.SSS");
    	return f.format(date);
    }

    public static String getYYYYMMDDHHMMWithPunctuation(Date date){
    	SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    	return f.format(date);
    }
    public static String getYYYYMMDDHHMMWithColons(Date date){
    	SimpleDateFormat f = new SimpleDateFormat("yyyy:MM:dd:HH:mm");
    	return f.format(date);
    }

    public static String getYYYYMMDDHHMMSSWithPunctuationNoSpaces(Long ms){
    	return getYYYYMMDDHHMMSSWithPunctuationNoSpaces(new Date(ms));
    }
    public static String getYYYYMMDDHHMMSSWithPunctuationNoSpaces(Date date){
    	SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
    	return f.format(date);
    }

    public static String getYYYYMMDDHHMM_UrlFriendly(){
    	return getYYYYMMDDHHMM_UrlFriendly(new Date());
    }
    public static String getYYYYMMDDHHMM_UrlFriendly(Date date){
    	SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
    	return f.format(date);
    }

    public static String getTimeOnDate(Date date){
    	SimpleDateFormat fTime = new SimpleDateFormat("h:mm a");
    	SimpleDateFormat fDate = new SimpleDateFormat("MMM d, yy");
    	String result = fTime.format(date) + " on " + fDate.format(date);
    	return result;
    }

    public static String getDateAtTime(Date date){
    	SimpleDateFormat fTime = new SimpleDateFormat("h:mm a");
    	SimpleDateFormat fDate = new SimpleDateFormat("MMM d, yy");
    	String result = fDate.format(date) + " at " + fTime.format(date);
    	return result;
    }

    public static String getYYYYMM(Long ms){
    	return getYYYYMM(new Date(ms));
    }

    public static String getYYYYMM(Date date){
    	return FORMAT_yyyyMM.format(date);
    }

    public static String getYYYY_MM_DD(Date date){
    	SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
    	return f.format(date);
    }

    public static String getMM_DD_YYYY(Date date){
    	SimpleDateFormat f = new SimpleDateFormat("MM/dd/yyyy");
    	return f.format(date);
    }

    public static String getDD(Date date){
    	SimpleDateFormat f = new SimpleDateFormat("dd");
    	return f.format(date);
    }

    public static String getMMDDYYYY_Slashed(Date date){
    	SimpleDateFormat f = new SimpleDateFormat("M/d/yyyy");
    	return f.format(date);
    }
    
    public static String format(Date date, String dateFormat){
    	SimpleDateFormat f = new SimpleDateFormat(dateFormat);
    	return f.format(date);
    }

    public static double getNumDaysAgo(Date date){
    	long millisecondsElapsed = System.currentTimeMillis() - date.getTime();
    	return ( (double) millisecondsElapsed) / (24*60*60*1000);
    }

	public static int getNumberOfDaysBetweenDates(
			Calendar cal1, Calendar cal2, boolean inclusive){
		cal1.setTimeZone(TimeZone.getTimeZone("UTC"));
		cal1.set(Calendar.HOUR_OF_DAY, 0);
		cal1.set(Calendar.MINUTE, 0);
		cal1.set(Calendar.SECOND, 0);
		cal1.set(Calendar.MILLISECOND, 0);

		cal2.setTimeZone(TimeZone.getTimeZone("UTC"));
		cal2.set(Calendar.HOUR_OF_DAY, 0);
		cal2.set(Calendar.MINUTE, 0);
		cal2.set(Calendar.SECOND, 0);
		cal2.set(Calendar.MILLISECOND, 0);

		Long numberOfDays =
			(cal2.getTimeInMillis() - cal1.getTimeInMillis())/(24*60*60*1000);
		return Math.abs(numberOfDays.intValue() + (inclusive ? 1 : -1));
	}
	
	public static Date getDateFrom(int daysAway, Date fromDate) {
		Calendar c = Calendar.getInstance();
		if (fromDate != null) {
			c.setTime(fromDate);
		}
		c.add(Calendar.DATE, 1 * daysAway);
		return c.getTime();
	}
	
	public static Date getDateFromNow(int daysAway) {
		return getDateFrom(daysAway, null);
	}
	
	public static Date getDateAgo(int daysAgo, Date fromDate) {
		Calendar c = Calendar.getInstance();
		if (fromDate != null) {
			c.setTime(fromDate);
		}
		c.add(Calendar.DATE, -1 * daysAgo);
		return c.getTime();
	}
	
	public static Date getDateAgo(int daysAgo) {
		return getDateAgo(daysAgo, null);
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
     * get the date in the form of "XX days, XX hours" from now
     * only returns minutes if less than 3 hours from now, and only seconds if less than 1 minute from now
     *
     * returns the same as getAgoString for times in the past, but with the ' ago'.  also works for the future
     *
     * @param date
     * @return "XXXXdays, XX hours" or "XX minutes" or "XX seconds" or "less than one second"
     */
    public static String getFromNowString(Date date){
    	return getFromNowString(date, DEFAULT_MAX_UNITS);
    }
    public static String getFromNowString(Date date, int maxUnits) {
    	long timeMillis = date.getTime() - new Date().getTime();
    	if (timeMillis < 0){
			timeMillis = -timeMillis;
		}

    	return getMillisAsString(timeMillis, maxUnits, DurationUnit.SECONDS);
    }
    
    /**
     * get the date difference in the form of "XX days, XX hours" 
     * only returns minutes if less than 3 hours between dates, and only seconds if less than 1 minute between dates
     *
     * @param date1
     * @param date2
     * @return "XXXXdays, XX hours" or "XX minutes" or "XX seconds" or "less than one second"
     */
    public static String getDifferenceString(Date date1, Date date2){
    	return getDifferenceString(date1, date2, DEFAULT_MAX_UNITS);
    }
    public static String getDifferenceString(Date date1, Date date2, int maxUnits){
    	long timeMillis = date2.getTime() - date1.getTime();
    	if (timeMillis < 0){
			timeMillis = -timeMillis;
		}

    	return getMillisAsString(timeMillis, maxUnits, DurationUnit.SECONDS);
    }
    
    public static Date getFuture(int daysFromNow){
    	Calendar c = Calendar.getInstance();
    	c.add(Calendar.DAY_OF_YEAR, daysFromNow);
    	return c.getTime();
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
	public static int calendarDifferenceInDays(Calendar start, Calendar end){
		if(start.after(end)){
			return 0;
		}
		int diff = 0;
		while(start.compareTo(end) == -1){
			diff++;
			start.add(Calendar.DAY_OF_MONTH, 1);
			if(diff > 1000){
				break;
			}
		}
		return diff;
	}

	public static Calendar getCalendar(int year, int month, int date, int hours, int minutes, int seconds){
		Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, year);
		c.set(Calendar.MONTH, month-1);
		c.set(Calendar.DATE, date);
		c.set(Calendar.HOUR_OF_DAY, hours);
		c.set(Calendar.MINUTE, minutes);
		c.set(Calendar.SECOND, seconds);
		return c;
	}
	
	/**
	 * 
	 * @param e.g. date=20130905
	 * @return 20130906
	 */
	public static int getNextYymmdd(int yyyymmdd){
		int year = yyyymmdd / 10000;
		int month = (yyyymmdd % 10000) / 100;
		int day = (yyyymmdd % 100);

		if((day < 28)
				|| (day < 29 && month == 2 && (year % 4 == 0 && year % 400 != 0))
				|| (day < 30 && month != 2)
				|| ((month == 1 || month == 3 || month == 5 || month == 7 || month == 8 || month == 10 || month == 12) && day < 31)){
			return year * 10000 + month * 100 + day + 1;
		}
		day = 1;
		month = month + 1;
		if(month > 12){
			month = 1;
			year = year + 1;
		}
		return year * 10000 + month * 100 + day;
	}
	
	
	
	/* https://developers.google.com/bigquery/timestamp */
	public static String getGoogleBigQueryDate(long ms){
		TimeZone tz = TimeZone.getTimeZone("GMT+00:00");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		sdf.setTimeZone(tz);
		return sdf.format(new Date(ms));
	}
	
	public static boolean isWeekday(Date date){
		int dayInteger = getDayInteger(date);
		return (dayInteger == MONDAY_INDEX) || (dayInteger == TUESDAY_INDEX) || (dayInteger == WEDNESDAY_INDEX)
				|| (dayInteger == THURSDAY_INDEX) || (dayInteger == FRIDAY_INDEX);
	}
	
	public static boolean isWeekend(Date date){
		int dayInteger = getDayInteger(date);
		return (dayInteger == SATURDAY_INDEX) || (dayInteger == SUNDAY_INDEX);
	}
	
	public static boolean isSunday(Date date){
		return SUNDAY_INDEX == getDayInteger(date);
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
	public static boolean isSaturday(Date date){
		return SATURDAY_INDEX == getDayInteger(date);
	}
	
	public static Date getStartOfDay( Date fromDate ) {
		return getHourOfDay( fromDate, 0 );
	}

	/**
	 * Fetch a clock time on a particular day.
	 * To handle hour that is negative or >= 24 then the date is adjusted too.
	 * If you want midnight (00:00) am then set hour = 0.
	 * @param fromDate the date to centre the clock time on
	 * @param hour the clock time (0-23) within the day to use
	 * @return a date with the clock time set to that hour for the day.
	 */
	public static Date getHourOfDay( Date fromDate, int hour ) {
		Calendar c = Calendar.getInstance();
		if (fromDate != null) {
			c.setTime(fromDate);
		}
		int days = ( hour / 24 );
		if ( hour < 0 ) { 
			days = days -1;
			hour = 24 - (-hour)%24;
		}
		if ( days != 0 ) {
			c.add(Calendar.DATE, days);
		}
		hour = hour%24;
		c.set( Calendar.HOUR_OF_DAY, hour );
		c.set( Calendar.MINUTE,  0 );
		c.set( Calendar.SECOND,  0 );
		c.set( Calendar.MILLISECOND, 0 );
		return c.getTime();
	}

	public static Date getTime( int yyyy, int month, int date, int hours, int minutes, int seconds ) {
		Calendar c = getCalendar( yyyy, month, date, hours, minutes, seconds);
		c.set(Calendar.MILLISECOND, 0);
		return c.getTime();
	}
	
	/************************** tests ******************************/


	public static class Tests {

		private SimpleDateFormat getTestFormat(){
			SimpleDateFormat format =
				new SimpleDateFormat("EEE MMM dd hh:mm:ss zzz yyyy");
//				new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
			format.setTimeZone(TimeZone.getTimeZone("US/Eastern"));
			return format;
		}

		private void checkDateMatches( SimpleDateFormat format, Date expected, Date result ) {
			Assert.assertTrue( (expected == null ) == ( result == null ) );
			if ( expected != null ) {
				
				String sExpected = format.format( expected );
				String sResult   = format.format( result );
				String msg = "Date " + sResult + " should match " + sExpected;
				System.out.println( msg );
				Assert.assertTrue( msg, sExpected.equals( sResult ) );
				
			}
		}

		@Test public void testHourOnDay() {
			Date d0 = getTime(2014,10,23,12,34,56);			
			Date midnight = getTime(2014,10,23,00,00,00);
			Date one_am   = getTime(2014,10,23,01,00,00);
			Date five_am  = getTime(2014,10,23,05,00,00);
			Date one_pm   = getTime(2014,10,23,13,00,00);
			Date tomorrow1 = getTime(2014,10,24,01,00,00);
			Date yesterdayAt2pm = getTime(2014,10,22,14,0,0);
			Date twoDaysAgo  = getTime(2014,10,21,0,0,0);
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
			
			checkDateMatches(format,midnight, getStartOfDay(d0));
			checkDateMatches(format,one_am, getHourOfDay(d0, 1));
			checkDateMatches(format,five_am, getHourOfDay(d0, 5));
			checkDateMatches(format,one_pm, getHourOfDay(d0, 13));
			checkDateMatches(format,tomorrow1, getHourOfDay(d0, 25));
			checkDateMatches(format,yesterdayAt2pm, getHourOfDay(d0,-10));
			checkDateMatches(format,twoDaysAgo, getHourOfDay(d0,-24));
		}

		@Test public void testGetDayRange(){
			System.err.println("DAY RANGES:");
			Pair<Date,Date> day = getDayRange(0,TimeZone.getTimeZone("US/Eastern"));
			System.err.println(day.getLeft()+" "+day.getRight());
			day = getDayRange(1,TimeZone.getTimeZone("US/Eastern"));
			System.err.println(day.getLeft()+" "+day.getRight());
			day = getDayRange(-1,TimeZone.getTimeZone("US/Eastern"));
			System.err.println(day.getLeft()+" "+day.getRight());
			day = getDayRange(2,TimeZone.getTimeZone("US/Eastern"));
			System.err.println(day.getLeft()+" "+day.getRight());
			System.err.println();
		}
		@Test public void testGetWeekRange(){
			System.err.println("WEEK RANGES:");
			Pair<Date,Date> week = getWeekRange(0, TimeZone.getTimeZone("US/Eastern"));
			System.err.println(week.getLeft()+" "+week.getRight());
			week = getWeekRange(1, TimeZone.getTimeZone("US/Eastern"));
			System.err.println(week.getLeft()+" "+week.getRight());
			week = getWeekRange(-1, TimeZone.getTimeZone("US/Eastern"));
			System.err.println(week.getLeft()+" "+week.getRight());
			week = getWeekRange(2, TimeZone.getTimeZone("US/Eastern"));
			System.err.println(week.getLeft()+" "+week.getRight());
			System.err.println();
		}
		@Test public void testGetAccountingWeekRange(){
			System.err.println("ACCOUNTING WEEK RANGES:");
			Pair<Date,Date> week = getAccountingWeekRange(0, TimeZone.getTimeZone("US/Eastern"));
			System.err.println(week.getLeft()+" "+week.getRight());
			week = getAccountingWeekRange(1, TimeZone.getTimeZone("US/Eastern"));
			System.err.println(week.getLeft()+" "+week.getRight());
			week = getAccountingWeekRange(-1, TimeZone.getTimeZone("US/Eastern"));
			System.err.println(week.getLeft()+" "+week.getRight());
			week = getAccountingWeekRange(2, TimeZone.getTimeZone("US/Eastern"));
			System.err.println(week.getLeft()+" "+week.getRight());
			System.err.println();
		}
		@Test public void testGetMonthRange(){
			System.err.println("MONTH RANGES:");
			Pair<Date,Date> month = getMonthRange(0, TimeZone.getTimeZone("US/Eastern"));
			System.err.println(month.getLeft()+" "+month.getRight());
			month = getMonthRange(1, TimeZone.getTimeZone("US/Eastern"));
			System.err.println(month.getLeft()+" "+month.getRight());
			month = getMonthRange(-1, TimeZone.getTimeZone("US/Eastern"));
			System.err.println(month.getLeft()+" "+month.getRight());
			month = getMonthRange(2, TimeZone.getTimeZone("US/Eastern"));
			System.err.println(month.getLeft()+" "+month.getRight());
			System.err.println();
		}

		@Test public void testGetCalendar(){
			Calendar c = getCalendar(2009,3,9,13,56,49);
			Assert.assertEquals("2009-03-09_13:56:49",
					getYYYYMMDDHHMMSSWithPunctuationNoSpaces(c.getTime()));
		}
		@Test public void testParseCommonDate(){
			/*
			{"MM dd yy","MMM dd yy","MMMMM dd yy",
				"MM dd", "MMM dd","MMMMM dd", };*/
			SimpleDateFormat df = new SimpleDateFormat("MM-dd-yyyy");
			Assert.assertEquals("11-05-2008",
					df.format(parseUserInputDate("11-05-08",null)));
			Assert.assertEquals("11-05-2008",
					df.format(parseUserInputDate("11-05-2008",null)));
			Assert.assertEquals("11-05-1982",
					df.format(parseUserInputDate("11-05-82",null)));
			Assert.assertEquals("11-05-1982",
					df.format(parseUserInputDate("11-05-1982",null)));
			Assert.assertEquals("11-05-2008",
					df.format(parseUserInputDate("Nov 5, 2008",null)));
			Assert.assertEquals("11-05-2008",
					df.format(parseUserInputDate("Nov 5, 08",null)));
			Assert.assertEquals("11-05-2008",
					df.format(parseUserInputDate("November 5, 2008",null)));
			Assert.assertEquals("11-05-2008",
					df.format(parseUserInputDate("November 05, 2008",null)));
			Assert.assertEquals("07-15-2011",
					df.format(parseUserInputDate("July 15th, 2011",null)));
			Assert.assertEquals("07-15-"+DateTool.getYearInteger(),
					df.format(parseUserInputDate("July 15th",2000)));
			Assert.assertEquals("01-01-2008",
					df.format(parseUserInputDate("1/1/2008",null)));
			Assert.assertEquals("01-01-2008",
					df.format(parseUserInputDate("01/01/2008",null)));
			Assert.assertEquals("01-05-2008",
					df.format(parseUserInputDate("01/05/2008",null)));
			Assert.assertEquals("01-05-2008",
					df.format(parseUserInputDate("1/5/08",null)));
			Assert.assertEquals("06-01-2010",
					df.format(parseUserInputDate("June 2010",null)));
			Assert.assertEquals("06-01-2010",
					df.format(parseUserInputDate("201006",null)));
			Assert.assertEquals("06-01-2010",
					df.format(parseUserInputDate("20100601",null)));
		}

		@Test public void testTwentyFourHoursAgo() {
			Date twentyFourHoursAgo = twentyFourHoursAgo();
			Date now = Calendar.getInstance().getTime();

			assertTrue(twentyFourHoursAgo.before(now));
			assertTrue(now.getTime() - twentyFourHoursAgo.getTime() >= 24*60*60*1000);
		}

		@Test public void test2() throws Exception{
			Date today = new Date();
			String dateAsString = DateTool.getYYYYMMDD(today);
			Date backToDate = DateTool.parseyyyyMMdd(dateAsString);
			Assert.assertEquals(dateAsString, DateTool.getYYYYMMDD(backToDate));
		}

		@Test public void testParseInternetDate() throws Exception{
			Date parsedDate = parseInternetDate("2006-11-07T14:10:00+00:00");

			Assert.assertEquals("Tue Nov 07 09:10:00 EST 2006",
					getTestFormat().format(parsedDate));

			Assert.assertEquals("Tue Nov 09 03:52:00 EST 2010",
					getTestFormat().format(
							parseInternetDate("2010-11-09T20:52:00Z")));
			
			String s0 = "2014-04-01T20:17:35.339+05:00",
					s1 = "2014-04-01T20:17:35.339+0500",
					s2 = "1994-11-05T08:15:30-05:00",
					s3 = "2014-04-02T23:43:48Z",
					s4 = "2014-04-02T23:43:48.554Z",
					s5 = "2009-08-27T09:02:00.0000000Z";
			
			Date d0 = DateTool.parseInternetDate(s0),
					d1 = DateTool.parseInternetDate(s1),
					d2 = DateTool.parseInternetDate(s2),
					d3 = DateTool.parseInternetDate(s3),
					d4 = DateTool.parseInternetDate(s4),
					d5 = DateTool.parseInternetDate(s5) ;
			
			Assert.assertEquals(1396365455339L, d0.getTime());
			Assert.assertEquals(1396365455339L, d1.getTime());
			Assert.assertEquals(784041330000L, d2.getTime());
			Assert.assertEquals(1396482228000L, d3.getTime());
			Assert.assertEquals(1396482228554L, d4.getTime());
			Assert.assertEquals(1251363720000L, d5.getTime());
		}

		@Test public void testAgoString() throws Exception{
			Assert.assertEquals("950 milliseconds",
					getMillisAsString(950, 1, DurationUnit.MILLISECONDS));
			Assert.assertEquals("950 milliseconds",
					getMillisAsString(950, 5, DurationUnit.MILLISECONDS));
			Assert.assertEquals("52 seconds",
					getMillisAsString(52950, 1, DurationUnit.MILLISECONDS));
			Assert.assertEquals("6 minutes",
					getMillisAsString(360051, 2, DurationUnit.MILLISECONDS));
			Assert.assertEquals("3 minutes, 45 seconds",
					getMillisAsString(225950, 2, DurationUnit.MILLISECONDS));
			Assert.assertEquals("2 hours, 50 minutes",
					getMillisAsString(10225950, 2, DurationUnit.MILLISECONDS));
			Assert.assertEquals("2 days, 18 hours, 43 minutes, 45 seconds",
					getMillisAsString(240225950, 4,	DurationUnit.MILLISECONDS));
		}

		@Test public void testThreeWideDate() throws Exception{
			Assert.assertEquals("Wed May 05 04:12:00 EDT 2010",
					getTestFormat().format(
							parseInternetDate("2010-05-05T04:12:00.000-04:00")));
			
			//threewide rets photo timestamp date format:
			Assert.assertEquals("Tue Mar 06 05:14:47 EST 2012",
					getTestFormat().format(parseInternetDate("2012-03-06T17:14:47-05:00")));
		}

		@Test public void testParseMMddyy() throws Exception{
			Assert.assertEquals("20100710",
					getYYYYMMDD(parseMMddyy("07/10/10 00:00:00")));
		}

		@Test public void testGetDayAbbreviationOneBased(){
			Assert.assertEquals("Sun",getDayAbbreviationOneBased(1));
			Assert.assertEquals("Thu",getDayAbbreviationOneBased(5));
		}
		@Test public void testGetMonthAbbreviationOneBased(){
			Assert.assertEquals("Jan",getMonthAbbreviationOneBased(1));
			Assert.assertEquals("May",getMonthAbbreviationOneBased(5));
			Assert.assertEquals("Nov",getMonthAbbreviationOneBased(11));
			Assert.assertEquals("Dec",getMonthAbbreviationOneBased(12));
			try{
				Assert.fail(getDayAbbreviationOneBased(13));
			}catch(Exception e){}
			try{
				Assert.fail(getDayAbbreviationOneBased(0));
			}catch(Exception e){}
		}

		@SuppressWarnings("deprecation") //test compares to deprecated methods
		@Test public void testGetIntegers() throws Exception{
			Date d = parseInternetDate("2006-11-07T14:10:06");
			Assert.assertEquals(new Integer(2006),getYearInteger(d));
			Assert.assertEquals(new Integer(d.getYear()+1900),getYearInteger(d));
			Assert.assertEquals(new Integer(11),getMonthIntegerOneBased(d));
			Assert.assertEquals(new Integer(d.getMonth()+1),getMonthIntegerOneBased(d));
			Assert.assertEquals(new Integer(7),getDateInteger(d));
			Assert.assertEquals(new Integer(d.getDate()),getDateInteger(d));
			Assert.assertEquals(new Integer(3),getDayInteger(d));
			Assert.assertEquals(new Integer(d.getDay()+1),getDayInteger(d));
			Assert.assertEquals(new Integer(14),getHourInteger(d));
			Assert.assertEquals(new Integer(d.getHours()),getHourInteger(d));
			Assert.assertEquals(new Integer(10),getMinuteInteger(d));
			Assert.assertEquals(new Integer(d.getMinutes()),getMinuteInteger(d));
			Assert.assertEquals(new Integer(6),getSecondInteger(d));
			Assert.assertEquals(new Integer(d.getSeconds()),getSecondInteger(d));
		}
		
		@Test public void testGetDaysBetween() throws Exception{	
			Date d1 = new Date(1352059736026L);
			int daysApart = 4;
			Date d2 = new Date(d1.getTime()+(MILLISECONDS_IN_DAY*daysApart));

			Assert.assertEquals(daysApart, DateTool.getDaysBetween(d1, d2), 1>>20);
			
			d2 = new Date(d1.getTime()+(MILLISECONDS_IN_DAY*daysApart)-4);
			Assert.assertTrue(daysApart > DateTool.getDaysBetween(d1, d2));
			Assert.assertTrue(daysApart-1 < DateTool.getDaysBetween(d1, d2));
		}
		
		@Test public void testGetGoogleBigQueryDate() throws Exception{
			
			String str1 = "1960-10-01 00:00:00.000";
			String str2 = "1970-01-01 00:00:00.000";
			String str3 = "1995-09-03 07:08:01.134";
			String str4 = "2013-11-03 23:08:01.934";
			
			long lon1 = -291945600000L;
			long lon2 = 0L;
			long lon3 = 810112081134L;
			long lon4 = 1383520081934L;
			
			Assert.assertEquals(getGoogleBigQueryDate(lon1),str1);
			Assert.assertEquals(getGoogleBigQueryDate(lon2),str2);
			Assert.assertEquals(getGoogleBigQueryDate(lon3),str3);
			Assert.assertEquals(getGoogleBigQueryDate(lon4),str4);
		}
		
		@Test public void testGetLastMillisecondOfPreviousDay() throws Exception {
			Date d = parseyyyyMMdd("20141105");
			Date lastMilli = getLastMillisecondOfPreviousDay(d);
			Assert.assertEquals(DateTool.getYearInteger(d), DateTool.getYearInteger(lastMilli));
			Assert.assertEquals(DateTool.getMonthIntegerOneBased(d), DateTool.getMonthIntegerOneBased(lastMilli));
			Assert.assertEquals((Integer)(DateTool.getDateInteger(d)-1), DateTool.getDateInteger(lastMilli));
			Assert.assertEquals((Integer)23, DateTool.getHourInteger(lastMilli));
			Assert.assertEquals((Integer)59, DateTool.getMinuteInteger(lastMilli));
			Assert.assertEquals((Integer)59, DateTool.getSecondInteger(lastMilli));
			Assert.assertEquals((Integer)999, getCalendarField(lastMilli, Calendar.MILLISECOND));
			
			d = parseyyyyMMdd("20140201");
			lastMilli = getLastMillisecondOfPreviousDay(d);
			Assert.assertEquals(DateTool.getYearInteger(d), DateTool.getYearInteger(lastMilli));
			Assert.assertEquals((Integer)1, DateTool.getMonthIntegerOneBased(lastMilli));
			Assert.assertEquals((Integer)31, DateTool.getDateInteger(lastMilli));
			Assert.assertEquals((Integer)23, DateTool.getHourInteger(lastMilli));
			Assert.assertEquals((Integer)59, DateTool.getMinuteInteger(lastMilli));
			Assert.assertEquals((Integer)59, DateTool.getSecondInteger(lastMilli));
			Assert.assertEquals((Integer)999, getCalendarField(lastMilli, Calendar.MILLISECOND));
			
			d = parseyyyyMMdd("20140101");
			lastMilli = getLastMillisecondOfPreviousDay(d);
			Assert.assertEquals((Integer)2013, DateTool.getYearInteger(lastMilli));
			Assert.assertEquals((Integer)12, DateTool.getMonthIntegerOneBased(lastMilli));
			Assert.assertEquals((Integer)31, DateTool.getDateInteger(lastMilli));
			Assert.assertEquals((Integer)23, DateTool.getHourInteger(lastMilli));
			Assert.assertEquals((Integer)59, DateTool.getMinuteInteger(lastMilli));
			Assert.assertEquals((Integer)59, DateTool.getSecondInteger(lastMilli));
			Assert.assertEquals((Integer)999, getCalendarField(lastMilli, Calendar.MILLISECOND));
			
			d = parseJavaDate("Thu Mar 20 16:44:45 PDT 2014");
			lastMilli = getLastMillisecondOfPreviousDay(d);
			Assert.assertEquals((Integer)2014, DateTool.getYearInteger(lastMilli));
			Assert.assertEquals((Integer)3, DateTool.getMonthIntegerOneBased(lastMilli));
			Assert.assertEquals((Integer)19, DateTool.getDateInteger(lastMilli));
			Assert.assertEquals((Integer)23, DateTool.getHourInteger(lastMilli));
			Assert.assertEquals((Integer)59, DateTool.getMinuteInteger(lastMilli));
			Assert.assertEquals((Integer)59, DateTool.getSecondInteger(lastMilli));
			Assert.assertEquals((Integer)999, getCalendarField(lastMilli, Calendar.MILLISECOND));
		}
		
		@Test public void testGetLastMillisecondOfCurrentDay() throws Exception {
			Date d = parseyyyyMMdd("20141105");
			Date lastMilli = getLastMillisecondOfCurrentDay(d);
			Assert.assertEquals(DateTool.getYearInteger(d), DateTool.getYearInteger(lastMilli));
			Assert.assertEquals(DateTool.getMonthIntegerOneBased(d), DateTool.getMonthIntegerOneBased(lastMilli));
			Assert.assertEquals((DateTool.getDateInteger(d)), DateTool.getDateInteger(lastMilli));
			Assert.assertEquals((Integer)23, DateTool.getHourInteger(lastMilli));
			Assert.assertEquals((Integer)59, DateTool.getMinuteInteger(lastMilli));
			Assert.assertEquals((Integer)59, DateTool.getSecondInteger(lastMilli));
			Assert.assertEquals((Integer)999, getCalendarField(lastMilli, Calendar.MILLISECOND));
			
			d = parseyyyyMMdd("20140201");
			lastMilli = getLastMillisecondOfCurrentDay(d);
			Assert.assertEquals(DateTool.getYearInteger(d), DateTool.getYearInteger(lastMilli));
			Assert.assertEquals((Integer)2, DateTool.getMonthIntegerOneBased(lastMilli));
			Assert.assertEquals((Integer)1, DateTool.getDateInteger(lastMilli));
			Assert.assertEquals((Integer)23, DateTool.getHourInteger(lastMilli));
			Assert.assertEquals((Integer)59, DateTool.getMinuteInteger(lastMilli));
			Assert.assertEquals((Integer)59, DateTool.getSecondInteger(lastMilli));
			Assert.assertEquals((Integer)999, getCalendarField(lastMilli, Calendar.MILLISECOND));
			
			d = parseyyyyMMdd("20140101");
			lastMilli = getLastMillisecondOfCurrentDay(d);
			Assert.assertEquals((Integer)2014, DateTool.getYearInteger(lastMilli));
			Assert.assertEquals((Integer)01, DateTool.getMonthIntegerOneBased(lastMilli));
			Assert.assertEquals((Integer)01, DateTool.getDateInteger(lastMilli));
			Assert.assertEquals((Integer)23, DateTool.getHourInteger(lastMilli));
			Assert.assertEquals((Integer)59, DateTool.getMinuteInteger(lastMilli));
			Assert.assertEquals((Integer)59, DateTool.getSecondInteger(lastMilli));
			Assert.assertEquals((Integer)999, getCalendarField(lastMilli, Calendar.MILLISECOND));
			
			d = parseJavaDate("Thu Mar 20 16:44:45 PDT 2014");
			lastMilli = getLastMillisecondOfCurrentDay(d);
			Assert.assertEquals((Integer)2014, DateTool.getYearInteger(lastMilli));
			Assert.assertEquals((Integer)3, DateTool.getMonthIntegerOneBased(lastMilli));
			Assert.assertEquals((Integer)20, DateTool.getDateInteger(lastMilli));
			Assert.assertEquals((Integer)23, DateTool.getHourInteger(lastMilli));
			Assert.assertEquals((Integer)59, DateTool.getMinuteInteger(lastMilli));
			Assert.assertEquals((Integer)59, DateTool.getSecondInteger(lastMilli));
			Assert.assertEquals((Integer)999, getCalendarField(lastMilli, Calendar.MILLISECOND));
		}
		
		
		@Test public void testGetFirstMillisecondOfMonth() throws ParseException{
			Date d = parseyyyyMMdd("20141105");
			Date firstMilli = getFirstMillisecondOfMonth(d);

			Assert.assertEquals((Integer)2014, DateTool.getYearInteger(firstMilli));
			Assert.assertEquals((Integer)11, DateTool.getMonthIntegerOneBased(firstMilli));
			Assert.assertEquals((Integer)1, DateTool.getDateInteger(firstMilli));
			Assert.assertEquals((Integer)0, DateTool.getHourInteger(firstMilli));
			Assert.assertEquals((Integer)0, DateTool.getMinuteInteger(firstMilli));
			Assert.assertEquals((Integer)0, DateTool.getSecondInteger(firstMilli));
			Assert.assertEquals((Integer)0, getCalendarField(firstMilli, Calendar.MILLISECOND));
			
			d = parseyyyyMMdd("20141105");
			firstMilli = getFirstMillisecondOfMonth(d);

			Assert.assertEquals((Integer)2014, DateTool.getYearInteger(firstMilli));
			Assert.assertEquals((Integer)11, DateTool.getMonthIntegerOneBased(firstMilli));
			Assert.assertEquals((Integer)1, DateTool.getDateInteger(firstMilli));
			Assert.assertEquals((Integer)0, DateTool.getHourInteger(firstMilli));
			Assert.assertEquals((Integer)0, DateTool.getMinuteInteger(firstMilli));
			Assert.assertEquals((Integer)0, DateTool.getSecondInteger(firstMilli));
			Assert.assertEquals((Integer)0, getCalendarField(firstMilli, Calendar.MILLISECOND));
			
		}
		
		@Test public void testToReverseDateLong() {
			Date now = new Date(), zero = new Date(0L), max = new Date(Long.MAX_VALUE);
			Assert.assertEquals((Long)(Long.MAX_VALUE - now.getTime()), DateTool.toReverseDateLong(now));
			Assert.assertEquals((Long)Long.MAX_VALUE, DateTool.toReverseDateLong(zero));
			Assert.assertEquals((Long)0L, DateTool.toReverseDateLong(max));
			Assert.assertNull(DateTool.toReverseDateLong(null));
		}
		
		@Test public void testFromReverseDateLong() {
			Date now = new Date(), zero = new Date(0L), max = new Date(Long.MAX_VALUE);
			Assert.assertEquals(now, DateTool.fromReverseDateLong(Long.MAX_VALUE - now.getTime()));
			Assert.assertEquals(zero, DateTool.fromReverseDateLong(Long.MAX_VALUE - zero.getTime()));
			Assert.assertEquals(max, DateTool.fromReverseDateLong(Long.MAX_VALUE - max.getTime()));
			Assert.assertNull(DateTool.fromReverseDateLong(null));
		}
		
		@Test public void testReverseDateLong() {
			Date now = new Date();
			Long nowTime = now.getTime();
			Assert.assertEquals(now, DateTool.fromReverseDateLong(DateTool.toReverseDateLong(now)));
			Assert.assertEquals(nowTime, DateTool.toReverseDateLong(DateTool.fromReverseDateLong(nowTime)));
			Assert.assertNull(DateTool.fromReverseDateLong(DateTool.toReverseDateLong(null)));
			Assert.assertNull(DateTool.toReverseDateLong(DateTool.fromReverseDateLong(null)));
		}
		private static final String format1 = PATTERN_RFC_2822;
		private static final String format2 = "dd MMM yyyy HH:mm:ss Z";

		private void testDate(String dateString, Calendar calendar, String format){
			SimpleDateFormat sdf = new SimpleDateFormat(format);

			try{
				Date cal = calendar.getTime();
				Date date = sdf.parse(dateString);
				String expected = sdf.format(cal);
				String parsed = sdf.format(date);
				String msg = "Date " + dateString + " parsed as " + parsed + " does not match expected " + expected;
				Assert.assertTrue(msg, date.equals(cal));
				// Now test that we have set up this properly in the DateTool also:
				Date javaDate = DateTool.parseModifiedSinceDate(dateString);
				Assert.assertTrue(msg, javaDate.equals(cal));

			}catch(ParseException e){
				Assert.fail("Failed to parse " + dateString);
			}
		}
		
		@Test
		public void testDate1(){
			Calendar cal = new GregorianCalendar(1994, 9, 29, 12, 43, 31);
			cal.setTimeZone(TimeZone.getTimeZone("PST"));
			testDate("Sat, 29 Oct 1994 19:43:31 GMT", cal, format1);
		}

		@Test
		public void testDate2(){
			Calendar cal = new GregorianCalendar(2006, 0, 12, 01, 02, 03);
			cal.setTimeZone(TimeZone.getTimeZone("PST"));
			testDate("12 Jan 2006 01:02:03 PST", cal, format2);
		}
	}

	//helpful site for generating timestamps: http://www.timestampgenerator.com/date-from-timestamp/
	@SuppressWarnings("deprecation")
	public static void main(String[] args){

		Calendar c = DailyCalendarTool.createUSEasternUSFromOneBasedYMD(2011, 8, 9);
		c.set(Calendar.HOUR_OF_DAY, 15);
		c.set(Calendar.MINUTE, 10);
		System.out.println(c.getTime());
		System.out.println(c.getTimeInMillis());

		for(int i=0; i < 24; ++i){
			System.out.println(" where id >= "
					+DailyCalendarTool.createUSEasternUSFromOneBasedYMD(2012, 9, 26).getTimeInMillis()*1000000
					+" and id < "
					+DailyCalendarTool.createUSEasternUSFromOneBasedYMD(2012, 8, 28).getTimeInMillis()*1000000);
		}
		System.out.println("yesterday start:"+DailyCalendarTool.createTodayOffsetUSEastern(-1).getTimeInMillis());
		System.out.println("today start"+DailyCalendarTool.createTodayUSEastern().getTimeInMillis());
		System.out.println(new Date(1312937197199L).toString());
		System.out.println("now:"+System.currentTimeMillis());
		for(int i=0; i < 24; ++i){
			System.out.println(i+" "+new Date(2012, 9, 13, i, 0, 0).getTime() * 1000000);
		}
	}
}

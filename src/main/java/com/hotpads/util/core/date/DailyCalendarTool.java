package com.hotpads.util.core.date;

import java.util.Calendar;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.SortedSet;
import java.util.TimeZone;

import org.junit.Assert;
import org.junit.Test;

import com.hotpads.util.core.ArrayTool;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ComparableTool;
import com.hotpads.util.core.GenericsFactory;
import com.hotpads.util.core.StringTool;


public class DailyCalendarTool {
	
	/************************** zones **************************************/
	//from api: For example, TimeZone.getTimeZone("GMT-8").getID() returns "GMT-08:00".
	
	public static final TimeZone 
		TIME_ZONE_GMT = TimeZone.getTimeZone("GMT"),
		TIME_ZONE_US_EASTERN = TimeZone.getTimeZone("America/New_York"),
		TIME_ZONE_US_CENTRAL = TimeZone.getTimeZone("America/Chicago"),
		TIME_ZONE_US_MOUNTAIN = TimeZone.getTimeZone("America/Denver"),
		TIME_ZONE_US_PACIFIC = TimeZone.getTimeZone("America/Los_Angeles");
	
	
	/*************************************** simple ******************************/
	public static Calendar clone(Calendar c){
		Calendar clone = Calendar.getInstance();
		clone.setTimeZone(c.getTimeZone());
		clone.setTime(c.getTime());
		return clone;
	}
	
	public static int getYear(Calendar c){
		if(c==null){ throw new IllegalArgumentException("c can't be null"); }
		int year = c.get(Calendar.YEAR);
		return year;
	}
	
	public static int getMonthOneBased(Calendar c){
		if(c==null){ throw new IllegalArgumentException("c can't be null"); }
		return 1 + c.get(Calendar.MONTH);
	}
	
	public static int getDate(Calendar c){
		if(c==null){ throw new IllegalArgumentException("c can't be null"); }
		return c.get(Calendar.DATE);
	}	
	
	public static String getDayAbbreviation(Calendar c){
		int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
		switch(dayOfWeek){
		case Calendar.SUNDAY: return "sun";
		case Calendar.MONDAY: return "mon";
		case Calendar.TUESDAY: return "tue";
		case Calendar.WEDNESDAY: return "wed";
		case Calendar.THURSDAY: return "thu";
		case Calendar.FRIDAY: return "fri";
		case Calendar.SATURDAY: return "sat";
		}
		return null;
	}
	
	//explicit meaning that 20090144 will return a date of 44, not roll it over to the next month
	public static int getYearExplicit(String yyyyPlusWhateverElse){
		return Integer.valueOf(yyyyPlusWhateverElse.substring(0,4));
	}
	
	public static int getMonthExplicit(String yyyymmPlusWhateverElse){
		return Integer.valueOf(yyyymmPlusWhateverElse.substring(4,6));
	}
	
	public static int getDateExplicit(String yyyymmddPlusWhateverElse){
		return Integer.valueOf(yyyymmddPlusWhateverElse.substring(6,8));
	}
	
	public static boolean isSun(Calendar c){
		return Calendar.SUNDAY == c.get(Calendar.DAY_OF_WEEK);
	}
	
	public static boolean isMon(Calendar c){
		return Calendar.MONDAY == c.get(Calendar.DAY_OF_WEEK);
	}
	
	public static boolean isTue(Calendar c){
		return Calendar.TUESDAY == c.get(Calendar.DAY_OF_WEEK);
	}
	
	public static boolean isWed(Calendar c){
		return Calendar.WEDNESDAY == c.get(Calendar.DAY_OF_WEEK);
	}
	
	public static boolean isThu(Calendar c){
		return Calendar.THURSDAY == c.get(Calendar.DAY_OF_WEEK);
	}
	
	public static boolean isFri(Calendar c){
		return Calendar.FRIDAY == c.get(Calendar.DAY_OF_WEEK);
	}
	
	public static boolean isSat(Calendar c){
		return Calendar.SATURDAY == c.get(Calendar.DAY_OF_WEEK);
	}
	
	public static boolean containsSun(Collection<Calendar> c){
		for(Calendar d : CollectionTool.nullSafe(c)){
			if(isSun(d)){ return true; }
		}
		return false;
	}
	
	public static boolean containsMon(Collection<Calendar> c){
		for(Calendar d : CollectionTool.nullSafe(c)){
			if(isMon(d)){ return true; }
		}
		return false;
	}
	
	public static boolean containsTue(Collection<Calendar> c){
		for(Calendar d : CollectionTool.nullSafe(c)){
			if(isTue(d)){ return true; }
		}
		return false;
	}
	
	public static boolean containsWed(Collection<Calendar> c){
		for(Calendar d : CollectionTool.nullSafe(c)){
			if(isWed(d)){ return true; }
		}
		return false;
	}
	
	public static boolean containsThu(Collection<Calendar> c){
		for(Calendar d : CollectionTool.nullSafe(c)){
			if(isThu(d)){ return true; }
		}
		return false;
	}
	
	public static boolean containsFri(Collection<Calendar> c){
		for(Calendar d : CollectionTool.nullSafe(c)){
			if(isFri(d)){ return true; }
		}
		return false;
	}
	
	public static boolean containsSat(Collection<Calendar> c){
		for(Calendar d : CollectionTool.nullSafe(c)){
			if(isSat(d)){ return true; }
		}
		return false;
	}

	public static String getYYYYMMDD(Calendar c){
		return getYYYYMMDD(c.get(Calendar.YEAR), (c.get(Calendar.MONTH)+1), c.get(Calendar.DATE));
	}

	public static String getYYYYMMDDDashed(){
		return getYYYYMMDDDashed(createTodayUSEastern());
	}

	public static String getYYYYMMDDDashed(Calendar c){
		return getYYYYMMDDDashed(c.get(Calendar.YEAR), (c.get(Calendar.MONTH)+1), c.get(Calendar.DATE));
	}

	public static String getMMDDYYYYSlashed(Calendar c){
		return getMMDDYYYYSlashed(c.get(Calendar.YEAR), (c.get(Calendar.MONTH)+1), c.get(Calendar.DATE));
	}

	public static String getYYYYMM(Calendar c){
		return getYYYYMM(c.get(Calendar.YEAR), (c.get(Calendar.MONTH)+1));
	}

	public static String getYYYYMMDD(int year, int month, int date){
		String output = ""
			+ StringTool.pad(""+year, '0', 4)
			+ StringTool.pad(""+month, '0', 2)
			+ StringTool.pad(""+date, '0', 2);
		return output;
	}

	public static String getYYYYMMSlashed(int year, int month){
		String output = ""
			+ StringTool.pad(""+year, '0', 4) + "/"
			+ StringTool.pad(""+month, '0', 2);
		return output;
	}
	
	public static String getYYYYMMSlashed(String yyyymmdd){
		if(StringTool.length(yyyymmdd)!=8 || ! StringTool.containsOnlyNumbers(yyyymmdd)){ 
			throw new IllegalArgumentException("Illegal yyyymmdd String: "+yyyymmdd); 
		}
		return yyyymmdd.substring(0, 4) + "/" + yyyymmdd.substring(4, 6);
	}

	public static String getYYYYMMSlashed(Calendar c){
		return getYYYYMMSlashed(c.get(Calendar.YEAR), (c.get(Calendar.MONTH)+1));
	}

	public static String getYYYYMMDDDashed(int year, int month, int date){
		String output = ""
			+ StringTool.pad(""+year, '0', 4) + "-"
			+ StringTool.pad(""+month, '0', 2) + "-"
			+ StringTool.pad(""+date, '0', 2);
		return output;
	}

	public static String getMMDDYYYYSlashed(int year, int month, int date){
		String output = ""
			+ StringTool.pad(""+month, '0', 2) + "/"
			+ StringTool.pad(""+date, '0', 2) + "/"
			+ StringTool.pad(""+year, '0', 4);
		return output;
	}

	public static String getYYYYMM(int year, int month){
		String output = ""
			+ StringTool.pad(""+year, '0', 4)
			+ StringTool.pad(""+month, '0', 2);
		return output;
	}
	
	public static String getYYYYMM(int year, int month, String separator){
		String output = ""
			+ StringTool.pad(""+year, '0', 4)
			+ separator
			+ StringTool.pad(""+month, '0', 2);
		return output;
	}
	
	public static Calendar parseYYYYMMDDEastern(String in){
		return createUSEasternUSFromOneBasedYMD(
				getYear(in), getMonth(in), getDate(in));
	}
	
	public static Calendar parseYYYYMMDDHHEastern(String yyyymmddhh){
		Calendar c = createUSEasternUSFromOneBasedYMD(getYear(yyyymmddhh), 
				getMonth(yyyymmddhh), getDate(yyyymmddhh));
		c.add(Calendar.HOUR_OF_DAY, getHour(yyyymmddhh));
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
	
	protected static int getHour(String yyyymmddhh){
		return Integer.valueOf(yyyymmddhh.substring(8, 10));
	}
	
	public static String mdyToYYYYMMDD(String mdy){
		String month=null, date=null, year=null;
		
		//convert to digits and spaces
		String spaceSeparatedString = "";
		for(int i=0; i < mdy.length(); ++i){
			Character c = mdy.charAt(i);
			if(Character.isDigit(c)){
				spaceSeparatedString += c;
			}else{
				spaceSeparatedString += " ";
			}
		}
		spaceSeparatedString = spaceSeparatedString.trim();
		String[] tokens = spaceSeparatedString.split(" ");
		
		//test for 6 or 8 characters, otherwise look for tokens.  (could still be smarter, but for another day)
		if(ArrayTool.nullSafeLength(tokens)==1 && spaceSeparatedString.length()==8){
			month = spaceSeparatedString.substring(0,2);
			date = spaceSeparatedString.substring(2,4);
			year = spaceSeparatedString.substring(4,8);
		}else if(ArrayTool.nullSafeLength(tokens)==1 && spaceSeparatedString.length()==6){
			month = spaceSeparatedString.substring(0,2);
			date = spaceSeparatedString.substring(2,4);
			year = "20"+spaceSeparatedString.substring(4,6);  //TODO avoid Y2.1k bug
		}else{
			for(int i=0; i < tokens.length; ++i){
				if(StringTool.notEmpty(StringTool.nullSafeTrim(tokens[i]))){
					if(month==null){
						month = tokens[i];
					}else if(date==null){
						date = tokens[i]; 
					}else if(year==null){
						year = tokens[i];
						if(year.length()==2){
							year = "20"+year;  //TODO avoid Y2.1k bug
						}
					}
				}
			}
		}
		
		//craete the Calendar object
		Integer monthInteger = Integer.valueOf(month);
		Integer dateInteger = Integer.valueOf(date);
		Integer yearInteger = Integer.valueOf(year);
		return getYYYYMMDD(yearInteger, monthInteger, dateInteger);
	}
	
	public static Calendar parseMDYEastern(String in){
		String yyyymmdd = mdyToYYYYMMDD(in);
		return parseYYYYMMDDEastern(yyyymmdd);
	}
	
//	public static Calendar createTodayGMTUS(){
//		Calendar c = Calendar.getInstance(TimeZone.getTimeZone(TIME_ZONE_GMT), Locale.US);
//		c.set(Calendar.HOUR_OF_DAY, 0);
//		c.set(Calendar.MINUTE, 0);
//		c.set(Calendar.SECOND, 0);
//		c.set(Calendar.MILLISECOND, 0);
//		return c;
//	}
//	
//	public static Calendar createTodayOffsetGMTUS(int daysToOffset){
//		Calendar c = createTodayGMTUS();
//		c.add(Calendar.DATE, daysToOffset);
//		return c;
//	}
//	
//	public static Calendar createGMTUSFromOneBasedYMD(int year, int monthOneBased, int date){
//		Calendar c = Calendar.getInstance(TimeZone.getTimeZone(TIME_ZONE_GMT), Locale.US);
//		c.set(year, monthOneBased-1, date, 0, 0, 0);
//		c.set(Calendar.MILLISECOND, 0);
//		return c;
//	}
	
	public static Calendar createUSEastern(long ms){
		Calendar c = Calendar.getInstance(TIME_ZONE_US_EASTERN, Locale.US);
		c.setTimeInMillis(ms);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c;
	}
	
	public static Calendar createTomorrowUSEastern(long ms){
		Calendar c = createUSEastern(ms);
		c.add(Calendar.DATE, 1);
		return c;
	}
	
	public static Calendar createTodayUSEastern(){
		Calendar c = Calendar.getInstance(TIME_ZONE_US_EASTERN, Locale.US);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c;
	}
	
	public static Calendar createTodayOffsetUSEastern(int daysToOffset){
		Calendar c = createTodayUSEastern();
		c.add(Calendar.DATE, daysToOffset);
		return c;
	}
	
	public static Calendar createUSEasternUSFromOneBasedYMD(int year, int monthOneBased, int date){
		Calendar c = Calendar.getInstance(TIME_ZONE_US_EASTERN, Locale.US);
		c.set(year, monthOneBased-1, date, 0, 0, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c;
	}
	
	public static Calendar getFirstDayOfMonth(Calendar in){
		Calendar firstDay = clone(in);
		firstDay.set(Calendar.DATE, 1);
		firstDay.set(Calendar.HOUR_OF_DAY, 0);
		firstDay.set(Calendar.MINUTE, 0);
		firstDay.set(Calendar.SECOND, 0);
		firstDay.set(Calendar.MILLISECOND, 0);
		return firstDay;
	}
	
	public static Calendar copyWithOnlyYMD(Calendar c){
		Calendar copy = (Calendar)c.clone();
		copy.set(Calendar.HOUR_OF_DAY, 0);
		copy.set(Calendar.MINUTE, 0);
		copy.set(Calendar.SECOND, 0);
		copy.set(Calendar.MILLISECOND, 0);
		return copy;
	}
	
	public static int numDays(Calendar start, boolean includeStart, Calendar end, boolean includeEnd){
		int direction = 1;
		if(start.compareTo(end) > 0){ 
			direction = -1;
		}
		int numDays = 0;
		Calendar startGMTUS = copyWithOnlyYMD(start);
		Calendar iterator = copyWithOnlyYMD(startGMTUS);
		if(includeStart){
			numDays += direction;
		}
		iterator.add(Calendar.DATE, direction);
		if(includeEnd){
			while(iterator.compareTo(end) * direction <= 0){
				numDays += direction;
				iterator.add(Calendar.DATE, direction);
			}
		}else{
			while(iterator.compareTo(end) * direction < 0){
				numDays += direction;
				iterator.add(Calendar.DATE, direction);
			}
		}
		return numDays;
	}
	
	public static int numDays(
			String startYYYYMMDD, boolean includeStart, String endYYYYMMDD, boolean includeEnd){
		Calendar start = parseYYYYMMDDEastern(startYYYYMMDD);
		Calendar end = parseYYYYMMDDEastern(endYYYYMMDD);
		return numDays(start, includeStart, end, includeEnd);
	}
	
	public static boolean dateExists(Integer year, Integer monthOneBased, Integer date){
		if(year==null || monthOneBased==null || date==null){ return false; }
		Calendar c = DailyCalendarTool.createUSEasternUSFromOneBasedYMD(year, monthOneBased, date);
		if(year != DailyCalendarTool.getYear(c)){ return false; }
		if(monthOneBased != DailyCalendarTool.getMonthOneBased(c)){ return false; }
		if(date != DailyCalendarTool.getDate(c)){ return false; }
		return true;
	}
	
	public static boolean dateExistsYYYYMMDD(String yyyymmdd){
		int year = getYearExplicit(yyyymmdd);
		int month = getMonthExplicit(yyyymmdd);
		int date = getDateExplicit(yyyymmdd);
		return dateExists(year, month, date);
	}
	
	public static boolean dateExistsMDY(String mdy){
		String yyyymmdd = mdyToYYYYMMDD(mdy);
		return dateExistsYYYYMMDD(yyyymmdd);
	}
	
	public static Calendar getCopyShiftDays(Calendar in, Integer numDays){
		if(in==null){ return null; }
		Calendar out = (Calendar)in.clone();
		if(numDays==null || numDays.equals(0)){ return out; }
		out.add(Calendar.DATE, numDays);
		return out;
	}
	
	public static Calendar getCopyShiftMonths(Calendar in, Integer numMonths){
		if(in==null){ return null; }
		Calendar out = (Calendar)in.clone();
		if(numMonths==null || numMonths.equals(0)){ return out; }
		out.add(Calendar.MONTH, numMonths);
		return out;
	}
	
	public static Calendar getCopyShiftYears(Calendar in, Integer numYears){
		if(in==null){ return null; }
		Calendar out = (Calendar)in.clone();
		if(numYears==null || numYears.equals(0)){ return out; }
		out.add(Calendar.YEAR, numYears);
		return out;
	}
	
	public static List<Calendar> getRangeOfCalendars(Calendar start, boolean includeStart, boolean forwards, 
			int numDays){
		List<Calendar> dates = GenericsFactory.makeLinkedList();
		Calendar startMidnight = copyWithOnlyYMD(start);
		Calendar iterator = copyWithOnlyYMD(startMidnight);
		int iterateAmount = forwards?1:-1;
		if( ! includeStart){
			iterator.add(Calendar.DATE, iterateAmount);
		}
		for(int i=0; i < numDays; ++i){
			Calendar dayBetween = copyWithOnlyYMD(iterator);
			dates.add(dayBetween);
			iterator.add(Calendar.DATE, iterateAmount);
		}
		iterator=null;
		return dates;
	}

	public static List<Calendar> getRangeOfCalendarsEastern(String startYYYYMMDD, boolean includeStart, 
			boolean forwards, int numDays){
		Calendar start = parseYYYYMMDDEastern(startYYYYMMDD);
		return getRangeOfCalendars(start, includeStart, forwards, numDays);
	}
	
	public static List<Calendar> getRangeOfCalendars(Calendar start, boolean includeStart, Calendar end, 
			boolean includeEnd){
		List<Calendar> dates = GenericsFactory.makeArrayList();
		Calendar startCopy = copyWithOnlyYMD(start);
		Calendar endCopy = copyWithOnlyYMD(end);
		if(includeStart){
			dates.add(startCopy);
		}
		Calendar iterator = copyWithOnlyYMD(startCopy);
		iterator.add(Calendar.DATE, 1);
//		System.out.println(CalendarTool.getXSDDateTime(startGMTUS)+","+CalendarTool.getXSDDateTime(endGMTUS)+","
//				+CalendarTool.getXSDDateTime(iterator));
		while(iterator.compareTo(endCopy) < 0){
			Calendar dayBetween = copyWithOnlyYMD(iterator);
			dates.add(dayBetween);
			iterator.add(Calendar.DATE, 1);
		}
		if(includeEnd){
			dates.add(endCopy);
		}
		return dates;
	}
	
	public static List<Calendar> getRangeOfCalendarsEastern(String startYYYYMMDD, boolean includeStart, 
			String endYYYYMMDD, boolean includeEnd){
		Calendar start = parseYYYYMMDDEastern(startYYYYMMDD);
		Calendar end = parseYYYYMMDDEastern(endYYYYMMDD);
		return getRangeOfCalendars(start, includeStart, end, includeEnd);
	}
	
	public static List<String> getRangeOfYYYYMMDD(Calendar start, boolean includeStart, Calendar end, 
			boolean includeEnd){
		List<Calendar> range = getRangeOfCalendars(start, includeStart, end, includeEnd);
		List<String> result = new LinkedList<String>();
		for(Calendar c : range){
			result.add(getYYYYMMDD(c));
		}
		return result;
	}
	
	public static List<String> getRangeOfYYYYMMDD(String startYYYYMMDD, boolean includeStart, String endYYYYMMDD, 
			boolean includeEnd){
		Calendar start = parseYYYYMMDDEastern(startYYYYMMDD);
		Calendar end = parseYYYYMMDDEastern(endYYYYMMDD);
		return getRangeOfYYYYMMDD(start, includeStart, end, includeEnd);
	}
	
	public static List<String> getRangeOfYYYYMMDD(Calendar start, boolean includeStart, boolean forwards, int numDays){
		List<Calendar> range = getRangeOfCalendars(start, includeStart, forwards, numDays);
		List<String> result = new LinkedList<String>();
		for(Calendar c : range){
			result.add(getYYYYMMDD(c));
		}
		return result;
	}
	
	public static SortedSet<String> getYYYYMMDD(SortedSet<Calendar> set){
		SortedSet<String> strings = GenericsFactory.makeTreeSet();
		for(Calendar c : CollectionTool.nullSafe(set)){
			strings.add(getYYYYMMDD(c));
		}
		return strings;
	}
	
	public static List<String> getYYYYMMDD(List<Calendar> set){
		List<String> strings = GenericsFactory.makeLinkedList();
		for(Calendar c : CollectionTool.nullSafe(set)){
			strings.add(getYYYYMMDD(c));
		}
		return strings;
	}
	
	public static List<String> getRangeOfYYYYMMDD(
			String startYYYYMMDD, boolean includeStart, boolean forwards, int numDays){
		Calendar start = parseYYYYMMDDEastern(startYYYYMMDD);
		return getRangeOfYYYYMMDD(start, includeStart, forwards, numDays);
	}
	
	public static List<Calendar> getAllCalendarsSortedInMonth(Calendar c){
		Calendar firstDayOfThisMonth = getFirstDayOfMonth(c);
		Calendar firstDayOfNextMonth = getCopyShiftMonths(firstDayOfThisMonth, 1);
		return getRangeOfCalendars(firstDayOfThisMonth, true, firstDayOfNextMonth, false);
	}
	
	public static List<Calendar> getFirstDaySortedOfEachMonthSpannedByEastern(Collection<Calendar> days){
		Calendar earliestDate = ComparableTool.getFirst(days);
		Calendar latestDate = ComparableTool.getLast(days);
		return getFirstDaySortedOfEachMonthInRangeEastern(earliestDate, latestDate);
	}

	public static List<Calendar> getFirstDaySortedOfEachMonthInRangeEastern(Calendar start, Calendar end){
		SortedSet<Calendar> firstDaysOfMonths = GenericsFactory.makeTreeSet();
		if(start == null && end==null){ 
			return null; 
		}else if(start==null){ 
			firstDaysOfMonths.add(getFirstDayOfMonth((Calendar)end)); 
		}else if(end==null){
			firstDaysOfMonths.add(getFirstDayOfMonth((Calendar)start));
		}else{
			Calendar cursor = getFirstDayOfMonth((Calendar)start);
			while(!cursor.after(end)){
				Calendar clone = clone(cursor);
				firstDaysOfMonths.add(clone(clone));
				cursor.add(Calendar.MONTH, 1);
			}
		}
		return GenericsFactory.makeArrayList(firstDaysOfMonths);
	}
	
	public static List<Calendar> getAllDaysSortedInEachMonthSpannedByEastern(Collection<Calendar> days){
		List<Calendar> firstDayOfEachMonth = getFirstDaySortedOfEachMonthSpannedByEastern(days);
		List<Calendar> allDays = GenericsFactory.makeLinkedList();
		for(Calendar firstOfMonth : CollectionTool.nullSafe(firstDayOfEachMonth)){
			allDays.addAll(getAllCalendarsSortedInMonth(firstOfMonth));
		}
		return allDays;
	}
	
	
	
	/************************ tests ****************************************************/
	
	public static class Tests {
		Calendar feb282008 = createUSEasternUSFromOneBasedYMD(2008,2,28);
		Calendar mar032008 = createUSEasternUSFromOneBasedYMD(2008,3,3);
		
		Calendar feb282009 = createUSEasternUSFromOneBasedYMD(2009,2,28);
		Calendar mar012009 = createUSEasternUSFromOneBasedYMD(2009,3,1);
		Calendar mar032009 = createUSEasternUSFromOneBasedYMD(2009,3,3);
		@Test public void testRollover(){
			Calendar fakeDate = createUSEasternUSFromOneBasedYMD(2009,7,33);
			Assert.assertEquals(8, getMonthOneBased(fakeDate));
			Assert.assertEquals(2, getDate(fakeDate));
		}
		@Test public void testDateExists(){
			Assert.assertTrue(dateExists(2009,1,1));
			Assert.assertFalse(dateExists(2009,1,35));
			Assert.assertTrue(dateExists(2008,2,29));//every 4 years
			Assert.assertFalse(dateExists(2009,2,29));//not every 4 years
			Assert.assertFalse(dateExists(2100,2,29));//not every 100, unless 400
			Assert.assertTrue(dateExists(2000,2,29));//every 400
		}
		@Test public void testCreate(){
			Assert.assertEquals("20090228", getYYYYMMDD(feb282009));
			Calendar feb28Copy = copyWithOnlyYMD(feb282009);
			Assert.assertEquals(feb282009, feb28Copy);
			Assert.assertTrue(feb282009.compareTo(feb28Copy) == 0);
		}
		@Test public void testParse(){
			Assert.assertEquals("20090228", getYYYYMMDD(parseMDYEastern("02282009")));
			Assert.assertEquals("20090228", getYYYYMMDD(parseMDYEastern("022809")));
			Assert.assertEquals("20090228", getYYYYMMDD(parseMDYEastern("02-28-2009")));
			Assert.assertEquals("20090228", getYYYYMMDD(parseMDYEastern("asdf  2- 28-wer2009  -")));
			Assert.assertEquals("20090228", getYYYYMMDD(parseMDYEastern("asdf  2- 28-wer09  -")));
		}
		@Test public void testRange1(){
			List<String> result = getRangeOfYYYYMMDD(feb282009, true, mar032009, true);
			List<String> expected = GenericsFactory.makeLinkedList("20090228", "20090301", "20090302", "20090303");
			Assert.assertEquals(expected, result);
		}
		@Test public void testRange2(){
			List<String> result = getRangeOfYYYYMMDD(feb282009, false, mar032009, false);
			List<String> expected = GenericsFactory.makeLinkedList("20090301", "20090302");
			Assert.assertEquals(expected, result);
		}
		@Test public void testRange3(){
			List<String> result = getRangeOfYYYYMMDD(feb282009, true, mar032009, false);
			List<String> expected = GenericsFactory.makeLinkedList("20090228", "20090301", "20090302");
			Assert.assertEquals(expected, result);
		}
		@Test public void testRange4(){
			List<String> result = getRangeOfYYYYMMDD(feb282009, false, mar032009, true);
			List<String> expected = GenericsFactory.makeLinkedList("20090301", "20090302", "20090303");
			Assert.assertEquals(expected, result);
		}
		@Test public void testLeapYearRange1(){
			List<String> result = getRangeOfYYYYMMDD(feb282008, true, mar032008, true);
			List<String> expected = GenericsFactory.makeLinkedList("20080228", "20080229", "20080301", "20080302", "20080303");
			Assert.assertEquals(expected, result);
		}
		@Test public void testRangeOfLength(){
			List<String> result = getRangeOfYYYYMMDD(feb282009, true, true, 4);
			List<String> expected = GenericsFactory.makeLinkedList("20090228", "20090301", "20090302", "20090303");
			Assert.assertEquals(expected, result);
			List<String> resultBackwards = getRangeOfYYYYMMDD(feb282009, true, false, 4);
			List<String> expectedBackwards = GenericsFactory.makeLinkedList("20090228", "20090227", "20090226", "20090225");
			Assert.assertEquals(expectedBackwards, resultBackwards);
		}
		@Test public void testDayOfWeek(){
			Calendar thursday = DailyCalendarTool.parseYYYYMMDDEastern("20090108");
			Assert.assertTrue(isThu(thursday));
			List<Calendar> week = DailyCalendarTool.getRangeOfCalendars(thursday, false, true, 7);
			Assert.assertTrue(isFri(week.get(0)));
			Assert.assertTrue(isSat(week.get(1)));
			Assert.assertTrue(isSun(week.get(2)));
			Assert.assertTrue(isMon(week.get(3)));
			Assert.assertTrue(isTue(week.get(4)));
			Assert.assertTrue(isWed(week.get(5)));
			Assert.assertTrue(isThu(week.get(6)));
		}
		@Test public void testNumDays(){
			Assert.assertEquals(1, numDays("20090111", true, "20090111", true));
			Assert.assertEquals(1, numDays("20090111", true, "20090111", false));
			Assert.assertEquals(0, numDays("20090111", false, "20090111", false));
			Assert.assertEquals(0, numDays("20090111", false, "20090112", false));
			Assert.assertEquals(1, numDays("20090111", false, "20090113", false));
			Assert.assertEquals(2, numDays("20090111", true, "20090113", false));
			Assert.assertEquals(3, numDays("20090111", true, "20090113", true));
		}
		@Test public void testNumDaysBackwards(){
			Assert.assertEquals(1, numDays("20090111", true, "20090111", true));
			Assert.assertEquals(1, numDays("20090111", true, "20090111", false));
			Assert.assertEquals(0, numDays("20090111", false, "20090111", false));
			Assert.assertEquals(0, numDays("20090112", false, "20090111", false));
			Assert.assertEquals(-1, numDays("20090113", false, "20090111", false));
			Assert.assertEquals(-2, numDays("20090113", true, "20090111", false));
			Assert.assertEquals(-3, numDays("20090113", true, "20090111", true));
		}
		@Test public void testGetFirstDayOfEachMonthSpannedBy(){
			List<Calendar> list = GenericsFactory.makeLinkedList();
			list.add(parseYYYYMMDDEastern("20091015"));
			list.add(parseYYYYMMDDEastern("20100101"));
			List<Calendar> span= getFirstDaySortedOfEachMonthSpannedByEastern(list);
			List<String> spanStrings = getYYYYMMDD(span);
			Assert.assertArrayEquals(
					new String[]{"20091001","20091101","20091201","20100101"}, 
					spanStrings.toArray());
		}
		@Test public void testGetFirstDaySortedOfEachMonthInRange(){
			Calendar feb1 = parseYYYYMMDDEastern("20100201");
			Calendar start = DailyCalendarTool.getCopyShiftMonths(feb1, -12);
			Assert.assertEquals(13,getFirstDaySortedOfEachMonthInRangeEastern(start, feb1).size());
		}
		@Test public void testGetYYYYMM(){
			Assert.assertEquals("2009_03", getYYYYMM(2009, 3, "_"));
			Assert.assertEquals("2001/03", getYYYYMMSlashed("20010303"));
		}
		@Test public void testClone(){
			Calendar c = Calendar.getInstance();
			Calendar clone = DailyCalendarTool.clone(c);
			Assert.assertEquals(c,clone);
		}
	}
	
	public static void main(String[] args){
//		for(int i=1; i <= 11; ++i){
//			long start = 1000000 * DailyCalendarTool.createUSEasternUSFromOneBasedYMD(2009, 1, i).getTimeInMillis();
//			long end = 1000000 * DailyCalendarTool.createUSEasternUSFromOneBasedYMD(2009, 1, i+1).getTimeInMillis();
//			System.out.println("#"+i+"\nupdate Event200901 set aggregatedAreaInfo=0 where id >= "+start+" and id < "+end+";\ncommit;");
//		}
		System.out.println(DailyCalendarTool.createTodayUSEastern().getTime());
	}
}

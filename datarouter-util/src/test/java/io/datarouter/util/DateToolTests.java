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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.util.duration.DurationUnit;

public class DateToolTests{

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

		Assert.assertEquals(df.format(DateTool.parseUserInputDate("July 15th",
				2000)), "07-15-" + DateTool.getYearInteger());
	}

	private void testParseUserInputDate(String expected, String original){
		Assert.assertEquals(df.format(DateTool.parseUserInputDate(original, null)), expected);
	}

	@Test
	public void testAgoString(){
		Assert.assertEquals(DateTool.getMillisAsString(950, 1, DurationUnit.MILLISECONDS), "950 milliseconds");
		Assert.assertEquals(DateTool.getMillisAsString(950, 5, DurationUnit.MILLISECONDS), "950 milliseconds");
		Assert.assertEquals(DateTool.getMillisAsString(52950, 1, DurationUnit.MILLISECONDS), "52 seconds");
		Assert.assertEquals(DateTool.getMillisAsString(360051, 2, DurationUnit.MILLISECONDS), "6 minutes");
		Assert.assertEquals(DateTool.getMillisAsString(225950, 2, DurationUnit.MILLISECONDS), "3 minutes, 45 seconds");
		Assert.assertEquals(DateTool.getMillisAsString(10225950, 2, DurationUnit.MILLISECONDS), "2 hours, 50 minutes");
		Assert.assertEquals(DateTool.getMillisAsString(240225950, 4, DurationUnit.MILLISECONDS),
				"2 days, 18 hours, 43 minutes, 45 seconds");
	}

	@Test
	public void testToReverseDateLong(){
		Date now = new Date(), zero = new Date(0L), max = new Date(Long.MAX_VALUE);
		Assert.assertEquals(DateTool.toReverseDateLong(now), (Long)(Long.MAX_VALUE - now.getTime()));
		Assert.assertEquals(DateTool.toReverseDateLong(zero), (Long)Long.MAX_VALUE);
		Assert.assertEquals(DateTool.toReverseDateLong(max), (Long)0L);
		Assert.assertNull(DateTool.toReverseDateLong(null));
	}

	@Test
	public void testFromReverseDateLong(){
		Date now = new Date(), zero = new Date(0L), max = new Date(Long.MAX_VALUE);
		Assert.assertEquals(DateTool.fromReverseDateLong(Long.MAX_VALUE - now.getTime()), now);
		Assert.assertEquals(DateTool.fromReverseDateLong(Long.MAX_VALUE - zero.getTime()), zero);
		Assert.assertEquals(DateTool.fromReverseDateLong(Long.MAX_VALUE - max.getTime()), max);
		Assert.assertNull(DateTool.fromReverseDateLong(null));
	}

	@Test
	public void testReverseDateLong(){
		Date now = new Date();
		Long nowTime = now.getTime();
		Assert.assertEquals(DateTool.fromReverseDateLong(DateTool.toReverseDateLong(now)), now);
		Assert.assertEquals(DateTool.toReverseDateLong(DateTool.fromReverseDateLong(nowTime)), nowTime);
		Assert.assertNull(DateTool.fromReverseDateLong(DateTool.toReverseDateLong(null)));
		Assert.assertNull(DateTool.toReverseDateLong(DateTool.fromReverseDateLong(null)));
	}

	@Test
	public void testGetDaysAgo(){
		Date date1 = new Date(1527182116155L);
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(1526836516155L);
		Date date2 = calendar.getTime();
		Assert.assertEquals(DateTool.getDaysAgo(4, date1), date2);
	}

	@Test
	public void testGetDaysBetween(){
		Date d1 = new Date(1352059736026L);
		int daysApart = 4;
		Date d2 = new Date(d1.getTime() + DateTool.MILLISECONDS_IN_DAY * daysApart);
		Assert.assertEquals(DateTool.getDaysBetween(d1, d2), daysApart, 1 >> 20);
		d2 = new Date(d1.getTime() + DateTool.MILLISECONDS_IN_DAY * daysApart - 4);
		Assert.assertTrue(daysApart > DateTool.getDaysBetween(d1, d2));
		Assert.assertTrue(daysApart - 1 < DateTool.getDaysBetween(d1, d2));
	}

	@Test
	public void testGetMinutesBetween(){
		Date d1 = new Date(1352059736026L);
		int minutesApart = 5;
		Date d2 = new Date(d1.getTime() + DateTool.MILLISECONDS_IN_MINUTE * minutesApart);
		Assert.assertEquals(DateTool.getMinutesBetween(d1, d2), minutesApart, 1 >> 20);
		d2 = new Date(d1.getTime() + DateTool.MILLISECONDS_IN_MINUTE * minutesApart - 10);
		Assert.assertTrue(minutesApart > DateTool.getMinutesBetween(d1, d2));
		Assert.assertTrue(minutesApart - 1 < DateTool.getMinutesBetween(d1, d2));
	}

}

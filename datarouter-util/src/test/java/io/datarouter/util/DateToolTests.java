/*
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

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.util.duration.DurationUnit;

@SuppressWarnings("deprecation")
public class DateToolTests{

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
	public void testFromReverseInstantLong(){
		Instant instant = Instant.now();
		long reverse = Long.MAX_VALUE - instant.toEpochMilli();
		Assert.assertEquals(instant.truncatedTo(ChronoUnit.MILLIS), DateTool.fromReverseInstantLong(reverse));
		Assert.assertNull(DateTool.fromReverseInstantLong(null));
	}

	@Test
	public void testGetDaysAgo(){
		Date date1 = new Date(1527182116155L);
		Date date2 = Date.from(Instant.ofEpochMilli(1526836516155L));
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

	@Test
	public void testIsWeekend(){
		ZonedDateTime zonedDateTime = ZonedDateTime.of(2020, 5, 16, 0, 0, 0, 0, ZoneId.systemDefault());
		Date date = Date.from(zonedDateTime.toInstant());
		Assert.assertTrue(DateTool.isWeekend(date));
	}

}

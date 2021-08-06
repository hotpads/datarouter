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
package io.datarouter.model.field.imp.custom;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.util.time.ZoneIds;

public class LocalDateFieldTester{

	@Test
	public void testParseStringEncodedValueButDoNotSet(){
		String dateStr = "2016-06-22 19:20:14.100";
		String dateStr2 = "2015-06-22 00:00:00.200";
		LocalDateTimeField field = new LocalDateTimeField(new LocalDateTimeFieldKey("test"), null);
		ZonedDateTime date = field.parseStringEncodedValueButDoNotSet(dateStr).atZone(ZoneId.systemDefault());
		Assert.assertEquals(date.toInstant().toEpochMilli(), 1466648414100L);
		ZonedDateTime date2 = field.parseStringEncodedValueButDoNotSet(dateStr2).atZone(ZoneId.systemDefault());
		Assert.assertEquals(date2.toInstant().toEpochMilli(), 1434956400200L);
		Assert.assertNotNull(date);
	}

	@Test
	public void testParseStringEncodedValueButDoNotSetWithTimeZone(){
		String dateStrEst = "2016-06-22 19:20:14.100"; // EST
		String dateStrPst = "2016-06-22 16:20:14.100"; // PST
		LocalDateTimeField field = new LocalDateTimeField(new LocalDateTimeFieldKey("test"), null);
		ZonedDateTime zonedDateTimeEst = field.parseStringEncodedValueButDoNotSet(dateStrEst)
				.atZone(ZoneIds.AMERICA_NEW_YORK);
		ZonedDateTime zonedDateTimePst = field.parseStringEncodedValueButDoNotSet(dateStrPst)
				.atZone(ZoneIds.AMERICA_LOS_ANGELES);
		Assert.assertEquals(zonedDateTimeEst.toInstant(), zonedDateTimePst.toInstant());
		Assert.assertEquals(zonedDateTimePst.toInstant().toEpochMilli(), 1466637614100L);
		Assert.assertEquals(zonedDateTimeEst.toInstant().toEpochMilli(), 1466637614100L);
	}

	@Test
	public void testfromBytesButDoNotSet(){
		String dateStr1 = "2016-06-22 19:20:14.100123456";
		String dateStr2 = "2014-06-24 03:20:14.210998531";
		DateTimeFormatter formatterWithNano = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSSSS");
		LocalDateTime localDateTime1 = LocalDateTime.parse(dateStr1, formatterWithNano);
		LocalDateTime localDateTime2 = LocalDateTime.parse(dateStr2, formatterWithNano);
		LocalDateTimeField field1 = new LocalDateTimeField(new LocalDateTimeFieldKey("test"), localDateTime1);
		LocalDateTimeField field2 = new LocalDateTimeField(new LocalDateTimeFieldKey("test"), localDateTime2);
		LocalDateTime localDateTimeFromBytes1 = field1.fromBytesButDoNotSet(field1.getBytes(), 0);
		LocalDateTime localDateTimeFromBytes2 = field2.fromBytesButDoNotSet(field2.getBytes(), 0);
		Assert.assertEquals(field1.getTruncatedLocalDateTime(localDateTime1), localDateTimeFromBytes1);
		Assert.assertEquals(field2.getTruncatedLocalDateTime(localDateTime2), localDateTimeFromBytes2);
	}

	@Test
	public void testGetTruncatedLocalDateTime(){
		String dateStr = "2014-06-24 03:20:14.210998531";
		DateTimeFormatter formatterWithNano = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSSSS");
		LocalDateTime localDateTime = LocalDateTime.parse(dateStr, formatterWithNano);
		LocalDateTime localDateTimeTest = LocalDateTime.parse(dateStr, formatterWithNano);
		LocalDateTimeField field1 = new LocalDateTimeField(new LocalDateTimeFieldKey("test")
				.overrideNumFractionalSeconds(3), localDateTime);
		LocalDateTimeField field2 = new LocalDateTimeField(new LocalDateTimeFieldKey("test")
				.overrideNumFractionalSeconds(7), localDateTime);
		LocalDateTimeField field3 = new LocalDateTimeField(new LocalDateTimeFieldKey("test")
				.overrideNumFractionalSeconds(0), localDateTime);
		LocalDateTimeField field4 = new LocalDateTimeField(new LocalDateTimeFieldKey("test")
				.overrideNumFractionalSeconds(9), localDateTime);
		Assert.assertEquals(field1.getTruncatedLocalDateTime(localDateTime), localDateTimeTest.withNano(210000000));
		Assert.assertEquals(field2.getTruncatedLocalDateTime(localDateTime), localDateTimeTest.withNano(210998500));
		Assert.assertEquals(field3.getTruncatedLocalDateTime(localDateTime), localDateTimeTest.withNano(0));
		Assert.assertEquals(field4.getTruncatedLocalDateTime(localDateTime), localDateTimeTest.withNano(210998531));
	}
}
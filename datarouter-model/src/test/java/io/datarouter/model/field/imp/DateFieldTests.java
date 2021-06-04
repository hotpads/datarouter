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
package io.datarouter.model.field.imp;

import java.util.Date;

import org.testng.Assert;
import org.testng.annotations.Test;

public class DateFieldTests{

	@Test
	public void testParseStringEncodedValueButDoNotSet(){
		String dateStr = "2016-06-22T19:20:14Z";
		@SuppressWarnings("deprecation")
		DateField field = new DateField(new DateFieldKey("test"), null);
		Date date = field.parseStringEncodedValueButDoNotSet(dateStr);
		Assert.assertEquals(date.getTime(), 1466623214000L);
		Assert.assertNotNull(date);
	}

	@Test
	public void testRoundTrip(){
		Date date = new Date(1573098803000L);
		@SuppressWarnings("deprecation")
		DateField dateField = new DateField(new DateFieldKey("key"), date);
		String string = dateField.getStringEncodedValue();
		Date newDate = dateField.parseStringEncodedValueButDoNotSet(string);
		Assert.assertEquals(date, newDate);
	}

	@Test
	public void testNumDecimalSecondsParse(){
		@SuppressWarnings("deprecation")
		DateField dateFieldMs = new DateField(new DateFieldKey("key").withPrecision(3), null);
		@SuppressWarnings("deprecation")
		DateField dateFieldNoMs = new DateField(new DateFieldKey("key").withPrecision(0), null);

		Date noMsDate = new Date(1573098803000L);
		String noMsString = "2019-11-07T03:53:23Z";

		Date msDate = new Date(1573098803456L);
		String msString = "2019-11-07T03:53:23.456Z";

		Date parsedDate;

		parsedDate = dateFieldMs.parseStringEncodedValueButDoNotSet(noMsString);
		Assert.assertEquals(parsedDate, noMsDate);

		parsedDate = dateFieldMs.parseStringEncodedValueButDoNotSet(msString);
		Assert.assertEquals(parsedDate, msDate);

		parsedDate = dateFieldNoMs.parseStringEncodedValueButDoNotSet(noMsString);
		Assert.assertEquals(parsedDate, noMsDate);

		parsedDate = dateFieldNoMs.parseStringEncodedValueButDoNotSet(msString);
		Assert.assertEquals(parsedDate, noMsDate);
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testNumDecimalSecondsSerialize(){
		DateFieldKey dateFieldMs = new DateFieldKey("key").withPrecision(3);
		DateFieldKey dateFieldNoMs = new DateFieldKey("key").withPrecision(0);

		Date noMsDate = new Date(1573098803000L);
		String noMsString = "2019-11-07T03:53:23Z";

		Date msDate = new Date(1573098803456L);
		String msString = "2019-11-07T03:53:23.456Z";
		String msZeroString = "2019-11-07T03:53:23.000Z";

		String serialized;

		serialized = new DateField(dateFieldMs, noMsDate).getStringEncodedValue();
		Assert.assertEquals(serialized, msZeroString);

		serialized = new DateField(dateFieldMs, msDate).getStringEncodedValue();
		Assert.assertEquals(serialized, msString);

		serialized = new DateField(dateFieldNoMs, noMsDate).getStringEncodedValue();
		Assert.assertEquals(serialized, noMsString);

		serialized = new DateField(dateFieldNoMs, msDate).getStringEncodedValue();
		Assert.assertEquals(serialized, noMsString);
	}

}

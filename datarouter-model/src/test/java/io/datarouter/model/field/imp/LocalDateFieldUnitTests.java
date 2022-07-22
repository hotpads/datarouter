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
package io.datarouter.model.field.imp;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.testng.Assert;
import org.testng.annotations.Test;

public class LocalDateFieldUnitTests{

	@Test
	public void testStringEncoding(){
		String dateStr = "9999-12-31";
		LocalDate localDate = LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
		LocalDateField field = new LocalDateField(new LocalDateFieldKey("test"), localDate);

		Assert.assertEquals(field.getStringEncodedValue(), dateStr);
		Assert.assertEquals(field.parseStringEncodedValueButDoNotSet(dateStr), localDate);
		Assert.assertEquals(localDate, field.parseStringEncodedValueButDoNotSet(field.getStringEncodedValue()));
	}

	@Test
	public void testByteEncoding(){
		short year = 9999;
		byte month = 12;
		byte day = 31;
		LocalDate localDate = LocalDate.of(year, month, day);
		LocalDateField field = new LocalDateField(new LocalDateFieldKey("test"), localDate);

		Assert.assertEquals(field.fromValueBytesButDoNotSet(field.getValueBytes(), 0), localDate);
	}

}
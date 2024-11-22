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
package io.datarouter.util.time;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.TimeZone;

import org.testng.Assert;
import org.testng.annotations.Test;

public class SimpleDateFormatToDateTimeFormatterConverterTests{

	private static final ZoneId ZONE_ID = ZoneIds.UTC;
	private static final ZoneOffset ZONE_OFFSET = ZoneOffset.UTC;


	private static final Instant INSTANT = LocalDateTime.of(2024, 7, 22, 10, 30, 0, 0)
			.toInstant(ZONE_OFFSET);
	private static final Date DATE = Date.from(INSTANT);


	@Test
	public void test1(){
		var simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		simpleDateFormat.setTimeZone(TimeZone.getTimeZone(ZONE_ID));
		var dateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss")
				.withZone(ZONE_ID);
		Assert.assertEquals(simpleDateFormat.format(Date.from(INSTANT)), dateTimeFormatter.format(INSTANT));
	}

}

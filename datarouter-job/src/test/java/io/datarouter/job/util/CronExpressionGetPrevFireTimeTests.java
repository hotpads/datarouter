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
package io.datarouter.job.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Date;

import org.apache.logging.log4j.core.util.CronExpression;
import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.util.time.ZoneIds;

public class CronExpressionGetPrevFireTimeTests{

	private static final ZoneId TZ = ZoneIds.AMERICA_LOS_ANGELES;

	/*
	 * 4 times per day at the following times:
	 * 01:55:13
	 * 08:55:13
	 * 17:55:13
	 * 21:55:13
	 */
	private static final CronExpression CRON_EXPRESSION = CronExpressionTool.parse("13 55 1,8,17,21 * * ?", TZ);

	private static final DateTimeFormatter DTF = new DateTimeFormatterBuilder()
			.parseCaseInsensitive()
			.append(DateTimeFormatter.ISO_LOCAL_DATE)
			.appendLiteral(' ')
			.append(DateTimeFormatter.ISO_LOCAL_TIME)
			.toFormatter()
			.withZone(TZ);

	@Test
	public void working(){
		Date current = parse("2024-10-20 21:55:00");
		assertEquals(CRON_EXPRESSION.getNextValidTimeAfter(current), "2024-10-20 21:55:13");
		assertEquals(CRON_EXPRESSION.getPrevFireTime(current), "2024-10-20 17:55:13");
	}

	@Test(enabled = false) // IN-12603
	public void firstNotWorking(){
		Date current = parse("2024-10-20 21:56:00");
		assertEquals(CRON_EXPRESSION.getNextValidTimeAfter(current), "2024-10-21 01:55:13");
		assertEquals(CRON_EXPRESSION.getPrevFireTime(current), "2024-10-20 21:55:13");
	}

	@Test(enabled = false) // IN-12603
	public void lastNotWorking(){
		Date current = parse("2024-10-21 00:55:00");
		assertEquals(CRON_EXPRESSION.getNextValidTimeAfter(current), "2024-10-21 01:55:13");
		assertEquals(CRON_EXPRESSION.getPrevFireTime(current), "2024-10-20 21:55:13");
	}

	@Test
	public void firstReWorking(){
		Date current = parse("2024-10-21 00:56:00");
		assertEquals(CRON_EXPRESSION.getNextValidTimeAfter(current), "2024-10-21 01:55:13");
		assertEquals(CRON_EXPRESSION.getPrevFireTime(current), "2024-10-20 21:55:13");
	}

	private static Date parse(String text){
		return new Date(DTF.parse(text, Instant::from).toEpochMilli());
	}

	private static void assertEquals(Date actual, String expected){
		Assert.assertEquals(Instant.ofEpochMilli(actual.getTime()), DTF.parse(expected, Instant::from));
	}
}

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

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class ZonedDateFormatterTool{

	// TODO: Should we rather use DateTimeFormatter.RFC_1123_DATE_TIME ?
	// 'Tue, 3 Jun 2008 11:05:30 GMT'.
	public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss zzz yyyy");
	public static final DateTimeFormatter FORMATTER_DESC = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss zzz");

	public static String formatInstantWithZone(Instant instant, ZoneId zoneId){
		ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instant, zoneId);
		return FORMATTER.format(zonedDateTime);
	}

}

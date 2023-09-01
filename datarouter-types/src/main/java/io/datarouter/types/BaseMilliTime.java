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
package io.datarouter.types;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public interface BaseMilliTime<T extends BaseMilliTime<T>>{

	DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss zzz");

	/*------------------------------ conversion -----------------------------*/

	long toEpochMilli();
	long toReversedEpochMilli();

	default MilliTime toMilliTime(){
		return MilliTime.ofEpochMilli(toEpochMilli());
	}

	default MilliTimeReversed toMilliTimeReversed(){
		return MilliTimeReversed.ofReversedEpochMilli(toReversedEpochMilli());
	}

	default Instant toInstant(){
		return Instant.ofEpochMilli(toEpochMilli());
	}

	default Date toDate(){
		return new Date(toEpochMilli());
	}

	/*------------------------------- readable ------------------------------*/

	default String format(ZoneId zoneId){
		return format(FORMATTER, zoneId);
	}

	default String format(DateTimeFormatter formatter, ZoneId zoneId){
		var zonedDateTime = ZonedDateTime.ofInstant(toInstant(), zoneId);
		return formatter.format(zonedDateTime);
	}

	/*--------------------------------- util --------------------------------*/

	default Duration age(){
		long ageMs = System.currentTimeMillis() - toEpochMilli();
		return Duration.ofMillis(ageMs);
	}

}

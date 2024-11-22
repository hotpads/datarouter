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
package io.datarouter.gson;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.types.MilliTime;
import io.datarouter.types.MilliTimeReversed;
import io.datarouter.types.Quad;
import io.datarouter.types.Ulid;
import io.datarouter.types.UlidReversed;

public class DatarouterGsonsHumanReadableTests{

	/*
	 * This excludes the Date type because it's known to be broken.
	 * Example Date serialization: "Sep 19, 2024, 8:58:12 PM"
	 * It's missing milliseconds in the encoded format, meaning the source and destination dates will usually differ.
	 * It's missing a timezone in the encoded format, so the hard-coded string will only work in the right timezone.
	 */
	private record FieldsDto(
			Duration duration,
			Instant instant,
			LocalDate localDate,
			LocalDateTime localDateTime,
			LocalTime localTime,
			MilliTime milliTime,
			MilliTimeReversed milliTimeReversed,
			Quad quad,
			Ulid ulid,
			UlidReversed ulidReversed){
	}

	private static final Duration DURATION = Duration.ofDays(3)
			.plusHours(4)
			.plusMinutes(5)
			.plusSeconds(6)
			.plusMillis(7);
	private static final Quad QUAD = new Quad("321210");
	private static final Instant INSTANT = Instant.ofEpochSecond(1_726_793_892, 123_456_789);
	private static final MilliTime MILLI_TIME = MilliTime.of(INSTANT);
	private static final MilliTimeReversed MILLI_TIME_REVERSED = MilliTimeReversed.of(INSTANT);
	private static final LocalDate LOCAL_DATE = INSTANT.atOffset(ZoneOffset.UTC).toLocalDate();
	private static final LocalDateTime LOCAL_DATE_TIME = INSTANT.atOffset(ZoneOffset.UTC).toLocalDateTime();
	private static final LocalTime LOCAL_TIME = INSTANT.atOffset(ZoneOffset.UTC).toLocalTime();
	private static final Ulid ULID = Ulid.createFirstUlidFromTimestamp(INSTANT.toEpochMilli());
	private static final UlidReversed ULID_REVERSED = UlidReversed.toUlidReversed(ULID);

	private static final FieldsDto DTO = new FieldsDto(
			DURATION,
			INSTANT,
			LOCAL_DATE,
			LOCAL_DATE_TIME,
			LOCAL_TIME,
			MILLI_TIME,
			MILLI_TIME_REVERSED,
			QUAD,
			ULID,
			ULID_REVERSED);

	private static final String EXPECTED_JSON = """
			{
			  "duration": {
			    "seconds": 273906,
			    "nanos": 7000000
			  },
			  "instant": {
			    "seconds": 1726793892,
			    "nanos": 123456789
			  },
			  "localDate": {
			    "year": 2024,
			    "month": 9,
			    "day": 20
			  },
			  "localDateTime": {
			    "date": {
			      "year": 2024,
			      "month": 9,
			      "day": 20
			    },
			    "time": {
			      "hour": 0,
			      "minute": 58,
			      "second": 12,
			      "nano": 123456789
			    }
			  },
			  "localTime": {
			    "hour": 0,
			    "minute": 58,
			    "second": 12,
			    "nano": 123456789
			  },
			  "milliTime": 1726793892123,
			  "milliTimeReversed": 9223370310060883684,
			  "quad": "321210",
			  "ulid": "01J86EZG8V0000000000000000",
			  "ulidReversed": "7YDQSH0FQ4ZZZZZZZZZZZZZZZZ"
			}""";

	@Test
	public void testAllFields(){
		String actualJsonString = DatarouterGsons.forPrettyPrint().toJson(DTO);
		Assert.assertEquals(actualJsonString, EXPECTED_JSON);
	}

}

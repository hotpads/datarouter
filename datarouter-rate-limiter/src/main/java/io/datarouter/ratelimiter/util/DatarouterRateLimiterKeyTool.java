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
package io.datarouter.ratelimiter.util;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import io.datarouter.ratelimiter.DatarouterRateLimiterConfig;
import io.datarouter.scanner.ObjectScanner;
import io.datarouter.util.time.ZoneIds;

public class DatarouterRateLimiterKeyTool{

	public static List<String> buildKeysToRead(String key, Instant instant, DatarouterRateLimiterConfig config){
		List<String> keys = new ArrayList<>();
		for(int i = 0; i < config.numIntervals; i++){
			int amount = i * config.bucketIntervalMs;
			String mapKey = makeMapKey(key, getTimeStr(instant.minusMillis(amount), config));
			keys.add(mapKey);
		}
		return keys;
	}

	public static String makeKeyPrefix(DatarouterRateLimiterConfig config, String... keyFields){
		return ObjectScanner.of(config.name)
				.append(keyFields)
				.exclude(String::isBlank)
				.collect(Collectors.joining("_"));
	}

	// makes the key to put in the map from the key given and current time bucket
	public static String makeMapKey(String key, String time){
		return key.replaceAll("!", "%21") + "!" + time;
	}

	// inverse of makeMapKey
	public static KeyTime unmakeMapKey(String mapKey){
		String[] splits = mapKey.split("!");
		return new KeyTime(splits[0].replaceAll("%21", "!"), splits[1]);
	}

	public record KeyTime(
			String key,
			String time){
	}

	/*
	 * returns a string of the time bucket closest to (and below) the given calendar
	 * ie:
	 *   2009-06-06 11:11:11.123 => 2009-06-06T11:11:10Z when timeUnit = seconds and bucketInterval = 10s
	 *   						 => 2009-06-06T06:00:00Z when timeUnit = hours   and bucketInterval = 6 hours
	 *   						 => 2009-06-06T11:08:00Z when timeUnit = minutes and bucketInterval = 4 minutes
	 */
	public static String getTimeStr(Instant instant, DatarouterRateLimiterConfig config){
		ChronoField chronoField = switch(config.unit){
			case DAYS -> ChronoField.DAY_OF_MONTH;
			case HOURS -> ChronoField.HOUR_OF_DAY;
			case MINUTES -> ChronoField.MINUTE_OF_HOUR;
			case SECONDS -> ChronoField.SECOND_OF_MINUTE;
			default -> ChronoField.MILLI_OF_SECOND;
		};
		Instant truncatedInstant = getCalendarFieldForBucket(instant, config.unit, chronoField,
				config.bucketTimeInterval);
		return DateTimeFormatter.ISO_INSTANT.format(truncatedInstant);
	}

	private static Instant getCalendarFieldForBucket(Instant instant, TimeUnit timeUnit, ChronoField chronoField,
			int fieldInterval){
		//Turn into a ZoneDateTime to have full ChronoUnitField support
		ZonedDateTime zonedDateTime = instant.atZone(ZoneIds.UTC);

		// rely on int rounding to truncate. 10*(x/10) gives closet multiple of 10 below x
		long newTemporalvalue = fieldInterval * (zonedDateTime.getLong(chronoField) / fieldInterval);

		// Day = 0 does not exist. It represents the previous month.
		if(timeUnit == TimeUnit.DAYS && newTemporalvalue == 0){
			return zonedDateTime.truncatedTo(timeUnit.toChronoUnit())
					.with(chronoField, 1)
					.minusDays(1)
					.toInstant();
		}
		return zonedDateTime.truncatedTo(timeUnit.toChronoUnit())
				.with(chronoField, newTemporalvalue)
				.toInstant();
	}

}

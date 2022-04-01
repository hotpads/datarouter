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
package io.datarouter.ratelimiter;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import io.datarouter.instrumentation.count.Counters;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.time.ZoneIds;
import io.datarouter.util.tuple.Pair;

//TODO rolling increases/decreases in limit,
//for spammers who hit the rate limit alot (decrease) and for people/things that are verified as not spam (increase)
public abstract class BaseCacheRateLimiter extends BaseRateLimiter{

	private static final String HIT_COUNTER_NAME = "rate limit hit";

	private final CacheRateLimiterConfig config;

	public BaseCacheRateLimiter(CacheRateLimiterConfig config){
		super(config.name);
		this.config = config;
	}

	protected abstract Long increment(String key);
	protected abstract Map<String,Long> readCounts(List<String> keys);

	@Override
	protected Pair<Boolean,Instant> internalAllow(String key, boolean increment){
		Instant now = Instant.now();
		Map<String,Long> results = readCounts(buildKeysToRead(key, now));

		String currentMapKey = makeMapKey(key, getTimeStr(now));
		int total = 0;

		for(Entry<String,Long> entry : results.entrySet()){
			Long numRequests = entry.getValue() == null ? 0L : entry.getValue();
			if(entry.getKey().equals(currentMapKey)){
				numRequests++;
			}

			// exceeded maxSpikeRequests
			if(numRequests > config.maxSpikeRequests){
				Instant exceededInstant = getDateFromKey(entry.getKey());
				Counters.inc(HIT_COUNTER_NAME);
				return new Pair<>(false, exceededInstant
						.plusMillis(config.bucketIntervalMs * (config.numIntervals - 1)));
			}
			total += numRequests;
		}

		double avgRequests = total / (double)config.numIntervals;

		// exceeded maxAvgRequests
		if(avgRequests > config.maxAverageRequests){
			List<Instant> instants = Scanner.of(results.keySet()).map(this::getDateFromKey).list();
			Instant lastTime = Instant.MIN;
			for(Instant instant : instants){
				if(instant.isAfter(lastTime)){
					lastTime = instant;
				}
			}
			Objects.requireNonNull(lastTime);

			// add to get next available time
			Counters.inc(HIT_COUNTER_NAME);
			return new Pair<>(false, lastTime.plusMillis(config.bucketIntervalMs));
		}
		if(increment){
			increment(currentMapKey);
		}
		return new Pair<>(true, null);
	}

	private List<String> buildKeysToRead(String key, Instant instant){
		List<String> keys = new ArrayList<>();
		for(int i = 0; i < config.numIntervals; i++){
			int amount = i * config.bucketIntervalMs;
			String mapKey = makeMapKey(key, getTimeStr(instant.minusMillis(amount)));
			keys.add(mapKey.toString());
		}
		return keys;
	}

	// makes the key to put in the map from the key given and current time bucket
	private String makeMapKey(String key, String time){
		return key.replaceAll("!", "%21") + "!" + time;
	}

	// inverse of makeMapKey
	private Pair<String,String> unmakeMapKey(String mapKey){
		String[] splits = mapKey.split("!");
		return new Pair<>(splits[0].replaceAll("%21", "!"), splits[1]);
	}

	/*
	 * returns a string of the time bucket closest to (and below) the given calendar
	 * ie:
	 *   2009-06-06 11:11:11.123 => 2009-06-06T11:11:10Z when timeUnit = seconds and bucketInterval = 10s
	 *   						 => 2009-06-06T06:00:00Z when timeUnit = hours   and bucketInterval = 6 hours
	 *   						 => 2009-06-06T11:08:00Z when timeUnit = minutes and bucketInterval = 4 minutes
	 */
	protected String getTimeStr(Instant instant){
		ChronoField chornoField;
		switch(config.unit){
		case DAYS:
			chornoField = ChronoField.DAY_OF_MONTH;
			break;
		case HOURS:
			chornoField = ChronoField.HOUR_OF_DAY;
			break;
		case MINUTES:
			chornoField = ChronoField.MINUTE_OF_HOUR;
			break;
		case SECONDS:
			chornoField = ChronoField.SECOND_OF_MINUTE;
			break;
		default:
			chornoField = ChronoField.MILLI_OF_SECOND;
			break;
		}
		Instant truncatedInstant = setCalendarFieldForBucket(instant,config.unit, chornoField,
				config.bucketTimeInterval);
		return DateTimeFormatter.ISO_INSTANT.format(truncatedInstant);
	}

	private Instant getDateFromKey(String key){
		String dateString = unmakeMapKey(key).getRight();
		try{
			return Instant.parse(dateString);
		}catch(DateTimeParseException e){
			throw new IllegalStateException("unparseable key " + key, e);
		}
	}

	private static Instant setCalendarFieldForBucket(Instant instant, TimeUnit timeUnit, ChronoField chronoField,
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

	public Duration getBucketTimeInterval(){
		return Duration.ofMillis(config.bucketIntervalMs);
	}

	public CacheRateLimiterConfig getConfig(){
		return config;
	}

}

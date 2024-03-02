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

import javax.servlet.http.HttpServletRequest;

import io.datarouter.instrumentation.metric.Metrics;
import io.datarouter.ratelimiter.storage.BaseTallyDao;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.time.ZoneIds;
import io.datarouter.web.util.http.RequestTool;

public class DatarouterRateLimiter{

	private static final String HIT_COUNTER_NAME = "rate limit hit";
	private static final String EXCEEDED_AVG = "rate limit exceeded avg";
	private static final String COUNTER_PREFIX = "RateLimiter ";

	private final BaseTallyDao tallyDao;
	private final DatarouterRateLimiterConfig config;

	public DatarouterRateLimiter(BaseTallyDao tallyDao, DatarouterRateLimiterConfig config){
		this.tallyDao = tallyDao;
		this.config = config;
	}

	public boolean peek(String key){
		return internalAllow(makeKey(key), false);
	}

	public boolean allowed(){
		return allowed("");
	}

	public boolean allowed(String dynamicKey){
		boolean allowed = internalAllow(makeKey(dynamicKey), true);
		if(allowed){
			Metrics.count(COUNTER_PREFIX + config.name + " allowed");
		}else{
			Metrics.count(COUNTER_PREFIX + config.name + " limit reached");
		}
		return allowed;
	}

	public boolean allowedForIp(HttpServletRequest request){
		return allowedForIp("", request);
	}

	public boolean allowedForIp(String dynamicKey, HttpServletRequest request){
		String ip = RequestTool.getIpAddress(request);
		boolean allowed = internalAllow(makeKey(dynamicKey,ip), true);
		if(allowed){
			Metrics.count(COUNTER_PREFIX + "ip " + config.name + " allowed");
		}else{
			Metrics.count(COUNTER_PREFIX + "ip " + config.name + " limit reached");
		}
		return allowed;
	}

	public String getName(){
		return config.name;
	}

	// null returned indicated that the cache datastore failed the operation
	protected Long increment(String key){
		return tallyDao.incrementAndGetCount(key, 1, config.expiration, Duration.ofMillis(200));
	}

	protected boolean internalAllow(String key, boolean increment){
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
				Metrics.count(HIT_COUNTER_NAME);
				return false;
			}
			total += numRequests;
		}

		double avgRequests = total / (double)config.numIntervals;

		// exceeded maxAvgRequests
		if(avgRequests > config.maxAverageRequests){
			List<Instant> instants = Scanner.of(results.keySet())
					.map(DatarouterRateLimiter::getDateFromKey)
					.list();
			Instant lastTime = Instant.MIN;
			for(Instant instant : instants){
				if(instant.isAfter(lastTime)){
					lastTime = instant;
				}
			}
			Objects.requireNonNull(lastTime);

			// add to get next available time
			Metrics.count(HIT_COUNTER_NAME);
			Metrics.count(EXCEEDED_AVG);
			return false;
		}
		if(increment){
			increment(currentMapKey);
		}
		return true;
	}

	/*
	 * returns a string of the time bucket closest to (and below) the given calendar
	 * ie:
	 *   2009-06-06 11:11:11.123 => 2009-06-06T11:11:10Z when timeUnit = seconds and bucketInterval = 10s
	 *   						 => 2009-06-06T06:00:00Z when timeUnit = hours   and bucketInterval = 6 hours
	 *   						 => 2009-06-06T11:08:00Z when timeUnit = minutes and bucketInterval = 4 minutes
	 */
	protected String getTimeStr(Instant instant){
		ChronoField chornoField = switch(config.unit){
		case DAYS -> ChronoField.DAY_OF_MONTH;
		case HOURS -> ChronoField.HOUR_OF_DAY;
		case MINUTES -> ChronoField.MINUTE_OF_HOUR;
		case SECONDS -> ChronoField.SECOND_OF_MINUTE;
		default -> ChronoField.MILLI_OF_SECOND;
		};
		Instant truncatedInstant = setCalendarFieldForBucket(instant,config.unit, chornoField,
				config.bucketTimeInterval);
		return DateTimeFormatter.ISO_INSTANT.format(truncatedInstant);
	}

	private Map<String,Long> readCounts(List<String> keys){
		return tallyDao.getMultiTallyCount(keys, config.expiration, Duration.ofMillis(200));
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
	private static String makeMapKey(String key, String time){
		return key.replaceAll("!", "%21") + "!" + time;
	}

	// inverse of makeMapKey
	private static KeyTime unmakeMapKey(String mapKey){
		String[] splits = mapKey.split("!");
		return new KeyTime(splits[0].replaceAll("%21", "!"), splits[1]);
	}

	private record KeyTime(
			String key,
			String time){
	}

	private static Instant getDateFromKey(String key){
		String dateString = unmakeMapKey(key).time();
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

	private static String makeKey(String... keyFields){
		return String.join("_", keyFields);
	}

}

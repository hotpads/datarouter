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
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import io.datarouter.instrumentation.metric.Metrics;
import io.datarouter.ratelimiter.storage.BaseTallyDao;
import io.datarouter.ratelimiter.util.DatarouterRateLimiterKeyTool;
import io.datarouter.scanner.Scanner;

public class DatarouterRateLimiter{

	private static final String HIT_COUNTER_NAME = "rate limit hit";
	private static final String EXCEEDED_AVG = "rate limit exceeded avg";
	private static final String EXCEEDED_PEAK = "rate limit exceeded peak";
	public static final String COUNTER_PREFIX = "RateLimiter ";

	private final BaseTallyDao tallyDao;
	private final DatarouterRateLimiterConfig config;

	public DatarouterRateLimiter(BaseTallyDao tallyDao, DatarouterRateLimiterConfig config){
		this.tallyDao = tallyDao;
		this.config = config;
	}

	public boolean peek(String key){
		return internalAllow(DatarouterRateLimiterKeyTool.makeKeyPrefix(config, key), false);
	}

	public boolean allowed(){
		return allowed("");
	}

	public boolean allowed(String dynamicKey){
		boolean allowed = internalAllow(DatarouterRateLimiterKeyTool.makeKeyPrefix(config, dynamicKey), true);
		if(allowed){
			Metrics.count(COUNTER_PREFIX + config.name + " allowed");
		}else{
			Metrics.count(COUNTER_PREFIX + config.name + " limit reached");
		}
		return allowed;
	}

	public boolean allowedForIp(String ip){
		return allowedForIp("", ip);
	}

	public boolean allowedForIp(String dynamicKey, String ip){
		boolean allowed = internalAllow(DatarouterRateLimiterKeyTool.makeKeyPrefix(config, dynamicKey, ip), true);
		if(allowed){
			Metrics.count(COUNTER_PREFIX + config.name + " ip allowed");
		}else{
			Metrics.count(COUNTER_PREFIX + config.name + " ip limit reached");
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
		Map<String,Long> results = readCounts(DatarouterRateLimiterKeyTool.buildKeysToRead(key, now, config));
		String currentMapKey =
				DatarouterRateLimiterKeyTool.makeMapKey(key, DatarouterRateLimiterKeyTool.getTimeStr(now, config));
		int total = 0;

		for(Entry<String,Long> entry : results.entrySet()){
			Long numRequests = entry.getValue() == null ? 0L : entry.getValue();
			if(entry.getKey().equals(currentMapKey)){
				numRequests++;
			}

			// exceeded maxSpikeRequests
			if(numRequests > config.maxSpikeRequests){
				Metrics.count(HIT_COUNTER_NAME);
				Metrics.count(EXCEEDED_PEAK);
				Metrics.count(COUNTER_PREFIX + config.name + " " + EXCEEDED_PEAK);
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
			Metrics.count(COUNTER_PREFIX + config.name + " " + EXCEEDED_AVG);
			return false;
		}
		if(increment){
			increment(currentMapKey);
		}
		return true;
	}

	private Map<String,Long> readCounts(List<String> keys){
		return tallyDao.getMultiTallyCount(keys, config.expiration, Duration.ofMillis(200));
	}

	private static Instant getDateFromKey(String key){
		String dateString = DatarouterRateLimiterKeyTool.unmakeMapKey(key).time();
		try{
			return Instant.parse(dateString);
		}catch(DateTimeParseException e){
			throw new IllegalStateException("unparseable key " + key, e);
		}
	}

	public DatarouterRateLimiterConfig getConfig(){
		return config;
	}

}

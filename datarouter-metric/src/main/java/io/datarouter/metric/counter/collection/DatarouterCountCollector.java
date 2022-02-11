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
package io.datarouter.metric.counter.collection;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.instrumentation.count.CountCollector;
import io.datarouter.model.util.CommonFieldSizes;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.setting.Setting;
import io.datarouter.util.DateTool;
import io.datarouter.util.string.StringTool;

public class DatarouterCountCollector implements CountCollector{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterCountCollector.class);

	public static final long PERIOD_GRANULARITY_MS = CountPartitions.PERIOD_5s.getPeriodMs();
	public static final int METRICS_INITIAL_CAPACITY = 512;//try to set higher than est num counters

	private final long flushIntervalMs;
	private final CountFlusher flusher;
	private final Setting<Boolean> saveCounts;

	private ConcurrentHashMap<Long,ConcurrentHashMap<String,AtomicLong>> valueByNameByPeriodStartMs;
	private long nextFlushMs;

	public DatarouterCountCollector(long flushIntervalMs, CountFlusher flusher, Setting<Boolean> saveCounts){
		this.flushIntervalMs = flushIntervalMs;
		this.nextFlushMs = DateTool.getPeriodStart(flushIntervalMs) + flushIntervalMs;
		this.valueByNameByPeriodStartMs = new ConcurrentHashMap<>();
		this.flusher = flusher;
		this.saveCounts = saveCounts;
	}

	private synchronized void flush(long flushingMs){
		if(nextFlushMs != flushingMs){
			return; //another thread flushed
		}
		nextFlushMs += flushIntervalMs;//not other threads waiting will return in the block above
		Map<Long,ConcurrentHashMap<String,AtomicLong>> snapshot = valueByNameByPeriodStartMs;
		valueByNameByPeriodStartMs = new ConcurrentHashMap<>();
		Scanner.of(snapshot.keySet())
				.include(period -> snapshot.get(period).isEmpty())
				.map(timestamp -> timestamp.toString())
				.flush(timestamps -> {
					if(timestamps.size() > 0){
						logger.info("found empty maps in snap for timestamps={}", String.join(",", timestamps));
					}
				});

		Map<Long,Map<String,Long>> valueByPeriodStartByName = Scanner.of(snapshot.entrySet())
				.toMap(Entry::getKey, entry -> Scanner.of(entry.getValue().entrySet())
						.toMap(Entry::getKey, nameValEntry -> nameValEntry.getValue().get()));
		Scanner.of(valueByPeriodStartByName.keySet())
				.include(period -> valueByPeriodStartByName.get(period).isEmpty())
				.map(timestamp -> timestamp.toString())
				.flush(timestamps -> {
					if(timestamps.size() > 0){
						logger.info("found empty maps in final for timestamps={}", String.join(",", timestamps));
					}
				});

		if(saveCounts.get() && !valueByPeriodStartByName.isEmpty()){
			flusher.saveCounts(valueByPeriodStartByName);
		}
	}

	@Override
	public long increment(String key){
		return increment(key, 1);
	}

	@Override
	public long increment(String key, long delta){
		if(System.currentTimeMillis() >= nextFlushMs){
			//time to flush, a few threads may wait here
			flush(nextFlushMs);
		}
		if(delta == 0){
			return 0;
		}
		if(key.contains("\n") || key.contains("\t")){
			Exception stackTrace = new IllegalArgumentException();
			logger.warn("discarding bad count key={}", key, stackTrace);
			return 0;
		}

		long periodStartMs = DateTool.getPeriodStart(PERIOD_GRANULARITY_MS);
		long total = valueByNameByPeriodStartMs
				.computeIfAbsent(periodStartMs, $ -> new ConcurrentHashMap<>(METRICS_INITIAL_CAPACITY))
				.computeIfAbsent(sanitizeName(key), $ -> new AtomicLong(0))
				.addAndGet(delta);
		if(total == 0){
			logger.info("empty counter sum for key={} and timestamp={}", key, periodStartMs);
		}
		return total;
	}

	@Override
	public void stopAndFlushAll(){
		flush(nextFlushMs);
	}

	private static String sanitizeName(String name){
		String sanitized = StringTool.trimToSize(name, CommonFieldSizes.DEFAULT_LENGTH_VARCHAR);
		sanitized = StringTool.removeNonStandardCharacters(sanitized);
		return sanitized;
	}

}

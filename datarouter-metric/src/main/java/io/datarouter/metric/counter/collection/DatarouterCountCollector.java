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

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.instrumentation.count.CountCollector;
import io.datarouter.metric.counter.conveyor.CountBuffers;
import io.datarouter.model.util.CommonFieldSizes;
import io.datarouter.storage.setting.Setting;
import io.datarouter.util.DateTool;
import io.datarouter.util.string.StringTool;

public class DatarouterCountCollector implements CountCollector{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterCountCollector.class);

	public static final long PERIOD_GRANULARITY_MS = CountPartitions.PERIOD_5s.getPeriodMs();
	public static final int METRICS_INITIAL_CAPACITY = 512;//try to set higher than est num counters

	private final long flushIntervalMs;
	private final CountBuffers countBuffers;
	private final Setting<Boolean> saveCounts;
	private final Setting<Boolean> saveCountStats;

	@Deprecated
	private HashMap<Long,Map<String,Long>> valueByNameByPeriodStartMs;
	private HashMap<Long,Map<String,CountCollectorStats>> statsByNameByPeriodStartMs;
	private long minTimeMs;
	private long nextFlushMs;

	public DatarouterCountCollector(long flushIntervalMs, CountBuffers countBuffers,
			Setting<Boolean> saveCounts, Setting<Boolean> saveCountStats){
		this.minTimeMs = DateTool.getPeriodStart(flushIntervalMs);
		this.flushIntervalMs = flushIntervalMs;
		this.nextFlushMs = minTimeMs + flushIntervalMs;
		this.valueByNameByPeriodStartMs = new HashMap<>();
		this.statsByNameByPeriodStartMs = new HashMap<>();
		this.countBuffers = countBuffers;
		this.saveCounts = saveCounts;
		this.saveCountStats = saveCountStats;
	}

	private void flush(long flushingMs){
		Map<Long,Map<String,Long>> snapshot;
		Map<Long,Map<String,CountCollectorStats>> statsSnapshot;

		//time to flush, a few threads may wait here
		synchronized(this){
			if(nextFlushMs != flushingMs){
				return; //another thread flushed
			}
			//other threads waiting will return in the block above
			minTimeMs = flushingMs;
			nextFlushMs = flushingMs + flushIntervalMs;
			snapshot = valueByNameByPeriodStartMs;
			statsSnapshot = statsByNameByPeriodStartMs;
			if(logger.isInfoEnabled()){
				logger.info(
						"flushing periods=[{}], currentFlush={}",
						snapshot.keySet().stream()
								.map(String::valueOf)
								.collect(Collectors.joining(",")),
						flushingMs);
			}
			valueByNameByPeriodStartMs = new HashMap<>();
			statsByNameByPeriodStartMs = new HashMap<>();
		}
		if(saveCounts.get() && !snapshot.isEmpty()){
			countBuffers.offer(snapshot);
		}
		if(saveCountStats.get() && !statsSnapshot.isEmpty()){
			countBuffers.offerCountStats(statsSnapshot);
		}
	}

	@Override
	public long increment(String key){
		return increment(key, 1);
	}

	@Override
	public long increment(String key, long delta){
		long timeMs = System.currentTimeMillis();
		if(timeMs >= nextFlushMs){
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
		key = sanitizeName(key);

		long total;
		synchronized(this){
			//fix timeMs if it should have been in last flush but wasn't
			timeMs = Long.max(System.currentTimeMillis(), minTimeMs);
			//fix timeMs if it should have triggered a flush but didn't
			timeMs = Long.min(timeMs, nextFlushMs - 1);
			//if flushIntervalMs and PERIOD_GRANULARITY_MS do not line up, periodStartMs may be < minTimeMs (this is ok)
			long periodStartMs = DateTool.getPeriodStart(timeMs, PERIOD_GRANULARITY_MS);
			total = valueByNameByPeriodStartMs
					.computeIfAbsent(periodStartMs, $ -> new HashMap<>(METRICS_INITIAL_CAPACITY))
					.merge(key, delta, Long::sum);

			CountCollectorStats prevStats = statsByNameByPeriodStartMs
					.computeIfAbsent(periodStartMs, $ -> new HashMap<>(METRICS_INITIAL_CAPACITY))
					.computeIfAbsent(key, $ -> new CountCollectorStats(0, 0, delta, delta));
			CountCollectorStats newStats = CountCollectorStats.updateStats(prevStats, delta);
			statsByNameByPeriodStartMs.get(periodStartMs).put(key, newStats);
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

	public static record CountCollectorStats(
			long sum,
			long count,
			long min,
			long max){

		public static CountCollectorStats updateStats(CountCollectorStats prevStats, long delta){
			return new CountCollectorStats(
					prevStats.sum + delta,
					prevStats.count + 1,
					Math.min(prevStats.min, delta),
					Math.max(prevStats.max, delta));
		}
	}

}

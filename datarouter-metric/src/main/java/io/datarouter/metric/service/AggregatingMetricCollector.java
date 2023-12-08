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
package io.datarouter.metric.service;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.instrumentation.gauge.MetricCollector;
import io.datarouter.metric.counter.collection.CountPartitions;
import io.datarouter.metric.service.AggregatedGaugesPublisher.MetricCollectorStats;
import io.datarouter.model.util.CommonFieldSizes;
import io.datarouter.util.string.StringTool;
import io.datarouter.util.time.EpochMillisTool;

public abstract class AggregatingMetricCollector implements MetricCollector{
	private static final Logger logger = LoggerFactory.getLogger(AggregatingMetricCollector.class);

	private static final long FLUSH_INTERVAL_MS = CountPartitions.PERIOD_5s.getPeriodMs();
	public static final long PERIOD_GRANULARITY_MS = CountPartitions.PERIOD_5s.getPeriodMs();
	public static final int METRICS_INITIAL_CAPACITY = 512;//try to set higher than est num counters

	private HashMap<Long,Map<String,MetricCollectorStats>> statsByNameByPeriodStartMs = new HashMap<>();
	private long minTimeMs = EpochMillisTool.getPeriodStart(FLUSH_INTERVAL_MS);
	private long nextFlushMs = minTimeMs + FLUSH_INTERVAL_MS;

	public abstract void offerMetricStats(Map<Long,Map<String,MetricCollectorStats>> metricStats);
	public abstract boolean saveToMemory();

	@Override
	public void save(String key, long value){
		saveKeyAndValue(key, value);
	}

	private long saveKeyAndValue(String key, long value){
		long timeMs = System.currentTimeMillis();
		if(timeMs >= nextFlushMs){
			flush(nextFlushMs);
		}
		if(value == 0 && !saveZeros()){
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
			long periodStartMs = EpochMillisTool.getPeriodStart(timeMs, PERIOD_GRANULARITY_MS);

			MetricCollectorStats prevStats = statsByNameByPeriodStartMs
					.computeIfAbsent(periodStartMs, $ -> new HashMap<>(METRICS_INITIAL_CAPACITY))
					.computeIfAbsent(key, $ -> new MetricCollectorStats(0, 0, value, value));
			MetricCollectorStats newStats = MetricCollectorStats.updateStats(prevStats, value);
			statsByNameByPeriodStartMs.get(periodStartMs).put(key, newStats);
			total = newStats.sum();
		}
		return total;
	}

	private static String sanitizeName(String name){
		String sanitized = StringTool.trimToSize(name, CommonFieldSizes.DEFAULT_LENGTH_VARCHAR);
		return StringTool.removeNonStandardCharacters(sanitized);
	}

	private void flush(long flushingMs){
		Map<Long,Map<String,MetricCollectorStats>> statsSnapshot;

		//time to flush, a few threads may wait here
		synchronized(this){
			if(nextFlushMs != flushingMs){
				return; //another thread flushed
			}
			//other threads waiting will return in the block above
			minTimeMs = flushingMs;
			nextFlushMs = flushingMs + FLUSH_INTERVAL_MS;
			statsSnapshot = statsByNameByPeriodStartMs;
			if(logger.isInfoEnabled()){
				logger.info(
						"flushing periods=[{}], currentFlush={}",
						statsSnapshot.keySet().stream()
								.map(String::valueOf)
								.collect(Collectors.joining(",")),
						flushingMs);
			}
			statsByNameByPeriodStartMs = new HashMap<>();
		}
		if(saveToMemory() && !statsSnapshot.isEmpty()){
			offerMetricStats(statsSnapshot);
		}
	}

	@Override
	public void stopAndFlushAll(){
		flush(nextFlushMs);
	}

}

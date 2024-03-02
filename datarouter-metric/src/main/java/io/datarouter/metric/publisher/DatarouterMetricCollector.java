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
package io.datarouter.metric.publisher;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import io.datarouter.instrumentation.metric.MetricCollector;
import io.datarouter.metric.MetricType;
import io.datarouter.metric.config.DatarouterMetricSettingRoot;
import io.datarouter.metric.publisher.MetricPublisher.PublishedMetricPeriod;
import io.datarouter.metric.publisher.MetricPublisher.PublishedMetricPeriod.PublishedCount;
import io.datarouter.metric.publisher.MetricPublisher.PublishedMetricPeriod.PublishedGauge;
import io.datarouter.metric.publisher.MetricPublisher.PublishedMetricPeriod.PublishedMeasurementList;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.properties.ServerName;
import io.datarouter.storage.config.properties.ServiceName;
import io.datarouter.types.Ulid;
import io.datarouter.util.time.EpochMillisTool;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterMetricCollector implements MetricCollector{

	private static final long PERIOD_GRANULARITY_MS = DatarouterMetricPeriod.PERIOD_5s.getPeriodMs();
	// Wait for the previous period to complete plus a little bit for increments after the synchronization.
	private static final long TAKE_OLDER_THAN_MS = PERIOD_GRANULARITY_MS + 200;
	private static final int MAX_RETAINED_PERIODS = 12;

	@Inject
	private DatarouterMetricSettingRoot metricSettingRoot;
	@Inject
	private ServiceName serviceName;
	@Inject
	private ServerName serverName;

	private final Map<Long,Map<String,AtomicMetric>> periods = new ConcurrentHashMap<>();
	private final AtomicBoolean stopped = new AtomicBoolean(false);

	@Override
	public void count(String name, long value){
		if(value != 0){
			collect(MetricType.COUNT, name, value, false);
		}
	}

	@Override
	public void measure(String name, long value, boolean retainIndividualValues){
		collect(MetricType.GAUGE, name, value, retainIndividualValues);
	}

	private void collect(MetricType type, String name, long value, boolean retainIndividualValues){
		if(stopped.get()
				|| !metricSettingRoot.saveMetricsToMemory.get()
				|| periods.size() > MAX_RETAINED_PERIODS
				|| MetricSanitizer.shouldReject(name)){
			return;
		}
		long nowMs = System.currentTimeMillis();
		long periodStartMs = EpochMillisTool.getPeriodStart(nowMs, PERIOD_GRANULARITY_MS);
		String sanitizedName = MetricSanitizer.sanitizeName(name);
		periods.computeIfAbsent(periodStartMs, $ -> new ConcurrentHashMap<>())
				.computeIfAbsent(sanitizedName, $ -> new AtomicMetric(type))
				.update(value, retainIndividualValues);
	}

	public Optional<PublishedMetricPeriod> poll(){
		return pollSynchronized()
				.map(entry -> toPublishedMetricPeriod(entry.getKey(), entry.getValue()));
	}

	private synchronized Optional<Map.Entry<Long,Map<String,AtomicMetric>>> pollSynchronized(){
		long takeBeforeTimeMs = System.currentTimeMillis() - TAKE_OLDER_THAN_MS;
		Optional<Map.Entry<Long,Map<String,AtomicMetric>>> optResult = Scanner.of(periods.entrySet())
				.include(entry -> stopped.get() || entry.getKey() < takeBeforeTimeMs)
				.findFirst();
		optResult
				.map(Entry::getKey)
				.ifPresent(periods::remove);
		return optResult;
	}

	private PublishedMetricPeriod toPublishedMetricPeriod(
			long periodStartMs,
			Map<String,AtomicMetric> valueByName){
		List<PublishedCount> counts = Scanner.of(valueByName.entrySet())
				.include(entry -> entry.getValue().type == MetricType.COUNT)
				.map(entry -> entry.getValue().toPublishedCount(entry.getKey()))
				.list();
		List<PublishedGauge> gauges = Scanner.of(valueByName.entrySet())
				.include(entry -> entry.getValue().type == MetricType.GAUGE)
				.map(entry -> entry.getValue().toPublishedGauge(entry.getKey()))
				.list();
		List<PublishedMeasurementList> measurementLists = Scanner.of(valueByName.entrySet())
				.include(entry -> entry.getValue().type == MetricType.GAUGE)
				.map(entry -> entry.getValue().toPublishedMeasurementList(entry.getKey()))
				.list();
		return new PublishedMetricPeriod(
				periodStartMs,
				Ulid.newValue(),
				serviceName.get(),
				serverName.get(),
				counts,
				gauges,
				measurementLists);
	}

	@Override
	public void stopAndFlushAll(){
		stopped.set(true);
	}

	/*-------- records ----------*/

	private static class AtomicMetric{
		public final MetricType type;
		private long sum;
		private long count;
		private long min;
		private long max;
		private List<Long> measurements = new ArrayList<>();

		public AtomicMetric(MetricType type){
			this.type = type;
		}

		synchronized void update(long value, boolean retainMeasurement){
			//TODO validate type?
			sum += value;
			if(type == MetricType.GAUGE){
				if(count == 0){
					min = value;
					max = value;
				}
				++count;
				min = Math.min(min, value);
				max = Math.max(max, value);
				if(retainMeasurement){
					measurements.add(value);
				}
			}
		}

		PublishedCount toPublishedCount(String name){
			return new PublishedCount(name, sum);
		}

		PublishedGauge toPublishedGauge(String name){
			return new PublishedGauge(name, sum, count, min, max);
		}

		PublishedMeasurementList toPublishedMeasurementList(String name){
			return new PublishedMeasurementList(name, measurements);
		}
	}
}

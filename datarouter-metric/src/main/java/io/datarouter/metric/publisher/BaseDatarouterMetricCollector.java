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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.instrumentation.metric.MetricCollector;
import io.datarouter.metric.MetricType;
import io.datarouter.metric.config.DatarouterMetricSettingRoot;
import io.datarouter.metric.publisher.MetricPublisher.PublishedMetricPeriod;
import io.datarouter.metric.publisher.MetricPublisher.PublishedMetricPeriod.PublishedCount;
import io.datarouter.metric.publisher.MetricPublisher.PublishedMetricPeriod.PublishedGauge;
import io.datarouter.metric.publisher.MetricPublisher.PublishedMetricPeriod.PublishedMeasurementList;
import io.datarouter.scanner.Scanner;
import io.datarouter.types.Ulid;
import io.datarouter.util.number.RandomTool;
import io.datarouter.util.time.EpochMillisTool;

public abstract class BaseDatarouterMetricCollector implements MetricCollector{
	private static final Logger logger = LoggerFactory.getLogger(BaseDatarouterMetricCollector.class);

	private static final long PERIOD_GRANULARITY_MS = DatarouterMetricPeriod.PERIOD_5s.getPeriodMs();
	// Wait for the previous period to complete plus a little bit for increments after the synchronization.
	private static final long TAKE_OLDER_THAN_MS = PERIOD_GRANULARITY_MS + 200;
	private static final int MAX_RETAINED_PERIODS = 12;

	private final Map<Long,Map<String,AtomicMetric>> periods = new ConcurrentHashMap<>();
	private final AtomicBoolean stopped = new AtomicBoolean(false);
	private long logPeriodsThrottleMs = 0;

	private final DatarouterMetricSettingRoot metricSettingRoot;
	private final String environmentName;
	private final String serviceName;
	private final String serverName;
	private final boolean useRandom;

	public BaseDatarouterMetricCollector(
			DatarouterMetricSettingRoot metricSettingRoot,
			String environmentName,
			String serviceName,
			String serverName,
			boolean useRandom){
		this.metricSettingRoot = metricSettingRoot;
		this.environmentName = environmentName;
		this.serviceName = serviceName;
		this.serverName = serverName;
		this.useRandom = useRandom;
	}

	@Override
	public void count(String name, long value){
		countWithTime(System.currentTimeMillis(), name, value);
	}

	public void countWithTime(long eventTimeMs, String name, long value){
		if(value != 0){
			collect(eventTimeMs, MetricType.COUNT, name, value, false);
		}
	}

	@Override
	public void measure(String name, long value, boolean retainIndividualValues){
		measureWithTime(System.currentTimeMillis(), name, value, retainIndividualValues);
	}

	public void measureWithTime(long eventTimeMs, String name, long value, boolean retainIndividualValues){
		collect(eventTimeMs, MetricType.GAUGE, name, value, retainIndividualValues);
	}

	private void collect(long eventTimeMs, MetricType type, String name, long value, boolean retainIndividualValues){
		if(stopped.get()
				|| !metricSettingRoot.saveMetricsToMemory.get()
				|| MetricSanitizer.shouldReject(name)){
			return;
		}
		if(periods.size() > MAX_RETAINED_PERIODS){
			long now = System.currentTimeMillis();
			if(now > logPeriodsThrottleMs){
				logPeriodsThrottleMs = now + 20_000;
				logger.debug("Max retained periods reached periods={}", periods.size());
			}
			return;
		}
		long periodStartMs = EpochMillisTool.getPeriodStart(eventTimeMs, PERIOD_GRANULARITY_MS);
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
				environmentName,
				serviceName,
				serverName,
				counts,
				gauges,
				measurementLists,
				useRandom ? Optional.of(RandomTool.nextPositiveInt()) : Optional.empty());
	}

	@Override
	public void stopAndFlushAll(){
		stopped.set(true);
	}

	/*-------- records ----------*/

	public static class AtomicMetric{
		public final MetricType type;
		private long sum;
		private long count;
		private long min;
		private long max;
		private final List<Long> measurements = new ArrayList<>();

		public AtomicMetric(MetricType type){
			this.type = type;
		}

		public AtomicMetric(MetricType type, long sum, long count, long min, long max){
			this.type = type;
			this.sum = sum;
			this.count = count;
			this.min = min;
			this.max = max;
		}

		public static AtomicMetric combine(AtomicMetric left, AtomicMetric right){
			var metric = new AtomicMetric(left.type);
			metric.sum = left.sum + right.sum;
			metric.count = left.count + right.count;
			metric.min = Math.min(left.min, right.min);
			metric.max = Math.max(left.max, right.max);
			metric.measurements.addAll(left.measurements);
			metric.measurements.addAll(right.measurements);
			return metric;
		}

		public synchronized void update(long value, boolean retainMeasurement){
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

		public long getSum(){
			return sum;
		}

		public long getCount(){
			return count;
		}

		public long getMin(){
			return min;
		}

		public long getMax(){
			return max;
		}

		public List<Long> getMeasurements(){
			return measurements;
		}
	}
}

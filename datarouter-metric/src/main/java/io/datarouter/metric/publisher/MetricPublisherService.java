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
import java.util.concurrent.atomic.AtomicLong;

import io.datarouter.instrumentation.response.PublishingResponseDto;
import io.datarouter.metric.publisher.DatarouterMetricGroupBinaryDto.DatarouterCountBinaryDto;
import io.datarouter.metric.publisher.DatarouterMetricGroupBinaryDto.DatarouterGaugeBinaryDto;
import io.datarouter.metric.publisher.DatarouterMetricGroupBinaryDto.DatarouterMeasurementBinaryDto;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.properties.EnvironmentName;
import io.datarouter.storage.config.properties.ServerName;
import io.datarouter.storage.config.properties.ServiceName;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class MetricPublisherService implements MetricPublisher{

	// Combine ~100 items into a single dto to reduce the overhead of serverName, etc
	// Then multiple dtos will be packed into a single message as space allows.
	private static final int COUNTS_PER_FRAGMENT = 100;
	private static final int GAUGES_PER_FRAGMENT = 100;
	private static final int MEASUREMENTS_PER_BATCH = 100;
	private static final int MEASUREMENT_BATCHES_PER_FRAGMENT = 100;

	@Inject
	private EnvironmentName environmentNameSupplier;
	@Inject
	private ServiceName serviceNameSupplier;
	@Inject
	private ServerName serverNameSupplier;
	@Inject
	private MetricQueueDao metricQueueDao;

	@Override
	public PublishingResponseDto publish(PublishedMetricPeriod period){
		List<DatarouterMetricGroupBinaryDto> metrics = new ArrayList<>();

		Scanner.of(period.counts())
				.map(publishedCount -> new DatarouterCountBinaryDto(
						publishedCount.name(),
						publishedCount.value()))
				.batch(COUNTS_PER_FRAGMENT)
				.map(countBatch -> new DatarouterMetricGroupBinaryDto(
						environmentNameSupplier.get(),
						serviceNameSupplier.get(),
						serverNameSupplier.get(),
						period.periodStartTimeMs(),
						countBatch,
						List.of(),
						List.of()))
				.forEach(metrics::add);

		Scanner.of(period.gauges())
				.map(publishedGauge -> new DatarouterGaugeBinaryDto(
						publishedGauge.name(),
						publishedGauge.sum(),
						publishedGauge.count(),
						publishedGauge.min(),
						publishedGauge.max()))
				.batch(GAUGES_PER_FRAGMENT)
				.map(gaugeBatch -> new DatarouterMetricGroupBinaryDto(
						environmentNameSupplier.get(),
						serviceNameSupplier.get(),
						serverNameSupplier.get(),
						period.periodStartTimeMs(),
						List.of(),
						gaugeBatch,
						List.of()))
				.forEach(metrics::add);

		Scanner.of(period.measurementLists())
				.concat(measurementList -> splitMeasurementList(
						measurementList.name(),
						measurementList.values()))
				.batch(MEASUREMENT_BATCHES_PER_FRAGMENT)
				.map(measurementBatch -> new DatarouterMetricGroupBinaryDto(
						environmentNameSupplier.get(),
						serviceNameSupplier.get(),
						serverNameSupplier.get(),
						period.periodStartTimeMs(),
						List.of(),
						List.of(),
						measurementBatch))
				.forEach(metrics::add);

		metricQueueDao.combineAndPut(metrics);
		return PublishingResponseDto.SUCCESS;
	}

	// Metrics with many measurements may be split into multiple fragments.
	private Scanner<DatarouterMeasurementBinaryDto> splitMeasurementList(
			String metricName,
			List<Long> measurements){
		var measurementBatchId = new AtomicLong();
		return Scanner.of(measurements)
				.batch(MEASUREMENTS_PER_BATCH)
				.map(measurementBatch -> new DatarouterMeasurementBinaryDto(
						measurementBatchId.getAndIncrement(),
						metricName,
						measurementBatch.stream().mapToLong(Long::valueOf).toArray()));
	}
}

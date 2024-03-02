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

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.bytes.KvString;
import io.datarouter.conveyor.Conveyor.ProcessResult;
import io.datarouter.conveyor.ConveyorConfiguration;
import io.datarouter.conveyor.ConveyorCounters;
import io.datarouter.conveyor.ConveyorRunnable;
import io.datarouter.metric.publisher.MetricPublisher.PublishedMetricPeriod;
import io.datarouter.metric.publisher.MetricPublisher.PublishedMetricPeriod.PublishedMeasurementList;
import io.datarouter.util.number.NumberFormatter;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class MetricPublisherConveyorConfiguration implements ConveyorConfiguration{
	private static final Logger logger = LoggerFactory.getLogger(MetricPublisherConveyorConfiguration.class);

	@Inject
	private DatarouterMetricCollector collector;
	@Inject
	private MetricPublisher publisher;

	@Override
	public ProcessResult process(ConveyorRunnable conveyor){
		Optional<PublishedMetricPeriod> optPeriod = collector.poll();
		optPeriod.ifPresent(period -> {
			int numCounts = period.counts().size();
			int numGauges = period.gauges().size();
			int numMeasurementLists = period.measurementLists().size();
			int numMeasurements = period.measurementLists().stream()
					.map(PublishedMeasurementList::values)
					.mapToInt(List::size)
					.sum();
			try{
				publisher.publish(period);
				ConveyorCounters.inc(conveyor, "published counts", numCounts);
				ConveyorCounters.inc(conveyor, "published gauges", numGauges);
				ConveyorCounters.inc(conveyor, "published measurementLists", numMeasurementLists);
				ConveyorCounters.inc(conveyor, "published measurements", numMeasurements);
				logger.info("published {}", new KvString()
						.add("counts", numCounts, NumberFormatter::addCommas)
						.add("gauges", numGauges, NumberFormatter::addCommas)
						.add("measurementLists", numMeasurementLists, NumberFormatter::addCommas)
						.add("measurements", numMeasurements, NumberFormatter::addCommas));
			}catch(RuntimeException e){
				logger.warn("", e);
				ConveyorCounters.inc(conveyor, "failed counts", numCounts);
				ConveyorCounters.inc(conveyor, "failed gauges", numGauges);
				ConveyorCounters.inc(conveyor, "failed measurementLists", numMeasurementLists);
				ConveyorCounters.inc(conveyor, "failed measurements", numMeasurements);
			}
		});
		return new ProcessResult(optPeriod.isPresent());
	}

	@Override
	public boolean shouldRunOnShutdown(){
		return true;
	}

	@Override
	public Duration delay(){
		return Duration.ofSeconds(1L);
	}

}

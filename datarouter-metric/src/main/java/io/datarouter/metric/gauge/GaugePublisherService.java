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
package io.datarouter.metric.gauge;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.instrumentation.response.PublishingResponseDto;
import io.datarouter.metric.gauge.conveyor.GaugeQueueDao;
import io.datarouter.metric.service.AggregatedGaugesPublisher;
import io.datarouter.storage.config.properties.ServerName;
import io.datarouter.storage.config.properties.ServiceName;
import io.datarouter.types.Ulid;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class GaugePublisherService implements AggregatedGaugesPublisher{
	private static final Logger logger = LoggerFactory.getLogger(GaugePublisherService.class);

	private static final String LOG_STRING = "queue";

	@Inject
	private GaugeQueueDao gaugeQueueDao;
	@Inject
	private ServiceName serviceName;
	@Inject
	private ServerName serverName;

	@Override
	public PublishingResponseDto publish(Map<Long,Map<String,MetricCollectorStats>> gauges){
		String ulid = new Ulid().value();
		var dtos = GaugeBinaryDto.createSizedGaugeBinaryDtosFromMetricStats(
				ulid,
				serviceName.get(),
				serverName.get(),
				gauges,
				100);
		logger.info(
				"writing size={} GaugeBinaryDtos with key={} to {}",
				dtos.size(),
				ulid,
				LOG_STRING);
		gaugeQueueDao.combineAndPut(dtos);
		return PublishingResponseDto.SUCCESS;

	}

}
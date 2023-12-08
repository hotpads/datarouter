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
package io.datarouter.metric.counter;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.instrumentation.response.PublishingResponseDto;
import io.datarouter.metric.counter.collection.CountPublisher;
import io.datarouter.metric.service.AggregatedGaugesPublisher.MetricCollectorStats;
import io.datarouter.storage.config.properties.ServerName;
import io.datarouter.storage.config.properties.ServiceName;
import io.datarouter.types.Ulid;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class CountPublisherService implements CountPublisher{
	private static final Logger logger = LoggerFactory.getLogger(CountPublisherService.class);

	private final CountQueueDao countQueueDao;
	private final ServiceName serviceName;
	private final ServerName serverName;

	@Inject
	public CountPublisherService(CountQueueDao countQueueDao, ServiceName serviceName, ServerName serverName){
		this.countQueueDao = countQueueDao;
		this.serviceName = serviceName;
		this.serverName = serverName;
	}

	@Override
	public PublishingResponseDto publishStats(Map<Long,Map<String,MetricCollectorStats>> counts){
		String ulid = new Ulid().value();
		var dtos = CountBinaryDto.createSizedCountBinaryDtos(
				ulid,
				serviceName.get(),
				serverName.get(),
				counts,
				100);
		logger.info(
				"writing size={} CountBinaryDtos with key={} to {}",
				dtos.size(),
				ulid,
				"queue");
		countQueueDao.combineAndPut(dtos);
		return PublishingResponseDto.SUCCESS;
	}

}

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.instrumentation.gauge.GaugeBatchDto;
import io.datarouter.instrumentation.gauge.GaugePublisher;
import io.datarouter.instrumentation.response.PublishingResponseDto;
import io.datarouter.metric.dto.RawGaugeBinaryDto;
import io.datarouter.metric.gauge.conveyor.RawGaugeQueueDao;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class RawGaugePublisherService implements GaugePublisher{
	private static final Logger logger = LoggerFactory.getLogger(RawGaugePublisherService.class);

	@Inject
	private RawGaugeQueueDao gaugeQueueDao;

	@Override
	public PublishingResponseDto publish(GaugeBatchDto gaugeBatchDto){
		var dtos = RawGaugeBinaryDto.createSizedDtos(
				gaugeBatchDto,
				100);//100 should easily fit inside a single queue message and still provide good space saving
		logger.info("writing size={} blobs", dtos.size());
		gaugeQueueDao.combineAndPut(dtos);
		return PublishingResponseDto.SUCCESS;
	}

}
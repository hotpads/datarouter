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

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.instrumentation.gauge.GaugeBatchDto;
import io.datarouter.instrumentation.gauge.GaugePublisher;
import io.datarouter.instrumentation.response.PublishingResponseDto;
import io.datarouter.metric.config.DatarouterGaugeSettingRoot;
import io.datarouter.metric.dto.GaugeBinaryDto;
import io.datarouter.util.Ulid;

@Singleton
public class GaugePublisherService implements GaugePublisher{
	private static final Logger logger = LoggerFactory.getLogger(GaugePublisherService.class);

	private final GaugeDirectoryDao gaugeDirectoryDao;
	private final GaugeQueueDao gaugeQueueDao;
	private final DatarouterGaugeSettingRoot gaugeSettings;

	@Inject
	public GaugePublisherService(GaugeDirectoryDao gaugeDirectoryDao, GaugeQueueDao gaugeQueueDao,
			DatarouterGaugeSettingRoot gaugeSettings){
		this.gaugeDirectoryDao = gaugeDirectoryDao;
		this.gaugeQueueDao = gaugeQueueDao;
		this.gaugeSettings = gaugeSettings;
	}

	@Override
	public PublishingResponseDto publish(GaugeBatchDto gaugeBatchDto){
		if(gaugeSettings.saveToQueueInsteadOfDirectory.get()){
			var dtos = GaugeBinaryDto.createSizedDtos(
					gaugeBatchDto,
					100);//100 should easily fit inside a single queue message and still provide good space saving
			logger.info("writing size={} blobs", dtos.size());
			gaugeQueueDao.combineAndPut(dtos);
			return PublishingResponseDto.SUCCESS;
		}
		var dto = GaugeBinaryDto.createSizedDtos(
				gaugeBatchDto,
				gaugeBatchDto.batch.size()).get(0);//put all into one DTO
		Ulid ulid = new Ulid();
		logger.info("writing key={}", ulid);
		gaugeDirectoryDao.write(dto, ulid);
		return PublishingResponseDto.SUCCESS;
	}

}
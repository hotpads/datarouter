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

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.conveyor.message.ConveyorMessage;
import io.datarouter.instrumentation.count.CountBatchDto;
import io.datarouter.instrumentation.count.CountPublisher;
import io.datarouter.instrumentation.response.PublishingResponseDto;
import io.datarouter.metric.config.DatarouterCountSettingRoot;
import io.datarouter.util.UlidTool;

@Singleton
public class CountBlobService implements CountPublisher{
	private static final Logger logger = LoggerFactory.getLogger(CountBlobService.class);

	private final CountBlobDao countBlobDao;
	private final DatarouterCountBlobQueueDao countBlobQueueDao;
	private final DatarouterCountSettingRoot countSettings;
	private final CountBlobPublishingSettings countBlobPublishingSettings;

	@Inject
	public CountBlobService(CountBlobDao countBlobDao, DatarouterCountBlobQueueDao countBlobQueueDao,
			DatarouterCountSettingRoot countSettings, CountBlobPublishingSettings countBlobPublishingSettings){
		this.countBlobDao = countBlobDao;
		this.countBlobQueueDao = countBlobQueueDao;
		this.countSettings = countSettings;
		this.countBlobPublishingSettings = countBlobPublishingSettings;
	}

	@Override
	public PublishingResponseDto add(CountBatchDto dto){
		var countBlobDto = toBlob(dto);
		if(countSettings.saveCountBlobsToQueueInsteadOfCloud.get()){
			logger.info("writing queue key={}", countBlobDto.ulid);
			countBlobQueueDao.put(new ConveyorMessage(countBlobDto.ulid, countBlobDto.serializeToString()));
		}else{
			logger.info("writing key={}", countBlobDto.ulid);
			countBlobDao.write(countBlobDto);
		}
		return PublishingResponseDto.SUCCESS;
	}

	private CountBlobDto toBlob(CountBatchDto dto){
		return new CountBlobDto(
				UlidTool.nextUlid(),
				dto.serviceName,
				dto.serverName,
				dto.counts,
				countBlobPublishingSettings.getApiKey());
	}

	public CountBatchDto fromBlob(CountBlobDto dto){
		return new CountBatchDto(
				UlidTool.getTimestamp(dto.ulid),
				dto.serviceName,
				dto.serverName,
				dto.counts);
	}

}

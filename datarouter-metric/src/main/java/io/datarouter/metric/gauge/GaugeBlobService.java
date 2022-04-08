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

import io.datarouter.bytes.ByteUnitType;
import io.datarouter.conveyor.message.ConveyorMessage;
import io.datarouter.instrumentation.gauge.GaugeBatchDto;
import io.datarouter.instrumentation.gauge.GaugePublisher;
import io.datarouter.instrumentation.response.PublishingResponseDto;
import io.datarouter.metric.config.DatarouterGaugeSettingRoot;
import io.datarouter.metric.counter.MetricBlobPublishingSettings;
import io.datarouter.metric.dto.GaugeBlobDto;
import io.datarouter.util.UlidTool;

@Singleton
public class GaugeBlobService implements GaugePublisher{
	private static final Logger logger = LoggerFactory.getLogger(GaugeBlobService.class);

	//based on AWS SQS message length limit
	private static final int MAX_SERIALIZED_BLOB_SIZE = ByteUnitType.KiB.toBytesInt(256) - 30;

	private final GaugeBlobDirectoryDao gaugeBlobDirectoryDao;
	private final GaugeBlobQueueDao gaugeBlobQueueDao;
	private final DatarouterGaugeSettingRoot gaugeSettings;
	private final MetricBlobPublishingSettings metricBlobPublishingSettings;

	@Inject
	public GaugeBlobService(GaugeBlobDirectoryDao gaugeBlobDirectoryDao, GaugeBlobQueueDao gaugeBlobQueueDao,
			DatarouterGaugeSettingRoot gaugeSettings, MetricBlobPublishingSettings metricBlobPublishingSettings){
		this.gaugeBlobDirectoryDao = gaugeBlobDirectoryDao;
		this.gaugeBlobQueueDao = gaugeBlobQueueDao;
		this.gaugeSettings = gaugeSettings;
		this.metricBlobPublishingSettings = metricBlobPublishingSettings;
	}

	@Override
	public PublishingResponseDto add(GaugeBatchDto gaugeBatchDto){
		GaugeBlobDto dto = new GaugeBlobDto(gaugeBatchDto, metricBlobPublishingSettings.getApiKey());
		String ulid = UlidTool.nextUlid();
		if(gaugeSettings.saveGaugeBlobsToQueueDaoInsteadOfDirectoryDao.get()){
			dto.serializeToStrings(MAX_SERIALIZED_BLOB_SIZE)
					.map(blob -> new ConveyorMessage(ulid, blob))
					.flush(blobs -> {
						if(blobs.size() > 1){
							logger.warn("writing size={} blobs with key={}", blobs.size(), ulid);
						}else{
							logger.info("writing size={} blobs with key={}", blobs.size(), ulid);
						}
						gaugeBlobQueueDao.putMulti(blobs);
					});
		}else{
			logger.info("writing key={}", ulid);
			gaugeBlobDirectoryDao.write(dto, ulid);
		}
		return PublishingResponseDto.SUCCESS;
	}

}
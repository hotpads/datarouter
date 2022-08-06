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
import io.datarouter.metric.config.MaxMetricBlobSize;
import io.datarouter.metric.counter.MetricBlobPublishingSettings;
import io.datarouter.metric.dto.GaugeBinaryDto;
import io.datarouter.metric.dto.GaugeBlobDto;
import io.datarouter.storage.queue.StringQueueMessage;
import io.datarouter.util.UlidTool;

@Singleton
public class GaugeBlobService implements GaugePublisher{
	private static final Logger logger = LoggerFactory.getLogger(GaugeBlobService.class);

	private final GaugeBlobDirectoryDao gaugeBlobDirectoryDao;
	private final GaugeBlobQueueDao gaugeBlobQueueDao;
	private final DatarouterGaugeSettingRoot gaugeSettings;
	private final MetricBlobPublishingSettings metricBlobPublishingSettings;
	private final MaxMetricBlobSize maxMetricBlobSize;

	@Inject
	public GaugeBlobService(GaugeBlobDirectoryDao gaugeBlobDirectoryDao, GaugeBlobQueueDao gaugeBlobQueueDao,
			DatarouterGaugeSettingRoot gaugeSettings, MetricBlobPublishingSettings metricBlobPublishingSettings,
			MaxMetricBlobSize maxMetricBlobSize){
		this.gaugeBlobDirectoryDao = gaugeBlobDirectoryDao;
		this.gaugeBlobQueueDao = gaugeBlobQueueDao;
		this.gaugeSettings = gaugeSettings;
		this.metricBlobPublishingSettings = metricBlobPublishingSettings;
		this.maxMetricBlobSize = maxMetricBlobSize;
	}

	@Override
	public PublishingResponseDto add(GaugeBatchDto gaugeBatchDto){
		if(gaugeSettings.saveGaugeBlobsToQueueDaoInsteadOfDirectoryDao.get() && gaugeSettings.useBinaryDtoQueue.get()){
			var dtos = GaugeBinaryDto.createSizedDtos(
					gaugeBatchDto,
					metricBlobPublishingSettings.getApiKey(),
					100);//100 should easily fit inside a single queue message and still provide good space saving
			logger.info("writing size={} blobs", dtos.size());
			gaugeBlobQueueDao.combineAndPut(dtos);
			return PublishingResponseDto.SUCCESS;
		}
		GaugeBlobDto dto = new GaugeBlobDto(gaugeBatchDto, metricBlobPublishingSettings.getApiKey());
		String ulid = UlidTool.nextUlid();
		if(gaugeSettings.saveGaugeBlobsToQueueDaoInsteadOfDirectoryDao.get()){
			dto.serializeToStrings(maxMetricBlobSize.get())
					.map(blob -> new StringQueueMessage(ulid, blob))
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
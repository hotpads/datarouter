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
package io.datarouter.trace.service;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.conveyor.message.ConveyorMessage;
import io.datarouter.instrumentation.response.PublishingResponseDto;
import io.datarouter.instrumentation.trace.Trace2BatchedBundleDto;
import io.datarouter.instrumentation.trace.TracePublisher;
import io.datarouter.trace.config.MaxTraceBlobSize;
import io.datarouter.trace.settings.DatarouterTracePublisherSettingRoot;
import io.datarouter.trace.settings.TraceBlobPublishingSettings;
import io.datarouter.trace.storage.trace.TraceBlobDirectoryDao;
import io.datarouter.trace.storage.trace.TraceBlobQueueDao;
import io.datarouter.util.UlidTool;

@Singleton
public class TraceBlobService implements TracePublisher{
	private static final Logger logger = LoggerFactory.getLogger(TraceBlobService.class);

	private final DatarouterTracePublisherSettingRoot traceSettings;
	private final TraceBlobPublishingSettings traceBlobPublishingSettings;

	private final TraceBlobDirectoryDao traceBlobDirectoryDao;
	private final TraceBlobQueueDao traceBlobQueueDao;

	private final MaxTraceBlobSize maxTraceBlobSize;

	@Inject
	public TraceBlobService(
			DatarouterTracePublisherSettingRoot traceSettings,
			TraceBlobPublishingSettings traceBlobPublishingSettings,
			TraceBlobDirectoryDao traceBlobDirectoryDao,
			TraceBlobQueueDao traceBlobQueueDao,
			MaxTraceBlobSize maxTraceBlobSize){
		this.traceSettings = traceSettings;
		this.traceBlobPublishingSettings = traceBlobPublishingSettings;
		this.traceBlobDirectoryDao = traceBlobDirectoryDao;
		this.traceBlobQueueDao = traceBlobQueueDao;
		this.maxTraceBlobSize = maxTraceBlobSize;
	}

	@Override
	public PublishingResponseDto addBatch(Trace2BatchedBundleDto traceBatchedDto){
		TraceBlobDto dto = new TraceBlobDto(traceBlobPublishingSettings.getApiKey(), traceBatchedDto.batch);
		String ulid = UlidTool.nextUlid();
		if(traceSettings.saveTraceBlobsToQueueDaoInsteadOfDirectoryDao.get()){
			var fielder = new ConveyorMessage.UnlimitedSizeConveyorMessageFielder();
			int nonMessageLength = fielder.getStringDatabeanCodec().toString(new ConveyorMessage(ulid, ""), fielder)
					.length();
			dto.serializeToStrings(maxTraceBlobSize.get() - nonMessageLength)
					.map(blob -> new ConveyorMessage(ulid, blob))
					.flush(blobs -> {
						if(blobs.size() > 1){
							logger.warn("writing size={} blobs with key={}", blobs.size(), ulid);
						}else{
							logger.info("writing size={} blobs with key={}", blobs.size(), ulid);
						}
						traceBlobQueueDao.putMulti(blobs);
					});
		}else{
			logger.info("writing key={}", ulid);
			traceBlobDirectoryDao.write(dto, ulid);
		}
		return PublishingResponseDto.SUCCESS;
	}

}

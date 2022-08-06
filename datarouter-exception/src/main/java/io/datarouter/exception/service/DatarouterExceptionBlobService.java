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
package io.datarouter.exception.service;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.bytes.ByteUnitType;
import io.datarouter.exception.config.DatarouterExceptionSettingRoot;
import io.datarouter.exception.config.MaxExceptionBlobSize;
import io.datarouter.exception.dto.ExceptionRecordBlobDto;
import io.datarouter.exception.dto.HttpRequestRecordBlobDto;
import io.datarouter.exception.storage.exceptionrecord.ExceptionRecordBlobDirectoryDao;
import io.datarouter.exception.storage.exceptionrecord.ExceptionRecordBlobQueueDao;
import io.datarouter.exception.storage.httprecord.HttpRequestRecordBlobDirectoryDao;
import io.datarouter.exception.storage.httprecord.HttpRequestRecordBlobQueueDao;
import io.datarouter.instrumentation.exception.DatarouterExceptionPublisher;
import io.datarouter.instrumentation.exception.ExceptionRecordBatchDto;
import io.datarouter.instrumentation.exception.HttpRequestRecordBatchDto;
import io.datarouter.instrumentation.response.PublishingResponseDto;
import io.datarouter.model.util.CommonFieldSizes;
import io.datarouter.storage.queue.StringQueueMessage;
import io.datarouter.util.UlidTool;

@Singleton
public class DatarouterExceptionBlobService implements DatarouterExceptionPublisher{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterExceptionBlobService.class);

	//TODO support MaxExceptionBlobSize for HttpRequestRecordBlob, too (need UnlimitedSizeConveyorMessageFielder)
	public static final int MAX_SERIALIZED_BLOB_SIZE_SQS = CommonFieldSizes.MAX_SQS_SIZE
			- ByteUnitType.KiB.toBytesInt(1);

	private final DatarouterExceptionSettingRoot exceptionSettings;
	private final ExceptionBlobPublishingSettings exceptionBlobPublishingSettings;

	private final ExceptionRecordBlobDirectoryDao exceptionRecordBlobDirectoryDao;
	private final ExceptionRecordBlobQueueDao exceptionRecordBlobQueueDao;

	private final HttpRequestRecordBlobDirectoryDao httpRequestRecordBlobDirectoryDao;
	private final HttpRequestRecordBlobQueueDao httpRequestRecordBlobQueueDao;

	private final MaxExceptionBlobSize maxExceptionBlobSize;

	@Inject
	public DatarouterExceptionBlobService(DatarouterExceptionSettingRoot exceptionSettings,
			ExceptionBlobPublishingSettings exceptionBlobPublishingSettings,
			ExceptionRecordBlobDirectoryDao exceptionRecordBlobDirectoryDao,
			ExceptionRecordBlobQueueDao exceptionRecordBlobQueueDao,
			HttpRequestRecordBlobDirectoryDao httpRequestRecordBlobDirectoryDao,
			HttpRequestRecordBlobQueueDao httpRequestRecordBlobQueueDao,
			MaxExceptionBlobSize maxExceptionBlobSize){
		this.exceptionSettings = exceptionSettings;
		this.exceptionBlobPublishingSettings = exceptionBlobPublishingSettings;
		this.exceptionRecordBlobDirectoryDao = exceptionRecordBlobDirectoryDao;
		this.exceptionRecordBlobQueueDao = exceptionRecordBlobQueueDao;
		this.httpRequestRecordBlobDirectoryDao = httpRequestRecordBlobDirectoryDao;
		this.httpRequestRecordBlobQueueDao = httpRequestRecordBlobQueueDao;
		this.maxExceptionBlobSize = maxExceptionBlobSize;
	}

	@Override
	public PublishingResponseDto addExceptionRecord(ExceptionRecordBatchDto exceptionRecordBatchDto){
		if(exceptionRecordBatchDto.records.isEmpty()){
			return PublishingResponseDto.SUCCESS;
		}
		ExceptionRecordBlobDto dto = new ExceptionRecordBlobDto(
				exceptionRecordBatchDto,
				exceptionBlobPublishingSettings.getApiKey());
		String ulid = UlidTool.nextUlid();
		if(exceptionSettings.saveExceptionRecordBlobsToQueueDaoInsteadOfDirectoryDao.get()){
			var fielder = new StringQueueMessage.UnlimitedSizeStringQueueMessageFielder();
			int nonMessageLength = fielder.getStringDatabeanCodec().toString(new StringQueueMessage(ulid, ""), fielder)
					.length();
			dto.serializeToStrings(maxExceptionBlobSize.get() - nonMessageLength)
					.map(blob -> new StringQueueMessage(ulid, blob))
					.flush(blobs -> {
						if(blobs.size() > 1){
							logger.warn("writing size={} blobs with key={}", blobs.size(), ulid);
						}else{
							logger.info("writing size={} blobs with key={}", blobs.size(), ulid);
						}
						exceptionRecordBlobQueueDao.putMulti(blobs);
					});
		}else{
			logger.info("writing key={}", ulid);
			exceptionRecordBlobDirectoryDao.write(dto, ulid);
		}
		return PublishingResponseDto.SUCCESS;
	}

	@Override
	public PublishingResponseDto addHttpRequestRecord(HttpRequestRecordBatchDto httpRequestRecordBatchDto){
		if(httpRequestRecordBatchDto.records.isEmpty()){
			return PublishingResponseDto.SUCCESS;
		}
		HttpRequestRecordBlobDto dto = new HttpRequestRecordBlobDto(
				httpRequestRecordBatchDto,
				exceptionBlobPublishingSettings.getApiKey());
		String ulid = UlidTool.nextUlid();
		if(exceptionSettings.saveHttpRequestRecordBlobsToQueueDaoInsteadOfDirectoryDao.get()){
			dto.serializeToStrings(MAX_SERIALIZED_BLOB_SIZE_SQS)
					.map(blob -> new StringQueueMessage(ulid, blob))
					.flush(blobs -> {
						if(blobs.size() > 1){
							logger.warn("writing size={} blobs with key={}", blobs.size(), ulid);
						}else{
							logger.info("writing size={} blobs with key={}", blobs.size(), ulid);
						}
						httpRequestRecordBlobQueueDao.putMulti(blobs);
					});
		}else{
			logger.info("writing key={}", ulid);
			httpRequestRecordBlobDirectoryDao.write(dto, ulid);
		}
		return PublishingResponseDto.SUCCESS;
	}

}

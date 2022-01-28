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

import io.datarouter.httpclient.security.DefaultSignatureGenerator;
import io.datarouter.instrumentation.count.CountBatchDto;
import io.datarouter.instrumentation.count.CountPublisher;
import io.datarouter.instrumentation.response.PublishingResponseDto;
import io.datarouter.util.Require;
import io.datarouter.util.UlidTool;

@Singleton
public class CountBlobService implements CountPublisher{
	private static final Logger logger = LoggerFactory.getLogger(CountBlobService.class);

	private final CountBlobDao countBlobDao;
	private final CountBlobPublishingSettings countBlobPublishingSettings;
	private final DefaultSignatureGenerator publisherSignatureGenerator;

	@Inject
	public CountBlobService(CountBlobDao countBlobDao, CountBlobPublishingSettings countBlobPublishingSettings){
		this.countBlobDao = countBlobDao;
		this.countBlobPublishingSettings = countBlobPublishingSettings;
		this.publisherSignatureGenerator = new DefaultSignatureGenerator(countBlobPublishingSettings::getPrivateKey);
	}

	@Override
	public PublishingResponseDto add(CountBatchDto dto){
		var countBlobDto = toBlob(dto);
		logger.info("writing key={}", countBlobDto.ulid);
		countBlobDao.write(countBlobDto);
		return PublishingResponseDto.SUCCESS;
	}

	private CountBlobDto toBlob(CountBatchDto dto){
		return new CountBlobDto(
				UlidTool.nextUlid(),
				dto.serviceName,
				dto.serverName,
				dto.counts,
				countBlobPublishingSettings.getApiKey(),
				publisherSignatureGenerator);
	}

	public CountBatchDto fromBlob(CountBlobDto dto, DefaultSignatureGenerator signatureGenerator){
		Require.equals(dto.signature, signatureGenerator.getHexSignature(dto.getSignatureMap()).signature);
		return new CountBatchDto(
				UlidTool.getTimestamp(dto.ulid),
				dto.serviceName,
				dto.serverName,
				dto.counts);
	}

}

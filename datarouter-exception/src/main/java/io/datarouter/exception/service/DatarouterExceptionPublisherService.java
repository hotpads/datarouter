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

import io.datarouter.exception.config.DatarouterExceptionSettingRoot;
import io.datarouter.exception.dto.ExceptionRecordBinaryDto;
import io.datarouter.exception.dto.HttpRequestRecordBinaryDto;
import io.datarouter.exception.storage.exceptionrecord.ExceptionRecordDirectoryDao;
import io.datarouter.exception.storage.exceptionrecord.ExceptionRecordQueueDao;
import io.datarouter.exception.storage.httprecord.HttpRequestRecordDirectoryDao;
import io.datarouter.exception.storage.httprecord.HttpRequestRecordQueueDao;
import io.datarouter.instrumentation.exception.DatarouterExceptionPublisher;
import io.datarouter.instrumentation.exception.ExceptionRecordBatchDto;
import io.datarouter.instrumentation.exception.HttpRequestRecordBatchDto;
import io.datarouter.instrumentation.response.PublishingResponseDto;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.properties.ServiceName;
import io.datarouter.types.Ulid;

@Singleton
public class DatarouterExceptionPublisherService implements DatarouterExceptionPublisher{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterExceptionPublisherService.class);

	private final DatarouterExceptionSettingRoot exceptionSettings;

	private final ExceptionRecordDirectoryDao exceptionRecordDirectoryDao;
	private final ExceptionRecordQueueDao exceptionRecordQueueDao;

	private final HttpRequestRecordDirectoryDao httpRequestRecordDirectoryDao;
	private final HttpRequestRecordQueueDao httpRequestRecordQueueDao;

	private final ServiceName serviceName;

	@Inject
	public DatarouterExceptionPublisherService(DatarouterExceptionSettingRoot exceptionSettings,
			ExceptionRecordDirectoryDao exceptionRecordDirectoryDao,
			ExceptionRecordQueueDao exceptionRecordQueueDao,
			HttpRequestRecordDirectoryDao httpRequestRecordDirectoryDao,
			HttpRequestRecordQueueDao httpRequestRecordQueueDao,
			ServiceName serviceName){
		this.exceptionSettings = exceptionSettings;
		this.exceptionRecordDirectoryDao = exceptionRecordDirectoryDao;
		this.exceptionRecordQueueDao = exceptionRecordQueueDao;
		this.httpRequestRecordDirectoryDao = httpRequestRecordDirectoryDao;
		this.httpRequestRecordQueueDao = httpRequestRecordQueueDao;
		this.serviceName = serviceName;
	}

	@Override
	public PublishingResponseDto addExceptionRecord(ExceptionRecordBatchDto exceptionRecordBatchDto){
		if(exceptionRecordBatchDto.records().isEmpty()){
			return PublishingResponseDto.SUCCESS;
		}

		boolean isQueue = exceptionSettings.saveExceptionRecordsToQueueDaoInsteadOfDirectoryDao.get();
		logger.info(
				"writing size={} exceptionRecords to {}",
				exceptionRecordBatchDto.records().size(),
				isQueue ? "queue" : "directory");
		if(isQueue){
			Scanner.of(exceptionRecordBatchDto.records())
					.map(ExceptionRecordBinaryDto::new)
					.then(exceptionRecordQueueDao::combineAndPut);
			return PublishingResponseDto.SUCCESS;
		}
		Scanner.of(exceptionRecordBatchDto.records())
				.map(ExceptionRecordBinaryDto::new)
				.then(scanner -> exceptionRecordDirectoryDao.write(scanner, new Ulid()));
		return PublishingResponseDto.SUCCESS;
	}

	@Override
	public PublishingResponseDto addHttpRequestRecord(HttpRequestRecordBatchDto httpRequestRecordBatchDto){
		if(httpRequestRecordBatchDto.records().isEmpty()){
			return PublishingResponseDto.SUCCESS;
		}

		boolean isQueue = exceptionSettings.saveHttpRequestRecordsToQueueDaoInsteadOfDirectoryDao.get();
		logger.info(
				"writing size={} httpRequestRecords to {}",
				httpRequestRecordBatchDto.records().size(),
				isQueue ? "queue" : "directory");
		if(isQueue){
			Scanner.of(httpRequestRecordBatchDto.records())
					.map(dto -> new HttpRequestRecordBinaryDto(dto, serviceName.get()))
					.then(httpRequestRecordQueueDao::combineAndPut);
			return PublishingResponseDto.SUCCESS;
		}
		Scanner.of(httpRequestRecordBatchDto.records())
				.map(dto -> new HttpRequestRecordBinaryDto(dto, serviceName.get()))
				.then(scanner -> httpRequestRecordDirectoryDao.write(scanner, new Ulid()));
		return PublishingResponseDto.SUCCESS;
	}

}

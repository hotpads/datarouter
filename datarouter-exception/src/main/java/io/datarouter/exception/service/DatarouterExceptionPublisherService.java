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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.exception.config.DatarouterExceptionSettingRoot;
import io.datarouter.exception.dto.ExceptionRecordBinaryDto;
import io.datarouter.exception.dto.HttpRequestRecordBinaryDto;
import io.datarouter.exception.dto.TaskExecutorRecordBinaryDto;
import io.datarouter.exception.storage.exceptionrecord.ExceptionRecordQueueDao;
import io.datarouter.exception.storage.httprecord.HttpRequestRecordQueueDao;
import io.datarouter.exception.storage.taskexecutorrecord.TaskExecutorRecordDirectoryDao;
import io.datarouter.exception.storage.taskexecutorrecord.TaskExecutorRecordQueueDao;
import io.datarouter.instrumentation.exception.DatarouterExceptionPublisher;
import io.datarouter.instrumentation.exception.ExceptionRecordBatchDto;
import io.datarouter.instrumentation.exception.HttpRequestRecordBatchDto;
import io.datarouter.instrumentation.exception.TaskExecutorRecordDto;
import io.datarouter.instrumentation.response.PublishingResponseDto;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.properties.ServiceName;
import io.datarouter.types.Ulid;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterExceptionPublisherService implements DatarouterExceptionPublisher{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterExceptionPublisherService.class);

	private final DatarouterExceptionSettingRoot exceptionSettings;

	private final ExceptionRecordQueueDao exceptionRecordQueueDao;

	private final HttpRequestRecordQueueDao httpRequestRecordQueueDao;

	private final TaskExecutorRecordDirectoryDao taskExecutorRecordDirectoryDao;
	private final TaskExecutorRecordQueueDao taskExecutorRecordQueueDao;

	private final ServiceName serviceName;

	@Inject
	public DatarouterExceptionPublisherService(DatarouterExceptionSettingRoot exceptionSettings,
			ExceptionRecordQueueDao exceptionRecordQueueDao,
			HttpRequestRecordQueueDao httpRequestRecordQueueDao,
			TaskExecutorRecordDirectoryDao taskExecutorRecordDirectoryDao,
			TaskExecutorRecordQueueDao taskExecutorRecordQueueDao,
			ServiceName serviceName){
		this.exceptionSettings = exceptionSettings;
		this.exceptionRecordQueueDao = exceptionRecordQueueDao;
		this.httpRequestRecordQueueDao = httpRequestRecordQueueDao;
		this.taskExecutorRecordDirectoryDao = taskExecutorRecordDirectoryDao;
		this.taskExecutorRecordQueueDao = taskExecutorRecordQueueDao;
		this.serviceName = serviceName;
	}

	@Override
	public PublishingResponseDto addExceptionRecord(ExceptionRecordBatchDto exceptionRecordBatchDto){
		if(exceptionRecordBatchDto.records().isEmpty()){
			return PublishingResponseDto.SUCCESS;
		}
		logger.info("writing size={} exceptionRecords to {}", exceptionRecordBatchDto.records().size(), "queue");
		Scanner.of(exceptionRecordBatchDto.records())
				.map(ExceptionRecordBinaryDto::new)
				.then(exceptionRecordQueueDao::combineAndPut);
		return PublishingResponseDto.SUCCESS;
	}

	@Override
	public PublishingResponseDto addHttpRequestRecord(HttpRequestRecordBatchDto httpRequestRecordBatchDto){
		if(httpRequestRecordBatchDto.records().isEmpty()){
			return PublishingResponseDto.SUCCESS;
		}

		logger.info("writing size={} httpRequestRecords to {}", httpRequestRecordBatchDto.records().size(), "queue");
		Scanner.of(httpRequestRecordBatchDto.records())
				.map(dto -> new HttpRequestRecordBinaryDto(dto, serviceName.get()))
				.then(httpRequestRecordQueueDao::combineAndPut);
		return PublishingResponseDto.SUCCESS;
	}

	@Override
	public PublishingResponseDto addTaskExecutorRecord(List<TaskExecutorRecordDto> taskExecutorRecords){
		if(taskExecutorRecords.isEmpty()){
			return PublishingResponseDto.SUCCESS;
		}
		boolean isQueue = exceptionSettings.saveTaskExecutorRecordsToQueueDaoInsteadOfDirectoryDao.get();
		logger.info(
				"writing size={} httpRequestRecords to {}",
				taskExecutorRecords.size(),
				isQueue ? "queue" : "directory");
		if(isQueue){
			Scanner.of(taskExecutorRecords)
					.map(record -> new TaskExecutorRecordBinaryDto(serviceName.get(), record))
					.then(taskExecutorRecordQueueDao::combineAndPut);
			return PublishingResponseDto.SUCCESS;
		}
		Scanner.of(taskExecutorRecords)
				.map(record -> new TaskExecutorRecordBinaryDto(serviceName.get(), record))
				.then(scanner -> taskExecutorRecordDirectoryDao.write(scanner, new Ulid()));
		return PublishingResponseDto.SUCCESS;
	}

}

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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;

import io.datarouter.exception.dto.ExceptionRecordBinaryDto;
import io.datarouter.exception.dto.HttpRequestRecordBinaryDto;
import io.datarouter.exception.dto.TaskExecutorRecordBinaryDto;
import io.datarouter.exception.storage.exceptionrecord.DatarouterDebuggingRecordDao;
import io.datarouter.exception.storage.exceptionrecord.DatarouterNonProdDebuggingRecordDao;
import io.datarouter.exception.storage.exceptionrecord.DatarouterNonProdDebuggingRecordDao.DatarouterDebuggingRecordGroupBinaryDto;
import io.datarouter.instrumentation.exception.DatarouterDebuggingRecordPublisher;
import io.datarouter.instrumentation.exception.ExceptionRecordDto;
import io.datarouter.instrumentation.exception.HttpRequestRecordDto;
import io.datarouter.instrumentation.exception.TaskExecutorRecordDto;
import io.datarouter.instrumentation.response.PublishingResponseDto;
import io.datarouter.instrumentation.trace.TraceBundleDto;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.properties.EnvironmentName;
import io.datarouter.storage.config.properties.ServerName;
import io.datarouter.storage.config.properties.ServiceName;
import io.datarouter.trace.storage.binarydto.TraceQueueBinaryDto;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterDebuggingRecordService implements DatarouterDebuggingRecordPublisher{

	private static final int BATCH_SIZE = 100;

	@Inject
	private DatarouterNonProdDebuggingRecordDao nonProdQueue;
	@Inject
	private DatarouterDebuggingRecordDao prodQueue;
	@Inject
	private ServiceName serviceName;
	@Inject
	private EnvironmentName envName;
	@Inject
	private ServerName serverName;
	@Inject
	private DefaultExceptionRecorder recorder;

	@Override
	public PublishingResponseDto addBatch(DebuggingRecordBatchDto batchDto){
		Queue<ExceptionRecordDto> exceptions = Scanner.of(batchDto.exceptions())
				.collect(LinkedList::new);
		Queue<HttpRequestRecordDto> requests = Scanner.of(batchDto.requests())
				.collect(LinkedList::new);
		Queue<TraceBundleDto> traces = Scanner.of(batchDto.traces())
				.collect(LinkedList::new);
		Queue<TaskExecutorRecordDto> executorRecords = Scanner.of(batchDto.executorRecords())
				.collect(LinkedList::new);
		List<DatarouterDebuggingRecordGroupBinaryDto> dtos = new ArrayList<>();
		while(!exceptions.isEmpty()
				|| !requests.isEmpty()
				|| !traces.isEmpty()
				|| !executorRecords.isEmpty()){
			DatarouterDebuggingRecordGroupBinaryDto dto = new
					DatarouterDebuggingRecordGroupBinaryDto(
							envName.get(),
							serviceName.get(),
							serverName.get(),
							Scanner.generate(exceptions::poll)
									.advanceUntil(Objects::isNull)
									.map(ExceptionRecordBinaryDto::new)
									.limit(BATCH_SIZE)
									.list(),
							Scanner.generate(requests::poll)
									.advanceUntil(Objects::isNull)
									.map(item -> new HttpRequestRecordBinaryDto(item, serviceName.get()))
									.limit(BATCH_SIZE)
									.list(),
							Scanner.generate(traces::poll)
									.advanceUntil(Objects::isNull)
									.map(TraceQueueBinaryDto::new)
									.limit(BATCH_SIZE)
									.list(),
							Scanner.generate(executorRecords::poll)
									.advanceUntil(Objects::isNull)
									.map(item -> new TaskExecutorRecordBinaryDto(serviceName.get(), item))
									.limit(BATCH_SIZE)
									.list());
			dtos.add(dto);
		}
		if(recorder.publishToSharedNonProdQueue()){
			nonProdQueue.combineAndPut(dtos);
		}else{
			prodQueue.combineAndPut(dtos);
		}
		return PublishingResponseDto.SUCCESS;
	}

}
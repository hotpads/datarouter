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
package io.datarouter.trace.conveyor;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.conveyor.BaseConveyor;
import io.datarouter.conveyor.ConveyorCounters;
import io.datarouter.conveyor.MemoryBuffer;
import io.datarouter.instrumentation.exception.DatarouterExceptionPublisher;
import io.datarouter.instrumentation.exception.HttpRequestRecordBatchDto;
import io.datarouter.instrumentation.trace.Trace2BatchedBundleDto;
import io.datarouter.instrumentation.trace.Trace2BundleAndHttpRequestRecordDto;
import io.datarouter.instrumentation.trace.TracePublisher;
import io.datarouter.instrumentation.trace.Traceparent;
import io.datarouter.scanner.Scanner;
import io.datarouter.web.exception.ExceptionRecorder;

public class TraceMemoryToPublisherConveyor extends BaseConveyor{
	private static final Logger logger = LoggerFactory.getLogger(TraceMemoryToPublisherConveyor.class);

	private static final int BATCH_SIZE = 500;

	private final TracePublisher tracePublisher;
	private final DatarouterExceptionPublisher exceptionPublisher;
	private final MemoryBuffer<Trace2BundleAndHttpRequestRecordDto> buffer;

	public TraceMemoryToPublisherConveyor(
			String name,
			Supplier<Boolean> shouldRun,
			ExceptionRecorder exceptionRecorder,
			MemoryBuffer<Trace2BundleAndHttpRequestRecordDto> buffer,
			TracePublisher tracePublisher,
			DatarouterExceptionPublisher exceptionPublisher){
		super(name, shouldRun, () -> false, exceptionRecorder);
		this.tracePublisher = tracePublisher;
		this.exceptionPublisher = exceptionPublisher;
		this.buffer = buffer;
	}

	public void processTraceEntityDtos(List<Trace2BundleAndHttpRequestRecordDto> traceAndRequestDtos){
		Scanner.of(traceAndRequestDtos)
				.map(dto -> dto.traceBundleDto)
				.flush(dtos -> tracePublisher.addBatch(new Trace2BatchedBundleDto(dtos)));
		Scanner.of(traceAndRequestDtos)
				.map(dto -> dto.httpRequestRecord)
				.include(Objects::nonNull)
				.flush(dtos -> exceptionPublisher.addHttpRequestRecord(new HttpRequestRecordBatchDto(dtos)));
	}

	@Override
	public ProcessBatchResult processBatch(){
		List<Trace2BundleAndHttpRequestRecordDto> dtos = buffer.pollMultiWithLimit(BATCH_SIZE);
		if(dtos.isEmpty()){
			return new ProcessBatchResult(false);
		}
		try{
			processTraceEntityDtos(dtos);
			ConveyorCounters.incPutMultiOpAndDatabeans(this, dtos.size());
			return new ProcessBatchResult(isShuttingDown());
		}catch(RuntimeException putMultiException){
			List<Traceparent> ids = Scanner.of(dtos)
					.map(Trace2BundleAndHttpRequestRecordDto::getTraceparent)
					.list();
			logger.warn("exception sending trace to sqs ids={}", ids, putMultiException);
			ConveyorCounters.inc(this, "putMulti exception", 1);
			return new ProcessBatchResult(isShuttingDown());
		}
	}

	@Override
	public boolean shouldRunOnShutdown(){
		return true;
	}

}

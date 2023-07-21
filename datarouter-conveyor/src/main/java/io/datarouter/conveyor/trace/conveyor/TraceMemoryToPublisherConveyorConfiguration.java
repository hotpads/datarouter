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
package io.datarouter.conveyor.trace.conveyor;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.conveyor.Conveyor.ProcessResult;
import io.datarouter.conveyor.ConveyorConfiguration;
import io.datarouter.conveyor.ConveyorCounters;
import io.datarouter.conveyor.ConveyorGauges;
import io.datarouter.conveyor.ConveyorRunnable;
import io.datarouter.instrumentation.exception.DatarouterExceptionPublisher;
import io.datarouter.instrumentation.exception.HttpRequestRecordBatchDto;
import io.datarouter.instrumentation.trace.Trace2BatchedBundleDto;
import io.datarouter.instrumentation.trace.Trace2BundleAndHttpRequestRecordDto;
import io.datarouter.instrumentation.trace.TracePublisher;
import io.datarouter.instrumentation.trace.Traceparent;
import io.datarouter.instrumentation.trace.TracerTool;
import io.datarouter.scanner.Scanner;
import io.datarouter.trace.conveyor.TraceBuffers;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class TraceMemoryToPublisherConveyorConfiguration implements ConveyorConfiguration{
	private static final Logger logger = LoggerFactory.getLogger(TraceMemoryToPublisherConveyorConfiguration.class);
	private static final int BATCH_SIZE = 500;

	@Inject
	private TraceBuffers traceBuffers;
	@Inject
	private TracePublisher tracePublisher;
	@Inject
	private DatarouterExceptionPublisher exceptionPublisher;
	@Inject
	private ConveyorGauges gaugeRecorder;

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
	public ProcessResult process(ConveyorRunnable conveyor){
		Instant beforePeek = Instant.now();
		List<Trace2BundleAndHttpRequestRecordDto> dtos = traceBuffers.buffer.pollMultiWithLimit(BATCH_SIZE);
		Instant afterPeek = Instant.now();
		gaugeRecorder.savePeekDurationMs(conveyor, Duration.between(beforePeek, afterPeek).toMillis());
		TracerTool.setAlternativeStartTime();
		if(dtos.isEmpty()){
			return new ProcessResult(false);
		}
		try{
			processTraceEntityDtos(dtos);
			ConveyorCounters.incPutMultiOpAndDatabeans(conveyor, dtos.size());
			return new ProcessResult(dtos.size() == BATCH_SIZE);
		}catch(RuntimeException putMultiException){
			List<Traceparent> ids = Scanner.of(dtos)
					.map(Trace2BundleAndHttpRequestRecordDto::getTraceparent)
					.list();
			logger.warn("exception sending trace to sqs ids={}", ids, putMultiException);
			ConveyorCounters.inc(conveyor, "putMulti exception", 1);
			// try it again
			return new ProcessResult(true);
		}
	}

	@Override
	public boolean shouldRunOnShutdown(){
		return true;
	}

	@Override
	public Duration delay(){
		return Duration.ofSeconds(5L);
	}

}

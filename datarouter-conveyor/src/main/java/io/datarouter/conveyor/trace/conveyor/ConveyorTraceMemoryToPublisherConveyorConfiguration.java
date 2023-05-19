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

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.conveyor.Conveyor.ProcessResult;
import io.datarouter.conveyor.ConveyorConfiguration;
import io.datarouter.conveyor.ConveyorCounters;
import io.datarouter.conveyor.ConveyorGauges;
import io.datarouter.conveyor.ConveyorRunnable;
import io.datarouter.conveyor.trace.ConveyorTraceBuffer;
import io.datarouter.instrumentation.exception.DatarouterExceptionPublisher;
import io.datarouter.instrumentation.trace.ConveyorTraceAndTaskExecutorBundleDto;
import io.datarouter.instrumentation.trace.Trace2BatchedBundleDto;
import io.datarouter.instrumentation.trace.TracePublisher;
import io.datarouter.instrumentation.trace.TracerTool;
import io.datarouter.scanner.OptionalScanner;
import io.datarouter.scanner.Scanner;

@Singleton
public class ConveyorTraceMemoryToPublisherConveyorConfiguration implements ConveyorConfiguration{

	private static final int BATCH_SIZE = 500;

	@Inject
	private ConveyorTraceBuffer traceBuffer;
	@Inject
	private TracePublisher tracePublisher;
	@Inject
	private DatarouterExceptionPublisher exceptionPublisher;
	@Inject
	private ConveyorGauges gaugeRecorder;

	@Override
	public ProcessResult process(ConveyorRunnable conveyor){
		Instant beforePeek = Instant.now();
		List<ConveyorTraceAndTaskExecutorBundleDto> dtos = traceBuffer.buffer.pollMultiWithLimit(BATCH_SIZE);
		Instant afterPeek = Instant.now();
		gaugeRecorder.savePeekDurationMs(conveyor, Duration.between(beforePeek, afterPeek).toMillis());
		TracerTool.setAlternativeStartTime();
		if(dtos.isEmpty()){
			return new ProcessResult(false);
		}
		Instant beforeProcess = Instant.now();
		Scanner.of(dtos)
				.map(ConveyorTraceAndTaskExecutorBundleDto::traceBundleDto)
				.flush(traceBundles -> tracePublisher.addBatch(new Trace2BatchedBundleDto(traceBundles)));
		Scanner.of(dtos)
				.map(ConveyorTraceAndTaskExecutorBundleDto::taskExecutorRecord)
				.concat(OptionalScanner::of)
				.flush(exceptionPublisher::addTaskExecutorRecord);
		Instant afterProcess = Instant.now();
		ConveyorCounters.incConsumedOpAndDatabeans(conveyor, Duration.between(beforeProcess, afterProcess).toMillis());
		gaugeRecorder.saveProcessBufferDurationMs(conveyor, BATCH_SIZE);
		return new ProcessResult(true);
	}

	@Override
	public boolean shouldRunOnShutdown(){
		return true;
	}

}

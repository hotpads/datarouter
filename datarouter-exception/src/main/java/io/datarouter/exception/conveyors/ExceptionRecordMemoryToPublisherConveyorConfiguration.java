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
package io.datarouter.exception.conveyors;

import java.time.Duration;
import java.time.Instant;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.conveyor.Conveyor.ProcessResult;
import io.datarouter.conveyor.ConveyorConfiguration;
import io.datarouter.conveyor.ConveyorCounters;
import io.datarouter.conveyor.ConveyorGauges;
import io.datarouter.conveyor.ConveyorRunnable;
import io.datarouter.exception.storage.exceptionrecord.ExceptionRecord;
import io.datarouter.instrumentation.exception.DatarouterExceptionPublisher;
import io.datarouter.instrumentation.exception.ExceptionRecordBatchDto;
import io.datarouter.scanner.Scanner;

@Singleton
public class ExceptionRecordMemoryToPublisherConveyorConfiguration implements ConveyorConfiguration{
	private static final Logger logger = LoggerFactory.getLogger(
			ExceptionRecordMemoryToPublisherConveyorConfiguration.class);
	//this only controls max buffer poll size. the publisher will split as necessary.
	private static final int BATCH_SIZE = 10_000;

	@Inject
	private DatarouterExceptionBuffers buffers;
	@Inject
	private DatarouterExceptionPublisher exceptionRecordPublisher;
	@Inject
	private ConveyorGauges gaugeRecorder;

	@Override
	public ProcessResult process(ConveyorRunnable conveyor){
		Instant beforePeek = Instant.now();
		ExceptionRecordBatchDto batch = Scanner.of(buffers.exceptionRecordPublishingBuffer.pollMultiWithLimit(
				BATCH_SIZE))
				.map(ExceptionRecord::toDto)
				.listTo(ExceptionRecordBatchDto::new);
		Instant afterPeek = Instant.now();
		gaugeRecorder.savePeekDurationMs(conveyor, Duration.between(beforePeek, afterPeek).toMillis());
		if(batch.records().isEmpty()){
			return new ProcessResult(false);
		}
		try{
			exceptionRecordPublisher.addExceptionRecord(batch);
			ConveyorCounters.incPutMultiOpAndDatabeans(conveyor, batch.records().size());
		}catch(Exception putMultiException){
			logger.warn("", putMultiException);
			ConveyorCounters.inc(conveyor, "putMulti exception", 1);
		}
		//process as many as possible if shutting down
		return new ProcessResult(conveyor.isShuttingDown());
	}

	@Override
	public boolean shouldRunOnShutdown(){
		return true;
	}

	@Override
	public long delaySeconds(){
		return 10L;
	}

}

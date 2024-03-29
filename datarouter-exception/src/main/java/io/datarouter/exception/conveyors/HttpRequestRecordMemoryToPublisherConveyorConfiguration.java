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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.conveyor.Conveyor.ProcessResult;
import io.datarouter.conveyor.ConveyorConfiguration;
import io.datarouter.conveyor.ConveyorCounters;
import io.datarouter.conveyor.ConveyorGauges;
import io.datarouter.conveyor.ConveyorRunnable;
import io.datarouter.exception.storage.httprecord.HttpRequestRecord;
import io.datarouter.instrumentation.exception.DatarouterExceptionPublisher;
import io.datarouter.instrumentation.exception.HttpRequestRecordBatchDto;
import io.datarouter.instrumentation.trace.TracerTool;
import io.datarouter.scanner.Scanner;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class HttpRequestRecordMemoryToPublisherConveyorConfiguration implements ConveyorConfiguration{
	private static final Logger logger = LoggerFactory.getLogger(
			HttpRequestRecordMemoryToPublisherConveyorConfiguration.class);

	//this only controls max buffer poll size. the publisher will split as necessary.
	private static final int BATCH_SIZE = 500;

	@Inject
	private DatarouterExceptionBuffers buffers;
	@Inject
	private DatarouterExceptionPublisher exceptionRecordPublisher;
	@Inject
	private ConveyorGauges gaugeRecorder;

	@Override
	public ProcessResult process(ConveyorRunnable conveyor){
		Instant beforePeek = Instant.now();
		HttpRequestRecordBatchDto batch = Scanner.of(buffers.httpRequestRecordPublishingBuffer.pollMultiWithLimit(
				BATCH_SIZE))
				.map(HttpRequestRecord::toDto)
				.listTo(HttpRequestRecordBatchDto::new);
		Instant afterPeek = Instant.now();
		gaugeRecorder.savePeekDurationMs(conveyor, Duration.between(beforePeek, afterPeek).toMillis());
		TracerTool.setAlternativeStartTime();
		if(batch.records().isEmpty()){
			return new ProcessResult(false);
		}
		try{
			exceptionRecordPublisher.addHttpRequestRecord(batch);
			ConveyorCounters.incPutMultiOpAndDatabeans(conveyor, batch.records().size());
			return new ProcessResult(batch.records().size() == BATCH_SIZE);
		}catch(Exception putMultiException){
			logger.warn("", putMultiException);
			ConveyorCounters.inc(conveyor, "putMulti exception", 1);
			// try it again
			return new ProcessResult(true);
		}
	}

	@Override
	public boolean shouldRunOnShutdown(){
		return true;
	}

}

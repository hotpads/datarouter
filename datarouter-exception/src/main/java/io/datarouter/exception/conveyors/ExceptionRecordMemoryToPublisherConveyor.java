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

import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.conveyor.BaseConveyor;
import io.datarouter.conveyor.ConveyorCounters;
import io.datarouter.conveyor.MemoryBuffer;
import io.datarouter.exception.storage.exceptionrecord.ExceptionRecord;
import io.datarouter.instrumentation.exception.DatarouterExceptionPublisher;
import io.datarouter.instrumentation.exception.ExceptionRecordBatchDto;
import io.datarouter.scanner.Scanner;
import io.datarouter.web.exception.ExceptionRecorder;

public class ExceptionRecordMemoryToPublisherConveyor extends BaseConveyor{
	private static final Logger logger = LoggerFactory.getLogger(ExceptionRecordMemoryToPublisherConveyor.class);

	//this only controls max buffer poll size. the publisher will split as necessary.
	private static final int BATCH_SIZE = 10_000;

	private final MemoryBuffer<ExceptionRecord> buffer;
	private final DatarouterExceptionPublisher exceptionRecordPublisher;

	public ExceptionRecordMemoryToPublisherConveyor(
			String name,
			Supplier<Boolean> shouldRun,
			MemoryBuffer<ExceptionRecord> buffer,
			ExceptionRecorder exceptionRecorder,
			DatarouterExceptionPublisher exceptionRecordPublisher){
		super(name, shouldRun, () -> false, exceptionRecorder);
		this.buffer = buffer;
		this.exceptionRecordPublisher = exceptionRecordPublisher;
	}

	@Override
	public ProcessBatchResult processBatch(){
		ExceptionRecordBatchDto batch = Scanner.of(buffer.pollMultiWithLimit(BATCH_SIZE))
				.map(ExceptionRecord::toDto)
				.listTo(ExceptionRecordBatchDto::new);
		if(batch.records.isEmpty()){
			return new ProcessBatchResult(false);
		}
		try{
			exceptionRecordPublisher.addExceptionRecord(batch);
			ConveyorCounters.incPutMultiOpAndDatabeans(this, batch.records.size());
		}catch(Exception putMultiException){
			logger.warn("", putMultiException);
			ConveyorCounters.inc(this, "putMulti exception", 1);
		}
		//process as many as possible if shutting down
		return new ProcessBatchResult(isShuttingDown());
	}

	@Override
	public boolean shouldRunOnShutdown(){
		return true;
	}

}

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

import java.util.List;

import io.datarouter.conveyor.Conveyor.ProcessResult;
import io.datarouter.conveyor.ConveyorConfiguration;
import io.datarouter.conveyor.ConveyorRunnable;
import io.datarouter.instrumentation.exception.DatarouterDebuggingRecordPublisher;
import io.datarouter.instrumentation.exception.DatarouterDebuggingRecordPublisher.DebuggingRecordBatchDto;
import io.datarouter.instrumentation.exception.ExceptionRecordDto;
import io.datarouter.instrumentation.exception.HttpRequestRecordDto;
import io.datarouter.instrumentation.exception.TaskExecutorRecordDto;
import io.datarouter.instrumentation.trace.TraceBundleDto;
import io.datarouter.trace.conveyor.DatarouterDebuggingBuffers;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterDebuggingRecordConveyorConfiguration implements ConveyorConfiguration{

	private static final int BATCH_SIZE = 1_000;

	@Inject
	private DatarouterDebuggingBuffers buffers;
	@Inject
	private DatarouterDebuggingRecordPublisher debuggingPublisher;

	@Override
	public ProcessResult process(ConveyorRunnable conveyor){
		List<ExceptionRecordDto> exceptions = buffers.exceptions.pollMultiWithLimit(
				BATCH_SIZE);
		List<HttpRequestRecordDto> requests = buffers.httpRequests.pollMultiWithLimit(
				BATCH_SIZE);
		List<TraceBundleDto> traces = buffers.traces.pollMultiWithLimit(BATCH_SIZE);
		List<TaskExecutorRecordDto> executorRecords = buffers.taskExecutors.pollMultiWithLimit(BATCH_SIZE);
		if(exceptions.isEmpty() && requests.isEmpty() && traces.isEmpty() && executorRecords.isEmpty()){
			return new ProcessResult(false);
		}
		var batch = new DebuggingRecordBatchDto(exceptions, requests, traces, executorRecords);
		debuggingPublisher.addBatch(batch);
		return new ProcessResult(true);
	}

	@Override
	public boolean shouldRunOnShutdown(){
		return true;
	}

}

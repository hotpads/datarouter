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

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.instrumentation.exception.ExceptionRecordDto;
import io.datarouter.instrumentation.exception.HttpRequestRecordDto;
import io.datarouter.instrumentation.exception.TaskExecutorRecordDto;
import io.datarouter.instrumentation.trace.TraceBundleDto;
import io.datarouter.util.buffer.MemoryBuffer;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterDebuggingBuffers{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterDebuggingBuffers.class);

	private static final int MAX_SIZE = 20_000;
	private static final int MAX_TRACES = 2_000;

	public final MemoryBuffer<ExceptionRecordDto> exceptions;
	public final MemoryBuffer<HttpRequestRecordDto> httpRequests;
	public final MemoryBuffer<TraceBundleDto> traces;
	public final MemoryBuffer<TaskExecutorRecordDto> taskExecutors;

	public DatarouterDebuggingBuffers(){
		this.exceptions = new MemoryBuffer<>("exceptions", MAX_SIZE);
		this.httpRequests = new MemoryBuffer<>("httpRequests", MAX_SIZE);
		this.traces = new MemoryBuffer<>("traces", MAX_TRACES);
		this.taskExecutors = new MemoryBuffer<>("taskExecutor", MAX_TRACES);
	}

	public Optional<String> offerTraces(TraceBundleDto dto){
		if(!traces.offer(dto)){
			logger.warn("error offering trace entity buffer={} traceparent={}",
					traces.getName(),
					dto.traceDto.traceparent);
			return Optional.empty();
		}
		return Optional.of(traces.getName());

	}

}

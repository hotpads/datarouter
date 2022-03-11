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
package io.datarouter.trace.conveyor.publisher;

import java.util.function.Supplier;

import com.google.gson.Gson;

import io.datarouter.conveyor.message.ConveyorMessage;
import io.datarouter.conveyor.message.ConveyorMessageKey;
import io.datarouter.conveyor.queue.GroupQueueConsumer;
import io.datarouter.instrumentation.response.PublishingResponseDto;
import io.datarouter.instrumentation.trace.Trace2BatchedBundleDto;
import io.datarouter.instrumentation.trace.TracePublisher;
import io.datarouter.trace.conveyor.BaseTrace2SqsDrainConveyor;
import io.datarouter.web.exception.ExceptionRecorder;

public class Trace2ForPublisherSqsDrainConveyor extends BaseTrace2SqsDrainConveyor{

	private final TracePublisher tracePublisher;

	public Trace2ForPublisherSqsDrainConveyor(
			String name,
			Supplier<Boolean> shouldRun,
			GroupQueueConsumer<ConveyorMessageKey,ConveyorMessage> groupQueueConsumer,
			Gson gson,
			TracePublisher tracePublisher,
			Supplier<Boolean> compactExceptionLogging,
			ExceptionRecorder exceptionRecorder){
		super(name, shouldRun, groupQueueConsumer, compactExceptionLogging, gson, exceptionRecorder);
		this.tracePublisher = tracePublisher;
	}

	@Override
	public void persistData(Trace2BatchedBundleDto batchDto){
		PublishingResponseDto response = tracePublisher.addBatch(batchDto);
		if(response.success == null || !response.success){
			throw new RuntimeException("failed to publish response=" + response.message);
		}
	}

}

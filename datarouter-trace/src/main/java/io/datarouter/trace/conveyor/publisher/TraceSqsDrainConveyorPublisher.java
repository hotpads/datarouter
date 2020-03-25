/**
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

import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;

import com.google.gson.Gson;

import io.datarouter.conveyor.message.ConveyorMessage;
import io.datarouter.conveyor.message.ConveyorMessageKey;
import io.datarouter.conveyor.queue.BaseGroupQueueConsumerConveyor;
import io.datarouter.conveyor.queue.GroupQueueConsumer;
import io.datarouter.instrumentation.response.PublishingResponseDto;
import io.datarouter.instrumentation.trace.TraceEntityBatchDto;
import io.datarouter.instrumentation.trace.TraceEntityDto;
import io.datarouter.instrumentation.trace.TracePublisher;
import io.datarouter.scanner.Scanner;

public class TraceSqsDrainConveyorPublisher extends BaseGroupQueueConsumerConveyor<ConveyorMessageKey,ConveyorMessage>{

	private final Gson gson;
	private final TracePublisher tracePublisher;

	public TraceSqsDrainConveyorPublisher(
			String name,
			Supplier<Boolean> shouldRun,
			GroupQueueConsumer<ConveyorMessageKey,ConveyorMessage> groupQueueConsumer,
			Gson gson,
			TracePublisher tracePublisher,
			Supplier<Boolean> compactExceptionLogging){
		super(name, shouldRun, groupQueueConsumer, compactExceptionLogging, Duration.ofSeconds(30));
		this.gson = gson;
		this.tracePublisher = tracePublisher;
	}

	@Override
	protected void processDatabeans(List<ConveyorMessage> databeans){
		TraceEntityBatchDto dto = Scanner.of(databeans)
				.map(ConveyorMessage::getMessage)
				.map(message -> gson.fromJson(message, TraceEntityDto.class))
				.listTo(TraceEntityBatchDto::new);
		PublishingResponseDto response = tracePublisher.add(dto);
		if(response.success == null || !response.success){
			throw new RuntimeException("failed to publish response=" + response.message);
		}
	}

}

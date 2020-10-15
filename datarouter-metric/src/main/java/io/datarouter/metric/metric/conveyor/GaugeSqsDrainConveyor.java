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
package io.datarouter.metric.metric.conveyor;

import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import io.datarouter.conveyor.message.ConveyorMessage;
import io.datarouter.conveyor.message.ConveyorMessageKey;
import io.datarouter.conveyor.queue.BaseGroupQueueConsumerConveyor;
import io.datarouter.conveyor.queue.GroupQueueConsumer;
import io.datarouter.instrumentation.gauge.GaugeBatchDto;
import io.datarouter.instrumentation.gauge.GaugeDto;
import io.datarouter.instrumentation.gauge.GaugePublisher;
import io.datarouter.instrumentation.response.PublishingResponseDto;
import io.datarouter.scanner.Scanner;
import io.datarouter.web.exception.ExceptionRecorder;

public class GaugeSqsDrainConveyor extends BaseGroupQueueConsumerConveyor<ConveyorMessageKey,ConveyorMessage>{
	private static final Logger logger = LoggerFactory.getLogger(GaugeSqsDrainConveyor.class);

	private final GaugePublisher publisher;
	private final Gson gson;

	public GaugeSqsDrainConveyor(
			String name,
			Supplier<Boolean> shouldRun,
			GroupQueueConsumer<ConveyorMessageKey,ConveyorMessage> groupQueueConsumer,
			Gson gson,
			GaugePublisher publisher,
			Supplier<Boolean> compactExceptionLogging,
			ExceptionRecorder exceptionRecorder){
		super(name, shouldRun, groupQueueConsumer, compactExceptionLogging, Duration.ofSeconds(30), exceptionRecorder);
		this.gson = gson;
		this.publisher = publisher;
	}

	@Override
	protected void processDatabeans(List<ConveyorMessage> databeans){
		GaugeBatchDto dto = Scanner.of(databeans)
				.map(ConveyorMessage::getMessage)
				.map(message -> gson.fromJson(message, GaugeDto.class))
				.listTo(GaugeBatchDto::new);
		PublishingResponseDto response = publisher.add(dto);
		if(response.message.equals(PublishingResponseDto.DISCARD_MESSAGE)){
			logger.info("The message was accepted but discarded");
		}
		if(response.success == null || !response.success){
			throw new RuntimeException("failed to publish response=" + response.message);
		}
	}

}

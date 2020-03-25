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
package io.datarouter.metric.counter.conveyor;

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
import io.datarouter.instrumentation.count.CountBatchDto;
import io.datarouter.instrumentation.count.CountDto;
import io.datarouter.instrumentation.count.CountPublisher;
import io.datarouter.instrumentation.response.PublishingResponseDto;
import io.datarouter.scanner.Scanner;

public class CountSqsDrainConveyor extends BaseGroupQueueConsumerConveyor<ConveyorMessageKey,ConveyorMessage>{
	private static final Logger logger = LoggerFactory.getLogger(CountSqsDrainConveyor.class);

	private final CountPublisher publisher;
	private final Gson gson;

	public CountSqsDrainConveyor(
			String name,
			Supplier<Boolean> shouldRunSetting,
			GroupQueueConsumer<ConveyorMessageKey,ConveyorMessage> groupQueueConsumer,
			Gson gson,
			CountPublisher publisher,
			Supplier<Boolean> compactExceptionLogging){
		super(name, shouldRunSetting, groupQueueConsumer, compactExceptionLogging, Duration.ofSeconds(30));
		this.gson = gson;
		this.publisher = publisher;
	}

	@Override
	protected void processDatabeans(List<ConveyorMessage> databeans){
		CountBatchDto dto = Scanner.of(databeans)
				.map(ConveyorMessage::getMessage)
				.map(message -> gson.fromJson(message, CountDto.class))
				.listTo(CountBatchDto::new);
		PublishingResponseDto response = publisher.add(dto);
		if(response.message.equals(PublishingResponseDto.DISCARD_MESSAGE)){
			logger.info("The message was accepted but discarded");
		}
		if(response.success == null || !response.success){
			throw new RuntimeException("failed to publish response=" + response.message);
		}
	}

}

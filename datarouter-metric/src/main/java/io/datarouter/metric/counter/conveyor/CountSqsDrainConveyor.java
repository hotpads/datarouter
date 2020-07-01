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

import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import io.datarouter.conveyor.message.ConveyorMessage;
import io.datarouter.conveyor.message.ConveyorMessageKey;
import io.datarouter.conveyor.queue.BaseQueueConsumerConveyor;
import io.datarouter.conveyor.queue.QueueConsumer;
import io.datarouter.instrumentation.count.CountPublisher;
import io.datarouter.instrumentation.count.CountBatchDto;
import io.datarouter.instrumentation.response.PublishingResponseDto;

public class CountSqsDrainConveyor extends BaseQueueConsumerConveyor<ConveyorMessageKey,ConveyorMessage>{
	private static final Logger logger = LoggerFactory.getLogger(CountSqsDrainConveyor.class);

	private final CountPublisher publisher;
	private final Gson gson;

	public CountSqsDrainConveyor(
			String name,
			Supplier<Boolean> shouldRun,
			QueueConsumer<ConveyorMessageKey,ConveyorMessage> queueConsumer,
			Gson gson,
			CountPublisher publisher){
		super(name, shouldRun, queueConsumer);
		this.gson = gson;
		this.publisher = publisher;
	}

	@Override
	protected void processOne(ConveyorMessage databean){
		CountBatchDto dto = gson.fromJson(databean.getMessage(), CountBatchDto.class);
		PublishingResponseDto response = publisher.add(dto);
		if(response.message.equals(PublishingResponseDto.DISCARD_MESSAGE)){
			logger.info("The message was accepted but discarded");
		}
		if(response.success == null || !response.success){
			throw new RuntimeException("failed to publish response=" + response.message);
		}
	}

}

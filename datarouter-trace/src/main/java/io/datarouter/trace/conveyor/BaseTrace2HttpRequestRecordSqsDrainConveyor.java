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
package io.datarouter.trace.conveyor;

import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;

import com.google.gson.Gson;

import io.datarouter.conveyor.message.ConveyorMessage;
import io.datarouter.conveyor.message.ConveyorMessageKey;
import io.datarouter.conveyor.queue.BaseGroupQueueConsumerConveyor;
import io.datarouter.conveyor.queue.GroupQueueConsumer;
import io.datarouter.instrumentation.exception.HttpRequestRecordBatchDto;
import io.datarouter.instrumentation.exception.HttpRequestRecordDto;
import io.datarouter.scanner.Scanner;
import io.datarouter.web.exception.ExceptionRecorder;

public abstract class BaseTrace2HttpRequestRecordSqsDrainConveyor
extends BaseGroupQueueConsumerConveyor<ConveyorMessageKey,ConveyorMessage>{

	private final Gson gson;

	public BaseTrace2HttpRequestRecordSqsDrainConveyor(String name,
			Supplier<Boolean> shouldRun,
			GroupQueueConsumer<ConveyorMessageKey,ConveyorMessage> consumer,
			Supplier<Boolean> compactExceptionLogging,
			Gson gson,
			ExceptionRecorder exceptionRecorder){
		super(name, shouldRun, consumer, compactExceptionLogging, Duration.ofSeconds(30), exceptionRecorder);
		this.gson = gson;
	}

	@Override
	protected void processDatabeans(List<ConveyorMessage> databeans){
		HttpRequestRecordBatchDto dto = Scanner.of(databeans)
				.map(ConveyorMessage::getMessage)
				.map(message -> gson.fromJson(message, HttpRequestRecordDto.class))
				.listTo(HttpRequestRecordBatchDto::new);
		persistData(dto);
	}

	public abstract void persistData(HttpRequestRecordBatchDto batchDto);

}

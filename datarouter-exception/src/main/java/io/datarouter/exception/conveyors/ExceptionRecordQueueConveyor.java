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
package io.datarouter.exception.conveyors;

import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.conveyor.queue.BaseGroupQueueConsumerConveyor;
import io.datarouter.conveyor.queue.GroupQueueConsumer;
import io.datarouter.exception.storage.exceptionrecord.ExceptionRecord;
import io.datarouter.exception.storage.exceptionrecord.ExceptionRecordKey;
import io.datarouter.instrumentation.exception.ExceptionRecordBatchDto;
import io.datarouter.instrumentation.exception.ExceptionRecordDto;
import io.datarouter.instrumentation.exception.ExceptionRecordPublisher;
import io.datarouter.instrumentation.response.PublishingResponseDto;
import io.datarouter.util.iterable.IterableTool;

@Singleton
public class ExceptionRecordQueueConveyor extends BaseGroupQueueConsumerConveyor<ExceptionRecordKey,ExceptionRecord>{
	private static final Logger logger = LoggerFactory.getLogger(ExceptionRecordQueueConveyor.class);

	private final ExceptionRecordPublisher publisher;

	@Inject
	public ExceptionRecordQueueConveyor(
			String name,
			Supplier<Boolean> shouldRun,
			GroupQueueConsumer<ExceptionRecordKey,ExceptionRecord> consumer,
			ExceptionRecordPublisher publisher,
			Supplier<Boolean> compactExceptionLogging){
		super(name, shouldRun, consumer, compactExceptionLogging, Duration.ofSeconds(30));
		this.publisher = publisher;
	}

	@Override
	protected void processDatabeans(List<ExceptionRecord> records){
		List<ExceptionRecordDto> exceptionRecordDtos = IterableTool.map(records, ExceptionRecord::toDto);
		ExceptionRecordBatchDto batch = new ExceptionRecordBatchDto(exceptionRecordDtos);
		PublishingResponseDto response = publisher.addExceptionRecord(batch);
		if(response.message.equals(PublishingResponseDto.DISCARD_MESSAGE)){
			logger.info("The message was accepted but discarded.");
		}
		if(response.success == null || !response.success){
			throw new RuntimeException("failed to publish response=" + response.message);
		}
	}

}

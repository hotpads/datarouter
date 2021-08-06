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

import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.conveyor.queue.BaseGroupQueueConsumerConveyor;
import io.datarouter.conveyor.queue.GroupQueueConsumer;
import io.datarouter.exception.storage.httprecord.HttpRequestRecord;
import io.datarouter.exception.storage.httprecord.HttpRequestRecordKey;
import io.datarouter.instrumentation.exception.ExceptionRecordPublisher;
import io.datarouter.instrumentation.exception.HttpRequestRecordBatchDto;
import io.datarouter.instrumentation.response.PublishingResponseDto;
import io.datarouter.scanner.Scanner;
import io.datarouter.web.exception.ExceptionRecorder;

@Singleton
public class HttpRequestRecordQueueConveyor
extends BaseGroupQueueConsumerConveyor<HttpRequestRecordKey,HttpRequestRecord>{
	private static final Logger logger = LoggerFactory.getLogger(HttpRequestRecordQueueConveyor.class);

	private final ExceptionRecordPublisher publisher;

	public HttpRequestRecordQueueConveyor(
			String name,
			Supplier<Boolean> shouldRun,
			GroupQueueConsumer<HttpRequestRecordKey,HttpRequestRecord> consumer,
			ExceptionRecordPublisher publisher,
			Supplier<Boolean> compactExceptionLogging,
			ExceptionRecorder exceptionRecorder){
		super(name, shouldRun, consumer, compactExceptionLogging, Duration.ofSeconds(30), exceptionRecorder);
		this.publisher = publisher;
	}

	@Override
	protected void processDatabeans(List<HttpRequestRecord> dtos){
		HttpRequestRecordBatchDto batch = Scanner.of(dtos)
				.map(HttpRequestRecord::toDto)
				.listTo(HttpRequestRecordBatchDto::new);
		PublishingResponseDto response = publisher.addHttpRequest(batch);
		if(response.message.equals(PublishingResponseDto.DISCARD_MESSAGE)){
			logger.warn("The message was accepted but discarded.");
		}
		if(response.success == null || !response.success){
			throw new RuntimeException("failed to publish response=" + response.message);
		}
	}

}

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
package io.datarouter.conveyor.queue;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.bytes.Codec;
import io.datarouter.conveyor.BaseConveyor;
import io.datarouter.conveyor.ConveyorCounters;
import io.datarouter.storage.queue.BlobQueueMessageDto;
import io.datarouter.web.exception.ExceptionRecorder;

public abstract class BaseBlobQueueConsumerConveyor<T>
extends BaseConveyor{
	private static final Logger logger = LoggerFactory.getLogger(BaseBlobQueueConsumerConveyor.class);

	private static final Duration PEEK_TIMEOUT = Duration.ofSeconds(30);
	private static final Duration VISIBILITY_TIMEOUT = Duration.ofSeconds(30);

	private final BlobQueueConsumer queueConsumer;
	private final Codec<T,byte[]> codec;

	protected BaseBlobQueueConsumerConveyor(
			String name,
			Supplier<Boolean> shouldRun,
			ExceptionRecorder exceptionRecorder,
			BlobQueueConsumer queueConsumer,
			Codec<T,byte[]> codec){
		super(name, shouldRun, () -> false, exceptionRecorder);
		this.queueConsumer = queueConsumer;
		this.codec = codec;
	}

	@Override
	public ProcessBatchResult processBatch(){
		Duration visibilityTimeout = getVisibilityTimeout();
		Optional<BlobQueueMessageDto> optionalMessage = queueConsumer.peek(PEEK_TIMEOUT, visibilityTimeout);
		if(optionalMessage.isEmpty()){
			logger.info("peeked conveyor={} nullMessage", name);
			return new ProcessBatchResult(false);
		}
		BlobQueueMessageDto messageDto = optionalMessage.get();
		logger.info("peeked conveyor={} messageCount={}", name, 1);
		T data = codec.decode(messageDto.getData());
		long start = System.currentTimeMillis();
		try{
			if(!processOneShouldAck(data)){
				return new ProcessBatchResult(true);
			}
		}catch(Exception e){
			throw new RuntimeException("failed to process message", e);
		}
		long durationMs = System.currentTimeMillis() - start;
		if(durationMs > visibilityTimeout.toMillis()){
			logger.warn("slow conveyor conveyor={} durationMs={}", name, durationMs);
		}
		logger.info("consumed conveyor={} messageCount={}", name, 1);
		ConveyorCounters.incConsumedOpAndDatabeans(this, 1);
		queueConsumer.ack(messageDto);
		logger.info("acked conveyor={} messageCount={}", name, 1);
		ConveyorCounters.incAck(this);
		return new ProcessBatchResult(true);
	}

	protected Duration getVisibilityTimeout(){
		return VISIBILITY_TIMEOUT;
	}

	protected boolean processOneShouldAck(T data){
		processOne(data);
		return true;
	}

	protected void processOne(@SuppressWarnings("unused") T data){
		throw new UnsupportedOperationException();
	}

}

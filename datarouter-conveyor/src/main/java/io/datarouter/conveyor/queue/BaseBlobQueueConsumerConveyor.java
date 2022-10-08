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
import java.time.Instant;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.conveyor.BaseConveyor;
import io.datarouter.conveyor.ConveyorCounters;
import io.datarouter.conveyor.ConveyorGaugeRecorder;
import io.datarouter.scanner.Scanner;
import io.datarouter.web.exception.ExceptionRecorder;

public abstract class BaseBlobQueueConsumerConveyor<T>
extends BaseConveyor{
	private static final Logger logger = LoggerFactory.getLogger(BaseBlobQueueConsumerConveyor.class);

	private static final Duration PEEK_TIMEOUT = Duration.ofSeconds(30);
	private static final Duration VISIBILITY_TIMEOUT = Duration.ofSeconds(30);

	private final BlobQueueConsumer<T> queueConsumer;

	protected BaseBlobQueueConsumerConveyor(
			String name,
			Supplier<Boolean> shouldRun,
			ExceptionRecorder exceptionRecorder,
			BlobQueueConsumer<T> queueConsumer,
			ConveyorGaugeRecorder metricRecorder){
		super(name, shouldRun, () -> false, exceptionRecorder, metricRecorder);
		this.queueConsumer = queueConsumer;
	}

	@Override
	public ProcessBatchResult processBatch(){
		Duration visibilityTimeout = getVisibilityTimeout();
		Instant beforePeek = Instant.now();
		var optionalMessage = queueConsumer.peek(PEEK_TIMEOUT, visibilityTimeout);
		Instant afterPeek = Instant.now();
		gaugeRecorder.savePeekDurationMs(this, Duration.between(beforePeek, afterPeek).toMillis());
		if(optionalMessage.isEmpty()){
			logger.info("peeked conveyor={} nullMessage", name);
			return new ProcessBatchResult(false);
		}
		var messageDto = optionalMessage.get();
		logger.info("peeked conveyor={} messageCount={}", name, 1);
		Instant beforeProcessBuffer = Instant.now();
		try{
			if(!processOneShouldAck(messageDto.scanSplitDecodedData())){
				return new ProcessBatchResult(true);
			}
		}catch(Exception e){
			throw new RuntimeException("failed to process message", e);
		}
		Instant afterProcessBuffer = Instant.now();
		gaugeRecorder.saveProcessBufferDurationMs(this, Duration.between(beforeProcessBuffer, afterProcessBuffer)
				.toMillis());
		if(Duration.between(beforeProcessBuffer, afterProcessBuffer)
				.toMillis() > visibilityTimeout.toMillis()){
			logger.warn("slow conveyor conveyor={} durationMs={}", name,
					Duration.between(beforeProcessBuffer, afterProcessBuffer).toMillis());
		}
		logger.info("consumed conveyor={} messageCount={}", name, 1);
		ConveyorCounters.incConsumedOpAndDatabeans(this, 1);
		Instant beforeAck = Instant.now();
		queueConsumer.ack(messageDto);
		Instant afterAck = Instant.now();
		gaugeRecorder.saveAckDurationMs(this, Duration.between(beforeAck, afterAck).toMillis());
		logger.info("acked conveyor={} messageCount={}", name, 1);
		ConveyorCounters.incAck(this);
		return new ProcessBatchResult(true);
	}

	protected Duration getVisibilityTimeout(){
		return VISIBILITY_TIMEOUT;
	}

	protected boolean processOneShouldAck(Scanner<T> data){
		processOne(data);
		return true;
	}

	protected abstract void processOne(Scanner<T> data);

}
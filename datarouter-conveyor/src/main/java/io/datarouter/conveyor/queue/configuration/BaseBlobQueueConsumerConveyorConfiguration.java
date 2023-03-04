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
package io.datarouter.conveyor.queue.configuration;

import java.time.Duration;
import java.time.Instant;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.conveyor.Conveyor.ProcessResult;
import io.datarouter.conveyor.ConveyorConfiguration;
import io.datarouter.conveyor.ConveyorCounters;
import io.datarouter.conveyor.ConveyorGauges;
import io.datarouter.conveyor.ConveyorRunnable;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.queue.consumer.BlobQueueConsumer;

public abstract class BaseBlobQueueConsumerConveyorConfiguration<T>
implements ConveyorConfiguration{
	private static final Logger logger = LoggerFactory.getLogger(BaseBlobQueueConsumerConveyorConfiguration.class);

	@Inject
	private ConveyorGauges gaugeRecorder;

	protected abstract BlobQueueConsumer<T> getQueueConsumer();
	protected abstract void processOne(Scanner<T> data);

	@Override
	public ProcessResult process(ConveyorRunnable conveyor){
		Instant beforePeek = Instant.now();
		var optionalMessage = getQueueConsumer().peek(DEFAULT_PEEK_TIMEOUT, DEFAULT_VISIBILITY_TIMEOUT);
		Instant afterPeek = Instant.now();
		gaugeRecorder.savePeekDurationMs(conveyor, Duration.between(beforePeek, afterPeek).toMillis());
		if(optionalMessage.isEmpty()){
			logger.info("peeked conveyor={} nullMessage", conveyor.getName());
			return new ProcessResult(false);
		}
		var messageDto = optionalMessage.get();
		logger.info("peeked conveyor={} messageCount={}", conveyor.getName(), 1);
		Instant beforeProcessBuffer = Instant.now();
		try{
			processOne(messageDto.scanSplitDecodedData());
			if(!shouldAck()){
				return new ProcessResult(true);
			}
		}catch(Exception e){
			throw new RuntimeException("failed to process message", e);
		}
		Instant afterProcessBuffer = Instant.now();
		gaugeRecorder.saveProcessBufferDurationMs(conveyor, Duration.between(beforeProcessBuffer, afterProcessBuffer)
				.toMillis());
		if(Duration.between(beforeProcessBuffer, afterProcessBuffer).toMillis() > DEFAULT_VISIBILITY_TIMEOUT
				.toMillis()){
			logger.warn("slow conveyor conveyor={} durationMs={}", conveyor.getName(), Duration.between(
					beforeProcessBuffer, afterProcessBuffer).toMillis());
		}
		logger.info("consumed conveyor={} messageCount={}", conveyor.getName(), 1);
		ConveyorCounters.incConsumedOpAndDatabeans(conveyor, 1);
		Instant beforeAck = Instant.now();
		getQueueConsumer().ack(messageDto);
		Instant afterAck = Instant.now();
		gaugeRecorder.saveAckDurationMs(conveyor, Duration.between(beforeAck, afterAck).toMillis());
		logger.info("acked conveyor={} messageCount={}", conveyor.getName(), 1);
		ConveyorCounters.incAck(conveyor);
		return new ProcessResult(true);
	}

	protected boolean shouldAck(){
		return true;
	}

}
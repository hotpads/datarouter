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
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.conveyor.Conveyor.ProcessResult;
import io.datarouter.conveyor.ConveyorConfiguration;
import io.datarouter.conveyor.ConveyorCounters;
import io.datarouter.conveyor.ConveyorGauges;
import io.datarouter.conveyor.ConveyorRunnable;
import io.datarouter.instrumentation.trace.TracerTool;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.queue.BlobQueueMessage;
import io.datarouter.storage.queue.consumer.BlobQueueConsumer;
import jakarta.inject.Inject;

public abstract class BaseBlobQueueConsumerConveyorConfiguration<T>
implements ConveyorConfiguration{
	private static final Logger logger = LoggerFactory.getLogger(BaseBlobQueueConsumerConveyorConfiguration.class);

	@Inject
	private ConveyorGauges gaugeRecorder;

	protected abstract BlobQueueConsumer<T> getQueueConsumer();
	protected abstract void processOne(Scanner<T> data);

	@Override
	public ProcessResult process(ConveyorRunnable conveyorRunnable){
		Optional<BlobQueueMessage<T>> optMessage = peek(conveyorRunnable);
		TracerTool.setAlternativeStartTime();
		if(optMessage.isEmpty()){
			logger.info("peeked conveyor={} nullMessage", conveyorRunnable.getName());
			return new ProcessResult(false);
		}
		BlobQueueMessage<T> message = optMessage.get();
		logger.info("peeked conveyor={} messageCount={}", conveyorRunnable.getName(), 1);
		Instant beforeProcessBuffer = Instant.now();
		try{
			processOne(message.scanSplitDecodedData());
			if(!shouldAck()){
				return new ProcessResult(true);
			}
		}catch(Exception e){
			throw new RuntimeException("failed to process message", e);
		}
		Duration processDuration = Duration.between(beforeProcessBuffer, Instant.now());
		trackProcess(conveyorRunnable, processDuration);
		ack(conveyorRunnable, message);
		return new ProcessResult(true);
	}

	protected boolean shouldAck(){
		return true;
	}

	private Optional<BlobQueueMessage<T>> peek(ConveyorRunnable conveyorRunnable){
		Instant beforePeek = Instant.now();
		Optional<BlobQueueMessage<T>> optMessage = getQueueConsumer().peek(
				DEFAULT_PEEK_TIMEOUT,
				DEFAULT_VISIBILITY_TIMEOUT);
		Duration peekDuration = Duration.between(beforePeek, Instant.now());
		gaugeRecorder.savePeekDurationMs(conveyorRunnable, peekDuration.toMillis());
		return optMessage;
	}

	private void trackProcess(ConveyorRunnable conveyorRunnable, Duration processDuration){
		gaugeRecorder.saveProcessBufferDurationMs(
				conveyorRunnable,
				processDuration.toMillis());
		if(processDuration.toMillis()
				> DEFAULT_VISIBILITY_TIMEOUT.toMillis()){
			logger.warn(
					"slow conveyor conveyor={} durationMs={}",
					conveyorRunnable.getName(),
					processDuration.toMillis());
		}
		logger.info("consumed conveyor={} messageCount={}", conveyorRunnable.getName(), 1);
		ConveyorCounters.incConsumedOpAndDatabeans(conveyorRunnable, 1);
	}

	private void ack(ConveyorRunnable conveyorRunnable, BlobQueueMessage<T> message){
		Instant beforeAck = Instant.now();
		getQueueConsumer().ack(message);
		Instant afterAck = Instant.now();
		gaugeRecorder.saveAckDurationMs(conveyorRunnable, Duration.between(beforeAck, afterAck).toMillis());
		logger.info("acked conveyor={} messageCount={}", conveyorRunnable.getName(), 1);
		ConveyorCounters.incAck(conveyorRunnable);
	}

}
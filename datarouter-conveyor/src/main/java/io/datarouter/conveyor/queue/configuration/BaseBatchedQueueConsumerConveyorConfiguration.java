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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.conveyor.Conveyor.ProcessResult;
import io.datarouter.conveyor.ConveyorConfiguration;
import io.datarouter.conveyor.ConveyorCounters;
import io.datarouter.conveyor.ConveyorGauges;
import io.datarouter.conveyor.ConveyorRunnable;
import io.datarouter.instrumentation.trace.TracerTool;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.queue.QueueMessage;
import io.datarouter.storage.queue.QueueMessageKey;
import io.datarouter.storage.queue.consumer.BatchedAckQueueConsumer;
import jakarta.inject.Inject;

/**
 * Drains the queue and stores the data in a buffer. When the buffer hits a set limit it will trigger the processing as
 * a batch. This implementation makes the best effort to process the buffer if the queue gets drained or it has been
 * interrupted.
 */
public abstract class BaseBatchedQueueConsumerConveyorConfiguration<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>>
implements ConveyorConfiguration{
	private static final Logger logger = LoggerFactory.getLogger(BaseBatchedQueueConsumerConveyorConfiguration.class);

	private static final Duration BATCHED_QUEUE_PEEK_TIMEOUT = Duration.ofSeconds(10);
	private static final int BATCH_SIZE = 100;

	private final Object lock = new Object();
	private final List<MessageAndTime<PK,D>> buffer = new ArrayList<>(BATCH_SIZE);

	@Inject
	private ConveyorGauges gaugeRecorder;

	protected abstract void processBuffer(List<D> databeans);
	protected abstract BatchedAckQueueConsumer<PK,D> getQueueConsumer();

	@Override
	public ProcessResult process(ConveyorRunnable conveyor){
		List<MessageAndTime<PK,D>> currentBuffer = List.of();
		Instant beforePeek = Instant.now();
		QueueMessage<PK,D> message = getQueueConsumer().peek(BATCHED_QUEUE_PEEK_TIMEOUT, DEFAULT_VISIBILITY_TIMEOUT);
		Instant afterPeek = Instant.now();
		gaugeRecorder.savePeekDurationMs(conveyor, Duration.between(beforePeek, afterPeek).toMillis());
		TracerTool.setAlternativeStartTime();
		if(message == null){
			logger.info("peeked conveyor={} nullMessage", conveyor.getName());
			synchronized(lock){
				currentBuffer = copyAndClearBuffer();
			}
			flushBuffer(currentBuffer, Optional.of(afterPeek), conveyor);
			return new ProcessResult(false);
		}
		synchronized(lock){
			logger.info("peeked conveyor={} messageCount={}", conveyor.getName(), 1);
			buffer.add(new MessageAndTime<>(message.getKey(), message.getDatabean(), System.currentTimeMillis()));
			if(buffer.size() >= BATCH_SIZE){
				currentBuffer = copyAndClearBuffer();
			}
		}
		flushBuffer(currentBuffer, Optional.of(afterPeek), conveyor);
		return new ProcessResult(true);
	}

	private void flushBuffer(
			List<MessageAndTime<PK,D>> currentBuffer,
			Optional<Instant> afterPeek,
			ConveyorRunnable conveyor){
		if(currentBuffer.isEmpty()){
			return;
		}
		Instant beforeProcessBuffer = Instant.now();
		afterPeek.ifPresent(time -> gaugeRecorder.savePeekToProcessBufferDurationMs(
				conveyor,
				Duration.between(time, beforeProcessBuffer).toMillis()));
		Scanner.of(currentBuffer)
				.map(mat -> mat.message)
				.flush(this::processBuffer);
		Instant afterProcessBuffer = Instant.now();
		gaugeRecorder.saveProcessBufferDurationMs(
				conveyor,
				Duration.between(beforeProcessBuffer, afterProcessBuffer).toMillis());
		ConveyorCounters.incFlushBuffer(conveyor, currentBuffer.size());
		logger.info("consumed conveyor={} messageCount={}", conveyor.getName(), currentBuffer.size());
		ConveyorCounters.incConsumedOpAndDatabeans(conveyor, currentBuffer.size());
		Scanner.of(currentBuffer)
				.forEach(mat -> {
					long waitDurationMs = beforeProcessBuffer.toEpochMilli() - mat.peekTime;
					if(waitDurationMs > DEFAULT_VISIBILITY_TIMEOUT.toMillis()){
						logger.warn("slow conveyor conveyor={} waitDurationMs={} databean={}",
								conveyor.getName(),
								waitDurationMs,
								mat.message);
					}
				});

		Instant beforeAck = Instant.now();
		Scanner.of(currentBuffer)
				.map(mat -> mat.queueMessageKey)
				.flush(list -> getQueueConsumer().ackMulti(list.size(), list));
		Instant afterAck = Instant.now();
		gaugeRecorder.saveAckDurationMs(conveyor, Duration.between(beforeAck, afterAck).toMillis());
		logger.info("acked conveyor={} messageCount={}", conveyor.getName(), currentBuffer.size());
		ConveyorCounters.incAck(conveyor, currentBuffer.size());
	}

	private List<MessageAndTime<PK,D>> copyAndClearBuffer(){
		List<MessageAndTime<PK,D>> currentBuffer = new ArrayList<>(buffer);
		buffer.clear();
		return currentBuffer;
	}

	@Override
	public void interrupted(ConveyorRunnable conveyor) throws Exception{
		try{
			List<MessageAndTime<PK,D>> currentBuffer;
			synchronized(lock){
				currentBuffer = copyAndClearBuffer();
			}
			flushBuffer(currentBuffer, Optional.empty(), conveyor);
		}catch(Exception ex){
			throw new Exception("Exception processing buffer. bufferSize=" + buffer.size() + " bufferMessages=" + Arrays
					.toString(buffer.toArray()), ex);
		}
	}

	private record MessageAndTime<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>(
			QueueMessageKey queueMessageKey,
			D message,
			long peekTime){
	}

}

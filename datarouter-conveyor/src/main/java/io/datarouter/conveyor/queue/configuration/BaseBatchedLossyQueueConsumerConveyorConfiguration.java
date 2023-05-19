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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

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
import io.datarouter.storage.queue.consumer.BatchedQueueConsumer;

/**
 * Drains the queue and stores the data in a buffer. When the buffer hits a set limit it will trigger the processing as
 * a batch. This implementation makes the best effort to process the buffer if the queue gets drained or it has been
 * interrupted.
 *
 * <b>NOTE:</b> If the thread running the conveyor is killed or crashes before the processing of the buffer is
 * completed the messages are lost forever. Along side the conveyor there must be some backup implementation that
 * re-queues messages that are lost.
 *
 * @param <PK> Primary key
 * @param <D> Databean
 */
public abstract class BaseBatchedLossyQueueConsumerConveyorConfiguration<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>>
implements ConveyorConfiguration{
	private static final Logger logger = LoggerFactory.getLogger(
			BaseBatchedLossyQueueConsumerConveyorConfiguration.class);

	private final Object lock = new Object();
	private final List<QueueMessage<PK,D>> buffer = new LinkedList<>();

	@Inject
	private ConveyorGauges gaugeRecorder;

	protected abstract void processBuffer(List<D> databeans);
	protected abstract BatchedQueueConsumer<PK,D> getQueueConsumer();

	@Override
	public ProcessResult process(ConveyorRunnable conveyor){
		List<QueueMessage<PK,D>> currentBuffer = Collections.emptyList();
		Instant beforePeek = Instant.now();
		List<QueueMessage<PK,D>> messages = getQueueConsumer().peekMulti(getMaxQuerySize(), getPeekTimeout(),
				DEFAULT_VISIBILITY_TIMEOUT);
		Instant afterPeek = Instant.now();
		gaugeRecorder.savePeekDurationMs(conveyor, Duration.between(beforePeek, afterPeek).toMillis());
		TracerTool.setAlternativeStartTime();
		if(messages.isEmpty()){
			logger.info("peeked conveyor={} nullMessage", conveyor.getName());
			synchronized(lock){
				currentBuffer = copyAndClearBuffer();
			}
			flushBuffer(currentBuffer, Optional.of(afterPeek), conveyor);
			return new ProcessResult(false);
		}
		Instant beforeAck = Instant.now();
		Scanner.of(messages)
				.map(QueueMessage::getKey)
				.flush(list -> getQueueConsumer().ackMulti(list.size(), list));
		Instant afterAck = Instant.now();
		gaugeRecorder.saveAckDurationMs(conveyor, Duration.between(beforeAck, afterAck).toMillis());
		logger.info("acked conveyor={} messageCount={}", conveyor.getName(), messages.size());
		ConveyorCounters.incAck(conveyor);

		synchronized(lock){
			logger.info("peeked conveyor={} messageCount={}", conveyor.getName(), messages.size());
			buffer.addAll(messages);
			if(buffer.size() >= getBatchSize()){
				currentBuffer = copyAndClearBuffer();
			}
		}
		logger.info("consumed conveyor={} messageCount={}", conveyor.getName(), messages.size());
		ConveyorCounters.incConsumedOpAndDatabeans(conveyor, messages.size());
		flushBuffer(currentBuffer, Optional.of(afterPeek), conveyor);
		return new ProcessResult(true);
	}

	@Override
	public void interrupted(ConveyorRunnable conveyor){
		List<QueueMessage<PK,D>> currentBuffer;
		synchronized(lock){
			currentBuffer = copyAndClearBuffer();
		}
		flushBuffer(currentBuffer, Optional.empty(), conveyor);
	}

	private void flushBuffer(List<QueueMessage<PK,D>> currentBuffer, Optional<Instant> afterPeek,
			ConveyorRunnable conveyor){
		if(currentBuffer.isEmpty()){
			return;
		}
		Instant beforeProcessBuffer = Instant.now();
		afterPeek.ifPresent(time -> gaugeRecorder.savePeekToProcessBufferDurationMs(conveyor, Duration.between(time,
				beforeProcessBuffer).toMillis()));
		Scanner.of(currentBuffer)
				.map(QueueMessage::getDatabean)
				.flush(this::processBuffer);
		Instant afterProcessBuffer = Instant.now();
		gaugeRecorder.saveProcessBufferDurationMs(conveyor, Duration.between(beforeProcessBuffer, afterProcessBuffer)
				.toMillis());
		ConveyorCounters.incFlushBuffer(conveyor, currentBuffer.size());
	}

	private List<QueueMessage<PK,D>> copyAndClearBuffer(){
		List<QueueMessage<PK,D>> currentBuffer = new ArrayList<>(buffer);
		buffer.clear();
		return currentBuffer;
	}

	protected int getMaxQuerySize(){
		return 1;
	}

	protected Duration getPeekTimeout(){
		return DEFAULT_PEEK_TIMEOUT;
	}

	protected int getBatchSize(){
		return 100;
	}

}

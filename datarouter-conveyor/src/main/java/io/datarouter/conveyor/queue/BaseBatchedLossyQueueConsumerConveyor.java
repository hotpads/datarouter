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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.conveyor.BaseConveyor;
import io.datarouter.conveyor.ConveyorCounters;
import io.datarouter.conveyor.ConveyorGaugeRecorder;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.queue.QueueMessage;
import io.datarouter.web.exception.ExceptionRecorder;

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
public abstract class BaseBatchedLossyQueueConsumerConveyor<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>>
extends BaseConveyor{
	private static final Logger logger = LoggerFactory.getLogger(BaseBatchedLossyQueueConsumerConveyor.class);

	private static final Duration PEEK_TIMEOUT = Duration.ofSeconds(5);
	private static final Duration VISIBILITY_TIMEOUT = Duration.ofSeconds(30);
	private static final int BATCH_SIZE = 100;

	private final BatchedQueueConsumer<PK,D> queueConsumer;
	private final Object lock = new Object();
	private final List<QueueMessage<PK,D>> buffer;

	public BaseBatchedLossyQueueConsumerConveyor(
			String name,
			Supplier<Boolean> shouldRun,
			BatchedQueueConsumer<PK,D> queueConsumer,
			ExceptionRecorder exceptionRecorder,
			ConveyorGaugeRecorder metricRecorder){
		super(name, shouldRun, () -> false, exceptionRecorder, metricRecorder);
		this.queueConsumer = queueConsumer;
		this.buffer = new ArrayList<>(BATCH_SIZE);
	}

	@Override
	public ProcessBatchResult processBatch(){
		List<QueueMessage<PK,D>> currentBuffer = Collections.emptyList();
		Instant beforePeek = Instant.now();
		List<QueueMessage<PK,D>> messages = queueConsumer.peekMulti(BATCH_SIZE, PEEK_TIMEOUT, VISIBILITY_TIMEOUT);
		Instant afterPeek = Instant.now();
		gaugeRecorder.savePeekDurationMs(this, Duration.between(beforePeek, afterPeek).toMillis());
		if(messages.isEmpty()){
			logger.info("peeked conveyor={} nullMessage", name);
			synchronized(lock){
				currentBuffer = copyAndClearBuffer();
			}
			flushBuffer(currentBuffer, afterPeek);
			return new ProcessBatchResult(false);
		}

		Instant beforeAck = Instant.now();
		Scanner.of(messages)
				.map(QueueMessage::getKey)
				.flush(list -> queueConsumer.ackMulti(BATCH_SIZE, list));
		Instant afterAck = Instant.now();
		gaugeRecorder.saveAckDurationMs(this, Duration.between(beforeAck, afterAck).toMillis());
		logger.info("acked conveyor={} messageCount={}", name, messages.size());
		ConveyorCounters.incAck(this);

		synchronized(lock){
			logger.info("peeked conveyor={} messageCount={}", name, messages.size());
			buffer.addAll(messages);
			if(buffer.size() >= BATCH_SIZE){
				currentBuffer = copyAndClearBuffer();
			}
		}
		logger.info("consumed conveyor={} messageCount={}", name, messages.size());
		ConveyorCounters.incConsumedOpAndDatabeans(this, messages.size());
		flushBuffer(currentBuffer, afterPeek);
		return new ProcessBatchResult(true);
	}

	@Override
	public void interrupted(){
		List<QueueMessage<PK,D>> currentBuffer;
		synchronized(lock){
			currentBuffer = copyAndClearBuffer();
		}
		flushBuffer(currentBuffer, null);
	}

	private void flushBuffer(List<QueueMessage<PK,D>> currentBuffer, Instant afterPeek){
		if(currentBuffer.isEmpty()){
			return;
		}

		Instant beforeProcessBuffer = Instant.now();
		if(afterPeek != null){
			gaugeRecorder.savePeekToProcessBufferDurationMs(this, Duration.between(afterPeek, beforeProcessBuffer)
					.toMillis());
		}
		Scanner.of(currentBuffer)
				.map(QueueMessage::getDatabean)
				.flush(this::processBuffer);
		Instant afterProcessBuffer = Instant.now();
		gaugeRecorder.saveProcessBufferDurationMs(this, Duration.between(beforeProcessBuffer, afterProcessBuffer)
				.toMillis());
		ConveyorCounters.incFlushBuffer(this, currentBuffer.size());
	}

	private List<QueueMessage<PK,D>> copyAndClearBuffer(){
		List<QueueMessage<PK,D>> currentBuffer = buffer.stream()
				.collect(Collectors.toList());
		buffer.clear();
		return currentBuffer;
	}

	protected abstract void processBuffer(List<D> databeans);

}

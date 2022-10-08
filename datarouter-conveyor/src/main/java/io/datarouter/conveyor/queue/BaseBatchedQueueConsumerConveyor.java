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
import java.util.Arrays;
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
import io.datarouter.storage.queue.QueueMessageKey;
import io.datarouter.web.exception.ExceptionRecorder;

/**
 * Drains the queue and stores the data in a buffer. When the buffer hits a set limit it will trigger the processing as
 * a batch. This implementation makes the best effort to process the buffer if the queue gets drained or it has been
 * interrupted.
 */
public abstract class BaseBatchedQueueConsumerConveyor<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>>
extends BaseConveyor{
	private static final Logger logger = LoggerFactory.getLogger(BaseBatchedQueueConsumerConveyor.class);

	private static final Duration PEEK_TIMEOUT = Duration.ofSeconds(10);
	private static final Duration VISIBILITY_TIMEOUT = Duration.ofSeconds(30);
	private static final int BATCH_SIZE = 100;

	private final BatchedAckQueueConsumer<PK,D> queueConsumer;
	private final Object lock = new Object();
	private final List<MessageAndTime<PK,D>> buffer;

	private static class MessageAndTime<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>{
		private final QueueMessageKey queueMessageKey;
		private final D message;
		private final long peekTime;

		public MessageAndTime(QueueMessageKey queueMessageKey, D message, long peekTime){
			this.queueMessageKey = queueMessageKey;
			this.message = message;
			this.peekTime = peekTime;
		}
	}

	public BaseBatchedQueueConsumerConveyor(
			String name,
			Supplier<Boolean> shouldRun,
			BatchedAckQueueConsumer<PK,D> queueConsumer,
			ExceptionRecorder exceptionRecorder,
			ConveyorGaugeRecorder metricRecorder){
		super(name, shouldRun, () -> false, exceptionRecorder, metricRecorder);
		this.queueConsumer = queueConsumer;
		this.buffer = new ArrayList<>(BATCH_SIZE);
	}

	@Override
	public ProcessBatchResult processBatch(){
		List<MessageAndTime<PK,D>> currentBuffer = Collections.emptyList();
		Instant beforePeek = Instant.now();
		QueueMessage<PK,D> message = queueConsumer.peek(PEEK_TIMEOUT, VISIBILITY_TIMEOUT);
		Instant afterPeek = Instant.now();
		gaugeRecorder.savePeekDurationMs(this, Duration.between(beforePeek, afterPeek).toMillis());
		if(message == null){
			logger.info("peeked conveyor={} nullMessage", name);
			synchronized(lock){
				currentBuffer = copyAndClearBuffer();
			}
			flushBuffer(currentBuffer, afterPeek);
			return new ProcessBatchResult(false);
		}
		synchronized(lock){
			logger.info("peeked conveyor={} messageCount={}", name, 1);
			buffer.add(new MessageAndTime<>(message.getKey(), message.getDatabean(), System.currentTimeMillis()));
			if(buffer.size() >= BATCH_SIZE){
				currentBuffer = copyAndClearBuffer();
			}
		}
		flushBuffer(currentBuffer, afterPeek);
		return new ProcessBatchResult(true);
	}

	@Override
	public void interrupted() throws Exception{
		try{
			List<MessageAndTime<PK,D>> currentBuffer;
			synchronized(lock){
				currentBuffer = copyAndClearBuffer();
			}
			flushBuffer(currentBuffer, null);
		}catch(Exception ex){
			throw new Exception("Exception processing buffer. bufferSize=" + buffer.size() + " bufferMessages=" + Arrays
					.toString(buffer.toArray()), ex);
		}
	}

	private void flushBuffer(List<MessageAndTime<PK,D>> currentBuffer, Instant afterPeek){
		if(currentBuffer.isEmpty()){
				return;
		}
		Instant beforeProcessBuffer = Instant.now();
		long processingStart = beforeProcessBuffer.toEpochMilli();
		if(afterPeek != null){
			gaugeRecorder.savePeekToProcessBufferDurationMs(this, Duration.between(afterPeek, beforeProcessBuffer)
					.toMillis());
		}
		Scanner.of(currentBuffer)
				.each(MessageAndTime::toString)
				.map(mat -> mat.message)
				.flush(this::processBuffer);
		Instant afterProcessBuffer = Instant.now();
		gaugeRecorder.saveProcessBufferDurationMs(this, Duration.between(beforeProcessBuffer, afterProcessBuffer)
				.toMillis());
		ConveyorCounters.incFlushBuffer(this, currentBuffer.size());
		logger.info("consumed conveyor={} messageCount={}", name, currentBuffer.size());
		ConveyorCounters.incConsumedOpAndDatabeans(this, currentBuffer.size());
		Scanner.of(currentBuffer)
				.forEach(mat -> {
					long waitDurationMs = processingStart - mat.peekTime;
					long processingDurationMs = System.currentTimeMillis() - waitDurationMs;
					if(waitDurationMs + processingDurationMs > VISIBILITY_TIMEOUT.toMillis()){
						logger.warn("slow conveyor conveyor={} waitDurationMs={} processingDurationMs={} databean={}",
								name,
								waitDurationMs,
								processingDurationMs,
								mat.message);
					}
				});
		Instant beforeAck = Instant.now();
		Scanner.of(currentBuffer)
				.map(mat -> mat.queueMessageKey)
				.flush(list -> queueConsumer.ackMulti(list.size(), list));
		Instant afterAck = Instant.now();
		gaugeRecorder.saveAckDurationMs(this, Duration.between(beforeAck, afterAck).toMillis());
		logger.info("acked conveyor={} messageCount={}", name, currentBuffer.size());
		ConveyorCounters.incAck(this, currentBuffer.size());
	}

	private List<MessageAndTime<PK,D>> copyAndClearBuffer(){
		List<MessageAndTime<PK,D>> currentBuffer = buffer.stream()
				.collect(Collectors.toList());
		buffer.clear();
		return currentBuffer;
	}

	protected abstract void processBuffer(List<D> databeans);

}

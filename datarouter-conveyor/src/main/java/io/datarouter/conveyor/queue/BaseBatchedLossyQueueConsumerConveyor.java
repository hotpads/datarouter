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
package io.datarouter.conveyor.queue;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.conveyor.BaseConveyor;
import io.datarouter.conveyor.ConveyorCounters;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.storage.queue.QueueMessage;
import io.datarouter.storage.setting.Setting;

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
	private static final int BATCH_SIZE = 100;

	private final QueueConsumer<PK,D> queueConsumer;
	private final Object lock = new Object();
	private List<D> buffer;

	public BaseBatchedLossyQueueConsumerConveyor(String name, Setting<Boolean> shouldRunSetting,
			QueueConsumer<PK,D> queueConsumer){
		super(name, shouldRunSetting, () -> false);
		this.queueConsumer = queueConsumer;
		this.buffer = new ArrayList<>(BATCH_SIZE);
	}

	@Override
	public ProcessBatchResult processBatch(){
		List<D> currentBuffer = Collections.emptyList();
		QueueMessage<PK,D> message = queueConsumer.peek(PEEK_TIMEOUT);
		if(message == null){
			logger.info("peeked conveyor={} nullMessage", name);
			synchronized(lock){
				currentBuffer = copyAndClearBuffer();
			}
			flushBuffer(currentBuffer);
			return new ProcessBatchResult(false);
		}
		synchronized(lock){
			D databean = message.getDatabean();
			logger.info("peeked conveyor={} messageCount={}", name, 1);
			buffer.add(databean);
			if(buffer.size() >= BATCH_SIZE){
				currentBuffer = copyAndClearBuffer();
			}
		}
		logger.info("consumed conveyor={} messageCount={}", name, 1);
		ConveyorCounters.incConsumedOpAndDatabeans(this, 1);
		queueConsumer.ack(message.getKey());
		logger.info("acked conveyor={} messageCount={}", name, 1);
		ConveyorCounters.incAck(this);
		flushBuffer(currentBuffer);
		return new ProcessBatchResult(true);
	}

	@Override
	public void interrupted(){
		List<D> currentBuffer;
		synchronized(lock){
			currentBuffer = copyAndClearBuffer();
		}
		flushBuffer(currentBuffer);
	}

	private void flushBuffer(List<D> currentBuffer){
		if(currentBuffer.isEmpty()){
			return;
		}
		processBuffer(currentBuffer);
		ConveyorCounters.incFlushBuffer(this, currentBuffer.size());
	}

	private List<D> copyAndClearBuffer(){
		List<D> currentBuffer = buffer.stream()
				.collect(Collectors.toList());
		buffer.clear();
		return currentBuffer;
	}

	protected abstract void processBuffer(List<D> databeans);

}

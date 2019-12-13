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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.conveyor.BaseConveyor;
import io.datarouter.conveyor.ConveyorCounters;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.storage.queue.QueueMessage;
import io.datarouter.storage.setting.Setting;

public abstract class BaseQueueConsumerConveyor<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>>
extends BaseConveyor{
	private static final Logger logger = LoggerFactory.getLogger(BaseQueueConsumerConveyor.class);

	private static final Duration PEEK_TIMEOUT = Duration.ofSeconds(5);

	private final QueueConsumer<PK,D> queueConsumer;

	public BaseQueueConsumerConveyor(String name, Setting<Boolean> shouldRunSetting, QueueConsumer<PK,D> queueConsumer){
		super(name, shouldRunSetting, () -> false);
		this.queueConsumer = queueConsumer;
	}

	@Override
	public ProcessBatchResult processBatch(){
		QueueMessage<PK,D> message = queueConsumer.peek(PEEK_TIMEOUT);
		if(message == null){
			logger.info("peeked conveyor={} nullMessage", name);
			return new ProcessBatchResult(false);
		}
		D databean = message.getDatabean();
		logger.info("peeked conveyor={} messageCount={}", name, 1);
		try{
			processOne(databean);
		}catch(Exception e){
			throw new RuntimeException("databean=" + databean, e);
		}
		logger.info("consumed conveyor={} messageCount={}", name, 1);
		ConveyorCounters.incConsumedOpAndDatabeans(this, 1);
		queueConsumer.ack(message.getKey());
		logger.info("acked conveyor={} messageCount={}", name, 1);
		ConveyorCounters.incAck(this);
		return new ProcessBatchResult(true);
	}

	protected abstract void processOne(D databean);

}

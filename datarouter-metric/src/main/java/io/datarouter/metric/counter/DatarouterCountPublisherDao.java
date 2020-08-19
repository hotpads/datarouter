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
package io.datarouter.metric.counter;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.conveyor.message.ConveyorMessage;
import io.datarouter.conveyor.message.ConveyorMessage.ConveyorMessageFielder;
import io.datarouter.conveyor.message.ConveyorMessageKey;
import io.datarouter.conveyor.queue.QueueConsumer;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.BaseDaoParams;
import io.datarouter.storage.node.factory.QueueNodeFactory;
import io.datarouter.storage.node.op.raw.QueueStorage;

@Singleton
public class DatarouterCountPublisherDao extends BaseDao{

	public static class DatarouterCountPublisherDaoParams extends BaseDaoParams{

		public DatarouterCountPublisherDaoParams(ClientId clientId){
			super(clientId);
		}

	}

	private final QueueStorage<ConveyorMessageKey,ConveyorMessage> node;

	@Inject
	public DatarouterCountPublisherDao(Datarouter datarouter, DatarouterCountPublisherDaoParams params,
			QueueNodeFactory queueNodeFactory){
		super(datarouter);
		node = queueNodeFactory
				.createSingleQueue(params.clientId, ConveyorMessage::new, ConveyorMessageFielder::new)
				.withQueueName("CountPublisher")
				.withIsSystemTable(true)
				.buildAndRegister();
	}

	public void put(ConveyorMessage databean){
		node.put(databean);
	}

	public QueueConsumer<ConveyorMessageKey,ConveyorMessage> getQueueConsumer(){
		return new QueueConsumer<>(node::peek, node::ack);
	}

}

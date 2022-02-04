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
package io.datarouter.metric.counter;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.conveyor.message.ConveyorMessage;
import io.datarouter.conveyor.message.ConveyorMessage.ConveyorMessageFielder;
import io.datarouter.conveyor.message.ConveyorMessageKey;
import io.datarouter.conveyor.queue.QueueConsumer;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.BaseRedundantDaoParams;
import io.datarouter.storage.node.factory.QueueNodeFactory;
import io.datarouter.storage.node.op.raw.QueueStorage.QueueStorageNode;
import io.datarouter.storage.tag.Tag;
import io.datarouter.virtualnode.redundant.RedundantQueueStorageNode;

@Singleton
public class DatarouterCountBlobQueueDao extends BaseDao{

	public static class DatarouterCountBlobQueueDaoParams extends BaseRedundantDaoParams{

		public DatarouterCountBlobQueueDaoParams(List<ClientId> clientIds){
			super(clientIds);
		}

	}

	private final QueueStorageNode<ConveyorMessageKey,ConveyorMessage,ConveyorMessageFielder> queueNode;

	@Inject
	public DatarouterCountBlobQueueDao(Datarouter datarouter, DatarouterCountBlobQueueDaoParams params,
			QueueNodeFactory queueNodeFactory){
		super(datarouter);
		queueNode = Scanner.of(params.clientIds)
				.map(clientId -> {
					QueueStorageNode<ConveyorMessageKey,ConveyorMessage,ConveyorMessageFielder> node = queueNodeFactory
							.createSingleQueue(clientId, ConveyorMessage::new, ConveyorMessageFielder::new)
							.withNamespace("shared")
							.withQueueName("CountBlob")
							.withTag(Tag.DATAROUTER)
							.build();
					return node;
				})
				.listTo(RedundantQueueStorageNode::new);
		datarouter.register(queueNode);
	}

	public void put(ConveyorMessage databean){
		queueNode.put(databean);
	}

	public QueueConsumer<ConveyorMessageKey,ConveyorMessage> getQueueConsumer(){
		return new QueueConsumer<>(queueNode::peek, queueNode::ack);
	}

}

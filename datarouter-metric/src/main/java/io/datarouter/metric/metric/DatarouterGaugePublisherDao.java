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
package io.datarouter.metric.metric;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.conveyor.message.ConveyorMessage;
import io.datarouter.conveyor.message.ConveyorMessage.ConveyorMessageFielder;
import io.datarouter.conveyor.message.ConveyorMessageKey;
import io.datarouter.conveyor.queue.GroupQueueConsumer;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.BaseRedundantDaoParams;
import io.datarouter.storage.node.factory.QueueNodeFactory;
import io.datarouter.storage.node.op.raw.GroupQueueStorage.GroupQueueStorageNode;
import io.datarouter.virtualnode.redundant.RedundantGroupQueueStorageNode;

@Singleton
public class DatarouterGaugePublisherDao extends BaseDao{

	public static class DatarouterGaugePublisherDaoParams extends BaseRedundantDaoParams{

		public DatarouterGaugePublisherDaoParams(List<ClientId> clientIds){
			super(clientIds);
		}

	}

	private final GroupQueueStorageNode<ConveyorMessageKey,ConveyorMessage,ConveyorMessageFielder> queueNode;

	@Inject
	public DatarouterGaugePublisherDao(Datarouter datarouter, DatarouterGaugePublisherDaoParams params,
			QueueNodeFactory queueNodeFactory){
		super(datarouter);
		queueNode = Scanner.of(params.clientIds)
				.map(clientId -> {
					GroupQueueStorageNode<ConveyorMessageKey,ConveyorMessage,ConveyorMessageFielder> node =
							queueNodeFactory.createGroupQueue(
									clientId, ConveyorMessage::new, ConveyorMessageFielder::new)
							.withQueueName("PublisherGauge")
							.withIsSystemTable(true)
							.build();
					return node;
				})
				.listTo(RedundantGroupQueueStorageNode::new);
		datarouter.register(queueNode);
	}

	public void putMulti(Collection<ConveyorMessage> databeans){
		queueNode.putMulti(databeans);
	}

	public GroupQueueConsumer<ConveyorMessageKey,ConveyorMessage> getGroupQueueConsumer(){
		return new GroupQueueConsumer<>(queueNode::peek, queueNode::ack);
	}

}

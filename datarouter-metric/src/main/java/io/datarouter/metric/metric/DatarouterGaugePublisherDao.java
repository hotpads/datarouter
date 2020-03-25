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
package io.datarouter.metric.metric;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.conveyor.message.ConveyorMessage;
import io.datarouter.conveyor.message.ConveyorMessage.ConveyorMessageFielder;
import io.datarouter.conveyor.message.ConveyorMessageKey;
import io.datarouter.conveyor.queue.GroupQueueConsumer;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.BaseDaoParams;
import io.datarouter.storage.node.factory.QueueNodeFactory;
import io.datarouter.storage.node.op.raw.GroupQueueStorage;

@Singleton
public class DatarouterGaugePublisherDao extends BaseDao{

	public static class DatarouterGaugePublisherDaoParams extends BaseDaoParams{

		public DatarouterGaugePublisherDaoParams(ClientId clientId){
			super(clientId);
		}

	}

	private final GroupQueueStorage<ConveyorMessageKey,ConveyorMessage> node;

	@Inject
	public DatarouterGaugePublisherDao(Datarouter datarouter, DatarouterGaugePublisherDaoParams params,
			QueueNodeFactory queueNodeFactory){
		super(datarouter);
		node = queueNodeFactory
				.createGroupQueue(params.clientId, ConveyorMessage::new, ConveyorMessageFielder::new)
				.withQueueName("PublisherGauge")
				.buildAndRegister();
	}

	public void putMulti(Collection<ConveyorMessage> databeans){
		node.putMulti(databeans);
	}

	public GroupQueueConsumer<ConveyorMessageKey,ConveyorMessage> getGroupQueueConsumer(){
		return new GroupQueueConsumer<>(node::peek, node::ack);
	}

}

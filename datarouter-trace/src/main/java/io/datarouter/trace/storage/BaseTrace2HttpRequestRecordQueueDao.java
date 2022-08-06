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
package io.datarouter.trace.storage;

import java.util.Collection;

import io.datarouter.conveyor.queue.GroupQueueConsumer;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.BaseRedundantDaoParams;
import io.datarouter.storage.node.factory.QueueNodeFactory;
import io.datarouter.storage.node.op.raw.GroupQueueStorage.GroupQueueStorageNode;
import io.datarouter.storage.queue.StringQueueMessage;
import io.datarouter.storage.queue.StringQueueMessage.StringQueueMessageFielder;
import io.datarouter.storage.queue.StringQueueMessageKey;
import io.datarouter.storage.tag.Tag;
import io.datarouter.virtualnode.redundant.RedundantGroupQueueStorageNode;

public abstract class BaseTrace2HttpRequestRecordQueueDao extends BaseDao{

	private final GroupQueueStorageNode<StringQueueMessageKey,StringQueueMessage,StringQueueMessageFielder> queueNode;

	public BaseTrace2HttpRequestRecordQueueDao(String queueName, Datarouter datarouter, BaseRedundantDaoParams params,
			QueueNodeFactory queueNodeFactory){
		super(datarouter);
		queueNode = Scanner.of(params.clientIds)
				.map(clientId -> {
					GroupQueueStorageNode<StringQueueMessageKey,StringQueueMessage,StringQueueMessageFielder> node =
							queueNodeFactory.createGroupQueue(clientId, StringQueueMessage::new,
									StringQueueMessageFielder::new)
							.withQueueName(queueName)
							.withTag(Tag.DATAROUTER)
							.build();
					return node;
					})
					.listTo(RedundantGroupQueueStorageNode::makeIfMulti);
		datarouter.register(queueNode);
	}

	public void putMulti(Collection<StringQueueMessage> databeans){
		queueNode.putMulti(databeans);
	}

	public GroupQueueConsumer<StringQueueMessageKey,StringQueueMessage> getGroupQueueConsumer(){
		return new GroupQueueConsumer<>(queueNode::peek, queueNode::ack);
	}

}

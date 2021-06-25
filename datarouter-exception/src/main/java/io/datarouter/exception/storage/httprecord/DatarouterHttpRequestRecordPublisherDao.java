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
package io.datarouter.exception.storage.httprecord;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.conveyor.queue.GroupQueueConsumer;
import io.datarouter.exception.storage.httprecord.HttpRequestRecord.HttpRequestRecordFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.BaseRedundantDaoParams;
import io.datarouter.storage.node.factory.QueueNodeFactory;
import io.datarouter.storage.node.op.raw.GroupQueueStorage.GroupQueueStorageNode;
import io.datarouter.virtualnode.redundant.RedundantGroupQueueStorageNode;

@Singleton
public class DatarouterHttpRequestRecordPublisherDao extends BaseDao{

	public static class DatarouterHttpRequestRecordPublisherDaoParams extends BaseRedundantDaoParams{

		public DatarouterHttpRequestRecordPublisherDaoParams(List<ClientId> clientIds){
			super(clientIds);
		}

	}

	private final GroupQueueStorageNode<HttpRequestRecordKey,HttpRequestRecord,HttpRequestRecordFielder> queueNode;

	@Inject
	public DatarouterHttpRequestRecordPublisherDao(Datarouter datarouter,
			DatarouterHttpRequestRecordPublisherDaoParams params, QueueNodeFactory queueNodeFactory){
		super(datarouter);
		queueNode = Scanner.of(params.clientIds)
				.map(clientId -> {
					GroupQueueStorageNode<HttpRequestRecordKey,HttpRequestRecord,HttpRequestRecordFielder> node =
							queueNodeFactory.createGroupQueue(clientId, HttpRequestRecord::new,
									HttpRequestRecordFielder::new)
							.withQueueName("PublisherHttpRequestRecord")
							.withIsSystemTable(true)
							.build();
					return node;
				})
				.listTo(RedundantGroupQueueStorageNode::new);
		datarouter.register(queueNode);
	}

	public GroupQueueConsumer<HttpRequestRecordKey,HttpRequestRecord> getGroupQueueConsumer(){
		return new GroupQueueConsumer<>(queueNode::peek, queueNode::ack);
	}

	public void put(HttpRequestRecord httpRequestRecord){
		queueNode.put(httpRequestRecord);
	}

}

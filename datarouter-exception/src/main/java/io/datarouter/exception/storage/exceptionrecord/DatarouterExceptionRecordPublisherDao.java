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
package io.datarouter.exception.storage.exceptionrecord;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.conveyor.queue.GroupQueueConsumer;
import io.datarouter.exception.storage.exceptionrecord.ExceptionRecord.ExceptionRecordFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.BaseRedundantDaoParams;
import io.datarouter.storage.node.factory.QueueNodeFactory;
import io.datarouter.storage.node.op.raw.GroupQueueStorage.GroupQueueStorageNode;
import io.datarouter.storage.tag.Tag;
import io.datarouter.virtualnode.redundant.RedundantGroupQueueStorageNode;

@Singleton
public class DatarouterExceptionRecordPublisherDao extends BaseDao{

	public static class DatarouterExceptionPublisherRouterParams extends BaseRedundantDaoParams{

		public DatarouterExceptionPublisherRouterParams(List<ClientId> clientIds){
			super(clientIds);
		}

	}

	private final GroupQueueStorageNode<ExceptionRecordKey,ExceptionRecord,ExceptionRecordFielder> queueNode;

	@Inject
	public DatarouterExceptionRecordPublisherDao(
			Datarouter datarouter,
			DatarouterExceptionPublisherRouterParams params,
			QueueNodeFactory queueNodeFactory){
		super(datarouter);
		queueNode = Scanner.of(params.clientIds)
				.map(clientId -> {
					GroupQueueStorageNode<ExceptionRecordKey,ExceptionRecord,ExceptionRecordFielder> node =
							queueNodeFactory.createGroupQueue(clientId, ExceptionRecord::new,
									ExceptionRecordFielder::new)
							.withQueueName("PublisherExceptionRecord")
							.withTag(Tag.DATAROUTER)
							.build();
					return node;
				})
				.listTo(RedundantGroupQueueStorageNode::makeIfMulti);
		datarouter.register(queueNode);
	}

	public GroupQueueConsumer<ExceptionRecordKey,ExceptionRecord> getGroupQueueConsumer(){
		return new GroupQueueConsumer<>(queueNode::peek, queueNode::ack);
	}

	public void put(ExceptionRecord exceptionRecord){
		queueNode.put(exceptionRecord);
	}

}

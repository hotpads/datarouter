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
package io.datarouter.gcp.pubsub.test;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collection;
import java.util.List;

import io.datarouter.client.gcp.pubsub.node.BaseGcpPubsubNode;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.TestDao;
import io.datarouter.storage.node.factory.QueueNodeFactory;
import io.datarouter.storage.node.op.raw.GroupQueueStorage.GroupQueueStorageNode;
import io.datarouter.storage.queue.GroupQueueMessage;
import io.datarouter.storage.queue.QueueMessageKey;
import io.datarouter.storage.test.TestDatabean;
import io.datarouter.storage.test.TestDatabeanFielder;
import io.datarouter.storage.test.TestDatabeanKey;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class GcpPubsubGroupTestDao extends BaseDao implements TestDao, GcpPubsubTestHelperDao{

	private final GroupQueueStorageNode<TestDatabeanKey,TestDatabean,TestDatabeanFielder> node;
	private final int topicLength;

	@Inject
	public GcpPubsubGroupTestDao(Datarouter context, QueueNodeFactory queueNodeFactory){
		super(context);
		node = queueNodeFactory.createGroupQueue(
				GcpPubsubTestClientIds.GCP_PUBSUB,
				TestDatabean::new,
				TestDatabeanFielder::new)
				.withQueueName("GroupTestDatabean")
				.buildAndRegister();

		BaseGcpPubsubNode<?,?,?> baseNode = (BaseGcpPubsubNode<?,?,?>)node.getPhysicalNodes().get(0);
		topicLength = baseNode.getTopicAndSubscriptionName().get()
				.topic()
				.toString()
				.getBytes(StandardCharsets.UTF_8)
				.length;
	}

	public void put(TestDatabean databean){
		node.put(databean);
	}

	public void ack(QueueMessageKey key, Duration timeout){
		node.ack(key, new Config().setTimeout(timeout));
	}

	public void ack(QueueMessageKey key){
		node.ack(key);
	}

	@Override
	public void putMulti(Collection<TestDatabean> databeans){
		node.putMulti(databeans);
	}

	public List<TestDatabean> pollMulti(){
		return node.pollMulti();
	}

	public GroupQueueMessage<TestDatabeanKey,TestDatabean> peek(){
		return node.peek();
	}

	public Scanner<GroupQueueMessage<TestDatabeanKey,TestDatabean>> peekUntilEmpty(){
		return node.peekUntilEmpty();
	}

	public List<GroupQueueMessage<TestDatabeanKey,TestDatabean>> peekMulti(){
		return node.peekMulti();
	}

	@Override
	public int getTopicLength(){
		return topicLength;
	}

}

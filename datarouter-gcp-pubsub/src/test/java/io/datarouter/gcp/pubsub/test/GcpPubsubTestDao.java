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
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.client.gcp.pubsub.node.BaseGcpPubsubNode;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.TestDao;
import io.datarouter.storage.node.factory.QueueNodeFactory;
import io.datarouter.storage.node.op.raw.QueueStorage.QueueStorageNode;
import io.datarouter.storage.queue.QueueMessage;
import io.datarouter.storage.queue.QueueMessageKey;
import io.datarouter.storage.test.TestDatabean;
import io.datarouter.storage.test.TestDatabeanFielder;
import io.datarouter.storage.test.TestDatabeanKey;

@Singleton
public class GcpPubsubTestDao extends BaseDao implements TestDao, GcpPubsubTestHelperDao{

	private final QueueStorageNode<TestDatabeanKey,TestDatabean,TestDatabeanFielder> node;
	private final int topicLength;

	@Inject
	public GcpPubsubTestDao(Datarouter context, QueueNodeFactory queueNodeFactory){
		super(context);
		node = queueNodeFactory.createSingleQueue(
				GcpPubsubTestClientIds.GCP_PUBSUB,
				TestDatabean::new,
				TestDatabeanFielder::new)
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

	public Scanner<TestDatabean> pollUntilEmpty(){
		return node.pollUntilEmpty();
	}

	public TestDatabean poll(){
		return node.poll();
	}

	public List<TestDatabean> pollMulti(int limit){
		return node.pollMulti(new Config().setLimit(limit));
	}

	@Override
	public void putMulti(Collection<TestDatabean> databeans){
		node.putMulti(databeans);
	}

	public QueueMessage<TestDatabeanKey,TestDatabean> peek(){
		return node.peek();
	}

	public Collection<QueueMessage<TestDatabeanKey,TestDatabean>> peekMulti(int limit){
		return node.peekMulti(new Config().setLimit(limit));
	}

	public Scanner<QueueMessage<TestDatabeanKey,TestDatabean>> peekUntilEmpty(){
		return node.peekUntilEmpty(new Config());
	}

	public void ack(QueueMessageKey key){
		node.ack(key);
	}

	@Override
	public int getTopicLength(){
		return topicLength;
	}

}

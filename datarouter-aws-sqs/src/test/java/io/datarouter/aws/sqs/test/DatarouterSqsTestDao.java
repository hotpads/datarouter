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
package io.datarouter.aws.sqs.test;

import java.time.Duration;
import java.util.Collection;
import java.util.List;

import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.TestDao;
import io.datarouter.storage.node.factory.QueueNodeFactory;
import io.datarouter.storage.node.op.raw.QueueStorage;
import io.datarouter.storage.queue.QueueMessage;
import io.datarouter.storage.queue.QueueMessageKey;
import io.datarouter.storage.test.TestDatabean;
import io.datarouter.storage.test.TestDatabeanFielder;
import io.datarouter.storage.test.TestDatabeanKey;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterSqsTestDao extends BaseDao implements TestDao, SqsTestHelperDao{

	private final QueueStorage<TestDatabeanKey,TestDatabean> node;

	@Inject
	public DatarouterSqsTestDao(Datarouter context, QueueNodeFactory queueNodeFactory){
		super(context);
		node = queueNodeFactory.createSingleQueue(
				DatarouterSqsTestClientIds.SQS,
				TestDatabean::new,
				TestDatabeanFielder::new)
				.buildAndRegister();
	}

	public void put(TestDatabean databean){
		node.put(databean);
	}

	public Scanner<TestDatabean> pollUntilEmpty(Duration timeout){
		var config = new Config().setTimeout(timeout);
		return node.pollUntilEmpty(config);
	}

	public Scanner<TestDatabean> pollUntilEmpty(){
		return node.pollUntilEmpty();
	}

	public TestDatabean poll(){
		return node.poll();
	}

	public TestDatabean poll(Duration timeout){
		return node.poll(new Config().setTimeout(timeout));
	}

	public List<TestDatabean> pollMulti(int limit, Duration timeout){
		return node.pollMulti(new Config().setLimit(limit).setTimeout(timeout));
	}

	@Override
	public void putMulti(Collection<TestDatabean> databeans){
		node.putMulti(databeans);
	}

	public QueueMessage<TestDatabeanKey,TestDatabean> peek(Duration timeout){
		return node.peek(new Config().setTimeout(timeout));
	}

	public Collection<QueueMessage<TestDatabeanKey,TestDatabean>> peekMulti(int limit){
		return node.peekMulti(new Config().setLimit(limit));
	}

	public Scanner<QueueMessage<TestDatabeanKey,TestDatabean>> peekUntilEmpty(Duration timeout){
		return node.peekUntilEmpty(new Config().setTimeout(timeout));
	}

	public void ack(QueueMessageKey key){
		node.ack(key);
	}

}

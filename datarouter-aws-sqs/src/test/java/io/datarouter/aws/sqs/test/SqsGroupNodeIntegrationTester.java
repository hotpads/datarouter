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
package io.datarouter.aws.sqs.test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import io.datarouter.aws.sqs.BaseSqsNode;
import io.datarouter.aws.sqs.DatarouterAwsSqsTestNgModuleFactory;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.queue.GroupQueueMessage;
import io.datarouter.storage.test.TestDatabean;
import io.datarouter.storage.test.TestDatabeanKey;

@Guice(moduleFactory = DatarouterAwsSqsTestNgModuleFactory.class)
@Test(singleThreaded = true)
public class SqsGroupNodeIntegrationTester{

	private static final int DATABEAN_COUNT = 15;

	private final Datarouter datarouter;
	private final SqsTestHelper sqsTestHelper;
	private final DatarouterSqsGroupTestDao dao;

	@Inject
	public SqsGroupNodeIntegrationTester(Datarouter datarouter, DatarouterSqsGroupTestDao dao){
		this.datarouter = datarouter;
		this.dao = dao;
		this.sqsTestHelper = new SqsTestHelper(dao);
	}

	@AfterClass
	public void afterClass(){
		datarouter.shutdown();
	}

	@BeforeMethod
	public void beforeMethod(){
		drainQueue();
	}

	private void drainQueue(){
		dao.peekUntilEmpty(Duration.ofSeconds(4)).forEach(message -> dao.ack(message.getKey(), Duration.ofSeconds(4)));
	}

	@Test
	public void testPutMultiAndPollMulti(){
		List<TestDatabean> databeans = new ArrayList<>(DATABEAN_COUNT);
		String longString = SqsTestHelper.makeStringOfByteSize(BaseSqsNode.MAX_BYTES_PER_MESSAGE / (DATABEAN_COUNT
				- 1));
		for(int i = 0; i < DATABEAN_COUNT; i++){
			databeans.add(new TestDatabean(longString, makeRandomString(), makeRandomString()));
		}
		dao.putMulti(databeans);
		List<TestDatabean> retrievedDatabeans = dao.pollMulti(Duration.ofSeconds(5));
		retrievedDatabeans.addAll(dao.pollMulti(Duration.ofSeconds(5)));
		Assert.assertEquals(retrievedDatabeans.size(), DATABEAN_COUNT);
	}

	@Test
	public void testPutAndPeek(){
		var databean = new TestDatabean(makeRandomString(), makeRandomString(), makeRandomString());
		dao.put(databean);
		GroupQueueMessage<TestDatabeanKey,TestDatabean> message = dao.peek(Duration.ofSeconds(5));
		Assert.assertEquals(message.getDatabeans().size(), 1);
		Assert.assertEquals(databean, message.getDatabeans().iterator().next());
		Assert.assertNull(dao.peek());
	}

	@Test
	public void testByteLimitMulti(){
		sqsTestHelper.testByteLimitMulti();
	}

	@Test
	public void testPeekTimeout(){
		Assert.assertNull(dao.peek());
		Assert.assertNull(dao.peek(Duration.ofSeconds(BaseSqsNode.MAX_TIMEOUT_SECONDS + 1)));
	}

	@Test
	public void testInterruptPeek(){
		SqsTestHelper.testInterruptPeek(() -> {
			Assert.assertNull(dao.peek(Duration.ofSeconds(5)));
			return null;
		});
	}

	private static String makeRandomString(){
		return UUID.randomUUID().toString();
	}

}

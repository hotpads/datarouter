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

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import io.datarouter.gcp.pubsub.DatarouterPubsubTestNgModuleFactory;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.queue.GroupQueueMessage;
import io.datarouter.storage.test.TestDatabean;
import io.datarouter.storage.test.TestDatabeanKey;
import jakarta.inject.Inject;

@Guice(moduleFactory = DatarouterPubsubTestNgModuleFactory.class)
@Test(singleThreaded = true)
public class GcpPubsubGroupNodeIntegrationTester{

	private static final int DATABEAN_COUNT = 15;

	private final Datarouter datarouter;
	private final GcpPubsubTestHelper testHelper;
	private final GcpPubsubGroupTestDao dao;

	@Inject
	public GcpPubsubGroupNodeIntegrationTester(Datarouter datarouter, GcpPubsubGroupTestDao dao){
		this.datarouter = datarouter;
		this.dao = dao;
		this.testHelper = new GcpPubsubTestHelper(dao);
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
		dao.peekUntilEmpty().forEach(message -> dao.ack(message.getKey(), Duration.ofSeconds(4)));
	}

	@Test
	public void testPutMultiAndPollMulti(){
		List<TestDatabean> databeans = new ArrayList<>(DATABEAN_COUNT);
		String longString = GcpPubsubTestHelper.makeStringOfByteSize(10_000);
		for(int i = 0; i < DATABEAN_COUNT; i++){
			databeans.add(new TestDatabean(longString, makeRandomString(), makeRandomString()));
		}
		dao.putMulti(databeans);
		List<TestDatabean> retrievedDatabeans = dao.pollMulti();
		Assert.assertEquals(retrievedDatabeans.size(), DATABEAN_COUNT);
	}

	@Test
	public void testPutAndPeek(){
		var databean = new TestDatabean(makeRandomString(), makeRandomString(), makeRandomString());
		dao.put(databean);
		GroupQueueMessage<TestDatabeanKey,TestDatabean> message = dao.peek();
		Assert.assertEquals(message.getDatabeans().size(), 1);
		Assert.assertEquals(databean, message.getDatabeans().iterator().next());
		Assert.assertNull(dao.peek());
	}

	@Test
	public void testByteLimitMulti(){
		testHelper.testByteLimitMulti();
	}

	@Test
	public void testInterruptPeek(){
		GcpPubsubTestHelper.testInterruptPeek(() -> {
			Assert.assertNull(dao.peek());
			return null;
		});
	}

	private static String makeRandomString(){
		return UUID.randomUUID().toString();
	}

}

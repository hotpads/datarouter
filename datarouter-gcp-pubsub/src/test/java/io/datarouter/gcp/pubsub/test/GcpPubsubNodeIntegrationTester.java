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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import io.datarouter.client.gcp.pubsub.GcpPubsubDataTooLargeException;
import io.datarouter.client.gcp.pubsub.node.BaseGcpPubsubNode;
import io.datarouter.gcp.pubsub.DatarouterPubsubTestNgModuleFactory;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.test.TestDatabean;
import jakarta.inject.Inject;

@Guice(moduleFactory = DatarouterPubsubTestNgModuleFactory.class)
@Test(singleThreaded = true)
public class GcpPubsubNodeIntegrationTester{
	private static final Logger logger = LoggerFactory.getLogger(GcpPubsubNodeIntegrationTester.class);

	private static final int DATABEAN_COUNT = 15;

	private final Datarouter datarouter;
	private final GcpPubsubTestHelper testHelper;
	private final GcpPubsubTestDao dao;

	@Inject
	public GcpPubsubNodeIntegrationTester(Datarouter datarouter, GcpPubsubTestDao dao){
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
		dao.pollUntilEmpty().forEach(testDatabean -> {
			logger.debug(testDatabean.toString());
		});
	}

	@Test
	public void testByteLimitMulti(){
		testHelper.testByteLimitMulti();
	}

	@Test
	public void testOverByteLimit(){
		testByteLimit(BaseGcpPubsubNode.MAX_TOPIC_PLUS_MESSAGE_SIZE - dao.getTopicLength());
		try{
			testByteLimit(BaseGcpPubsubNode.MAX_SERIALIZED_REQUEST_SIZE);//not enough room for topic
		}catch(GcpPubsubDataTooLargeException e){
			//intentional
		}
	}

	private void testByteLimit(int size){
		String longString = GcpPubsubTestHelper.makeLongStringWithDatabeanSizeTarget(size);
		var databean = new TestDatabean(longString, "", "");
		dao.put(databean);
	}

	@Test
	public void testPeekLimit(){
		putRandomDatabeans();
		int testLimit = 10;
		int size = dao.peekMulti(testLimit).size();
		Assert.assertTrue(size <= testLimit);
	}

	@Test
	public void testPutAndPoll(){
		var databean = new TestDatabean(makeRandomString(), makeRandomString(), makeRandomString());
		dao.put(databean);
		TestDatabean retrievedDatabean = dao.poll();
		Assert.assertEquals(retrievedDatabean.getKey().getFoo(), databean.getKey().getFoo());
		Assert.assertEquals(retrievedDatabean.getBar(), databean.getBar());
		Assert.assertEquals(retrievedDatabean.getBaz(), databean.getBaz());
		Assert.assertNull(dao.poll());
	}

	@Test
	public void testPutMultiAndPollMulti(){
		putRandomDatabeans();
		Set<Integer> ids = new HashSet<>();
		List<TestDatabean> retrievedDatabeans;
		do{
			retrievedDatabeans = dao.pollMulti(5);
			Assert.assertTrue(retrievedDatabeans.size() <= 5);
			for(TestDatabean databean : retrievedDatabeans){
				Integer id = Integer.valueOf(databean.getKey().getFoo());
				Assert.assertTrue(id < DATABEAN_COUNT);
				Assert.assertTrue(id >= 0);
				ids.add(id);
			}
		}while(retrievedDatabeans.size() > 0 || ids.size() < DATABEAN_COUNT);
	}

	private void putRandomDatabeans(){
		List<TestDatabean> databeans = new ArrayList<>(DATABEAN_COUNT);
		for(int i = 0; i < DATABEAN_COUNT; i++){
			databeans.add(new TestDatabean(String.valueOf(i), makeRandomString(), makeRandomString()));
		}
		dao.putMulti(databeans);
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

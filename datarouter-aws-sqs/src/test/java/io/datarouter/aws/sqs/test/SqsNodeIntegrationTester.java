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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import io.datarouter.aws.sqs.BaseSqsNode;
import io.datarouter.aws.sqs.DatarouterAwsSqsTestNgModuleFactory;
import io.datarouter.aws.sqs.SqsDataTooLargeException;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.test.TestDatabean;

@Guice(moduleFactory = DatarouterAwsSqsTestNgModuleFactory.class)
@Test(singleThreaded = true)
public class SqsNodeIntegrationTester{
	private static final Logger logger = LoggerFactory.getLogger(SqsNodeIntegrationTester.class);

	private static final int DATABEAN_COUNT = 15;

	private final Datarouter datarouter;
	private final SqsTestHelper sqsTestHelper;
	private final DatarouterSqsTestDao dao;

	@Inject
	public SqsNodeIntegrationTester(Datarouter datarouter, DatarouterSqsTestDao dao){
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
		drainQueue(Duration.ofSeconds(2));
	}

	private void drainQueue(Duration duration){
		dao.pollUntilEmpty(duration).forEach(testDatabean -> {
			logger.debug(duration.toSeconds() + "\t" + testDatabean);
		});
	}

	@Test
	public void testByteLimitMulti(){
		sqsTestHelper.testByteLimitMulti();
	}

	@Test
	public void testUnderByteLimit(){
		testByteLimit(BaseSqsNode.MAX_BYTES_PER_MESSAGE);
	}

	@Test(expectedExceptions = {SqsDataTooLargeException.class})
	public void testOverByteLimit(){
		testByteLimit(BaseSqsNode.MAX_BYTES_PER_MESSAGE + 1);
	}

	private void testByteLimit(int size){
		String longString = SqsTestHelper.makeLongStringWithDatabeanSizeTarget(size);
		var databean = new TestDatabean(longString, "", "");
		dao.put(databean);
	}

	@Test
	public void testPeekLimit(){
		putRandomDatabeans();
		int testLimit = 3;
		Assert.assertTrue(dao.peekMulti(testLimit).size() <= testLimit);
	}

	@Test
	public void testPutAndPoll(){
		var databean = new TestDatabean(makeRandomString(), makeRandomString(), makeRandomString());
		dao.put(databean);
		TestDatabean retrievedDatabean = dao.poll(Duration.ofMillis(Long.MAX_VALUE));
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
			retrievedDatabeans = dao.pollMulti(5, Duration.ofSeconds(5));
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
		SqsTestHelper.testInterruptPeek(() -> {
			Assert.assertNull(dao.peek(Duration.ofSeconds(5)));
			return null;
		});
	}

	@Test
	public void testPollTimeout(){
		drainQueue(Duration.ofSeconds(40));// extra cleanup
		var databean = new TestDatabean(makeRandomString(), makeRandomString(), makeRandomString());
		dao.put(databean);
		long time = System.currentTimeMillis();
		TestDatabean retrievedDatabean = dao.poll(Duration.ofSeconds(5));
		Assert.assertNotNull(retrievedDatabean);
		Assert.assertTrue(System.currentTimeMillis() - time < 6000L);

		testPollNullWithTimeout(Duration.ofSeconds(25));
		testPollNullWithTimeout(Duration.ofSeconds(25));
	}

	private void testPollNullWithTimeout(Duration timeout){
		long time = System.currentTimeMillis();
		TestDatabean retrievedDatabean = dao.poll(timeout);
		Assert.assertNull(retrievedDatabean);
		Assert.assertTrue(System.currentTimeMillis() - time < timeout.toMillis() + 1000);
		Assert.assertTrue(System.currentTimeMillis() - time >= timeout.toDays());
	}

	private static String makeRandomString(){
		return UUID.randomUUID().toString();
	}

}

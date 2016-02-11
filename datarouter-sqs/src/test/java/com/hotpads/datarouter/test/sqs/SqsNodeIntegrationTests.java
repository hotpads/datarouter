package com.hotpads.datarouter.test.sqs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import com.hotpads.datarouter.client.imp.sqs.BaseSqsNode;
import com.hotpads.datarouter.client.imp.sqs.SqsDataTooLargeException;
import com.hotpads.datarouter.client.imp.sqs.config.DatarouterSqsTestModuleFactory;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.test.TestDatabean;

@Guice(moduleFactory = DatarouterSqsTestModuleFactory.class)
@Test(singleThreaded=true)
public class SqsNodeIntegrationTests{
	private static final Logger logger = LoggerFactory.getLogger(SqsNodeIntegrationTests.class);

	private static final int DATABEAN_COUNT = 15;

	@Inject
	private Datarouter datarouter;

	private final SqsTestHelper sqsTestHelper;
	private final SqsTestRouter router;

	@Inject
	public SqsNodeIntegrationTests(SqsTestRouter router){
		this.router = router;
		this.sqsTestHelper = new SqsTestHelper(router.testDatabean);
	}

	@AfterClass
	public void shutdown(){
		datarouter.shutdown();
	}

	@BeforeMethod
	public void setUp(){
		cleanUp(2);
	}

	private void cleanUp(int seconds){
		Config config = new Config().setTimeout(seconds, TimeUnit.SECONDS);
		for(TestDatabean testDatabean : router.testDatabean.pollUntilEmpty(config)){
			logger.debug(seconds + "\t" + testDatabean);
		}
	}

	@Test
	public void testByteLimitMulti(){
		sqsTestHelper.testByteLimitMulti();
	}

	@Test
	public void testUnderByteLimit(){
		testByteLimit(BaseSqsNode.MAX_BYTES_PER_MESSAGE);
	}

	@Test(expectedExceptions={SqsDataTooLargeException.class})
	public void testOverByteLimit(){
		testByteLimit(BaseSqsNode.MAX_BYTES_PER_MESSAGE + 1);
	}

	private void testByteLimit(int size){
		String longString = SqsTestHelper.makeLongStringWithDatabeanSizeTarget(size);
		TestDatabean databean = new TestDatabean(longString, "", "");
		router.testDatabean.put(databean, null);
	}

	@Test
	public void testPeekLimit(){
		putRandomDatabeans();
		int testLimit = 3;
		Assert.assertTrue(router.testDatabean.peekMulti(new Config().setLimit(testLimit)).size() <= testLimit);
	}

	@Test
	public void testPutAndPoll(){
		TestDatabean databean = new TestDatabean(makeRandomString(), makeRandomString(), makeRandomString());
		router.testDatabean.put(databean, null);
		TestDatabean retrievedDatabean = router.testDatabean.poll(new Config().setTimeoutMs(Long.MAX_VALUE));
		Assert.assertEquals(retrievedDatabean.getFoo(), databean.getFoo());
		Assert.assertEquals(retrievedDatabean.getBar(), databean.getBar());
		Assert.assertEquals(retrievedDatabean.getBaz(), databean.getBaz());
		Assert.assertNull(router.testDatabean.poll(null));
	}

	@Test
	public void testPutMultiAndPollMulti(){
		putRandomDatabeans();
		Set<Integer> ids = new HashSet<>();
		List<TestDatabean> retrievedDatabeans;
		do{
			retrievedDatabeans = router.testDatabean.pollMulti(new Config().setLimit(5).setTimeoutMs(5000L));
			Assert.assertTrue(retrievedDatabeans.size() <= 5);
			for(TestDatabean databean : retrievedDatabeans){
				Integer id = Integer.valueOf(databean.getFoo());
				Assert.assertTrue(id < DATABEAN_COUNT);
				Assert.assertTrue(id >= 0);
				ids.add(id);
			}
		}while(retrievedDatabeans.size() > 0 || ids.size() < DATABEAN_COUNT);
	}

	private void putRandomDatabeans(){
		List<TestDatabean> databeans = new ArrayList<>(DATABEAN_COUNT);
		for(int i = 0 ; i < DATABEAN_COUNT ; i++){
			databeans.add(new TestDatabean(String.valueOf(i), makeRandomString(), makeRandomString()));
		}
		router.testDatabean.putMulti(databeans, null);
	}

	@Test
	public void testInterruptPeek(){
		SqsTestHelper.testInterruptPeek(new Callable<Void>(){

			@Override
			public Void call(){
				Config config = new Config().setTimeoutMs(5000L);
				Assert.assertNull(router.testDatabean.peek(config));
				return null;
			}

		});
	}

	@Test//Too long to run for every build
	public void testPollTimeout(){
		cleanUp(40);//extra cleanup
		TestDatabean databean = new TestDatabean(makeRandomString(), makeRandomString(), makeRandomString());
		router.testDatabean.put(databean, null);

		Config config = new Config().setTimeoutMs(5000L);
		long time = System.currentTimeMillis();
		TestDatabean retrievedDatabean = router.testDatabean.poll(config);
		Assert.assertNotNull(retrievedDatabean);
		Assert.assertTrue(System.currentTimeMillis() - time < 6000L);

		testPollNullWithTimeout(25000);
		testPollNullWithTimeout(25000);
	}

	private void testPollNullWithTimeout(long timeout){
		Config config = new Config().setTimeoutMs(timeout);
		long time = System.currentTimeMillis();
		TestDatabean retrievedDatabean = router.testDatabean.poll(config);
		Assert.assertNull(retrievedDatabean);
		Assert.assertTrue(System.currentTimeMillis() - time < timeout + 1000);
		Assert.assertTrue(System.currentTimeMillis() - time >= timeout);
	}

	private static String makeRandomString(){
		return UUID.randomUUID().toString();
	}
}

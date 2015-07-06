package com.hotpads.datarouter.test.sqs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import com.hotpads.datarouter.client.imp.sqs.SqsDataTooLargeException;
import com.hotpads.datarouter.client.imp.sqs.SqsNode;
import com.hotpads.datarouter.client.imp.sqs.config.DatarouterSqsTestModuleFactory;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.test.TestDatabean;
import com.hotpads.datarouter.test.TestDatabeanFielder;
import com.hotpads.util.core.bytes.StringByteTool;

@Guice(moduleFactory = DatarouterSqsTestModuleFactory.class)
@Test(singleThreaded=true)
public class SqsNodeIntegrationTests{
	private static final int DATABEAN_COUNT = 15;
	
	@Inject
	private SqsTestRouter router;
	
	@BeforeMethod
	public void setUp(){
		cleanUp();
	}
	
	@AfterMethod
	public void tearDown(){
		cleanUp();
	}
	
	private void cleanUp(){
		while(router.testDatabean.pollUntilEmpty(null) != null){
			//do nothing
		}
	}
	
	@Test
	public void testUnderByteLimit(){
		testByteLimit(SqsNode.MAX_BYTES_PER_MESSAGE);
	}
	
	@Test(expectedExceptions={SqsDataTooLargeException.class})
	public void testOverByteLimit(){
		testByteLimit(SqsNode.MAX_BYTES_PER_MESSAGE + 1);
	}
	
	private void testByteLimit(int size){
		TestDatabean emptyDatabean = new TestDatabean("", "", "");
		TestDatabeanFielder fielder = new TestDatabeanFielder();
		String stringDatabean = fielder.getStringDatabeanCodec().toString(emptyDatabean, fielder);
		int emptyDatabeanSize = StringByteTool.getUtf8Bytes(stringDatabean).length;
		String longString = makeStringOfByteSize(size - emptyDatabeanSize);
		TestDatabean databean = new TestDatabean(longString, "", "");
		router.testDatabean.put(databean, null);
	}
	
	private static String makeStringOfByteSize(int requiredSize){
		Assert.assertEquals(StringByteTool.getUtf8Bytes("a").length, 1);
		StringBuilder longString = new StringBuilder();
		for(int size = 0 ; size < requiredSize ; size++){
			longString.append("a");
		}
		return longString.toString();
	}
	
	@Test
	public void testPeekLimit(){
		List<TestDatabean> databeans = new ArrayList<>(DATABEAN_COUNT);
		for(int i = 0 ; i < DATABEAN_COUNT ; i++){
			databeans.add(new TestDatabean(String.valueOf(i), makeRandomString(), makeRandomString()));
		}
		router.testDatabean.putMulti(databeans, null);
		int testLimit = 3;
		Assert.assertTrue(router.testDatabean.peekMulti(new Config().setLimit(testLimit)).size() < testLimit);
	}
	
	@Test
	public void testPutAndPoll(){
		TestDatabean databean = new TestDatabean(makeRandomString(), makeRandomString(), makeRandomString());
		router.testDatabean.put(databean, null);
		TestDatabean retrievedDatabean = router.testDatabean.poll(new Config().setTimeoutMs(Long.MAX_VALUE));
		Assert.assertEquals(retrievedDatabean.getA(), databean.getA());
		Assert.assertEquals(retrievedDatabean.getB(), databean.getB());
		Assert.assertEquals(retrievedDatabean.getC(), databean.getC());
		Assert.assertNull(router.testDatabean.poll(null));
	}
	
	@Test
	public void testPutMultiAndPollMulti(){
		List<TestDatabean> databeans = new ArrayList<>(DATABEAN_COUNT);
		for(int i = 0 ; i < DATABEAN_COUNT ; i++){
			databeans.add(new TestDatabean(String.valueOf(i), makeRandomString(), makeRandomString()));
		}
		router.testDatabean.putMulti(databeans, null);
		Set<Integer> ids = new HashSet<>();
		List<TestDatabean> retrievedDatabeans;
		do{
			retrievedDatabeans = router.testDatabean.pollMulti(new Config().setLimit(5).setTimeoutMs(5000L));
			Assert.assertTrue(retrievedDatabeans.size() <= 5);
			for(TestDatabean databean : retrievedDatabeans){
				Integer id = Integer.valueOf(databean.getA());
				Assert.assertTrue(id < DATABEAN_COUNT);
				Assert.assertTrue(id >= 0);
				ids.add(id);
			}
		}while(retrievedDatabeans.size() > 0 || ids.size() < DATABEAN_COUNT);
	}

	@Test(enabled = false)//Too long to run for every build
	public void testPollTimeout(){
		TestDatabean databean = new TestDatabean(makeRandomString(), makeRandomString(), makeRandomString());
		router.testDatabean.put(databean, null);
		
		Config config = new Config().setTimeoutMs(5000L);
		long time = System.currentTimeMillis();
		TestDatabean retrievedDatabean = router.testDatabean.poll(config);
		Assert.assertNotNull(retrievedDatabean);
		Assert.assertTrue((System.currentTimeMillis() - time) < 6000L);
		
		testPollNullWithTimeout(5000);
		testPollNullWithTimeout(25000);
		testPollNullWithTimeout(45000);
	}
	
	private void testPollNullWithTimeout(long timeout){
		Config config = new Config().setTimeoutMs(timeout);
		long time = System.currentTimeMillis();
		TestDatabean retrievedDatabean = router.testDatabean.poll(config);
		Assert.assertNull(retrievedDatabean);
		Assert.assertTrue((System.currentTimeMillis() - time) < timeout + 1000);
		Assert.assertTrue((System.currentTimeMillis() - time) >= timeout);
	}
	
	@Test(enabled = false)//Too long to run for every build
	public void testPollMultiTimeout(){
		TestDatabean databean = new TestDatabean(makeRandomString(), makeRandomString(), makeRandomString());
		router.testDatabean.put(databean, null);
		
		Config config = new Config().setTimeoutMs(5000L);
		long time = System.currentTimeMillis();
		List<TestDatabean> retrievedDatabeans = router.testDatabean.pollMulti(config);
		Assert.assertTrue((System.currentTimeMillis() - time) < 6000L);
		Assert.assertTrue(retrievedDatabeans.size() > 0);
		
		testPollMultiNullWithTimeout(5000);
		testPollMultiNullWithTimeout(25000);
		testPollMultiNullWithTimeout(45000);
	}
	
	private void testPollMultiNullWithTimeout(long timeout){
		Config config = new Config().setTimeoutMs(timeout);
		long time = System.currentTimeMillis();
		List<TestDatabean> retrievedDatabeans = router.testDatabean.pollMulti(config);
		Assert.assertTrue((System.currentTimeMillis() - time) < timeout + 1000);
		Assert.assertTrue((System.currentTimeMillis() - time) >= timeout);
		Assert.assertTrue(retrievedDatabeans.size() == 0);
	}
	
	private static String makeRandomString(){
		return UUID.randomUUID().toString();
	}
}

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
import com.hotpads.datarouter.client.imp.sqs.encode.SqsEncoder;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.test.TestDatabean;
import com.hotpads.util.core.concurrent.ThreadTool;

@Guice(moduleFactory = DatarouterSqsTestModuleFactory.class)
@Test(singleThreaded=true)
public class SqsNodeIntegrationTests{
	private static final int DATABEAN_COUNT = 15;
	
	@Inject
	private SqsTestRouter router;
	@Inject
	private SqsEncoder sqsEncoder;
	
	@BeforeMethod
	public void setUp(){
		cleanUp();
	}
	
	@AfterMethod
	public void tearDown(){
		cleanUp();
	}
	
	private void cleanUp(){
		TestDatabean databean;
		while((databean = router.testDatabean.poll(null)) != null){
			System.out.println("Removed " + databean);
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
		int emptyDatabeanSize = sqsEncoder.encode(new TestDatabean("", "", "")).getBytes().length;
		String longString = makeStringOfByteSize(size - emptyDatabeanSize);
		TestDatabean databean = new TestDatabean(longString, "", "");
		router.testDatabean.put(databean, null);
	}
	
	private static String makeStringOfByteSize(int requiredSize){
		Assert.assertEquals("a".getBytes().length, 1);
		StringBuilder longString = new StringBuilder();
		for(int size = 0 ; size < requiredSize ; size++){
			longString.append("a");
		}
		return longString.toString();
	}
	
	@Test
	public void testPutAndPoll(){
		TestDatabean databean = new TestDatabean(makeRandomString(), makeRandomString(), makeRandomString());
		router.testDatabean.put(databean, null);
		TestDatabean retrievedDatabean;
		while((retrievedDatabean = router.testDatabean.poll(null)) == null){
			ThreadTool.sleep(1000);
		}
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
		ThreadTool.sleep(2000);
		Set<Integer> ids = new HashSet<>();
		List<TestDatabean> retrievedDatabeans;
		do{
			retrievedDatabeans = router.testDatabean.pollMulti(new Config().setLimit(5));
			for(TestDatabean databean : retrievedDatabeans){
				Integer id = Integer.valueOf(databean.getA());
				Assert.assertTrue(id < DATABEAN_COUNT);
				Assert.assertTrue(id >= 0);
				ids.add(id);
			}
		}while(retrievedDatabeans.size() > 0 || ids.size() < DATABEAN_COUNT);
	}
	
	private static String makeRandomString(){
		return UUID.randomUUID().toString();
	}

}

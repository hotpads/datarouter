package com.hotpads.datarouter.test.sqs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import com.hotpads.datarouter.client.imp.sqs.config.DatarouterSqsTestModuleFactory;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.test.TestDatabean;

//This test passes *almost* every time
@Guice(moduleFactory = DatarouterSqsTestModuleFactory.class)
@Test(singleThreaded=true)
public class SqsNodeIntegrationTester{
	private static final int DATABEAN_COUNT = 15;
	
	@Inject
	private SqsTestRouter router;
	
	@BeforeMethod
	public void setUp(){
		TestDatabean databean;
		while((databean = router.testDatabean.poll(null)) != null){
			System.out.println("Removed " + databean);
		}
	}
	
	@Test
	public void testPutAndPoll(){
		TestDatabean databean = new TestDatabean(makeRandomString(), makeRandomString(), makeRandomString());
		router.testDatabean.put(databean, null);
		TestDatabean retrievedDatabean = router.testDatabean.poll(null);
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
			retrievedDatabeans = router.testDatabean.pollMulti(new Config().setLimit(5));
			for(TestDatabean databean : retrievedDatabeans){
				Integer id = Integer.valueOf(databean.getA());
				Assert.assertTrue(id < DATABEAN_COUNT);
				Assert.assertTrue(id >= 0);
				ids.add(id);
			}
		}while(retrievedDatabeans.size() > 0);
		Assert.assertTrue(ids.size() >= DATABEAN_COUNT);//at least once delivery
	}
	
	private static String makeRandomString(){
		return UUID.randomUUID().toString();
	}

}

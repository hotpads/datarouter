package com.hotpads.datarouter.test.sqs;

import javax.inject.Inject;

import org.testng.Assert;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import com.hotpads.datarouter.client.imp.sqs.config.DatarouterSqsTestModuleFactory;
import com.hotpads.datarouter.test.TestDatabean;

@Guice(moduleFactory = DatarouterSqsTestModuleFactory.class)
public class SqsNodeIntegrationTests{
	
	@Inject
	private SqsTestRouter router;
	
	@Test
	public void test(){
		TestDatabean databean = new TestDatabean("foo", "bar", "baz");
		router.testDatabean.put(databean, null);
		TestDatabean retrievedDatabean = router.testDatabean.poll(null);
		Assert.assertEquals(databean.getA(), retrievedDatabean.getA());
		Assert.assertEquals(databean.getB(), retrievedDatabean.getB());
		Assert.assertEquals(databean.getC(), retrievedDatabean.getC());
		Assert.assertNull(router.testDatabean.poll(null));
	}

}

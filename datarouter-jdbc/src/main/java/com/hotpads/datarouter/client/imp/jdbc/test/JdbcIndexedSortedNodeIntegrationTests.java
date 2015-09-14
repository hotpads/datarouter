package com.hotpads.datarouter.client.imp.jdbc.test;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import com.hotpads.datarouter.client.imp.jdbc.TestDatarouterJdbcModuleFactory;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.test.DrTestConstants;
import com.hotpads.datarouter.test.node.basic.sorted.BaseIndexedNodeIntegrationTests;
import com.hotpads.datarouter.util.core.DrIterableTool;

@Guice(moduleFactory = TestDatarouterJdbcModuleFactory.class)
public class JdbcIndexedSortedNodeIntegrationTests extends BaseIndexedNodeIntegrationTests{

	@BeforeClass
	public void beforeClass(){
		setup(DrTestConstants.CLIENT_drTestJdbc0, true, false);
	}

	@AfterClass
	public void afterClass(){
		testIndexedDelete();
		datarouter.shutdown();
	}

	@Test
	public void testScanLimit(){
		long count = DrIterableTool.count(router.sortedBean().scan(null, null));
		Assert.assertNotEquals(0, count);
		Assert.assertEquals(scanAndCountWithConfig(new Config().setIterateBatchSize(10)), count);
		Assert.assertEquals(scanAndCountWithConfig(new Config().setLimit((int)count)), count);
		Assert.assertEquals(scanAndCountWithConfig(new Config().setLimit(10)), 10);
		Assert.assertEquals(scanAndCountWithConfig(new Config().setLimit((int)(2*count))), count);
		Assert.assertEquals(scanAndCountWithConfig(new Config().setIterateBatchSize(10).setLimit(100)), 100);
	}
	
	private long scanAndCountWithConfig(Config config){
		return DrIterableTool.count(router.sortedBean().scan(null, config));
	}
}

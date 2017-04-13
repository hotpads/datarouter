package com.hotpads.datarouter.client.imp.jdbc.test;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;

import com.hotpads.datarouter.client.imp.jdbc.TestDatarouterJdbcModuleFactory;
import com.hotpads.datarouter.test.DatarouterTestClientIds;
import com.hotpads.datarouter.test.node.basic.sorted.BaseIndexedNodeIntegrationTests;

@Guice(moduleFactory = TestDatarouterJdbcModuleFactory.class)
public class JdbcIndexedSortedNodeIntegrationTests extends BaseIndexedNodeIntegrationTests{

	@BeforeClass
	public void beforeClass(){
		setup(DatarouterTestClientIds.jdbc0, false);
	}

	@AfterClass
	public void afterClass(){
		postTestTests();
		datarouter.shutdown();
	}

	@Override
	protected void testIgnoreNull(){
		// Skip because feature is not yet implemented
	}

}

package com.hotpads.datarouter.client.imp.jdbc.test;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;

import com.hotpads.datarouter.client.imp.jdbc.DatarouterJdbcGuiceModule.DatarouterJdbcModuleFactory;
import com.hotpads.datarouter.test.DRTestConstants;
import com.hotpads.datarouter.test.node.basic.sorted.BaseIndexedNodeIntegrationTests;

@Guice(moduleFactory = DatarouterJdbcModuleFactory.class)
public class JdbcIndexedSortedNodeIntegrationTests extends BaseIndexedNodeIntegrationTests{

	@BeforeClass
	public void beforeClass(){
		setup(DRTestConstants.CLIENT_drTestJdbc0, true, false);
	}

	@AfterClass
	public void afterClass(){
		testIndexedDelete();
		drContext.shutdown();
	}
}

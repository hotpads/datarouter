package com.hotpads.datarouter.client.imp.hibernate.test;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;

import com.hotpads.datarouter.client.imp.jdbc.TestDatarouterJdbcModuleFactory;
import com.hotpads.datarouter.test.DrTestConstants;
import com.hotpads.datarouter.test.node.basic.sorted.BaseIndexedNodeIntegrationTests;

@Guice(moduleFactory = TestDatarouterJdbcModuleFactory.class)
public class HibernateIndexedSortedNodeIntegrationTests extends BaseIndexedNodeIntegrationTests{

	@BeforeClass
	public void beforeClass(){
		setup(DrTestConstants.CLIENT_drTestHibernate0, false, false);
	}

	@AfterClass
	public void afterClass(){
		testIndexedDelete();
		drContext.shutdown();
	}
}

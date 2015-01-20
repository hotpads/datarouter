package com.hotpads.datarouter.test.node.basic.sorted.test;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.hotpads.datarouter.test.DRTestConstants;
import com.hotpads.datarouter.test.node.basic.sorted.BaseIndexedNodeIntegrationTests;

public class JdbcIndexedSortedNodeIntegrationTests extends BaseIndexedNodeIntegrationTests{

	@BeforeClass
	public static void beforeClass(){
		setup(DRTestConstants.CLIENT_drTestJdbc0, true, false);
	}

	@AfterClass
	public static void afterClass(){
		testIndexedDelete();
		drContext.shutdown();
	}
}

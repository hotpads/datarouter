package com.hotpads.datarouter.test.node.basic.sorted.test;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import com.hotpads.datarouter.test.DRTestConstants;
import com.hotpads.datarouter.test.node.basic.sorted.BaseSortedNodeIntegrationTests;


public class HBaseSortedNodeIntegrationTests extends BaseSortedNodeIntegrationTests{

	@BeforeClass
	public static void beforeClass(){
		setup(DRTestConstants.CLIENT_drTestHBase, true, false);
	}

	@AfterClass
	public static void afterClass(){
		testSortedDelete();
		drContext.shutdown();
	}
}
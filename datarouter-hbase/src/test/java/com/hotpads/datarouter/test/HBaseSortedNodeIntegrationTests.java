package com.hotpads.datarouter.test;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import com.hotpads.datarouter.test.DrTestConstants;
import com.hotpads.datarouter.test.node.basic.sorted.BaseSortedNodeIntegrationTests;

public class HBaseSortedNodeIntegrationTests extends BaseSortedNodeIntegrationTests{

	@BeforeClass
	public void beforeClass(){
		setup(DrTestConstants.CLIENT_drTestHBase, true, false);
	}

	@AfterClass
	public void afterClass(){
		testSortedDelete();
		datarouter.shutdown();
	}

}
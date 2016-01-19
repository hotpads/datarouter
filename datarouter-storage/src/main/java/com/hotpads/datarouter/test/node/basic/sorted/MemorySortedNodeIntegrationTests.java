package com.hotpads.datarouter.test.node.basic.sorted;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import com.hotpads.datarouter.test.DrTestConstants;

public class MemorySortedNodeIntegrationTests extends BaseSortedNodeIntegrationTests{

	@BeforeClass
	public void beforeClass(){
		setup(DrTestConstants.CLIENT_drTestMemory, true, false);
	}

	@AfterClass
	public void afterClass(){
		testSortedDelete();
		datarouter.shutdown();
	}

}

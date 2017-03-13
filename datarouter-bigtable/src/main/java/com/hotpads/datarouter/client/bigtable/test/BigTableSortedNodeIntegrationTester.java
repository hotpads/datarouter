package com.hotpads.datarouter.client.bigtable.test;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import com.hotpads.datarouter.test.DrTestConstants;
import com.hotpads.datarouter.test.node.basic.sorted.BaseSortedNodeIntegrationTests;

//TODO need to configure jenkins
public class BigTableSortedNodeIntegrationTester extends BaseSortedNodeIntegrationTests{

	@BeforeClass
	public void beforeClass(){
		setup(DrTestConstants.CLIENT_drTestBigTable, false);
	}

	@AfterClass
	public void afterClass(){
		postTestTests();
		datarouter.shutdown();
	}

}

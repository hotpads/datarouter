package com.hotpads.datarouter.test;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;

import com.hotpads.datarouter.test.node.basic.map.BaseMapStorageIntegrationTests;

@Guice(moduleFactory = DatarouterStorageTestModuleFactory.class)
public class HBaseMapStorageIntegrationTests extends BaseMapStorageIntegrationTests{

	@BeforeClass
	public void beforeClass(){
		setup(DrTestConstants.CLIENT_drTestHBase);
	}

	@AfterClass
	public void afterClass(){
		datarouter.shutdown();
	}
}

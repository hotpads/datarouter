package com.hotpads.datarouter.test.node.basic.map;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;

import com.hotpads.datarouter.test.DatarouterStorageTestModuleFactory;
import com.hotpads.datarouter.test.DrTestConstants;

@Guice(moduleFactory=DatarouterStorageTestModuleFactory.class)
public class MemoryMapStorageIntegrationTests extends BaseMapStorageIntegrationTests{

	@BeforeClass
	public void beforeClass(){
		setup(DrTestConstants.CLIENT_drTestMemory, false);
	}
}
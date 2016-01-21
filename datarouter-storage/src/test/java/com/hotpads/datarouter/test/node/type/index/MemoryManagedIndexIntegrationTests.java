package com.hotpads.datarouter.test.node.type.index;

import org.testng.annotations.Guice;

import com.hotpads.datarouter.test.DatarouterTestModuleFactory;
import com.hotpads.datarouter.test.DrTestConstants;

@Guice(moduleFactory=DatarouterTestModuleFactory.class)
public class MemoryManagedIndexIntegrationTests extends BaseManagedIndexIntegrationTests{

	public MemoryManagedIndexIntegrationTests(){
		super(DrTestConstants.CLIENT_drTestMemory);
	}

}

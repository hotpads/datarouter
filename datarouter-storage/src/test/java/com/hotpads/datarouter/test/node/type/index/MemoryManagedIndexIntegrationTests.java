package com.hotpads.datarouter.test.node.type.index;

import org.testng.annotations.Guice;

import com.hotpads.datarouter.test.DatarouterStorageTestModuleFactory;
import com.hotpads.datarouter.test.DrTestConstants;

@Guice(moduleFactory = DatarouterStorageTestModuleFactory.class)
public class MemoryManagedIndexIntegrationTests extends BaseManagedIndexIntegrationTests{

	public MemoryManagedIndexIntegrationTests(){
		super(DrTestConstants.CLIENT_drTestMemory);
	}
}

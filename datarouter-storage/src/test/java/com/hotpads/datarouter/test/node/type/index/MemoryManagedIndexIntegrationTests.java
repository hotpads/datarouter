package com.hotpads.datarouter.test.node.type.index;

import org.testng.annotations.Guice;

import com.hotpads.datarouter.test.DatarouterStorageTestModuleFactory;
import com.hotpads.datarouter.test.DatarouterTestClientIds;

@Guice(moduleFactory = DatarouterStorageTestModuleFactory.class)
public class MemoryManagedIndexIntegrationTests extends BaseManagedIndexIntegrationTests{

	public MemoryManagedIndexIntegrationTests(){
		super(DatarouterTestClientIds.CLIENT_drTestMemory);
	}
}

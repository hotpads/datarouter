package com.hotpads.datarouter.test.node.basic.manyfield.test;

import org.testng.annotations.BeforeClass;

import com.hotpads.datarouter.test.DrTestConstants;
import com.hotpads.datarouter.test.node.basic.manyfield.BaseManyFieldIntegrationTests;

public class MemoryManyFieldIntegrationTests extends BaseManyFieldIntegrationTests{

	@BeforeClass
	public void beforeClass(){
		setup(DrTestConstants.CLIENT_drTestMemory, true);
	}

	@Override
	public boolean isMemory(){
		return true;
	}
}

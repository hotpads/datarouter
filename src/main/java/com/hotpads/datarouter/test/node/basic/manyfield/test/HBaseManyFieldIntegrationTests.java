package com.hotpads.datarouter.test.node.basic.manyfield.test;

import org.junit.BeforeClass;

import com.hotpads.datarouter.test.DRTestConstants;
import com.hotpads.datarouter.test.node.basic.manyfield.BaseManyFieldIntegrationTests;

public class HBaseManyFieldIntegrationTests extends BaseManyFieldIntegrationTests{

	@BeforeClass
	public static void beforeClass(){
		setup(DRTestConstants.CLIENT_drTestHBase, true);
	}
	
	@Override
	public boolean isHBase(){
		return true;
	}
}
package com.hotpads.datarouter.test.node.basic.manyfield.test;

import org.junit.BeforeClass;

import com.hotpads.datarouter.test.DRTestConstants;
import com.hotpads.datarouter.test.node.basic.manyfield.BaseManyFieldIntegrationTests;

public class HibernateManyFieldIntegrationTests extends BaseManyFieldIntegrationTests{

	@BeforeClass
	public static void beforeClass(){
		setup(DRTestConstants.CLIENT_drTestHibernate0, false);
	}
	
	@Override
	public boolean isHibernate(){
		return true;
	}
}

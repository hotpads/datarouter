package com.hotpads.datarouter.client.imp.hibernate.test;

import org.testng.annotations.BeforeClass;

import com.hotpads.datarouter.test.DRTestConstants;
import com.hotpads.datarouter.test.node.basic.manyfield.BaseManyFieldIntegrationTests;

public class HibernateManyFieldIntegrationTests extends BaseManyFieldIntegrationTests{

	@BeforeClass
	public void beforeClass(){
		setup(DRTestConstants.CLIENT_drTestHibernate0, false);
	}

	@Override
	public boolean isHibernate(){
		return true;
	}
}
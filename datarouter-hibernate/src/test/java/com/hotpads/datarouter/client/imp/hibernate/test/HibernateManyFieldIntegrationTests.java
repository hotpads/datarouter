package com.hotpads.datarouter.client.imp.hibernate.test;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;

import com.hotpads.datarouter.client.imp.jdbc.TestDatarouterJdbcModuleFactory;
import com.hotpads.datarouter.test.DrTestConstants;
import com.hotpads.datarouter.test.node.basic.manyfield.BaseManyFieldIntegrationTests;

@Guice(moduleFactory = TestDatarouterJdbcModuleFactory.class)
public class HibernateManyFieldIntegrationTests extends BaseManyFieldIntegrationTests{

	@BeforeClass
	public void beforeClass(){
		setup(DrTestConstants.CLIENT_drTestHibernate0, false);
	}

	@Override
	public boolean isHibernate(){
		return true;
	}
}

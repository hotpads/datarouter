package com.hotpads.datarouter.client.imp.jdbc.test;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;

import com.hotpads.datarouter.client.imp.jdbc.TestDatarouterJdbcModuleFactory;
import com.hotpads.datarouter.test.DRTestConstants;
import com.hotpads.datarouter.test.node.basic.manyfield.BaseManyFieldIntegrationTests;

@Guice(moduleFactory = TestDatarouterJdbcModuleFactory.class)
public class JdbcManyFieldIntegrationTests extends BaseManyFieldIntegrationTests{

	@BeforeClass
	public void beforeClass(){
		setup(DRTestConstants.CLIENT_drTestJdbc0, true);
	}

	@Override
	public boolean isJdbc(){
		return true;
	}

}

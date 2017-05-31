package com.hotpads.datarouter.client.imp.mysql.test;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;

import com.hotpads.datarouter.client.imp.mysql.TestDatarouterJdbcModuleFactory;
import com.hotpads.datarouter.test.DatarouterTestClientIds;
import com.hotpads.datarouter.test.node.basic.manyfield.BaseManyFieldIntegrationTests;

@Guice(moduleFactory = TestDatarouterJdbcModuleFactory.class)
public class JdbcManyFieldIntegrationTests extends BaseManyFieldIntegrationTests{

	@BeforeClass
	public void beforeClass(){
		setup(DatarouterTestClientIds.jdbc0);
	}

	@Override
	public boolean isJdbc(){
		return true;
	}

}

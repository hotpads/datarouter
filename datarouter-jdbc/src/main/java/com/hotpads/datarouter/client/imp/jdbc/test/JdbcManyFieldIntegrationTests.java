package com.hotpads.datarouter.client.imp.jdbc.test;

import org.testng.annotations.BeforeClass;

import com.hotpads.datarouter.test.DRTestConstants;
import com.hotpads.datarouter.test.node.basic.manyfield.BaseManyFieldIntegrationTests;

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

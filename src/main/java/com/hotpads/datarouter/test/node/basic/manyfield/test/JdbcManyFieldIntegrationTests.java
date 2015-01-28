package com.hotpads.datarouter.test.node.basic.manyfield.test;

import org.junit.BeforeClass;

import com.hotpads.datarouter.test.DRTestConstants;
import com.hotpads.datarouter.test.node.basic.manyfield.BaseManyFieldIntegrationTests;

public class JdbcManyFieldIntegrationTests extends BaseManyFieldIntegrationTests{

	@BeforeClass
	public static void beforeClass(){
		setup(DRTestConstants.CLIENT_drTestJdbc0, true);
	}

	@Override
	public boolean isJdbc(){
		return true;
	}
}

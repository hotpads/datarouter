package com.hotpads.datarouter.test.node.basic.map;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import com.hotpads.datarouter.test.DrTestConstants;

public class JdbcMapStorageIntegrationTests extends BaseMapStorageIntegrationTests{

	@BeforeClass
	public void beforeClass(){
		setup(DrTestConstants.CLIENT_drTestJdbc0);
	}

	@AfterClass
	public void afterClass(){
		datarouter.shutdown();
	}
}

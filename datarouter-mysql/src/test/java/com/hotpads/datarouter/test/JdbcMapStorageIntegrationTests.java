package com.hotpads.datarouter.test;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;

import com.hotpads.datarouter.client.imp.jdbc.TestDatarouterJdbcModuleFactory;
import com.hotpads.datarouter.test.node.basic.map.BaseMapStorageIntegrationTests;

@Guice(moduleFactory = TestDatarouterJdbcModuleFactory.class)
public class JdbcMapStorageIntegrationTests extends BaseMapStorageIntegrationTests{

	@BeforeClass
	public void beforeClass(){
		setup(DatarouterTestClientIds.jdbc0, false);
	}
}

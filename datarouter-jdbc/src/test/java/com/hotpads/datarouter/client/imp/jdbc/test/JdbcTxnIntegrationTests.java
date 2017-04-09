package com.hotpads.datarouter.client.imp.jdbc.test;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;

import com.hotpads.datarouter.client.imp.jdbc.TestDatarouterJdbcModuleFactory;
import com.hotpads.datarouter.test.DatarouterTestClientIds;
import com.hotpads.datarouter.test.client.txn.test.BaseTxnIntegrationTests;

@Guice(moduleFactory = TestDatarouterJdbcModuleFactory.class)
public class JdbcTxnIntegrationTests extends BaseTxnIntegrationTests{

	@BeforeClass
	public void beforeClass(){
		setup(DatarouterTestClientIds.jdbc0, true);
		resetTable();
	}
}


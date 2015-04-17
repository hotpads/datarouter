package com.hotpads.datarouter.client.imp.jdbc.test;

import org.testng.annotations.BeforeClass;

import com.hotpads.datarouter.test.DRTestConstants;
import com.hotpads.datarouter.test.client.txn.test.BaseTxnIntegrationTests;

public class JdbcTxnIntegrationTests extends BaseTxnIntegrationTests{

	@BeforeClass
	public static void beforeClass(){
		setup(DRTestConstants.CLIENT_drTestJdbc0, true);
		resetTable();
	}
}

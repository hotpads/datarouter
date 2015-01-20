package com.hotpads.datarouter.test.client.txn.test;

import org.junit.BeforeClass;

import com.hotpads.datarouter.test.DRTestConstants;

public class JdbcTxnIntegrationTests extends BaseTxnIntegrationTests{

	@BeforeClass
	public static void beforeClass(){
		setup(DRTestConstants.CLIENT_drTestJdbc0, true);
		resetTable();
	}
}

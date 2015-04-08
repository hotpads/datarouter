package com.hotpads.datarouter.client.imp.hibernate.test;

import org.testng.annotations.BeforeClass;

import com.hotpads.datarouter.test.DRTestConstants;
import com.hotpads.datarouter.test.client.txn.test.BaseTxnIntegrationTests;

public class HibernateTxnIntegrationTests extends BaseTxnIntegrationTests{

	@BeforeClass
	public static void beforeClass(){
		setup(DRTestConstants.CLIENT_drTestHibernate0, false);
		resetTable();
	}

	@Override
	protected boolean hasSession(){
		return true;
	}
}

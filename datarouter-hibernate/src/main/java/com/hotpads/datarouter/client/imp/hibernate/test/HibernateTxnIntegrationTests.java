package com.hotpads.datarouter.client.imp.hibernate.test;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;

import com.hotpads.datarouter.client.imp.jdbc.DatarouterJdbcGuiceModule.DatarouterJdbcModuleFactory;
import com.hotpads.datarouter.test.DRTestConstants;
import com.hotpads.datarouter.test.client.txn.test.BaseTxnIntegrationTests;

@Guice(moduleFactory = DatarouterJdbcModuleFactory.class)
public class HibernateTxnIntegrationTests extends BaseTxnIntegrationTests{

	@BeforeClass
	public void beforeClass(){
		setup(DRTestConstants.CLIENT_drTestHibernate0, false);
		resetTable();
	}

	@Override
	protected boolean hasSession(){
		return true;
	}
}

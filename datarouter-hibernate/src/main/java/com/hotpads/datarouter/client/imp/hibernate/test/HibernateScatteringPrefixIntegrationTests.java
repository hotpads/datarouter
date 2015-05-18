package com.hotpads.datarouter.client.imp.hibernate.test;

import java.io.IOException;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;

import com.hotpads.datarouter.client.imp.jdbc.TestDatarouterJdbcGuiceModule.TestDatarouterJdbcModuleFactory;
import com.hotpads.datarouter.test.DRTestConstants;
import com.hotpads.datarouter.test.node.basic.prefixed.test.BaseScatteringPrefixIntegrationTests;

@Guice(moduleFactory = TestDatarouterJdbcModuleFactory.class)
public class HibernateScatteringPrefixIntegrationTests extends BaseScatteringPrefixIntegrationTests{

	@BeforeClass
	public void beforeClass() throws IOException{
		setup(DRTestConstants.CLIENT_drTestHibernate0);
	}

}

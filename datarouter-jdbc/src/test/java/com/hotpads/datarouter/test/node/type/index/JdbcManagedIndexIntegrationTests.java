package com.hotpads.datarouter.test.node.type.index;

import org.testng.annotations.Guice;

import com.hotpads.datarouter.client.imp.jdbc.TestDatarouterJdbcModuleFactory;
import com.hotpads.datarouter.test.DatarouterTestClientIds;

@Guice(moduleFactory=TestDatarouterJdbcModuleFactory.class)
public class JdbcManagedIndexIntegrationTests extends BaseManagedIndexIntegrationTests{

	public JdbcManagedIndexIntegrationTests(){
		super(DatarouterTestClientIds.CLIENT_drTestJdbc0);
	}

}

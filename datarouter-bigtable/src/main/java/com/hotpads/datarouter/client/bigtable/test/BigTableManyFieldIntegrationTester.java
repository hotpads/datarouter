package com.hotpads.datarouter.client.bigtable.test;

import org.testng.annotations.BeforeClass;

import com.hotpads.datarouter.test.DatarouterTestClientIds;
import com.hotpads.datarouter.test.node.basic.manyfield.BaseManyFieldIntegrationTests;

// need to configure jenkins
public class BigTableManyFieldIntegrationTester extends BaseManyFieldIntegrationTests{

	@BeforeClass
	public void beforeClass(){
		setup(DatarouterTestClientIds.CLIENT_drTestBigTable);
	}

	@Override
	public boolean isHBase(){
		return true;
	}

}

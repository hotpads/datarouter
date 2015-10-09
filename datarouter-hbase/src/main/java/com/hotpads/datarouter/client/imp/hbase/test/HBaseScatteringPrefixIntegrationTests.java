package com.hotpads.datarouter.client.imp.hbase.test;

import org.testng.annotations.BeforeClass;

import com.hotpads.datarouter.test.DrTestConstants;
import com.hotpads.datarouter.test.node.basic.prefixed.test.BaseScatteringPrefixIntegrationTests;

public class HBaseScatteringPrefixIntegrationTests extends BaseScatteringPrefixIntegrationTests{

	@BeforeClass
	public void beforeClass(){
		setup(DrTestConstants.CLIENT_drTestHBase);
	}

}

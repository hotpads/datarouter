package com.hotpads.datarouter.test;

import org.testng.annotations.BeforeClass;

import com.hotpads.datarouter.test.node.basic.prefixed.test.BaseScatteringPrefixIntegrationTests;

public class HBaseScatteringPrefixIntegrationTests extends BaseScatteringPrefixIntegrationTests{

	@BeforeClass
	public void beforeClass(){
		setup(DatarouterTestClientIds.hbase);
	}

}

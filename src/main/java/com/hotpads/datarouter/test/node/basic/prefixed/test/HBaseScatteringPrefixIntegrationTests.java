package com.hotpads.datarouter.test.node.basic.prefixed.test;

import java.io.IOException;

import org.testng.annotations.BeforeClass;

import com.hotpads.datarouter.test.DRTestConstants;

public class HBaseScatteringPrefixIntegrationTests extends BaseScatteringPrefixIntegrationTests{

	@BeforeClass
	public void beforeClass() throws IOException{
		setup(DRTestConstants.CLIENT_drTestHBase);
	}

}

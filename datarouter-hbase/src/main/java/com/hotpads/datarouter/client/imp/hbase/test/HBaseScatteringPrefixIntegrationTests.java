package com.hotpads.datarouter.client.imp.hbase.test;

import java.io.IOException;

import org.testng.annotations.BeforeClass;

import com.hotpads.datarouter.test.DRTestConstants;
import com.hotpads.datarouter.test.node.basic.prefixed.test.BaseScatteringPrefixIntegrationTests;

public class HBaseScatteringPrefixIntegrationTests extends BaseScatteringPrefixIntegrationTests{

	@BeforeClass
	public void beforeClass() throws IOException{
		setup(DRTestConstants.CLIENT_drTestHBase);
	}

}
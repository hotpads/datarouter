package com.hotpads.datarouter.test.node.basic.sorted.test;

import org.junit.BeforeClass;

import com.hotpads.datarouter.client.imp.jdbc.JdbcClientType;
import com.hotpads.datarouter.test.DRTestConstants;
import com.hotpads.datarouter.test.node.basic.sorted.BaseIndexedNodeIntegrationTests;

public class JdbcIndexedNodeIntegrationTests extends BaseIndexedNodeIntegrationTests{

	@BeforeClass
	public static void beforeClass(){
		setup(DRTestConstants.CLIENT_drTestJdbc0, JdbcClientType.INSTANCE, true);
	}
	
}

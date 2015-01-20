package com.hotpads.datarouter.test.node.basic.prefixed.test;

import java.io.IOException;

import org.junit.BeforeClass;

import com.hotpads.datarouter.test.DRTestConstants;

public class HibernateScatteringPrefixIntegrationTests extends BaseScatteringPrefixIntegrationTests{

	@BeforeClass
	public static void beforeClass() throws IOException{
		setup(DRTestConstants.CLIENT_drTestHibernate0);
	}

}

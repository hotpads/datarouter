package com.hotpads.datarouter.client.imp.memcached.test;

import org.testng.annotations.BeforeClass;

import com.hotpads.datarouter.test.DatarouterTestClientIds;
import com.hotpads.datarouter.test.node.basic.manyfield.BaseManyFieldIntegrationTests;

public class MemcachedManyFieldIntegrationTests extends BaseManyFieldIntegrationTests{

	@BeforeClass
	public void beforeClass(){
		setup(DatarouterTestClientIds.memcached);
	}

	@Override
	public boolean isMemcached(){
		return true;
	}
}

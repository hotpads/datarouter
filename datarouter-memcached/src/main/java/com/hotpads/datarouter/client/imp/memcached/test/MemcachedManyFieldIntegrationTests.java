package com.hotpads.datarouter.client.imp.memcached;

import org.testng.annotations.BeforeClass;

import com.hotpads.datarouter.test.DRTestConstants;
import com.hotpads.datarouter.test.node.basic.manyfield.BaseManyFieldIntegrationTests;

public class MemcachedManyFieldIntegrationTests extends BaseManyFieldIntegrationTests{

	@BeforeClass
	public void beforeClass(){
		setup(DRTestConstants.CLIENT_drTestMemcached, true);
	}

	@Override
	public boolean isMemcached(){
		return true;
	}
}

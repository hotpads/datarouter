package com.hotpads.datarouter.client.imp.redis.test;

import org.testng.annotations.BeforeClass;

import com.hotpads.datarouter.test.DatarouterTestClientIds;
import com.hotpads.datarouter.test.node.basic.manyfield.BaseManyFieldIntegrationTests;

public class RedisManyFieldIntegrationTests extends BaseManyFieldIntegrationTests{

	@BeforeClass
	public void beforeClass(){
		setup(DatarouterTestClientIds.redis);
	}

	@Override
	public boolean isRedis(){
		return true;
	}
}
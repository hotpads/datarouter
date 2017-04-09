package com.hotpads.datarouter.client.imp.redis.test;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;

import com.hotpads.datarouter.test.DatarouterStorageTestModuleFactory;
import com.hotpads.datarouter.test.DatarouterTestClientIds;
import com.hotpads.datarouter.test.node.basic.map.BaseMapStorageIntegrationTests;

@Guice(moduleFactory = DatarouterStorageTestModuleFactory.class)
public class RedisMapStorageIntegrationTests extends BaseMapStorageIntegrationTests{

	@BeforeClass
	public void beforeClass(){
		setup(DatarouterTestClientIds.CLIENT_drTestRedis, false);
	}
}

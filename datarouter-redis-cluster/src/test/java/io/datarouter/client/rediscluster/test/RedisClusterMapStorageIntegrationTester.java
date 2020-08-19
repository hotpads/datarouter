/**
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.client.rediscluster.test;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;

import io.datarouter.client.rediscluster.DatarouterRedisClusterTestNgModuleFactory;
import io.datarouter.client.rediscluster.RedisClusterTestClientIds;
import io.datarouter.storage.test.node.basic.map.BaseMapStorageIntegrationTests;

// tester since it needs to point to a redis-cluster
@Guice(moduleFactory = DatarouterRedisClusterTestNgModuleFactory.class)
public class RedisClusterMapStorageIntegrationTester extends BaseMapStorageIntegrationTests{

	@BeforeClass
	public void beforeClass(){
		setup(RedisClusterTestClientIds.REDIS_CLUSTER, false);
	}

}
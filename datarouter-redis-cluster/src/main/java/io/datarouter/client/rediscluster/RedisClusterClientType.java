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
package io.datarouter.client.rediscluster;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.client.rediscluster.client.RedisClusterClientManager;
import io.datarouter.client.rediscluster.web.RedisClusterWebInspector;
import io.datarouter.storage.client.ClientType;
import io.datarouter.web.browse.DatarouterClientWebInspectorRegistry;

@Singleton
public class RedisClusterClientType implements ClientType<RedisClusterClientNodeFactory,RedisClusterClientManager>{

	public static final String NAME = "redis-cluster";

	@Inject
	public RedisClusterClientType(DatarouterClientWebInspectorRegistry datarouterClientWebInspectorRegistry){
		datarouterClientWebInspectorRegistry.register(NAME, RedisClusterWebInspector.class);
	}

	@Override
	public String getName(){
		return NAME;
	}

	@Override
	public Class<RedisClusterClientNodeFactory> getClientNodeFactoryClass(){
		return RedisClusterClientNodeFactory.class;
	}

	@Override
	public Class<RedisClusterClientManager> getClientManagerClass(){
		return RedisClusterClientManager.class;
	}

}

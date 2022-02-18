/*
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
package io.datarouter.aws.memcached;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.aws.memcached.client.AwsMemcachedClientManager;
import io.datarouter.aws.memcached.client.nodefactory.MemcachedMapStorageClientNodeFactory;
import io.datarouter.aws.memcached.web.AwsMemcachedWebInspector;
import io.datarouter.storage.client.ClientType;
import io.datarouter.web.browse.DatarouterClientWebInspectorRegistry;

@Singleton
public class MemcachedMapStorageClientType
implements ClientType<MemcachedMapStorageClientNodeFactory,AwsMemcachedClientManager>{

	public static final String NAME = "mapStorageMemcached";

	@Inject
	public MemcachedMapStorageClientType(DatarouterClientWebInspectorRegistry datarouterClientWebInspectorRegistry){
		datarouterClientWebInspectorRegistry.register(NAME, AwsMemcachedWebInspector.class);
	}

	@Override
	public String getName(){
		return NAME;
	}

	@Override
	public Class<MemcachedMapStorageClientNodeFactory> getClientNodeFactoryClass(){
		return MemcachedMapStorageClientNodeFactory.class;
	}

	@Override
	public Class<AwsMemcachedClientManager> getClientManagerClass(){
		return AwsMemcachedClientManager.class;
	}

}
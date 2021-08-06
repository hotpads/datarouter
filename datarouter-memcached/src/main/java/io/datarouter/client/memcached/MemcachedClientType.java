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
package io.datarouter.client.memcached;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.client.memcached.client.MemcachedClientManager;
import io.datarouter.client.memcached.client.nodefactory.MemcachedClientNodeFactory;
import io.datarouter.client.memcached.web.MemcachedWebInspector;
import io.datarouter.storage.client.ClientType;
import io.datarouter.web.browse.DatarouterClientWebInspectorRegistry;

@Singleton
public class MemcachedClientType implements ClientType<MemcachedClientNodeFactory,MemcachedClientManager>{

	public static final String NAME = "memcached";

	@Inject
	public MemcachedClientType(DatarouterClientWebInspectorRegistry datarouterClientWebInspectorRegistry){
		datarouterClientWebInspectorRegistry.register(NAME, MemcachedWebInspector.class);
	}

	@Override
	public String getName(){
		return NAME;
	}

	@Override
	public Class<MemcachedClientNodeFactory> getClientNodeFactoryClass(){
		return MemcachedClientNodeFactory.class;
	}

	@Override
	public Class<MemcachedClientManager> getClientManagerClass(){
		return MemcachedClientManager.class;
	}

}

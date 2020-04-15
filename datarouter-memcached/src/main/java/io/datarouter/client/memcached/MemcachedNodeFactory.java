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
package io.datarouter.client.memcached;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.client.memcached.client.MemcachedClientManager;
import io.datarouter.client.memcached.node.MemcachedNode;
import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.node.NodeParams;

@Singleton
public class MemcachedNodeFactory{

	@Inject
	private MemcachedClientType clientType;
	@Inject
	private MemcachedClientManager clientManager;

	public <PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	MemcachedNode<PK,D,F> createTallyNode(NodeParams<PK,D,F> params){
		return new MemcachedNode<>(params, clientType, clientManager, params.getClientId());
	}

}

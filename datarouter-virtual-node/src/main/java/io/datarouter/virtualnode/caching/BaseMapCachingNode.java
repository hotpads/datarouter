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
package io.datarouter.virtualnode.caching;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.BaseNode;
import io.datarouter.storage.node.Node;
import io.datarouter.storage.node.NodeParams.NodeParamsBuilder;
import io.datarouter.storage.node.type.physical.PhysicalNode;

public abstract class BaseMapCachingNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends Node<PK,D,F>>
extends BaseNode<PK,D,F>{

	protected final N cachingNode;
	protected final N backingNode;

	protected long lastAttemptedContact = 0L;
	protected long lastContact = 0L;

	public BaseMapCachingNode(N cacheNode, N backingNode){
		super(new NodeParamsBuilder<>(
				backingNode.getFieldInfo().getDatabeanSupplier(),
				backingNode.getFieldInfo().getFielderSupplier())
				.build());
		this.cachingNode = cacheNode;
		this.backingNode = backingNode;
	}

	public void updateLastAttemptedContact(){
		lastAttemptedContact = System.currentTimeMillis();
	}

	public void updateLastContact(){
		lastContact = System.currentTimeMillis();
	}

	public static boolean useCache(Config config){
		if(config == null || config.getCacheOk() == null){
			return Config.DEFAULT_CACHE_OK;
		}
		return config.getCacheOk();
	}

	@Override
	public String getName(){
		return getClass().getSimpleName() + "[" + cachingNode.getName() + "," + backingNode.getName() + "]";
	}

	@Override
	public List<ClientId> getClientIds(){
		Set<ClientId> clientIds = new HashSet<>(backingNode.getClientIds());
		clientIds.addAll(cachingNode.getClientIds());
		return new ArrayList<>(clientIds);
	}

	@Override
	public List<? extends Node<PK,D,F>> getChildNodes(){
		return List.of(backingNode, cachingNode);
	}

	@Override
	public List<? extends PhysicalNode<PK,D,F>> getPhysicalNodes(){
		return backingNode.getPhysicalNodes();
	}

	@Override
	public List<? extends PhysicalNode<PK,D,F>> getPhysicalNodesForClient(String clientName){
		return backingNode.getPhysicalNodesForClient(clientName);
	}

	@Override
	public boolean usesClient(String clientName){
		return cachingNode.usesClient(clientName) || backingNode.usesClient(clientName);
	}

	public N getBackingNode(){
		return backingNode;
	}

	public N getCachingNode(){
		return cachingNode;
	}

}

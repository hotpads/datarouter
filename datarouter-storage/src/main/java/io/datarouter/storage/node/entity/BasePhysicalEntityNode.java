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
package io.datarouter.storage.node.entity;

import java.util.HashMap;
import java.util.Map;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.entity.Entity;
import io.datarouter.model.key.entity.EntityKey;
import io.datarouter.model.key.primary.EntityPrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.client.ClientTableNodeNames;
import io.datarouter.storage.serialize.fieldcache.EntityFieldInfo;

public abstract class BasePhysicalEntityNode<
		EK extends EntityKey<EK>,
		E extends Entity<EK>>
extends BaseEntityNode<EK,E>
implements PhysicalEntityNode<EK,E>{

	protected final EntityFieldInfo<EK,E> entityFieldInfo;
	private final ClientTableNodeNames clientTableNodeNames;//currently acting as a cache of superclass fields
	private final Map<String,SubEntitySortedMapStorageReaderNode<EK,?,?,?>> nodeByQualifierPrefix;

	public BasePhysicalEntityNode(EntityNodeParams<EK,E> entityNodeParams, ClientTableNodeNames clientTableNodeNames){
		super(clientTableNodeNames.getNodeName());
		this.entityFieldInfo = new EntityFieldInfo<>(entityNodeParams);
		this.clientTableNodeNames = clientTableNodeNames;
		this.nodeByQualifierPrefix = new HashMap<>();
	}

	@Override
	public <PK extends EntityPrimaryKey<EK,PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	void register(SubEntitySortedMapStorageReaderNode<EK,PK,D,F> subEntityNode){
		super.register(subEntityNode);
		nodeByQualifierPrefix.put(subEntityNode.getEntityNodePrefix(), subEntityNode);
	}

	@Override
	public String getClientName(){
		return clientTableNodeNames.getClientName();
	}

	@Override
	public String getTableName(){
		return clientTableNodeNames.getTableName();
	}

	@Override
	public Map<String,? extends SubEntitySortedMapStorageReaderNode<EK,?,?,?>> getNodeByQualifierPrefix(){
		return nodeByQualifierPrefix;
	}

	public EntityFieldInfo<EK,E> getEntityFieldInfo(){
		return entityFieldInfo;
	}

}

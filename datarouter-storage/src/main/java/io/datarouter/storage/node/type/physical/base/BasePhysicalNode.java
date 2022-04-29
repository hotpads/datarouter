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
package io.datarouter.storage.node.type.physical.base;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.ClientType;
import io.datarouter.storage.node.BaseNode;
import io.datarouter.storage.node.Node;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.storage.serialize.fieldcache.PhysicalDatabeanFieldInfo;

public abstract class BasePhysicalNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BaseNode<PK,D,F>
implements PhysicalNode<PK,D,F>{

	private final ClientType<?,?> clientType;
	private final PhysicalDatabeanFieldInfo<PK,D,F> fieldInfo;

	public BasePhysicalNode(NodeParams<PK,D,F> params, ClientType<?,?> clientType){
		super(params);
		this.clientType = clientType;
		this.fieldInfo = new PhysicalDatabeanFieldInfo<>(params);
	}

	@Override
	public final String getName(){
		return fieldInfo.getNodeName();
	}

	@Override
	public List<ClientId> getClientIds(){
		return Collections.singletonList(getFieldInfo().getClientId());
	}

	@Override
	public ClientId getClientId(){
		return getFieldInfo().getClientId();
	}

	@Override
	public boolean usesClient(String clientName){
		return Objects.equals(getFieldInfo().getClientId().getName(), clientName);
	}

	@Override
	public List<Node<PK,D,F>> getChildNodes(){
		return new ArrayList<>();
	}

	@Override
	public List<? extends PhysicalNode<PK,D,F>> getPhysicalNodes(){
		List<PhysicalNode<PK,D,F>> physicalNodes = new LinkedList<>();
		physicalNodes.add(this);
		return physicalNodes;
	}

	@Override
	public List<PhysicalNode<PK,D,F>> getPhysicalNodesForClient(String clientName){
		List<PhysicalNode<PK,D,F>> physicalNodes = new LinkedList<>();
		if(clientName.equals(getFieldInfo().getClientId().getName())){
			physicalNodes.add(this);
		}
		return physicalNodes;
	}

	@Override
	public String toString(){
		return this.getName();
	}

	@Override
	public ClientType<?,?> getClientType(){
		return clientType;
	}

	@Override
	public PhysicalDatabeanFieldInfo<PK,D,F> getFieldInfo(){
		return fieldInfo;
	}

}

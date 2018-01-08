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
package io.datarouter.storage.node.type.physical.base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.node.BaseNode;
import io.datarouter.storage.node.Node;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.util.collection.ListTool;
import io.datarouter.util.collection.SetTool;

public abstract class BasePhysicalNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BaseNode<PK,D,F>
implements PhysicalNode<PK,D,F>{

	/****************************** constructors ********************************/

	public BasePhysicalNode(NodeParams<PK,D,F> params){
		super(params);
	}

	/****************************** node methods ********************************/

	@Override
	public final String getName(){
		String name = getClientId().getName() + "." + getTableName();
		if(getFieldInfo().getEntityNodePrefix() != null){
			return name + "." + getFieldInfo().getEntityNodePrefix();
		}
		return name;
	}

	@Override
	public PhysicalNode<PK,D,F> getPhysicalNodeIfApplicable(){
		return this;
	}

	@Override
	public List<String> getClientNamesForPrimaryKeysForSchemaUpdate(Collection<PK> keys){
		return ListTool.createLinkedList(fieldInfo.getClientId().getName());
	}

	/********************** physical node methods *********************************/

	@Override
	public ClientId getClientId(){
		return fieldInfo.getClientId();
	}

	@Override
	public Set<String> getAllNames(){
		return SetTool.wrap(getName());
	}

	@Override
	public List<String> getClientNames(){
		return Collections.singletonList(getClientId().getName());
	}

	@Override
	public List<ClientId> getClientIds(){
		return Collections.singletonList(getClientId());
	}

	@Override
	public boolean usesClient(String clientName){
		return Objects.equals(getClientId().getName(), clientName);
	}

	@Override
	public Node<PK,D,F> getMaster(){
		return null;
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
		if(clientName.equals(getClientId().getName())){
			physicalNodes.add(this);
		}
		return physicalNodes;
	}

	@Override
	public String getTableName(){
		return fieldInfo.getTableName();
	}

	@Override
	public String toString(){
		return this.getName();
	}

}
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
package io.datarouter.storage.node.adapter;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.field.Field;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.node.Node;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.storage.serialize.fieldcache.DatabeanFieldInfo;

public abstract class BaseAdapter<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends Node<PK,D,F>>
implements Node<PK,D,F>{

	protected final N backingNode;

	public BaseAdapter(N backingNode){
		this.backingNode = backingNode;
	}

	@Override
	public Node<PK,D,F> getMaster(){
		return backingNode.getMaster();
	}

	@Override
	public PhysicalNode<PK,D,F> getPhysicalNodeIfApplicable(){
		return backingNode.getPhysicalNodeIfApplicable();
	}

	@Override
	public String getName(){
		return backingNode.getName();
	}

	@Override
	public Class<PK> getPrimaryKeyType(){
		return backingNode.getPrimaryKeyType();
	}

	@Override
	public DatabeanFieldInfo<PK,D,F> getFieldInfo(){
		return backingNode.getFieldInfo();
	}

	@Override
	public List<Field<?>> getFields(){
		return backingNode.getFields();
	}

	@Override
	public List<Field<?>> getNonKeyFields(D databean){
		return backingNode.getNonKeyFields(databean);
	}

	@Override
	public Set<String> getAllNames(){
		return backingNode.getAllNames();
	}

	@Override
	public List<String> getClientNames(){
		return backingNode.getClientNames();
	}

	@Override
	public List<ClientId> getClientIds(){
		return backingNode.getClientIds();
	}

	@Override
	public boolean usesClient(String clientName){
		return backingNode.usesClient(clientName);
	}

	@Override
	public List<String> getClientNamesForPrimaryKeysForSchemaUpdate(Collection<PK> keys){
		return backingNode.getClientNamesForPrimaryKeysForSchemaUpdate(keys);
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
	public List<? extends Node<PK,D,F>> getChildNodes(){
		return backingNode.getChildNodes();
	}

	@Override
	public int compareTo(Node<PK,D,F> that){
		return backingNode.compareTo(that);
	}

	@Override
	public String toString(){
		return getToStringPrefix() + "[" + backingNode.toString() + "]";
	}

	protected abstract String getToStringPrefix();

}

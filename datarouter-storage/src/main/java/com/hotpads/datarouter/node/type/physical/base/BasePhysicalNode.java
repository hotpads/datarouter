package com.hotpads.datarouter.node.type.physical.base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.node.BaseNode;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.NodeId;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.datarouter.util.core.DrObjectTool;
import com.hotpads.datarouter.util.core.DrSetTool;

public abstract class BasePhysicalNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BaseNode<PK,D,F>
implements PhysicalNode<PK,D>{

	/****************************** constructors ********************************/

	public BasePhysicalNode(NodeParams<PK,D,F> params){
		super(params);
		this.setId(new NodeId<>(getClass().getSimpleName(), params, fieldInfo.getExplicitNodeName()));
	}

	/****************************** node methods ********************************/

	@Override
	public boolean isPhysicalNodeOrWrapper(){
		return true;
	}

	@Override
	public PhysicalNode<PK,D> getPhysicalNodeIfApplicable(){
		return this;
	}

	@Override
	public List<String> getClientNamesForPrimaryKeysForSchemaUpdate(Collection<PK> keys) {
		return DrListTool.createLinkedList(fieldInfo.getClientId().getName());
	}

	/********************** physical node methods *********************************/

	@Override
	public ClientId getClientId(){
		return fieldInfo.getClientId();
	}

	@Override
	public Set<String> getAllNames(){
		return DrSetTool.wrap(getName());
	}

	@Override
	public List<String> getClientNames() {
		return Collections.singletonList(getClientId().getName());
	}

	@Override
	public List<ClientId> getClientIds(){
		return Collections.singletonList(getClientId());
	}

	@Override
	public boolean usesClient(String clientName) {
		return DrObjectTool.nullSafeEquals(getClientId().getName(), clientName);
	}

	@Override
	public Node<PK,D> getMaster() {
		return null;
	}

	@Override
	public List<Node<PK,D>> getChildNodes(){
		return new ArrayList<>();
	}

	@Override
	public List<? extends PhysicalNode<PK,D>> getPhysicalNodes() {
		List<PhysicalNode<PK,D>> physicalNodes = new LinkedList<>();
		physicalNodes.add(this);
		return physicalNodes;
	}

	@Override
	public List<PhysicalNode<PK,D>> getPhysicalNodesForClient(String clientName) {
		List<PhysicalNode<PK,D>> physicalNodes = new LinkedList<>();
		if(clientName.equals(getClientId().getName())){
			physicalNodes.add(this);
		}
		return physicalNodes;
	}

	@Override
	public String getTableName() {
		return fieldInfo.getTableName();
	}

	@Override
	public String getPackagedTableName(){
		return fieldInfo.getPackagedTableName();
	}

	@Override
	public String toString(){
		return this.getName();
	}
}

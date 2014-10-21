package com.hotpads.datarouter.node.type.physical.base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotpads.datarouter.node.BaseNode;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.NodeId;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.type.index.ManagedNode;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.view.index.IndexEntry;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.ObjectTool;
import com.hotpads.util.core.SetTool;

public abstract class BasePhysicalNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BaseNode<PK,D,F>
implements PhysicalNode<PK,D>
{
	protected Logger logger = LoggerFactory.getLogger(getClass());
	
	private List<ManagedNode<?,?,?>> managedNodes;
	
//	protected String clientName;
//	protected String tableName;
//	protected String packagedTableName;

	
	/****************************** constructors ********************************/
	
	public BasePhysicalNode(NodeParams<PK,D,F> params){
		super(params);
		managedNodes = new ArrayList<>();
		this.setId(new NodeId<PK,D,F>((Class<Node<PK,D>>)getClass(), params, fieldInfo.getExplicitNodeName()));
	}
	

	/****************************** node methods ********************************/
	
	@Override
	public List<String> getClientNamesForPrimaryKeysForSchemaUpdate(Collection<PK> keys) {
		return ListTool.createLinkedList(fieldInfo.getClientName());
	}

	/********************** physical node methods *********************************/
	
	public String getClientName() {
		return fieldInfo.getClientName();
	}

	@Override
	public Set<String> getAllNames(){
		return SetTool.wrap(getName());
	}

	@Override
	public List<String> getClientNames() {
		SortedSet<String> clientNames = SetTool.createTreeSet();
		clientNames.add(getClientName());
		return ListTool.createArrayList(clientNames);
	}

	@Override
	public boolean usesClient(String clientName) {
		return ObjectTool.nullSafeEquals(getClientName(), clientName);
	}
	
	@Override
	public Node<PK,D> getMaster() {
		return null;
	}
	
	@Override
	public List<Node<PK,D>> getChildNodes(){
		return ListTool.create();
	}
		
	@Override
	public List<? extends PhysicalNode<PK,D>> getPhysicalNodes() {
		List<PhysicalNode<PK,D>> physicalNodes = ListTool.createLinkedList();
		physicalNodes.add(this);
		return physicalNodes;
	}

	@Override
	public List<PhysicalNode<PK,D>> getPhysicalNodesForClient(String clientName) {
		List<PhysicalNode<PK,D>> physicalNodes = ListTool.createLinkedList();
		if(clientName.equals(getClientName())){
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
	
	@Override
	public 
	<IK extends PrimaryKey<IK>, 
	IE extends IndexEntry<IK, IE, PK, D>, 
	IF extends DatabeanFielder<IK, IE>, 
	N extends ManagedNode<IK, IE, IF>> N registerManaged(N managedNode){
		managedNodes.add(managedNode);
		return managedNode;
	}
	
	@Override
	public List<ManagedNode<?, ?, ?>> getManagedNodes(){
		return managedNodes;
	}

//	public abstract Client getClient();

}

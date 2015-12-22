package com.hotpads.datarouter.node.adapter;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.routing.Router;
import com.hotpads.datarouter.serialize.fieldcache.DatabeanFieldInfo;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public abstract class BaseAdapter<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends Node<PK,D>>
implements Node<PK,D>{

	protected final N backingNode;

	public BaseAdapter(N backingNode){
		this.backingNode = backingNode;
	}

	@Override
	public N getMaster() {
		return (N)backingNode.getMaster();
	}


	@Override
	public Datarouter getDatarouter(){
		return backingNode.getDatarouter();
	}


	@Override
	public Router getRouter(){
		return backingNode.getRouter();
	}


	@Override
	public boolean isPhysicalNodeOrWrapper(){
		return backingNode.isPhysicalNodeOrWrapper();
	}


	@Override
	public PhysicalNode<PK,D> getPhysicalNodeIfApplicable(){
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
	public DatabeanFieldInfo<PK,D,?> getFieldInfo(){
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
	public List<? extends PhysicalNode<PK,D>> getPhysicalNodes(){
		return backingNode.getPhysicalNodes();
	}


	@Override
	public List<? extends PhysicalNode<PK,D>> getPhysicalNodesForClient(String clientName){
		return backingNode.getPhysicalNodesForClient(clientName);
	}


	@Override
	public List<N> getChildNodes(){
		return (List<N>)backingNode.getChildNodes();
	}

	@Override
	public int compareTo(Node<PK,D> that){
		return backingNode.compareTo(that);
	}

	@Override
	public String toString(){
		return getToStringPrefix() + "[" + backingNode.toString() + "]";
	}

	protected abstract String getToStringPrefix();

}

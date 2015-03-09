package com.hotpads.datarouter.node.adapter.counter;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.routing.DatarouterContext;
import com.hotpads.datarouter.serialize.fieldcache.DatabeanFieldInfo;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.DRCounters;

public /*abstract*/ class BaseCounterAdapter<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends Node<PK,D>> 
implements Node<PK,D>{
	
	protected final N backingNode;
	
	public BaseCounterAdapter(N backingNode){
		this.backingNode = backingNode;
	}


	/**************** Comparable ************************/
	
	@Override
	public int compareTo(Node<PK,D> that){
		return backingNode.compareTo(that);
	}
	
	
	/*************************** Node *************************/
	
	@Override
	public N getMaster() {
		return (N)backingNode.getMaster();
	}


	@Override
	public DatarouterContext getDatarouterContext(){
		return backingNode.getDatarouterContext();
	}


	@Override
	public Datarouter getRouter(){
		return backingNode.getRouter();
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
	public Class<D> getDatabeanType(){
		return backingNode.getDatabeanType();
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
	public List<Field<?>> getNonKeyFields(D d){
		return backingNode.getNonKeyFields(d);
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
	
	
	/******************** count ************************/
	
	public String getTraceName(String opName){
		return backingNode.getName() + " " + opName;
	}
	
	public void count(String key){
		count(key, 1);
	}
	
	//overriden in BaseCounterPhysicalAdapter
	public void count(String key, long delta){
		DRCounters.incSuffixNode(key, backingNode.getName(), delta);
	}

	
	
	
	
	
}

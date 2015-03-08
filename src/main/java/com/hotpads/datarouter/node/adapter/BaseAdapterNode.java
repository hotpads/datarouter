package com.hotpads.datarouter.node.adapter;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.routing.DatarouterContext;
import com.hotpads.datarouter.serialize.fieldcache.DatabeanFieldInfo;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.callsite.CallsiteRecorder;
import com.hotpads.datarouter.util.core.DrBooleanTool;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.profile.callsite.LineOfCode;
import com.hotpads.util.core.cache.Cached;

public /*abstract*/ class BaseAdapterNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends Node<PK,D>> 
implements Node<PK,D>{
	
	protected N backingNode;
	private Cached<Boolean> recordCallsites;
	
	public BaseAdapterNode(NodeParams<PK,D,F> params, N backingNode){
//		super(params);
		this.backingNode = backingNode;
		this.recordCallsites = params.getRecordCallsites();
	}
	
	
	/*************************** node methods *************************/
	
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
	

	/**************** Comparable ************************/
	
	@Override
	public int compareTo(Node<PK,D> that){
		return backingNode.compareTo(that);
	}
	
	
	/******************** callsite ************************/
	
	public LineOfCode getCallsite(){
		LineOfCode callsite = new LineOfCode(3);//adjust for this method and adapter method
		return callsite;
	}
	
	public void recordCollectionCallsite(Config config, long startTimeNs, Collection<?> items){
		recordCallsite(config, startTimeNs, DrCollectionTool.size(items));
	}
	
	public void recordCallsite(Config config, long startNs, int numItems){
		if(recordCallsites == null || DrBooleanTool.isFalseOrNull(recordCallsites.get())){ return; }
		LineOfCode datarouterMethod = new LineOfCode(2);
		long durationNs = System.nanoTime() - startNs;
		CallsiteRecorder.record(backingNode.getName(), datarouterMethod.getMethodName(), config.getCallsite(),
				numItems, durationNs);
	}


	
	
	
	
	
}

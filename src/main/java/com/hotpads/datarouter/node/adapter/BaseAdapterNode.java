package com.hotpads.datarouter.node.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.BaseNode;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.callsite.CallsiteRecorder;
import com.hotpads.profile.callsite.LineOfCode;
import com.hotpads.util.core.BooleanTool;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.SetTool;
import com.hotpads.util.core.cache.Cached;

public abstract class BaseAdapterNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends Node<PK,D>> 
extends BaseNode<PK,D,F>{
	
	protected N backingNode;
	private Cached<Boolean> recordCallsites;
	
	public BaseAdapterNode(NodeParams<PK,D,F> params, N backingNode){
		super(params);
		this.backingNode = backingNode;
		this.recordCallsites = params.getRecordCallsites();
	}
	
	
	/*************************** node methods *************************/

	@Override
	public Set<String> getAllNames(){
		Set<String> names = SetTool.wrap(getName());
		names.addAll(backingNode.getAllNames());
		return names;
	}
	
	@Override
	public List<PhysicalNode<PK,D>> getPhysicalNodes() {
		List<PhysicalNode<PK,D>> all = new ArrayList<>();
		all.addAll(backingNode.getPhysicalNodes());
		return all;
	}

	@Override
	public List<PhysicalNode<PK,D>> getPhysicalNodesForClient(String clientName) {
		List<PhysicalNode<PK,D>> all = new ArrayList<>();
		all.addAll(backingNode.getPhysicalNodesForClient(clientName));
		return all;
	}
	

	@Override
	public List<String> getClientNames() {
		SortedSet<String> clientNames = new TreeSet<>();
		SetTool.nullSafeSortedAddAll(clientNames, backingNode.getClientNames());
		return ListTool.createArrayList(clientNames);
	}

	@Override
	public boolean usesClient(String clientName){
		if(backingNode.usesClient(clientName)){ return true; }
		return false;
	}

	@Override
	public List<String> getClientNamesForPrimaryKeysForSchemaUpdate(Collection<PK> keys) {
		return backingNode.getClientNamesForPrimaryKeysForSchemaUpdate(keys);
	}
	
	@Override
	public void clearThreadSpecificState(){
		if(this.backingNode!=null){ backingNode.clearThreadSpecificState(); }
	}
	
	/************************ callsite methods ***************************/
	
	@Override
	public N getMaster() {
		return backingNode;
	}
	
	@Override
	public List<N> getChildNodes(){
		return ListTool.wrap(backingNode);
	}
	
	
	public LineOfCode getCallsite(){
		LineOfCode callsite = new LineOfCode(3);//adjust for this method and adapter method
		return callsite;
	}
	
	public void recordCollectionCallsite(Config config, long startTimeNs, Collection<?> items){
		recordCallsite(config, startTimeNs, CollectionTool.size(items));
	}
	
	public void recordCallsite(Config config, long startNs, int numItems){
		if(recordCallsites == null || BooleanTool.isFalseOrNull(recordCallsites.get())){ return; }
		LineOfCode datarouterMethod = new LineOfCode(2);
		long durationNs = System.nanoTime() - startNs;
		CallsiteRecorder.record(backingNode.getName(), datarouterMethod.getMethodName(), config.getCallsite(),
				numItems, durationNs);
	}
	
}

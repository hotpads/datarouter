package com.hotpads.datarouter.node.type.caching.map.base;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.BaseNode;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.SetTool;

public abstract class BaseMapCachingNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends Node<PK,D>> 
extends BaseNode<PK,D,F>{
	
	
	/***************************** Node pass-through stuff **********************************/

	protected N cachingNode;
	protected N backingNode;
	
	protected Long lastAttemptedContact = 0L;
	protected Long lastContact = 0L;
	
	public BaseMapCachingNode(N cacheNode, N backingNode){
		super(backingNode.getDatabeanType(), (Class<F>)backingNode.getFieldInfo().getFielderClass());
		this.cachingNode = cacheNode;
		this.backingNode = backingNode;
		//use the inputs to make a unique name.  randomness will not place nicely with the counters
		this.name = backingNode.getName()+".cache";
//		this.name = fieldInfo.getDatabeanClass().getSimpleName()+"."+getClass().getSimpleName()+"."+System.identityHashCode(this);
	}

	
	@Override
	public void clearThreadSpecificState(){
	}

	public void updateLastAttemptedContact(){
		lastAttemptedContact = System.currentTimeMillis();
	}

	public void updateLastContact(){
		lastContact = System.currentTimeMillis();
	}

	/************************* util ***************************/
	
	public static boolean useCache(final Config config){
		if(config==null || config.getCacheOk()==null){ return Config.DEFAULT_CACHE_OK; }
		return config.getCacheOk();
	}
	
	
	
	/**************************************************************************/
	
	@Override
	public List<String> getClientNames() {
		SortedSet<String> clientNames = SetTool.createTreeSet();
		SetTool.nullSafeSortedAddAll(clientNames, cachingNode.getClientNames());
		SetTool.nullSafeSortedAddAll(clientNames, backingNode.getClientNames());
		return ListTool.createArrayList(clientNames);
	}

	@Override
	public List<String> getClientNamesForPrimaryKeysForSchemaUpdate(Collection<PK> keys) {
		return backingNode.getClientNamesForPrimaryKeysForSchemaUpdate(keys);
	}

	@Override
	public Class<D> getDatabeanType() {
		return backingNode.getDatabeanType();
	}

	@Override
	public Node<PK,D> getMaster() {
		return backingNode.getMaster();
	}
	
	@Override
	public List<? extends Node<PK,D>> getChildNodes(){
		if(backingNode==null){ return ListTool.create(); }
		return ListTool.wrap(backingNode);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Set<String> getAllNames(){
		Set<String> names = SetTool.createHashSet();
		names.add(name);
		names.addAll(CollectionTool.nullSafe(cachingNode.getAllNames()));
		names.addAll(CollectionTool.nullSafe(backingNode.getAllNames()));
		return names;
	}

	@Override
	public List<? extends PhysicalNode<PK,D>> getPhysicalNodes() {
		return backingNode.getPhysicalNodes();
	}

	@Override
	public List<? extends PhysicalNode<PK,D>> getPhysicalNodesForClient(String clientName) {
		return backingNode.getPhysicalNodesForClient(clientName);
	}

	@Override
	public boolean usesClient(String clientName) {
		return cachingNode.usesClient(clientName) || backingNode.usesClient(clientName);
	}

	public N getBackingNode(){
		return backingNode;
	}


	public N getCachingNode(){
		return cachingNode;
	}

	
	
}

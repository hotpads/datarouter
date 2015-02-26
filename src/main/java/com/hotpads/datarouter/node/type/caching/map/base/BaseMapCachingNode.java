package com.hotpads.datarouter.node.type.caching.map.base;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.BaseNode;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.NodeId;
import com.hotpads.datarouter.node.NodeParams.NodeParamsBuilder;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.datarouter.util.core.DrSetTool;

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
		super(new NodeParamsBuilder<PK,D,F>(backingNode.getRouter(), backingNode.getDatabeanType())
				.withFielder((Class<F>)backingNode.getFieldInfo().getFielderClass())
				.build());
		this.cachingNode = cacheNode;
		this.backingNode = backingNode;
		//use the inputs to make a unique name.  randomness will not place nicely with the counters
		this.setId(new NodeId<PK,D,F>((Class<Node<PK,D>>)getClass(), backingNode.getDatabeanType(), 
				backingNode.getRouter().getName(), null, null, backingNode.getName()+".cache"));
//		this.name = fieldInfo.getDatabeanClass().getSimpleName()+"."+getClass().getSimpleName()+"."+System.identityHashCode(this);
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
		SortedSet<String> clientNames = DrSetTool.createTreeSet();
		DrSetTool.nullSafeSortedAddAll(clientNames, cachingNode.getClientNames());
		DrSetTool.nullSafeSortedAddAll(clientNames, backingNode.getClientNames());
		return DrListTool.createArrayList(clientNames);
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
		if(backingNode==null){ return DrListTool.create(); }
		return DrListTool.wrap(backingNode);
	}

//	@Override
//	public String getName() {
//		return name;
//	}

	@Override
	public Set<String> getAllNames(){
		Set<String> names = DrSetTool.createHashSet();
		names.add(getName());
		names.addAll(DrCollectionTool.nullSafe(cachingNode.getAllNames()));
		names.addAll(DrCollectionTool.nullSafe(backingNode.getAllNames()));
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

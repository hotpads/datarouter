package com.hotpads.datarouter.node.type.caching.map.base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Supplier;

import com.hotpads.datarouter.client.ClientId;
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
		super(new NodeParamsBuilder<PK,D,F>(backingNode.getRouter(), backingNode.getFieldInfo().getDatabeanSupplier())
				.withFielder((Supplier<F>)backingNode.getFieldInfo().getFielderSupplier())
				.build());
		this.cachingNode = cacheNode;
		this.backingNode = backingNode;
		//use the inputs to make a unique name.  randomness will not place nicely with the counters
		this.setId(new NodeId<PK,D,F>(getClass().getSimpleName(), backingNode.getFieldInfo().getSampleDatabean()
				.getDatabeanName(), backingNode.getRouter().getName(), null, null, backingNode.getName()+".cache"));
	}

	public void updateLastAttemptedContact(){
		lastAttemptedContact = System.currentTimeMillis();
	}

	public void updateLastContact(){
		lastContact = System.currentTimeMillis();
	}

	/************************* util ***************************/

	public static boolean useCache(final Config config){
		if(config==null || config.getCacheOk()==null){
			return Config.DEFAULT_CACHE_OK;
		}
		return config.getCacheOk();
	}

	/**************************************************************************/

	@Override
	public List<String> getClientNames() {
		SortedSet<String> clientNames = new TreeSet<>();
		DrSetTool.nullSafeSortedAddAll(clientNames, cachingNode.getClientNames());
		DrSetTool.nullSafeSortedAddAll(clientNames, backingNode.getClientNames());
		return DrListTool.createArrayList(clientNames);
	}

	@Override
	public List<ClientId> getClientIds(){
		Set<ClientId> clientIds = new HashSet<>(backingNode.getClientIds());
		clientIds.addAll(cachingNode.getClientIds());
		return new ArrayList<>(clientIds);
	}

	@Override
	public List<String> getClientNamesForPrimaryKeysForSchemaUpdate(Collection<PK> keys) {
		return backingNode.getClientNamesForPrimaryKeysForSchemaUpdate(keys);
	}

	@Override
	public Node<PK,D> getMaster() {
		return backingNode.getMaster();
	}

	@Override
	public List<? extends Node<PK,D>> getChildNodes(){
		if(backingNode==null){
			return new ArrayList<>();
		}
		return DrListTool.wrap(backingNode);
	}

	@Override
	public Set<String> getAllNames(){
		Set<String> names = new HashSet<>();
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
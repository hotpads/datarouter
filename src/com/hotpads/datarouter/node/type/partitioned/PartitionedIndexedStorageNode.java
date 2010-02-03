package com.hotpads.datarouter.node.type.partitioned;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.IndexedStorageNode;
import com.hotpads.datarouter.node.type.physical.PhysicalIndexedStorageNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.Key;
import com.hotpads.datarouter.storage.lookup.Lookup;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.MapTool;

public abstract class PartitionedIndexedStorageNode<D extends Databean,N extends PhysicalIndexedStorageNode<D>>
extends PartitionedMapStorageReaderNode<D,N>
implements IndexedStorageNode<D>{
	
	public PartitionedIndexedStorageNode(Class<D> persistentClass, DataRouter router) {
		super(persistentClass, router);
	}


	/***************** Indexed Storage Write Methods ************************************/
	
	@Override
	public void delete(Lookup<D> multiKey, Config config) {
		Collection<N> nodes = this.getPhysicalNodes(multiKey);
		for(N node : CollectionTool.nullSafe(nodes)){
			node.delete(multiKey, config);
		}
	}
	
	
	/******************* map storage writer methods ***************************/
	/*
	 * MULTIPLE INHERITANCE... copied from: PartitionedMapStorage
	 */
	
	@Override
	public void delete(Key<D> key, Config config) {
		for(N node : CollectionTool.nullSafe(getPhysicalNodes(key))){
			node.delete(key, config);
		}
	}

	@Override
	public void deleteMulti(Collection<? extends Key<D>> keys, Config config) {
		Map<N,List<Key<D>>> keysByNode = this.getKeysByPhysicalNode(keys);
		for(N node : MapTool.nullSafe(keysByNode).keySet()){
			Collection<Key<D>> keysForNode = keysByNode.get(node);
			if(CollectionTool.notEmpty(keysForNode)){
				node.deleteMulti(keysForNode, config);
			}
		}
	}
	
	@Override
	public void deleteAll(Config config) {
		for(N node : CollectionTool.nullSafe(this.getPhysicalNodes())){
			node.deleteAll(config);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public void put(D databean, Config config) {
		Collection<N> nodes = this.getPhysicalNodes(databean.getKey());
		for(N node : CollectionTool.nullSafe(nodes)){
			node.put(databean, config);
		}
	}

	@Override
	public void putMulti(Collection<D> databeans, Config config) {
		Map<N,? extends Collection<D>> databeansByNode = this.getDatabeansByPhysicalNode(databeans);
		for(N node : MapTool.nullSafe(databeansByNode).keySet()){
			Collection<D> databeansForNode = databeansByNode.get(node);
			if(CollectionTool.notEmpty(databeansForNode)){
				node.putMulti(databeansForNode, config);
			}
		}
	}


}

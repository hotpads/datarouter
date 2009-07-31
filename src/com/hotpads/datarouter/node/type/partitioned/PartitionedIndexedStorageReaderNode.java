package com.hotpads.datarouter.node.type.partitioned;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.base.partitioned.BasePartitionedNode;
import com.hotpads.datarouter.node.op.IndexedStorageReaderNode;
import com.hotpads.datarouter.node.type.physical.PhysicalIndexedStorageReaderNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.index.Lookup;
import com.hotpads.datarouter.storage.key.Key;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;

public abstract class PartitionedIndexedStorageReaderNode<D extends Databean,N extends PhysicalIndexedStorageReaderNode<D>>
extends BasePartitionedNode<D,N>
implements IndexedStorageReaderNode<D>{
	
	public PartitionedIndexedStorageReaderNode(Class<D> persistentClass, DataRouter router) {
		super(persistentClass, router);
	}

	/**************************** MapStorageReader ***********************************/
	
	@Override
	public boolean exists(Key<D> key, Config config){
		for(N node : CollectionTool.nullSafe(getPhysicalNodes(key))){
			if(node.exists(key, config)){
				return true;
			}
		}
		return false;
	}

	@Override
	public D get(Key<D> key, Config config) {
		for(N node : CollectionTool.nullSafe(getPhysicalNodes(key))){
			D databean = node.get(key,config);
			if(databean != null){
				return databean;
			}
		}
		return null;
	}

	@Override
	public List<D> getAll(Config config) {
		List<D> all = ListTool.createLinkedList();
		for(N node : CollectionTool.nullSafe(this.getPhysicalNodes())){
			all.addAll(CollectionTool.nullSafe(node.getAll(config)));
		}
		return all;
	}

	@Override
	public List<D> getMulti(Collection<? extends Key<D>> keys, Config config) {
		Map<N,List<Key<D>>> keysByNode = this.getKeysByPhysicalNode(keys);
		List<D> all = ListTool.createLinkedList();
		for(N node : MapTool.nullSafe(keysByNode).keySet()){
			Collection<Key<D>> keysForNode = keysByNode.get(node);
			if(CollectionTool.notEmpty(keysForNode)){
				all.addAll(node.getMulti(keysForNode, config));
			}
		}
		return all;
	}



	/***************** IndexedStorageReader ************************************/
	
	@Override
	public List<D> lookup(Lookup<D> multiKey, Config config) {
		List<D> all = ListTool.createLinkedList();
		Collection<N> nodes = this.getPhysicalNodes(multiKey);
		for(N node : CollectionTool.nullSafe(nodes)){
			all.addAll(node.lookup(multiKey, config));
		}
		return all;
	}
	
}

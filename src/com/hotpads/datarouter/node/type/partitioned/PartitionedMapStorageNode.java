package com.hotpads.datarouter.node.type.partitioned;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.MapStorageNode;
import com.hotpads.datarouter.node.type.physical.PhysicalMapStorageNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.datarouter.storage.key.unique.primary.PrimaryKey;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.MapTool;

public abstract class PartitionedMapStorageNode<D extends Databean,
PK extends PrimaryKey<D>,N extends PhysicalMapStorageNode<D,PK>>
extends PartitionedMapStorageReaderNode<D,PK,N>
implements MapStorageNode<D,PK>{
	
	public PartitionedMapStorageNode(Class<D> persistentClass, DataRouter router) {
		super(persistentClass, router);
	}



	/***************** MapStorageWriter ************************************/
	
	/*
	 * MULTIPLE INHERITANCE... copied to:
	 *   - PartitionedSortedStorageNode
	 *   - PartitionedIndexedStorageNode
	 */
	
	@Override
	public void delete(UniqueKey<D> key, Config config) {
		for(N node : CollectionTool.nullSafe(getPhysicalNodes(key))){
			node.delete(key, config);
		}
	}

	@Override
	public void deleteMulti(Collection<? extends UniqueKey<D>> keys, Config config) {
		Map<N,List<UniqueKey<D>>> keysByNode = this.getKeysByPhysicalNode(keys);
		for(N node : MapTool.nullSafe(keysByNode).keySet()){
			Collection<UniqueKey<D>> keysForNode = keysByNode.get(node);
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

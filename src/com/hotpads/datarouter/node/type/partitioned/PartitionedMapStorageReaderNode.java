package com.hotpads.datarouter.node.type.partitioned;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.base.partitioned.BasePartitionedNode;
import com.hotpads.datarouter.node.op.MapStorageReaderNode;
import com.hotpads.datarouter.node.type.physical.PhysicalMapStorageReaderNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;

public abstract class PartitionedMapStorageReaderNode<D extends Databean<PK>,PK extends PrimaryKey<PK>,
N extends PhysicalMapStorageReaderNode<D,PK>>
extends BasePartitionedNode<D,PK,N>
implements MapStorageReaderNode<D,PK>{
	
	public PartitionedMapStorageReaderNode(Class<D> databeanClass, DataRouter router) {
		super(databeanClass, router);
	}

	/**************************** MapStorageReader ***********************************/
	
	@Override
	public boolean exists(UniqueKey<PK> key, Config config){
		for(N node : CollectionTool.nullSafe(getPhysicalNodes(key))){
			if(node.exists(key, config)){
				return true;
			}
		}
		return false;
	}

	@Override
	public D get(UniqueKey<PK> key, Config config) {
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
	public List<D> getMulti(Collection<? extends UniqueKey<PK>> keys, Config config) {
		Map<N,List<UniqueKey<PK>>> keysByNode = this.getKeysByPhysicalNode(keys);
		List<D> all = ListTool.createLinkedList();
		for(N node : MapTool.nullSafe(keysByNode).keySet()){
			Collection<UniqueKey<PK>> keysForNode = keysByNode.get(node);
			if(CollectionTool.notEmpty(keysForNode)){
				all.addAll(node.getMulti(keysForNode, config));
			}
		}
		return all;
	}


}

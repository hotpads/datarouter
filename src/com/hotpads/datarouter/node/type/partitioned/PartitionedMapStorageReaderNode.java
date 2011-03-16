package com.hotpads.datarouter.node.type.partitioned;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.base.partitioned.BasePartitionedNode;
import com.hotpads.datarouter.node.op.raw.read.MapStorageReader.MapStorageReaderNode;
import com.hotpads.datarouter.node.op.raw.read.MapStorageReader.PhysicalMapStorageReaderNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;

public abstract class PartitionedMapStorageReaderNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends PhysicalMapStorageReaderNode<PK,D>>
extends BasePartitionedNode<PK,D,N>
implements MapStorageReaderNode<PK,D>{
	
	public PartitionedMapStorageReaderNode(Class<D> databeanClass, DataRouter router) {
		super(databeanClass, router);
	}

	/**************************** MapStorageReader ***********************************/
	
	@Override
	public boolean exists(PK key, Config config){
		for(N node : CollectionTool.nullSafe(getPhysicalNodes(key))){
			if(node.exists(key, config)){
				return true;
			}
		}
		return false;
	}

	@Override
	public D get(PK key, Config config) {
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
	public List<D> getMulti(Collection<PK> keys, Config config) {
		Map<N,List<PK>> keysByNode = this.getPrimaryKeysByPhysicalNode(keys);
		List<D> all = ListTool.createLinkedList();
		for(N node : MapTool.nullSafe(keysByNode).keySet()){
			Collection<PK> keysForNode = keysByNode.get(node);
			if(CollectionTool.notEmpty(keysForNode)){
				all.addAll(node.getMulti(keysForNode, config));
			}
		}
		return all;
	}

	@Override
	public List<PK> getKeys(Collection<PK> keys, Config config) {
		Map<N,List<PK>> keysByNode = this.getPrimaryKeysByPhysicalNode(keys);
		List<PK> all = ListTool.createLinkedList();
		for(N node : MapTool.nullSafe(keysByNode).keySet()){
			Collection<PK> keysForNode = keysByNode.get(node);
			if(CollectionTool.notEmpty(keysForNode)){
				all.addAll(node.getKeys(keysForNode, config));
			}
		}
		return all;
	}

}

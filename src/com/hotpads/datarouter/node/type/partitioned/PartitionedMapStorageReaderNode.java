package com.hotpads.datarouter.node.type.partitioned;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.read.MapStorageReader.MapStorageReaderNode;
import com.hotpads.datarouter.node.op.raw.read.MapStorageReader.PhysicalMapStorageReaderNode;
import com.hotpads.datarouter.node.type.partitioned.base.BasePartitionedNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;

public abstract class PartitionedMapStorageReaderNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends PhysicalMapStorageReaderNode<PK,D>>
extends BasePartitionedNode<PK,D,F,N>
implements MapStorageReaderNode<PK,D>{
	
	public PartitionedMapStorageReaderNode(Class<D> databeanClass, Class<F> fielderClass, DataRouter router) {
		super(databeanClass, fielderClass, router);
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
		for(N node : CollectionTool.nullSafe(getPhysicalNodes())){
			List<D> allFromPhysicalNode = node.getAll(config);
			//need to filter in case the physical node is hosting things not in its partitions
			List<D> filtered = filterDatabeansForPhysicalNode(allFromPhysicalNode, node);
			all.addAll(CollectionTool.nullSafe(filtered));
		}
		return all;
	}

	@Override
	public List<D> getMulti(Collection<PK> keys, Config config) {
		Map<N,List<PK>> keysByNode = getPrimaryKeysByPhysicalNode(keys);
		List<D> all = ListTool.createLinkedList();
		for(N node : MapTool.nullSafe(keysByNode).keySet()){
			Collection<PK> keysForNode = keysByNode.get(node);
			if(CollectionTool.isEmpty(keysForNode)){ continue; }//should not be empty, but being safer
			List<D> databeansFromNode = node.getMulti(keysForNode, config);
			all.addAll(CollectionTool.nullSafe(databeansFromNode));
		}
		return all;
	}

	@Override
	public List<PK> getKeys(Collection<PK> keys, Config config) {
		Map<N,List<PK>> keysByNode = getPrimaryKeysByPhysicalNode(keys);
		List<PK> all = ListTool.createLinkedList();
		for(N node : MapTool.nullSafe(keysByNode).keySet()){
			Collection<PK> keysForNode = keysByNode.get(node);
			if(CollectionTool.isEmpty(keysForNode)){ continue; }//should not be empty, but being safer
			List<PK> pksFromNode = node.getKeys(keysForNode, config);
			all.addAll(CollectionTool.nullSafe(pksFromNode));
		}
		return all;
	}

}

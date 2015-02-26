package com.hotpads.datarouter.node.type.partitioned.mixin;

import java.util.Collection;
import java.util.Map;

import junit.framework.Assert;

import com.google.common.collect.Multimap;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.write.MapStorageWriter;
import com.hotpads.datarouter.node.op.raw.write.MapStorageWriter.PhysicalMapStorageWriterNode;
import com.hotpads.datarouter.node.type.partitioned.base.BasePartitionedNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.CollectionTool;
import com.hotpads.datarouter.util.core.MapTool;

public class PartitionedMapStorageWriterMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends PhysicalMapStorageWriterNode<PK,D>>
implements MapStorageWriter<PK,D>{
	
	protected BasePartitionedNode<PK,D,F,N> target;
	
	public PartitionedMapStorageWriterMixin(BasePartitionedNode<PK,D,F,N> target){
		this.target = target;
	}

	@Override
	public void delete(PK key, Config config) {
		N node = target.getPhysicalNode(key);
		node.delete(key, config);
	}
	
	@Override
	public void deleteMulti(Collection<PK> pks, Config config) {
		Multimap<N,PK> keysByNode = target.getPrimaryKeysByPhysicalNode(pks);
		if(keysByNode==null){ return; }
		for(N node : keysByNode.keySet()){
			Collection<PK> keysForNode = keysByNode.get(node);
			if(CollectionTool.notEmpty(keysForNode)){
				node.deleteMulti(keysForNode, config);
			}
		}
	}

	@Override
	public void deleteAll(Config config) {
		for(N node : CollectionTool.nullSafe(target.getPhysicalNodes())){
			node.deleteAll(config);
		}
	}

	@Override
	public void put(D databean, Config config) {
		N node = target.getPhysicalNode(databean.getKey());
		node.put(databean, config);
	}

	@Override
	public void putMulti(Collection<D> databeans, Config config) {
		Multimap<N,D> databeansByNode = target.getDatabeansByPhysicalNode(databeans);
		if(databeansByNode==null){ return; }
		for(N node : databeansByNode.keySet()){
			Collection<D> databeansForNode = databeansByNode.get(node);
			if(CollectionTool.isEmpty(databeansForNode)){ continue; }//shouldn't be needed, but safer
			node.putMulti(databeansForNode, config);
		}
	}

	
	
}

package com.hotpads.datarouter.node.type.partitioned.mixin;

import java.util.Collection;

import com.google.common.collect.Multimap;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.write.MapStorageWriter;
import com.hotpads.datarouter.node.op.raw.write.MapStorageWriter.PhysicalMapStorageWriterNode;
import com.hotpads.datarouter.node.type.partitioned.PartitionedNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.DrCollectionTool;

public interface PartitionedMapStorageWriterMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends PhysicalMapStorageWriterNode<PK,D>>
extends MapStorageWriter<PK,D>, PartitionedNode<PK,D,N>{

	@Override
	public default void delete(PK key, Config config) {
		N node = getPhysicalNode(key);
		node.delete(key, config);
	}

	@Override
	public default void deleteMulti(Collection<PK> pks, Config config) {
		Multimap<N,PK> keysByNode = getPrimaryKeysByPhysicalNode(pks);
		if(keysByNode==null){
			return;
		}
		for(N node : keysByNode.keySet()){
			Collection<PK> keysForNode = keysByNode.get(node);
			if(DrCollectionTool.notEmpty(keysForNode)){
				node.deleteMulti(keysForNode, config);
			}
		}
	}

	@Override
	public default void deleteAll(Config config) {
		for(N node : DrCollectionTool.nullSafe(getPhysicalNodes())){
			node.deleteAll(config);
		}
	}

	@Override
	public default void put(D databean, Config config) {
		N node = getPhysicalNode(databean.getKey());
		node.put(databean, config);
	}

	@Override
	public default void putMulti(Collection<D> databeans, Config config) {
		Multimap<N,D> databeansByNode = getDatabeansByPhysicalNode(databeans);
		if(databeansByNode==null){
			return;
		}
		for(N node : databeansByNode.keySet()){
			Collection<D> databeansForNode = databeansByNode.get(node);
			if(DrCollectionTool.isEmpty(databeansForNode)){
				continue;//shouldn't be needed, but safer
			}
			node.putMulti(databeansForNode, config);
		}
	}



}

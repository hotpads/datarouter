package com.hotpads.datarouter.node.type.partitioned.mixin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.Multimap;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.raw.MapStorage;
import com.hotpads.datarouter.node.op.raw.MapStorage.PhysicalMapStorageNode;
import com.hotpads.datarouter.node.type.partitioned.PartitionedNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.DrCollectionTool;

public interface PartitionedMapStorageMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends PhysicalMapStorageNode<PK,D>>
extends MapStorage<PK,D>, PartitionedNode<PK,D,N>{

	@Override
	default void delete(PK key, Config config) {
		N node = getPhysicalNode(key);
		node.delete(key, config);
	}

	@Override
	default void deleteMulti(Collection<PK> pks, Config config) {
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
	default void deleteAll(Config config) {
		for(N node : DrCollectionTool.nullSafe(getPhysicalNodes())){
			node.deleteAll(config);
		}
	}

	@Override
	default void put(D databean, Config config) {
		N node = getPhysicalNode(databean.getKey());
		node.put(databean, config);
	}

	@Override
	default void putMulti(Collection<D> databeans, Config config) {
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

	@Override
	default boolean exists(PK key, Config config){
		N node = getPhysicalNode(key);
		if(node == null){
			return false;
		}
		if(node.exists(key, config)){
			return true;
		}
		return false;
	}

	@Override
	default D get(PK key, Config config) {
		N node = getPhysicalNode(key);
		if(node == null){
			return null;
		}
		D databean = node.get(key, config);
		if(databean != null){
			return databean;
		}
		return null;
	}

	@Override
	default List<D> getMulti(Collection<PK> keys, Config config) {
		Multimap<N,PK> keysByNode = getPrimaryKeysByPhysicalNode(keys);
		List<D> all = new ArrayList<>();
		if(keysByNode == null){
			return all;
		}
		for(N node : keysByNode.keySet()){
			Collection<PK> keysForNode = keysByNode.get(node);
			if(DrCollectionTool.isEmpty(keysForNode)){// should not be empty, but being safer
				continue;
			}
			List<D> databeansFromNode = node.getMulti(keysForNode, config);
			all.addAll(DrCollectionTool.nullSafe(databeansFromNode));
		}
		return all;
	}

	@Override
	default List<PK> getKeys(Collection<PK> keys, Config config) {
		Multimap<N,PK> keysByNode = getPrimaryKeysByPhysicalNode(keys);
		List<PK> all = new ArrayList<>();
		if(keysByNode == null){
			return all;
		}
		for(N node : keysByNode.keySet()){
			Collection<PK> keysForNode = keysByNode.get(node);
			if(DrCollectionTool.isEmpty(keysForNode)){// should not be empty, but being safer
				continue;
			}
			List<PK> pksFromNode = node.getKeys(keysForNode, config);
			all.addAll(DrCollectionTool.nullSafe(pksFromNode));
		}
		return all;
	}

}

package com.hotpads.datarouter.client.imp.memory.node;

import java.util.Collection;
import java.util.Collections;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage.PhysicalIndexedSortedMapStorageNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.databean.DatabeanTool;
import com.hotpads.datarouter.storage.key.Key;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.datarouter.util.core.DrCollectionTool;

public class MemoryNode<PK extends PrimaryKey<PK>,D extends Databean<PK,D>,F extends DatabeanFielder<PK,D>>
extends MemoryReaderNode<PK,D,F>
implements PhysicalIndexedSortedMapStorageNode<PK,D>{

	public MemoryNode(NodeParams<PK,D,F> params){
		super(params);
	}

	@Override
	public Node<PK,D> getMaster() {
		return this;
	}

	@Override
	public void delete(PK key, Config config){
		if(key == null){
			return;
		}
		backingMap.remove(key);
	}

	@Override
	public void delete(Lookup<PK> lookup, Config config){
		deleteMulti(DatabeanTool.getKeys(lookup(lookup, false, config)), config);
	}

	@Override
	public void deleteMulti(Collection<PK> keys, Config config) {
		for(Key<PK> key : DrCollectionTool.nullSafe(keys)){
			backingMap.remove(key);
		}
	}


	@Override
	public void deleteAll(Config config) {
		backingMap.clear();
	}


	@Override
	public void put(final D databean, Config config){
		if(databean == null || databean.getKey() == null){
			return;
		}
		backingMap.put(databean.getKey(), databean);
	}


	@Override
	public void putMulti(Collection<D> databeans, Config config) {
		for(D databean : DrCollectionTool.nullSafe(databeans)){
			put(databean, config);
		}
	}

	@Override
	public void deleteRangeWithPrefix(PK prefix, boolean wildcardLastField, Config config){
		for(D databean : getWithPrefix(prefix, wildcardLastField, config)){
			delete(databean.getKey(), config);
		}
	}

	@Override
	public void deleteUnique(UniqueKey<PK> uniqueKey, Config config){
		deleteMultiUnique(Collections.singleton(uniqueKey), config);
	}

	@Override
	public void deleteMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config config){
		deleteMulti(DatabeanTool.getKeys(lookupMultiUnique(uniqueKeys, config)), config);
	}

	@Override
	public <IK extends PrimaryKey<IK>> void deleteByIndex(Collection<IK> keys, Config config){
		deleteMulti(DatabeanTool.getKeys(getMultiByIndex(keys, config)), config);
	}

}

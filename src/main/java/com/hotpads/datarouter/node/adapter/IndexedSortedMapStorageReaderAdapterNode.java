package com.hotpads.datarouter.node.adapter;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.combo.reader.IndexedSortedMapStorageReader.IndexedSortedMapStorageReaderNode;
import com.hotpads.datarouter.node.op.raw.read.IndexedStorageReader;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.util.core.CollectionTool;

public class IndexedSortedMapStorageReaderAdapterNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends IndexedSortedMapStorageReaderNode<PK,D>>
extends SortedMapStorageReaderAdapterNode<PK,D,F,N>
implements IndexedSortedMapStorageReaderNode<PK,D>{
	
	public IndexedSortedMapStorageReaderAdapterNode(Class<D> databeanClass, DataRouter router, N backingNode){		
		super(databeanClass, router, backingNode);
	}
	

	/***************** IndexedSortedMapStorageReader ************************************/

	@Override
	public Long count(Lookup<PK> lookup, Config pConfig){
		Config config = Config.nullSafe(pConfig).setCallsite(getCallsite(IndexedStorageReader.OP_count, 1));
		return backingNode.count(lookup, config);
	}

	@Override
	public D lookupUnique(UniqueKey<PK> uniqueKey, Config pConfig){
		Config config = Config.nullSafe(pConfig).setCallsite(getCallsite(IndexedStorageReader.OP_lookupUnique, 1));
		return backingNode.lookupUnique(uniqueKey, config);
	}

	@Override
	public List<D> lookupMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config pConfig){
		int numItems = CollectionTool.size(uniqueKeys);
		Config config = Config.nullSafe(pConfig).setCallsite(getCallsite(IndexedStorageReader.OP_lookupMultiUnique, numItems));
		return backingNode.lookupMultiUnique(uniqueKeys, config);
	}

	@Override
	public List<D> lookup(Lookup<PK> lookup, boolean wildcardLastField, Config pConfig){
		Config config = Config.nullSafe(pConfig).setCallsite(getCallsite(IndexedStorageReader.OP_lookup, 1));
		return backingNode.lookup(lookup, wildcardLastField, config);
	}

	@Override
	public List<D> lookup(Collection<? extends Lookup<PK>> lookups, Config pConfig){
		int numItems = CollectionTool.size(lookups);
		Config config = Config.nullSafe(pConfig).setCallsite(getCallsite(IndexedStorageReader.OP_lookupMulti, numItems));
		return backingNode.lookup(lookups, config);
	}
	
}

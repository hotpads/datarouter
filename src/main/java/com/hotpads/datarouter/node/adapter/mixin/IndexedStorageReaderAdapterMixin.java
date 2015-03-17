package com.hotpads.datarouter.node.adapter.mixin;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.adapter.BaseAdapterNode;
import com.hotpads.datarouter.node.op.raw.read.IndexedStorageReader;
import com.hotpads.datarouter.node.op.raw.read.IndexedStorageReader.IndexedStorageReaderNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.datarouter.util.core.DrCollectionTool;

public class IndexedStorageReaderAdapterMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends IndexedStorageReaderNode<PK,D>>
implements IndexedStorageReader<PK,D>{
	
	private BaseAdapterNode<PK,D,F,N> adapterNode;
	private N backingNode;
	
	
	public IndexedStorageReaderAdapterMixin(BaseAdapterNode<PK,D,F,N> adapterNode, N backingNode){
		this.adapterNode = adapterNode;
		this.backingNode = backingNode;
	}
	

	/***************** IndexedSortedMapStorageReader ************************************/

	@Override
	public Long count(Lookup<PK> lookup, Config pConfig){
		Config config = Config.nullSafe(pConfig).setCallsite(adapterNode.getCallsite());
		long startNs = System.nanoTime();
		try{
			return backingNode.count(lookup, config);
		}finally{
			adapterNode.recordCallsite(config, startNs, 1);
		}
	}

	@Override
	public D lookupUnique(UniqueKey<PK> uniqueKey, Config pConfig){
		Config config = Config.nullSafe(pConfig).setCallsite(adapterNode.getCallsite());
		long startNs = System.nanoTime();
		try{
			return backingNode.lookupUnique(uniqueKey, config);
		}finally{
			adapterNode.recordCallsite(config, startNs, 1);
		}
	}

	@Override
	public List<D> lookupMultiUnique(Collection<? extends UniqueKey<PK>> uniqueKeys, Config pConfig){
		Config config = Config.nullSafe(pConfig).setCallsite(adapterNode.getCallsite());
		long startNs = System.nanoTime();
		try{
			return backingNode.lookupMultiUnique(uniqueKeys, config);
		}finally{
			adapterNode.recordCollectionCallsite(config, startNs, uniqueKeys);
		}
	}

	@Override
	public List<D> lookup(Lookup<PK> lookup, boolean wildcardLastField, Config pConfig){
		Config config = Config.nullSafe(pConfig).setCallsite(adapterNode.getCallsite());
		long startNs = System.nanoTime();
		try{
			return backingNode.lookup(lookup, wildcardLastField, config);
		}finally{
			adapterNode.recordCallsite(config, startNs, 1);
		}
	}

	@Override
	public List<D> lookup(Collection<? extends Lookup<PK>> lookups, Config pConfig){
		Config config = Config.nullSafe(pConfig).setCallsite(adapterNode.getCallsite());
		long startNs = System.nanoTime();
		try{
			return backingNode.lookup(lookups, config);
		}finally{
			adapterNode.recordCollectionCallsite(config, startNs, lookups);
		}
	}
	
}

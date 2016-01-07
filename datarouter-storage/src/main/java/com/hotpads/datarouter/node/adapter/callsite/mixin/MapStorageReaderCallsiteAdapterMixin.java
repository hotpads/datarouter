package com.hotpads.datarouter.node.adapter.callsite.mixin;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.adapter.callsite.BaseCallsiteAdapter;
import com.hotpads.datarouter.node.op.raw.read.MapStorageReader;
import com.hotpads.datarouter.node.op.raw.read.MapStorageReader.MapStorageReaderNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class MapStorageReaderCallsiteAdapterMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends MapStorageReaderNode<PK,D>>
implements MapStorageReader<PK,D>{
	
	private BaseCallsiteAdapter<PK,D,F,N> adapterNode;
	private N backingNode;
	
	
	public MapStorageReaderCallsiteAdapterMixin(BaseCallsiteAdapter<PK,D,F,N> adapterNode, N backingNode){
		this.adapterNode = adapterNode;
		this.backingNode = backingNode;
	}


	/**************************** MapStorageReader ***********************************/
	
	@Override
	public boolean exists(PK key, Config pConfig){
		Config config = Config.nullSafe(pConfig).setCallsite(adapterNode.getCallsite());
		long startNs = System.nanoTime();
		boolean result = false;
		try{
			result = backingNode.exists(key, config);
			return result;
		}finally{
			int numResults = result ? 1 : 0;
			adapterNode.recordCallsite(config, startNs, numResults);
		}
	}

	@Override
	public D get(PK key, Config pConfig){
		Config config = Config.nullSafe(pConfig).setCallsite(adapterNode.getCallsite());
		long startNs = System.nanoTime();
		D result = null;
		try{
			result = backingNode.get(key, config);
			return result;
		}finally{
			int numResults = result == null ? 0 : 1;
			adapterNode.recordCallsite(config, startNs, numResults);
		}
	}

	@Override
	public List<D> getMulti(Collection<PK> keys, Config pConfig) {
		Config config = Config.nullSafe(pConfig).setCallsite(adapterNode.getCallsite());
		long startNs = System.nanoTime();
		List<D> results = null;
		try{
			results = backingNode.getMulti(keys, config);
			return results;
		}finally{
			adapterNode.recordCollectionCallsite(config, startNs, results);
		}
	}

	@Override
	public List<PK> getKeys(Collection<PK> keys, Config pConfig) {
		Config config = Config.nullSafe(pConfig).setCallsite(adapterNode.getCallsite());
		long startNs = System.nanoTime();
		List<PK> results = null;
		try{
			results = backingNode.getKeys(keys, config);
			return results;
		}finally{
			adapterNode.recordCollectionCallsite(config, startNs, results);
		}
	}
	
}

package com.hotpads.datarouter.node.adapter.callsite.mixin;

import java.util.Collection;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.adapter.callsite.BaseCallsiteAdapter;
import com.hotpads.datarouter.node.op.raw.write.MapStorageWriter;
import com.hotpads.datarouter.node.op.raw.write.MapStorageWriter.MapStorageWriterNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.datarouter.util.core.DrCollectionTool;

public class MapStorageWriterCallsiteAdapterMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends MapStorageWriterNode<PK,D>>
implements MapStorageWriter<PK,D>{
	
	private BaseCallsiteAdapter<PK,D,F,N> adapterNode;
	private N backingNode;
	
	
	public MapStorageWriterCallsiteAdapterMixin(BaseCallsiteAdapter<PK,D,F,N> adapterNode, N backingNode){
		this.adapterNode = adapterNode;
		this.backingNode = backingNode;
	}

	
	@Override
	public void put(D databean, Config pConfig){
		Config config = Config.nullSafe(pConfig).setCallsite(adapterNode.getCallsite());
		long startNs = System.nanoTime();
		try{
			backingNode.put(databean, config);
		}finally{
			adapterNode.recordCallsite(config, startNs, 1);
		}
	}

	@Override
	public void putMulti(Collection<D> databeans, Config pConfig){
		Config config = Config.nullSafe(pConfig).setCallsite(adapterNode.getCallsite());
		long startNs = System.nanoTime();
		try{
			backingNode.putMulti(databeans, config);
		}finally{
			adapterNode.recordCollectionCallsite(config, startNs, databeans);
		}
	}

	@Override
	public void delete(PK key, Config pConfig){
		Config config = Config.nullSafe(pConfig).setCallsite(adapterNode.getCallsite());
		long startNs = System.nanoTime();
		try{
			backingNode.delete(key, config);
		}finally{
			adapterNode.recordCallsite(config, startNs, 1);
		}
	}

	@Override
	public void deleteMulti(Collection<PK> keys, Config pConfig){
		Config config = Config.nullSafe(pConfig).setCallsite(adapterNode.getCallsite());
		long startNs = System.nanoTime();
		try{
			backingNode.deleteMulti(keys, config);
		}finally{
			adapterNode.recordCollectionCallsite(config, startNs, keys);
		}
	}

	@Override
	public void deleteAll(Config pConfig){
		Config config = Config.nullSafe(pConfig).setCallsite(adapterNode.getCallsite());
		long startNs = System.nanoTime();
		try{
			backingNode.deleteAll(config);
		}finally{
			adapterNode.recordCallsite(config, startNs, 0);
		}
	}
	
}
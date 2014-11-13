package com.hotpads.datarouter.node.adapter.mixin;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.adapter.BaseAdapterNode;
import com.hotpads.datarouter.node.op.raw.read.MapStorageReader;
import com.hotpads.datarouter.node.op.raw.read.MapStorageReader.MapStorageReaderNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.CollectionTool;

public class MapStorageReaderAdapterMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends MapStorageReaderNode<PK,D>>
implements MapStorageReader<PK,D>{
	
	private BaseAdapterNode<PK,D,F,N> adapterNode;
	private N backingNode;
	
	
	public MapStorageReaderAdapterMixin(BaseAdapterNode<PK,D,F,N> adapterNode, N backingNode){
		this.adapterNode = adapterNode;
		this.backingNode = backingNode;
	}


	/**************************** MapStorageReader ***********************************/
	
	@Override
	public boolean exists(PK key, Config pConfig){
		Config config = Config.nullSafe(pConfig).setCallsite(adapterNode.getCallsite(MapStorageReader.OP_exists, 1));
		return backingNode.exists(key, config);
	}

	@Override
	public D get(PK key, Config pConfig){
		Config config = Config.nullSafe(pConfig).setCallsite(adapterNode.getCallsite(MapStorageReader.OP_get, 1));
		return backingNode.get(key, config);
	}

	@Override
	public List<D> getMulti(Collection<PK> keys, Config pConfig) {
		int numItems = CollectionTool.size(keys);
		Config config = Config.nullSafe(pConfig).setCallsite(adapterNode.getCallsite(MapStorageReader.OP_getMulti, numItems));
		return backingNode.getMulti(keys, config);
	}

	@Override
	public List<PK> getKeys(Collection<PK> keys, Config pConfig) {
		int numItems = CollectionTool.size(keys);
		Config config = Config.nullSafe(pConfig).setCallsite(adapterNode.getCallsite(MapStorageReader.OP_getKeys, numItems));
		return backingNode.getKeys(keys, config);
	}
	
}

package com.hotpads.datarouter.node.adapter.counter;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.adapter.counter.formatter.NodeCounterFormatter;
import com.hotpads.datarouter.node.adapter.counter.mixin.MapStorageReaderCounterAdapterMixin;
import com.hotpads.datarouter.node.op.raw.read.MapStorageReader.MapStorageReaderNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class MapStorageReaderCounterAdapter<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends MapStorageReaderNode<PK,D>>
extends BaseCounterAdapter<PK,D,F,N>
implements MapStorageReaderNode<PK,D>{

	private MapStorageReaderCounterAdapterMixin<PK,D,F,N> mapStorageReaderMixin;
	
	public MapStorageReaderCounterAdapter(N backingNode){		
		super(backingNode);
		NodeCounterFormatter<PK,D,F,N> nodeCounterFormatter = new NodeCounterFormatter<PK,D,F,N>(backingNode);
		this.mapStorageReaderMixin = new MapStorageReaderCounterAdapterMixin<PK,D,F,N>(nodeCounterFormatter, 
				backingNode);
	}

	
	/**************************** MapStorageReader ***********************************/
	
	@Override
	public boolean exists(PK key, Config pConfig){
		return mapStorageReaderMixin.exists(key, pConfig);
	}

	@Override
	public D get(PK key, Config pConfig){
		return mapStorageReaderMixin.get(key, pConfig);
	}

	@Override
	public List<D> getMulti(Collection<PK> keys, Config pConfig) {
		return mapStorageReaderMixin.getMulti(keys, pConfig);
	}

	@Override
	public List<PK> getKeys(Collection<PK> keys, Config pConfig) {
		return mapStorageReaderMixin.getKeys(keys, pConfig);
	}
	
}

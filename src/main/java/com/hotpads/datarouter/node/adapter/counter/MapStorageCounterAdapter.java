package com.hotpads.datarouter.node.adapter.counter;

import java.util.Collection;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.adapter.counter.formatter.NodeCounterFormatter;
import com.hotpads.datarouter.node.adapter.counter.mixin.MapStorageWriterCounterAdapterMixin;
import com.hotpads.datarouter.node.op.raw.MapStorage.MapStorageNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class MapStorageCounterAdapter<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends MapStorageNode<PK,D>>
extends MapStorageReaderCounterAdapter<PK,D,F,N>
implements MapStorageNode<PK,D>{
	
	private MapStorageWriterCounterAdapterMixin<PK,D,F,N> mapStorageWriterMixin;

	public MapStorageCounterAdapter(N backingNode){		
		super(backingNode);
		NodeCounterFormatter<PK,D,F,N> nodeCounterFormatter = new NodeCounterFormatter<PK,D,F,N>(backingNode);
		this.mapStorageWriterMixin = new MapStorageWriterCounterAdapterMixin<PK,D,F,N>(nodeCounterFormatter, 
				backingNode);
	}
	
	
	/**************************** MapStorage ***********************************/

	@Override
	public void put(D databean, Config pConfig){
		mapStorageWriterMixin.put(databean, pConfig);
	}

	@Override
	public void putMulti(Collection<D> databeans, Config pConfig){
		mapStorageWriterMixin.putMulti(databeans, pConfig);
	}

	@Override
	public void delete(PK key, Config pConfig){
		mapStorageWriterMixin.delete(key, pConfig);
	}

	@Override
	public void deleteMulti(Collection<PK> keys, Config pConfig){
		mapStorageWriterMixin.deleteMulti(keys, pConfig);
	}

	@Override
	public void deleteAll(Config pConfig){
		mapStorageWriterMixin.deleteAll(pConfig);
	}

	
}

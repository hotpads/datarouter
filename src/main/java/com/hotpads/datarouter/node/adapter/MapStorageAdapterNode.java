package com.hotpads.datarouter.node.adapter;

import java.util.Collection;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.adapter.mixin.MapStorageWriterAdapterMixin;
import com.hotpads.datarouter.node.op.raw.MapStorage.MapStorageNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class MapStorageAdapterNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends MapStorageNode<PK,D>>
extends MapStorageReaderAdapterNode<PK,D,F,N>
implements MapStorageNode<PK,D>{
	
	private MapStorageWriterAdapterMixin<PK,D,F,N> mapStorageWriterMixin;

	public MapStorageAdapterNode(NodeParams<PK,D,F> params, N backingNode){		
		super(params, backingNode);
		this.mapStorageWriterMixin = new MapStorageWriterAdapterMixin<PK,D,F,N>(this, backingNode);
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

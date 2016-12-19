package com.hotpads.datarouter.node.adapter.callsite;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.adapter.callsite.mixin.MapStorageReaderCallsiteAdapterMixin;
import com.hotpads.datarouter.node.op.raw.read.MapStorageReader.MapStorageReaderNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class MapStorageReaderCallsiteAdapter<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends MapStorageReaderNode<PK,D>>
extends BaseCallsiteAdapter<PK,D,F,N>
implements MapStorageReaderNode<PK,D>{

	private MapStorageReaderCallsiteAdapterMixin<PK,D,F,N> mapStorageReaderMixin;

	public MapStorageReaderCallsiteAdapter(NodeParams<PK,D,F> params, N backingNode){
		super(params, backingNode);
		this.mapStorageReaderMixin = new MapStorageReaderCallsiteAdapterMixin<>(this, backingNode);
	}

	/**************************** MapStorageReader ***********************************/

	@Override
	public boolean exists(PK key, Config config){
		return mapStorageReaderMixin.exists(key, config);
	}

	@Override
	public D get(PK key, Config config){
		return mapStorageReaderMixin.get(key, config);
	}

	@Override
	public List<D> getMulti(Collection<PK> keys, Config config){
		return mapStorageReaderMixin.getMulti(keys, config);
	}

	@Override
	public List<PK> getKeys(Collection<PK> keys, Config config){
		return mapStorageReaderMixin.getKeys(keys, config);
	}

}

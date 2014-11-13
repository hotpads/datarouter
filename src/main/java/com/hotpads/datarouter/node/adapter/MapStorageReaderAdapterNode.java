package com.hotpads.datarouter.node.adapter;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.NodeParams.NodeParamsBuilder;
import com.hotpads.datarouter.node.adapter.mixin.MapStorageReaderAdapterMixin;
import com.hotpads.datarouter.node.op.raw.read.MapStorageReader.MapStorageReaderNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class MapStorageReaderAdapterNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends MapStorageReaderNode<PK,D>>
extends BaseAdapterNode<PK,D,F,N>
implements MapStorageReaderNode<PK,D>{


	private MapStorageReaderAdapterMixin<PK,D,F,N> mapStorageReaderMixin;
	
	public MapStorageReaderAdapterNode(Class<D> databeanClass, DataRouter router, N backingNode){		
		super(new NodeParamsBuilder<PK,D,F>(router, databeanClass)
				.withFielder((Class<F>)backingNode.getFieldInfo().getFielderClass())
				.build(), backingNode);
		this.mapStorageReaderMixin = new MapStorageReaderAdapterMixin<PK,D,F,N>(this, backingNode);
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

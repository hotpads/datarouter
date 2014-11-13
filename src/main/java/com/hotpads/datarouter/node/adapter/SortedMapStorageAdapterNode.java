package com.hotpads.datarouter.node.adapter;

import java.util.Collection;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.adapter.mixin.MapStorageWriterAdapterMixin;
import com.hotpads.datarouter.node.adapter.mixin.SortedStorageWriterAdapterMixin;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class SortedMapStorageAdapterNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends SortedMapStorageNode<PK,D>>
extends SortedMapStorageReaderAdapterNode<PK,D,F,N>
implements SortedMapStorageNode<PK,D>{

	private MapStorageWriterAdapterMixin<PK,D,F,N> mapStorageWritermixin;
	private SortedStorageWriterAdapterMixin<PK,D,F,N> sortedStorageWriterMixin;
	
	public SortedMapStorageAdapterNode(Class<D> databeanClass, DataRouter router, N backingNode){		
		super(databeanClass, router, backingNode);
		this.mapStorageWritermixin = new MapStorageWriterAdapterMixin<PK,D,F,N>(this, backingNode);
		this.sortedStorageWriterMixin = new SortedStorageWriterAdapterMixin<PK,D,F,N>(this, backingNode);
	}

	
	/**************************** MapStorage ***********************************/
	
	@Override
	public void put(D databean, Config pConfig){
		mapStorageWritermixin.put(databean, pConfig);
	}

	@Override
	public void putMulti(Collection<D> databeans, Config pConfig){
		mapStorageWritermixin.putMulti(databeans, pConfig);
	}

	@Override
	public void delete(PK key, Config pConfig){
		mapStorageWritermixin.delete(key, pConfig);
	}

	@Override
	public void deleteMulti(Collection<PK> keys, Config pConfig){
		mapStorageWritermixin.deleteMulti(keys, pConfig);
	}

	@Override
	public void deleteAll(Config pConfig){
		mapStorageWritermixin.deleteAll(pConfig);
	}
	

	/***************** SortedStorageWriter ************************************/

	@Override
	public void deleteRangeWithPrefix(PK prefix, boolean wildcardLastField, Config pConfig){
		sortedStorageWriterMixin.deleteRangeWithPrefix(prefix, wildcardLastField, pConfig);
	}
	
}

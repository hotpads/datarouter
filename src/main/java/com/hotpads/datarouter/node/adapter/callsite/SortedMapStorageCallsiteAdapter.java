package com.hotpads.datarouter.node.adapter.callsite;

import java.util.Collection;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.adapter.callsite.mixin.MapStorageWriterCallsiteAdapterMixin;
import com.hotpads.datarouter.node.adapter.callsite.mixin.SortedStorageWriterCallsiteAdapterMixin;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class SortedMapStorageCallsiteAdapter<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends SortedMapStorageNode<PK,D>>
extends SortedMapStorageReaderCallsiteAdapter<PK,D,F,N>
implements SortedMapStorageNode<PK,D>{

	private MapStorageWriterCallsiteAdapterMixin<PK,D,F,N> mapStorageWritermixin;
	private SortedStorageWriterCallsiteAdapterMixin<PK,D,F,N> sortedStorageWriterMixin;
	
	public SortedMapStorageCallsiteAdapter(NodeParams<PK,D,F> params, N backingNode){		
		super(params, backingNode);
		this.mapStorageWritermixin = new MapStorageWriterCallsiteAdapterMixin<PK,D,F,N>(this, backingNode);
		this.sortedStorageWriterMixin = new SortedStorageWriterCallsiteAdapterMixin<PK,D,F,N>(this, backingNode);
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

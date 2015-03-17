package com.hotpads.datarouter.node.adapter.counter;

import java.util.Collection;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.adapter.counter.formatter.NodeCounterFormatter;
import com.hotpads.datarouter.node.adapter.counter.mixin.MapStorageWriterCounterAdapterMixin;
import com.hotpads.datarouter.node.adapter.counter.mixin.SortedStorageWriterCounterAdapterMixin;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;

public class SortedMapStorageCounterAdapter<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends SortedMapStorageNode<PK,D>>
extends SortedMapStorageReaderCounterAdapter<PK,D,F,N>
implements SortedMapStorageNode<PK,D>{

	private MapStorageWriterCounterAdapterMixin<PK,D,F,N> mapStorageWritermixin;
	private SortedStorageWriterCounterAdapterMixin<PK,D,F,N> sortedStorageWriterMixin;
	
	public SortedMapStorageCounterAdapter(N backingNode){		
		super(backingNode);
		NodeCounterFormatter<PK,D,F,N> nodeCounterFormatter = new NodeCounterFormatter<PK,D,F,N>(backingNode);
		this.mapStorageWritermixin = new MapStorageWriterCounterAdapterMixin<PK,D,F,N>(nodeCounterFormatter, 
				backingNode);
		this.sortedStorageWriterMixin = new SortedStorageWriterCounterAdapterMixin<PK,D,F,N>(nodeCounterFormatter, 
				backingNode);
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

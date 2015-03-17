package com.hotpads.datarouter.node.adapter.counter;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.adapter.counter.formatter.NodeCounterFormatter;
import com.hotpads.datarouter.node.adapter.counter.mixin.SortedStorageReaderCounterAdapterMixin;
import com.hotpads.datarouter.node.op.combo.reader.SortedMapStorageReader.SortedMapStorageReaderNode;
import com.hotpads.datarouter.node.op.raw.read.SortedStorageReader.SortedStorageReaderNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.core.iterable.scanner.iterable.SortedScannerIterable;

public class SortedMapStorageReaderCounterAdapter<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends SortedMapStorageReaderNode<PK,D>>
extends MapStorageReaderCounterAdapter<PK,D,F,N>
implements SortedStorageReaderNode<PK,D>{

	private SortedStorageReaderCounterAdapterMixin<PK,D,F,N> sortedStorageReaderMixin;
	
	public SortedMapStorageReaderCounterAdapter(N backingNode){		
		super(backingNode);
		NodeCounterFormatter<PK,D,F,N> nodeCounterFormatter = new NodeCounterFormatter<PK,D,F,N>(backingNode);
		this.sortedStorageReaderMixin = new SortedStorageReaderCounterAdapterMixin<PK,D,F,N>(nodeCounterFormatter, 
				backingNode);
	}

	/***************** SortedStorageReader ************************************/
	
	@Override
	public D getFirst(Config pConfig){
		return sortedStorageReaderMixin.getFirst(pConfig);
	}

	@Override
	public PK getFirstKey(Config pConfig){
		return sortedStorageReaderMixin.getFirstKey(pConfig);
	}

	@Override
	public List<D> getWithPrefix(PK prefix, boolean wildcardLastField, Config pConfig){
		return sortedStorageReaderMixin.getWithPrefix(prefix, wildcardLastField, pConfig);
	}

	@Override
	public List<D> getWithPrefixes(Collection<PK> prefixes, boolean wildcardLastField, Config pConfig){
		return sortedStorageReaderMixin.getWithPrefixes(prefixes, wildcardLastField, pConfig);
	}
	
	@Override
	public SortedScannerIterable<PK> scanKeys(Range<PK> range, Config pConfig){
		return sortedStorageReaderMixin.scanKeys(range, pConfig);
	}
	
	@Override
	public SortedScannerIterable<D> scan(Range<PK> range, Config pConfig){
		return sortedStorageReaderMixin.scan(range, pConfig);
	}
	
}

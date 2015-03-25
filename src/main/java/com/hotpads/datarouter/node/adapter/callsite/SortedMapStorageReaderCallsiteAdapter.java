package com.hotpads.datarouter.node.adapter.callsite;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.adapter.callsite.mixin.SortedStorageReaderCallsiteAdapterMixin;
import com.hotpads.datarouter.node.op.combo.reader.SortedMapStorageReader.SortedMapStorageReaderNode;
import com.hotpads.datarouter.node.op.raw.read.SortedStorageReader.SortedStorageReaderNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.core.iterable.scanner.iterable.SortedScannerIterable;

public class SortedMapStorageReaderCallsiteAdapter<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends SortedMapStorageReaderNode<PK,D>>
extends MapStorageReaderCallsiteAdapter<PK,D,F,N>
implements SortedStorageReaderNode<PK,D>{

	private SortedStorageReaderCallsiteAdapterMixin<PK,D,F,N> sortedStorageReaderMixin;
	
	public SortedMapStorageReaderCallsiteAdapter(NodeParams<PK,D,F> params, N backingNode){		
		super(params, backingNode);
		this.sortedStorageReaderMixin = new SortedStorageReaderCallsiteAdapterMixin<PK,D,F,N>(this, backingNode);
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

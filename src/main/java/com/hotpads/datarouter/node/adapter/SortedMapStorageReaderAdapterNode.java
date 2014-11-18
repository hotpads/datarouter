package com.hotpads.datarouter.node.adapter;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.NodeParams;
import com.hotpads.datarouter.node.adapter.mixin.SortedStorageReaderAdapterMixin;
import com.hotpads.datarouter.node.op.combo.reader.SortedMapStorageReader.SortedMapStorageReaderNode;
import com.hotpads.datarouter.node.op.raw.read.SortedStorageReader.SortedStorageReaderNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.core.iterable.scanner.iterable.SortedScannerIterable;

public class SortedMapStorageReaderAdapterNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends SortedMapStorageReaderNode<PK,D>>
extends MapStorageReaderAdapterNode<PK,D,F,N>
implements SortedStorageReaderNode<PK,D>{

	private SortedStorageReaderAdapterMixin<PK,D,F,N> sortedStorageReaderMixin;
	
	public SortedMapStorageReaderAdapterNode(NodeParams<PK,D,F> params, N backingNode){		
		super(params, backingNode);
		this.sortedStorageReaderMixin = new SortedStorageReaderAdapterMixin<PK,D,F,N>(this, backingNode);
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
	public List<D> getPrefixedRange(
			PK prefix, boolean wildcardLastField,
			PK start, boolean startInclusive, Config pConfig){
		return sortedStorageReaderMixin.getPrefixedRange(prefix, wildcardLastField, start, startInclusive, pConfig);
	}

	@Override
	public List<PK> getKeysInRange(PK start, boolean startInclusive, PK end,
			boolean endInclusive, Config pConfig){
		return sortedStorageReaderMixin.getKeysInRange(start, startInclusive, end, endInclusive, pConfig);
	}

	@Override
	public List<D> getRange(PK start, boolean startInclusive, PK end,
			boolean endInclusive, Config pConfig){
		return sortedStorageReaderMixin.getRange(start, startInclusive, end, endInclusive, pConfig);
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

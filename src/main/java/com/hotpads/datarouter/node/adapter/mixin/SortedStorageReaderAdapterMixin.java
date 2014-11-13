package com.hotpads.datarouter.node.adapter.mixin;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.adapter.BaseAdapterNode;
import com.hotpads.datarouter.node.op.raw.read.SortedStorageReader;
import com.hotpads.datarouter.node.op.raw.read.SortedStorageReader.SortedStorageReaderNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.core.iterable.scanner.iterable.SortedScannerIterable;

public class SortedStorageReaderAdapterMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends SortedStorageReaderNode<PK,D>>
implements SortedStorageReader<PK,D>{
	
	private BaseAdapterNode<PK,D,F,N> adapterNode;
	private N backingNode;
	
	
	public SortedStorageReaderAdapterMixin(BaseAdapterNode<PK,D,F,N> adapterNode, N backingNode){
		this.adapterNode = adapterNode;
		this.backingNode = backingNode;
	}


	/***************** SortedStorageReader ************************************/
	
	@Override
	public D getFirst(Config pConfig){
		Config config = Config.nullSafe(pConfig).setCallsite(adapterNode.getCallsite());
		long startNs = System.nanoTime();
		try{
			return backingNode.getFirst(config);
		}finally{
			adapterNode.recordCallsite(config, startNs, 0);
		}
	}

	@Override
	public PK getFirstKey(Config pConfig){
		Config config = Config.nullSafe(pConfig).setCallsite(adapterNode.getCallsite());
		long startNs = System.nanoTime();
		try{
			return backingNode.getFirstKey(config);
		}finally{
			adapterNode.recordCallsite(config, startNs, 0);
		}
	}
	
	@Override
	public List<D> getPrefixedRange(
			PK prefix, boolean wildcardLastField,
			PK start, boolean startInclusive, Config pConfig){
		Config config = Config.nullSafe(pConfig).setCallsite(adapterNode.getCallsite());
		long startNs = System.nanoTime();
		try{
			return backingNode.getPrefixedRange(prefix, wildcardLastField, start, startInclusive, config);
		}finally{
			adapterNode.recordCallsite(config, startNs, 1);
		}
	}

	@Override
	public List<PK> getKeysInRange(PK start, boolean startInclusive, PK end,
			boolean endInclusive, Config pConfig){
		Config config = Config.nullSafe(pConfig).setCallsite(adapterNode.getCallsite());
		long startNs = System.nanoTime();
		try{
			return backingNode.getKeysInRange(start, startInclusive, end, endInclusive, config);
		}finally{
			adapterNode.recordCallsite(config, startNs, 1);
		}
	}

	@Override
	public List<D> getRange(PK start, boolean startInclusive, PK end,
			boolean endInclusive, Config pConfig){
		Config config = Config.nullSafe(pConfig).setCallsite(adapterNode.getCallsite());
		long startNs = System.nanoTime();
		try{
			return backingNode.getRange(start, startInclusive, end, endInclusive, config);
		}finally{
			adapterNode.recordCallsite(config, startNs, 1);
		}
	}

	@Override
	public List<D> getWithPrefix(PK prefix, boolean wildcardLastField, Config pConfig){
		Config config = Config.nullSafe(pConfig).setCallsite(adapterNode.getCallsite());
		long startNs = System.nanoTime();
		try{
			return backingNode.getWithPrefix(prefix, wildcardLastField, config);
		}finally{
			adapterNode.recordCallsite(config, startNs, 1);
		}
	}

	@Override
	public List<D> getWithPrefixes(Collection<PK> prefixes, boolean wildcardLastField, Config pConfig){
		Config config = Config.nullSafe(pConfig).setCallsite(adapterNode.getCallsite());
		long startNs = System.nanoTime();
		try{
			return backingNode.getWithPrefixes(prefixes, wildcardLastField, config);
		}finally{
			adapterNode.recordCollectionCallsite(config, startNs, prefixes);
		}
	}
	
	@Override
	public SortedScannerIterable<PK> scanKeys(Range<PK> range, Config pConfig){
		Config config = Config.nullSafe(pConfig).setCallsite(adapterNode.getCallsite());
		long startNs = System.nanoTime();
		try{
			return backingNode.scanKeys(range, config);
		}finally{
			adapterNode.recordCallsite(config, startNs, 1);
		}
	}
	
	@Override
	public SortedScannerIterable<D> scan(Range<PK> range, Config pConfig){
		Config config = Config.nullSafe(pConfig).setCallsite(adapterNode.getCallsite());
		long startNs = System.nanoTime();
		try{
			return backingNode.scan(range, config);
		}finally{
			adapterNode.recordCallsite(config, startNs, 1);
		}
	}
}

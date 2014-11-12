package com.hotpads.datarouter.node.adapter;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.combo.reader.SortedMapStorageReader.SortedMapStorageReaderNode;
import com.hotpads.datarouter.node.op.raw.read.SortedStorageReader;
import com.hotpads.datarouter.node.op.raw.read.SortedStorageReader.SortedStorageReaderNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.core.iterable.scanner.iterable.SortedScannerIterable;

public class SortedMapStorageReaderAdapterNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends SortedMapStorageReaderNode<PK,D>>
extends MapStorageReaderAdapterNode<PK,D,F,N>
implements SortedStorageReaderNode<PK,D>{
	
	public SortedMapStorageReaderAdapterNode(Class<D> databeanClass, DataRouter router, N backingNode){		
		super(databeanClass, router, backingNode);
	}

	/***************** SortedStorageReader ************************************/
	
	@Override
	public D getFirst(Config pConfig){
		Config config = Config.nullSafe(pConfig).setCallsite(getCallsite(SortedStorageReader.OP_getFirst, 0));
		return backingNode.getFirst(config);
	}

	@Override
	public PK getFirstKey(Config pConfig){
		Config config = Config.nullSafe(pConfig).setCallsite(getCallsite(SortedStorageReader.OP_getFirstKey, 0));
		return backingNode.getFirstKey(config);
	}
	
	@Override
	public List<D> getPrefixedRange(
			PK prefix, boolean wildcardLastField,
			PK start, boolean startInclusive, Config pConfig){
		Config config = Config.nullSafe(pConfig).setCallsite(getCallsite(SortedStorageReader.OP_getPrefixedRange, 1));
		return backingNode.getPrefixedRange(prefix, wildcardLastField, start, startInclusive, config);
	}

	@Override
	public List<PK> getKeysInRange(PK start, boolean startInclusive, PK end,
			boolean endInclusive, Config pConfig){
		Config config = Config.nullSafe(pConfig).setCallsite(getCallsite(SortedStorageReader.OP_getKeysInRange, 1));
		return backingNode.getKeysInRange(start, startInclusive, end, endInclusive, config);
	}

	@Override
	public List<D> getRange(PK start, boolean startInclusive, PK end,
			boolean endInclusive, Config pConfig){
		Config config = Config.nullSafe(pConfig).setCallsite(getCallsite(SortedStorageReader.OP_getRange, 1));
		return backingNode.getRange(start, startInclusive, end, endInclusive, config);
	}

	@Override
	public List<D> getWithPrefix(PK prefix, boolean wildcardLastField, Config pConfig){
		Config config = Config.nullSafe(pConfig).setCallsite(getCallsite(SortedStorageReader.OP_getWithPrefix, 1));
		return backingNode.getWithPrefix(prefix, wildcardLastField, config);
	}

	@Override
	public List<D> getWithPrefixes(Collection<PK> prefixes, boolean wildcardLastField, Config pConfig){
		int numItems = CollectionTool.size(prefixes);
		Config config = Config.nullSafe(pConfig).setCallsite(getCallsite(SortedStorageReader.OP_getWithPrefixes, numItems));
		return backingNode.getWithPrefixes(prefixes, wildcardLastField, config);
	}
	
	@Override
	public SortedScannerIterable<PK> scanKeys(Range<PK> range, Config pConfig){
		Config config = Config.nullSafe(pConfig).setCallsite(getCallsite(SortedStorageReader.OP_scanKeys, 1));
		return backingNode.scanKeys(range, config);
	}
	
	@Override
	public SortedScannerIterable<D> scan(Range<PK> range, Config pConfig){
		Config config = Config.nullSafe(pConfig).setCallsite(getCallsite(SortedStorageReader.OP_scan, 1));
		return backingNode.scan(range, config);
	}
	
}

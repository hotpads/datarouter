package com.hotpads.datarouter.node.type.indexing;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.combo.reader.SortedMapStorageReader.SortedMapStorageReaderNode;
import com.hotpads.datarouter.node.op.raw.read.SortedStorageReader.SortedStorageReaderNode;
import com.hotpads.datarouter.serialize.fielder.DatabeanFielder;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.iterable.PeekableIterable;
import com.hotpads.util.core.iterable.scanner.iterable.SortedScannerIterable;

public class IndexingSortedMapStorageReaderNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends SortedMapStorageReaderNode<PK,D>>
extends IndexingMapStorageReaderNode<PK,D,F,N>
implements SortedStorageReaderNode<PK,D>{
	
	public IndexingSortedMapStorageReaderNode(N mainNode) {
		super(mainNode);
	}

	/***************** SortedStorageReader ************************************/
	
	@Override
	public D getFirst(Config config) {
		return mainNode.getFirst(config);
	}

	@Override
	public PK getFirstKey(Config config) {
		return mainNode.getFirstKey(config);
	}
	
	@Override
	public List<D> getPrefixedRange(
			PK prefix, boolean wildcardLastField,
			PK start, boolean startInclusive, Config config) {
		return mainNode.getPrefixedRange(
				prefix, wildcardLastField, start, startInclusive, config);
	}

	@Override
	public List<PK> getKeysInRange(PK start, boolean startInclusive, PK end,
			boolean endInclusive, Config config) {
		return mainNode.getKeysInRange(start, startInclusive, end, endInclusive, config);
	}

	@Override
	public List<D> getRange(PK start, boolean startInclusive, PK end,
			boolean endInclusive, Config config) {
		return mainNode.getRange(start, startInclusive, end, endInclusive, config);
	}

	@Override
	public List<D> getWithPrefix(PK prefix, boolean wildcardLastField, Config config) {
		return mainNode.getWithPrefix(prefix,wildcardLastField, config);
	}

	@Override
	public List<D> getWithPrefixes(Collection<PK> prefixes, boolean wildcardLastField, Config config) {
		return mainNode.getWithPrefixes(prefixes, wildcardLastField, config);
	}
	
	@Override
	public SortedScannerIterable<PK> scanKeys(PK startKey, boolean startInclusive, PK end, boolean endInclusive, 
			Config config){
		return mainNode.scanKeys(startKey,startInclusive, end, endInclusive, config);
	};
	
	@Override
	public SortedScannerIterable<D> scan(PK startKey, boolean startInclusive, PK end, boolean endInclusive, 
			Config config){
		return mainNode.scan(startKey,startInclusive, end, endInclusive, config);
	};
	
}

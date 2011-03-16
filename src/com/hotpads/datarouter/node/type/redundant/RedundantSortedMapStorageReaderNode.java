package com.hotpads.datarouter.node.type.redundant;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.op.combo.reader.SortedMapStorageReader.SortedMapStorageReaderNode;
import com.hotpads.datarouter.node.op.raw.read.SortedStorageReader.SortedStorageReaderNode;
import com.hotpads.datarouter.routing.DataRouter;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.iterable.PeekableIterable;

public class RedundantSortedMapStorageReaderNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		N extends SortedMapStorageReaderNode<PK,D>>
extends RedundantMapStorageReaderNode<PK,D,N>
implements SortedStorageReaderNode<PK,D>{
	
	public RedundantSortedMapStorageReaderNode(Class<D> databeanClass, DataRouter router,
			Collection<N> writeNodes, N readNode) {
		super(databeanClass, router, writeNodes, readNode);
	}

	/***************** SortedStorageReader ************************************/
	
	@Override
	public D getFirst(Config config) {
		return readNode.getFirst(config);
	}

	@Override
	public PK getFirstKey(Config config) {
		return readNode.getFirstKey(config);
	}
	
	@Override
	public List<D> getPrefixedRange(
			PK prefix, boolean wildcardLastField,
			PK start, boolean startInclusive, Config config) {
		return readNode.getPrefixedRange(
				prefix, wildcardLastField, start, startInclusive, config);
	}

	@Override
	public List<PK> getKeysInRange(PK start, boolean startInclusive, PK end,
			boolean endInclusive, Config config) {
		return readNode.getKeysInRange(start, startInclusive, end, endInclusive, config);
	}

	@Override
	public List<D> getRange(PK start, boolean startInclusive, PK end,
			boolean endInclusive, Config config) {
		return readNode.getRange(start, startInclusive, end, endInclusive, config);
	}

	@Override
	public List<D> getWithPrefix(PK prefix, boolean wildcardLastField, Config config) {
		return readNode.getWithPrefix(prefix,wildcardLastField, config);
	}

	@Override
	public List<D> getWithPrefixes(Collection<? extends PK> prefixes, boolean wildcardLastField, Config config) {
		return readNode.getWithPrefixes(prefixes, wildcardLastField, config);
	}
	
	@Override
	public PeekableIterable<PK> scanKeys(PK startKey, boolean startInclusive, PK end, boolean endInclusive, Config config){
		return readNode.scanKeys(startKey,startInclusive, end, endInclusive, config);
	};
	
	@Override
	public PeekableIterable<D> scan(PK startKey, boolean startInclusive, PK end, boolean endInclusive, Config config){
		return readNode.scan(startKey,startInclusive, end, endInclusive, config);
	};
	
}

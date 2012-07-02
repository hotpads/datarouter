package com.hotpads.datarouter.node.op.raw.read;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.op.NodeOps;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.iterable.PeekableIterable;
import com.hotpads.util.core.iterable.scanner.iterable.SortedScannerIterable;

public interface SortedStorageReader<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
extends NodeOps<PK,D>
{

	PK getFirstKey(Config config);
	D getFirst(Config config);
//	
//	List<Key<D>> getKeysWithPrefix(Key<D> prefix, Config config);
	List<D> getWithPrefix(PK prefix, boolean wildcardLastField, Config config);
	List<D> getWithPrefixes(Collection<PK> prefixes, boolean wildcardLastField, Config config);

	List<PK> getKeysInRange(PK start, boolean startInclusive, PK end, boolean endInclusive, Config config);
	List<D> getRange(PK start, boolean startInclusive, PK end, boolean endInclusive, Config config);

	List<D> getPrefixedRange(
			final PK prefix, final boolean wildcardLastField, 
			final PK start, final boolean startInclusive, 
			final Config config);

	SortedScannerIterable<PK> scanKeys(
			final PK start, final boolean startInclusive, 
			final PK end, final boolean endInclusive, 
			final Config config);
	SortedScannerIterable<D> scan(
			final PK start, final boolean startInclusive, 
			final PK end, final boolean endInclusive, 
			final Config config);

	
	
	public interface SortedStorageReaderNode<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	extends Node<PK,D>, SortedStorageReader<PK,D>
	{
	}

	public interface PhysicalSortedStorageReaderNode<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	extends PhysicalNode<PK,D>, SortedStorageReaderNode<PK,D>
	{
	}
}

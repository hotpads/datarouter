package com.hotpads.datarouter.node.op.raw.read;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.op.NodeOps;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.core.iterable.scanner.iterable.SortedScannerIterable;

/**
 * Methods for reading from storage mechanisms that keep databeans sorted by PrimaryKey.  Similar to java's TreeMap.
 * 
 * Possible implementations include TreeMap, RDBMS, HBase, LevelDB, Google Cloud Datastore, etc
 * 
 * @author mcorgan
 *
 * @param <PK>
 * @param <D>
 */
public interface SortedStorageReader<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
extends NodeOps<PK,D>
{
	public static final String
		OP_getFirstKey = "getFirstKey",
		OP_getFirst = "getFirst",
		OP_getWithPrefix = "getWithPrefix",
		OP_getWithPrefixes = "getWithPrefixes",
		OP_getKeysInRange = "getKeysInRange",
		OP_getRange = "getRange",
		OP_getPrefixedRange = "getPrefixedRange",
		OP_scanKeys = "scanKeys",
		OP_scan = "scan";

	PK getFirstKey(Config config);
	D getFirst(Config config);
	
	List<D> getWithPrefix(PK prefix, boolean wildcardLastField, Config config);
	List<D> getWithPrefixes(Collection<PK> prefixes, boolean wildcardLastField, Config config);

	SortedScannerIterable<PK> scanKeys(final Range<PK> range, final Config config);
	SortedScannerIterable<D> scan(final Range<PK> range, final Config config);

	
	
	public interface SortedStorageReaderNode<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	extends Node<PK,D>, SortedStorageReader<PK,D>
	{
	}

	public interface PhysicalSortedStorageReaderNode<PK extends PrimaryKey<PK>,D extends Databean<PK,D>>
	extends PhysicalNode<PK,D>, SortedStorageReaderNode<PK,D>
	{
	}
}

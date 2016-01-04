package com.hotpads.datarouter.node.op.raw.read;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.Iterables;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.Node;
import com.hotpads.datarouter.node.op.NodeOps;
import com.hotpads.datarouter.node.type.physical.PhysicalNode;
import com.hotpads.datarouter.op.util.SortedStorageCountingTool;
import com.hotpads.datarouter.storage.databean.Databean;
import com.hotpads.datarouter.storage.key.primary.PrimaryKey;
import com.hotpads.util.core.collections.KeyRangeTool;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.core.stream.StreamTool;

/**
 * Methods for reading from storage mechanisms that keep databeans sorted by PrimaryKey.  Similar to java's TreeMap.
 *
 * Possible implementations include TreeMap, RDBMS, HBase, LevelDB, Google Cloud Datastore, Google Cloud BigTable, etc
 */
public interface SortedStorageReader<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>>
extends NodeOps<PK,D>{

	public static final String
		OP_getWithPrefix = "getWithPrefix",
		OP_getWithPrefixes = "getWithPrefixes",
		OP_getKeysInRange = "getKeysInRange",
		OP_getRange = "getRange",
		OP_getPrefixedRange = "getPrefixedRange",
		OP_scanKeys = "scanKeys",
		OP_scan = "scan";


	/**
	 * @deprecated use stream(KeyRangeTool.forPrefix(prefix), config) or scan
	 * If you want to wildcard the last field, use KeyRangeTool.forPrefixWithWildCard
	 */
	@Deprecated
	List<D> getWithPrefix(PK prefix, boolean wildcardLastField, Config config);

	/**
	 * @deprecated iterate on the prefixes and see getWithPrefix
	 */
	@Deprecated
	List<D> getWithPrefixes(Collection<PK> prefixes, boolean wildcardLastField, Config config);

	Iterable<PK> scanKeys(final Range<PK> range, final Config config);
	Iterable<D> scan(final Range<PK> range, final Config config);

	default Stream<PK> streamKeys(Range<PK> range, Config config){
		return StreamTool.stream(scanKeys(range, config));
	}

	default Stream<D> stream(Range<PK> range, Config config){
		return StreamTool.stream(scan(range, config));
	}

	default Long count(Range<PK> range){
		return SortedStorageCountingTool.count(this, range);
	}

	default Stream<PK> streamKeysWithPrefix(PK prefix, Config config){
		return streamKeys(KeyRangeTool.forPrefix(prefix), config);
	}

	default Stream<D> streamWithPrefix(PK prefix, Config config){
		return stream(KeyRangeTool.forPrefix(prefix), config);
	}

	default Iterable<PK> scanKeysWithPrefix(PK prefix, Config config){
		return scanKeys(KeyRangeTool.forPrefix(prefix), config);
	}

	default Iterable<D> scanWithPrefix(PK prefix, Config config){
		return scan(KeyRangeTool.forPrefix(prefix), config);
	}

	default Stream<PK> streamKeysWithPrefixes(Collection<PK> prefixes, Config config){
		return prefixes.stream().flatMap(prefix -> streamKeysWithPrefix(prefix, config));
	}

	default Stream<D> streamWithPrefixes(Collection<PK> prefixes, Config config){
		return getWithPrefixes(prefixes, false, config).stream();
	}

	default Iterable<PK> scanKeysWithPrefixes(Collection<PK> prefixes, Config config){
		return Iterables.concat(prefixes.stream()
				.map(prefix -> scanKeysWithPrefix(prefix, config))
				.collect(Collectors.toList()));
	}

	default Iterable<D> scanWithPrefixes(Collection<PK> prefixes, Config config){
		return Iterables.concat(prefixes.stream()
				.map(prefix -> scanWithPrefix(prefix, config))
				.collect(Collectors.toList()));
	}

	/*************** sub-interfaces ***********************/

	public interface SortedStorageReaderNode<
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>>
	extends Node<PK,D>, SortedStorageReader<PK,D>{
	}


	public interface PhysicalSortedStorageReaderNode<
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>>
	extends PhysicalNode<PK,D>, SortedStorageReaderNode<PK,D>{
	}
}

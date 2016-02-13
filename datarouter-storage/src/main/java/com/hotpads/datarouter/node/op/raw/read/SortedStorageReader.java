package com.hotpads.datarouter.node.op.raw.read;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
 *
 *
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
		OP_scanKeysMulti = "scanKeysMulti",
		OP_scan = "scan",
		OP_scanMulti = "scanMulti";


	/**
	 * @deprecated If wildcardLastField is false, use {@link #streamWithPrefix(PK, Config)}
	 * or {@link #scanWithPrefix(PK, Config)}. If wildcardLastField is true, use {@link #stream(Range, Config)} or
	 * {@link #scan(Range, Config)} with
	 * {@link KeyRangeTool#forPrefixWithWildcard(String,
	 * com.hotpads.util.core.collections.KeyRangeTool.KeyWithStringFieldSuffixProvider)} to make the Range
	 */
	@Deprecated
	List<D> getWithPrefix(PK prefix, boolean wildcardLastField, Config config);

	/**
	 * @deprecated use {@link #streamWithPrefixes(Collection, Config)} or {@link #scanWithPrefixes(Collection, Config)}
	 */
	@Deprecated
	List<D> getWithPrefixes(Collection<PK> prefixes, boolean wildcardLastField, Config config);

	Iterable<D> scanMulti(Collection<Range<PK>> ranges, Config config);
	Iterable<PK> scanKeysMulti(Collection<Range<PK>> ranges, Config config);


	/*******************************************************
	 * default interface methods
	 *******************************************************/

	/****************** scan *************************/

	/**
	 * The scan method accepts a Range<PK> which identifies the startKey and endKey, and returns all of the rows between
	 * those keys without skipping any. Implementations will generally query the database in batches to avoid long
	 * transactions and huge result sets.
	 *
	 * When providing startKey and endKey, implementations will ignore fields after the first null.  For example, when
	 * scanning a phone book with startKey: <br/>
	 * * (null, null) is valid and will start at the beginning of the book<br/>
	 * * (Corgan, null) is valid and will start at the first Corgan <br/>
	 * * (Corgan, Matt) is valid and will start at Corgan, Matt <br/>
	 * * (null, Matt) is invalid.  The Matt is ignored, so it is equivalent to (null, null) <br/>
	 *
	 * Note that (null, Matt) will NOT do any filtering for rows with firstName=Matt. To avoid tablescans we are
	 * returning all rows in the range to the client where the client can then filter. A predicate push-down feature may
	 * be added, but it will likely use a separate interface method.
	 */
	default Iterable<D> scan(final Range<PK> range, final Config config){
		return scanMulti(Arrays.asList(Range.nullSafe(range)), config);
	}

	default Iterable<PK> scanKeys(final Range<PK> range, final Config config){
		return scanKeysMulti(Arrays.asList(Range.nullSafe(range)), config);
	}

	/****************** stream *************************/

	default Stream<D> stream(Range<PK> range, Config config){
		return StreamTool.stream(scan(range, config));
	}

	default Stream<D> streamMulti(Collection<Range<PK>> ranges, Config config){
		return StreamTool.stream(scanMulti(ranges, config));
	}

	default Stream<PK> streamKeys(Range<PK> range, Config config){
		return StreamTool.stream(scanKeys(range, config));
	}

	default Stream<PK> streamKeysMulti(Collection<Range<PK>> ranges, Config config){
		return StreamTool.stream(scanKeysMulti(ranges, config));
	}

	/****************** count  *************************/

	default Long count(Range<PK> range){
		return SortedStorageCountingTool.count(this, range);
	}

	/****************** prefix *************************/

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
		return streamKeysMulti(getRangesFromPrefixes(prefixes), config);
	}

	default Stream<D> streamWithPrefixes(Collection<PK> prefixes, Config config){
		return StreamTool.stream(scanWithPrefixes(prefixes, config));
	}

	default Iterable<PK> scanKeysWithPrefixes(Collection<PK> prefixes, Config config){
		return scanKeysMulti(getRangesFromPrefixes(prefixes), config);
	}

	default Iterable<D> scanWithPrefixes(Collection<PK> prefixes, Config config){
		return scanMulti(getRangesFromPrefixes(prefixes), config);
	}

	/************** static methods *************************/

	static <PK extends PrimaryKey<PK>> List<Range<PK>> getRangesFromPrefixes(Collection<PK> prefixes){
		return prefixes.stream().map(KeyRangeTool::forPrefix).collect(Collectors.toList());
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

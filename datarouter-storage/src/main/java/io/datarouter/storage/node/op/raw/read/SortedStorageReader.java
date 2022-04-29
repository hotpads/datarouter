/*
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.storage.node.op.raw.read;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.Node;
import io.datarouter.storage.node.op.NodeOps;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.storage.op.scan.stride.StrideScanner.StrideScannerBuilder;
import io.datarouter.storage.util.KeyRangeTool;
import io.datarouter.util.tuple.Range;

/**
 * Methods for reading from storage mechanisms that keep databeans sorted by PrimaryKey.  Similar to java's TreeMap.
 *
 * Possible implementations include TreeMap, RDBMS, HBase, LevelDB, Google Cloud Datastore, Google Cloud BigTable, etc
 */
public interface SortedStorageReader<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>>
extends NodeOps<PK,D>{

	public static final String OP_getKeysInRange = "getKeysInRange";
	public static final String OP_getRange = "getRange";
	public static final String OP_getPrefixedRange = "getPrefixedRange";
	public static final String OP_scanKeys = "scanKeys";
	public static final String OP_scanRangesKeys = "scanRangesKeys";
	public static final String OP_scan = "scan";
	public static final String OP_scanRanges = "scanRanges";

	/*-------------------------------- scan multi ---------------------------------*/

	Scanner<D> scanRanges(Collection<Range<PK>> ranges, Config config);

	default Scanner<D> scanRanges(Collection<Range<PK>> ranges){
		return scanRanges(ranges, new Config());
	}

	/*-------------------------------- scan ---------------------------------*/

	/**
	 * The scan method accepts a Range&lt;PK&gt; which identifies the startKey and endKey, and returns all contiguous
	 * rows between them, not skipping or filtering any. Implementations will generally query the database in batches to
	 * avoid long transactions and huge result sets. <br>
	 * <br>
	 * When providing startKey and endKey, implementations will ignore fields after the first null.  For example, when
	 * scanning a phone book with startKey: <br>
	 * * (null, null) is valid and will start at the beginning of the book<br>
	 * * (Corgan, null) is valid and will start at the first Corgan <br>
	 * * (Corgan, Matt) is valid and will start at Corgan, Matt <br>
	 * * (null, Matt) is invalid.  The Matt is ignored, so it is equivalent to (null, null) <br>
	 * <br>
	 * Note that (null, Matt) will NOT do any filtering for rows with firstName=Matt. To avoid tablescans we are
	 * returning all rows in the range to the client where the client can then filter. A predicate push-down feature may
	 * be added, but it will likely use a separate interface method.
	 */
	default Scanner<D> scan(Range<PK> range, Config config){
		return scanRanges(Collections.singletonList(range), config);
	}

	default Scanner<D> scan(Range<PK> range){
		return scan(range, new Config());
	}

	default Scanner<D> scan(Config config){
		return scan(Range.everything(), config);
	}

	default Scanner<D> scan(){
		return scan(Range.everything(), new Config());
	}

	/*-------------------------------- scan keys multi ------------------------------*/

	Scanner<PK> scanRangesKeys(Collection<Range<PK>> ranges, Config config);

	default Scanner<PK> scanRangesKeys(Collection<Range<PK>> ranges){
		return scanRangesKeys(ranges, new Config());
	}

	/*-------------------------------- scan keys ------------------------------*/

	default Scanner<PK> scanKeys(Range<PK> range, Config config){
		return scanRangesKeys(Collections.singletonList(range), config);
	}

	default Scanner<PK> scanKeys(Range<PK> range){
		return scanKeys(range, new Config());
	}

	default Scanner<PK> scanKeys(Config config){
		return scanKeys(Range.everything(), config);
	}

	default Scanner<PK> scanKeys(){
		return scanKeys(Range.everything(), new Config());
	}

	/*-------------------------------- prefix -------------------------------*/

	default Scanner<D> scanWithPrefix(PK prefix, Config config){
		return scan(KeyRangeTool.forPrefix(prefix), config);
	}

	default Scanner<D> scanWithPrefix(PK prefix){
		return scanWithPrefix(prefix, new Config());
	}

	/*-------------------------------- prefix keys -------------------------------*/

	default Scanner<PK> scanKeysWithPrefix(PK prefix, Config config){
		return scanKeys(KeyRangeTool.forPrefix(prefix), config);
	}

	default Scanner<PK> scanKeysWithPrefix(PK prefix){
		return scanKeysWithPrefix(prefix, new Config());
	}

	/*-------------------------------- prefixes -------------------------------*/

	default Scanner<D> scanWithPrefixes(Collection<PK> prefixes, Config config){
		return scanRanges(getRangesFromPrefixes(prefixes), config);
	}

	default Scanner<D> scanWithPrefixes(Collection<PK> prefixes){
		return scanWithPrefixes(prefixes, new Config());
	}

	/*-------------------------------- prefixes keys -------------------------------*/

	default Scanner<PK> scanKeysWithPrefixes(Collection<PK> prefixes, Config config){
		return scanRangesKeys(getRangesFromPrefixes(prefixes), config);
	}

	default Scanner<PK> scanKeysWithPrefixes(Collection<PK> prefixes){
		return scanKeysWithPrefixes(prefixes, new Config());
	}

	/*-------------------------------- count --------------------------------*/

	default long count(Range<PK> range){
		return new StrideScannerBuilder<>(this)
				.withRange(range)
				.build()
				.findLast()
				.map(sample -> sample.totalCount)
				.orElse(0L);
	}

	/*---------------------------- static methods ---------------------------*/

	static <PK extends PrimaryKey<PK>> List<Range<PK>> getRangesFromPrefixes(Collection<PK> prefixes){
		return prefixes.stream()
				.map(KeyRangeTool::forPrefix)
				.collect(Collectors.toList());
	}

	/*---------------------------- sub-interfaces ---------------------------*/

	public interface SortedStorageReaderNode<
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	extends Node<PK,D,F>, SortedStorageReader<PK,D>{
	}


	public interface PhysicalSortedStorageReaderNode<
			PK extends PrimaryKey<PK>,
			D extends Databean<PK,D>,
			F extends DatabeanFielder<PK,D>>
	extends PhysicalNode<PK,D,F>, SortedStorageReaderNode<PK,D,F>{
	}
}

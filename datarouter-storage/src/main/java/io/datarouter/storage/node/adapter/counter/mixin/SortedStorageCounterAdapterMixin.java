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
package io.datarouter.storage.node.adapter.counter.mixin;

import java.util.Collection;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.adapter.counter.CounterAdapter;
import io.datarouter.storage.node.op.raw.SortedStorage;
import io.datarouter.storage.node.op.raw.SortedStorage.SortedStorageNode;
import io.datarouter.util.tuple.Range;

public interface SortedStorageCounterAdapterMixin<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>,
		N extends SortedStorageNode<PK,D,F>>
extends SortedStorage<PK,D>, CounterAdapter<PK,D,F,N>{

	// keys
	final String NAME_scanKeys_scans = "scanKeys scans";
	final String NAME_scanKeys_ranges = "scanKeys ranges";
	final String NAME_scanKeys_batches = "scanKeys batches";
	final String NAME_scanKeys_rows = "scanKeys rows";
	// databeans
	final String NAME_scanDatabeans_scans = "scanDatabeans scans";
	final String NAME_scanDatabeans_ranges = "scanDatabeans ranges";
	final String NAME_scanDatabeans_batches = "scanDatabeans batches";
	final String NAME_scanDatabeans_rows = "scanDatabeans rows";
	// keys+databeans
	final String NAME_scan_scans = "scan scans";
	final String NAME_scan_ranges = "scan ranges";
	final String NAME_scan_batches = "scan batches";
	final String NAME_scan_rows = "scan rows";
	//count
	final String NAME_count_rows = "count rows";

	// TODO To avoid batching/unbatching:
	//   Modify the SortedStorageReader interface to allow implementations to return batches.
	//   Convert the per-row scanner methods to default interface methods that flatten the batches.

	// TODO To avoid duplicate code:
	//   Make scanKeys(range) and scan(range) default interface methods.
	//   They can call scanKeysRanges(List.of(range)) and scanRanges(List.of(range)).

	@Override
	default Scanner<PK> scanKeys(Range<PK> range, Config config){
		getCounter().count(NAME_scan_scans);
		getCounter().count(NAME_scanKeys_scans);
		getCounter().count(NAME_scan_ranges);
		getCounter().count(NAME_scanKeys_ranges);
		return getBackingNode().scanKeys(range, config)
				// Assumes batch/count/flatten is more efficient than incrementing for each row.
				.batch(config.findResponseBatchSize().orElse(Config.DEFAULT_RESPONSE_BATCH_SIZE))
				.each(batch -> {
					getCounter().count(NAME_scan_batches);
					getCounter().count(NAME_scanKeys_batches);
					getCounter().count(NAME_scan_rows, batch.size());
					getCounter().count(NAME_scanKeys_rows, batch.size());
				})
				.concat(Scanner::of);
	}

	@Override
	default Scanner<PK> scanRangesKeys(Collection<Range<PK>> ranges, Config config){
		getCounter().count(NAME_scan_scans);
		getCounter().count(NAME_scanKeys_scans);
		getCounter().count(NAME_scan_ranges, ranges.size());
		getCounter().count(NAME_scanKeys_ranges, ranges.size());
		return getBackingNode().scanRangesKeys(ranges, config)
				.batch(config.findResponseBatchSize().orElse(Config.DEFAULT_RESPONSE_BATCH_SIZE))
				.each(batch -> {
					getCounter().count(NAME_scan_batches);
					getCounter().count(NAME_scanKeys_batches);
					getCounter().count(NAME_scan_rows, batch.size());
					getCounter().count(NAME_scanKeys_rows, batch.size());
				})
				.concat(Scanner::of);
	}

	@Override
	default Scanner<D> scan(Range<PK> range, Config config){
		getCounter().count(NAME_scan_scans);
		getCounter().count(NAME_scanDatabeans_scans);
		getCounter().count(NAME_scan_ranges);
		getCounter().count(NAME_scanDatabeans_ranges);
		return getBackingNode().scan(range, config)
				.batch(config.findResponseBatchSize().orElse(Config.DEFAULT_RESPONSE_BATCH_SIZE))
				.each(batch -> {
					getCounter().count(NAME_scan_batches);
					getCounter().count(NAME_scanDatabeans_batches);
					getCounter().count(NAME_scan_rows, batch.size());
					getCounter().count(NAME_scanDatabeans_rows, batch.size());
				})
				.concat(Scanner::of);
	}

	@Override
	default Scanner<D> scanRanges(Collection<Range<PK>> ranges, Config config){
		getCounter().count(NAME_scan_scans);
		getCounter().count(NAME_scanDatabeans_scans);
		getCounter().count(NAME_scan_ranges, ranges.size());
		getCounter().count(NAME_scanDatabeans_ranges, ranges.size());
		return getBackingNode().scanRanges(ranges, config)
				.batch(config.findResponseBatchSize().orElse(Config.DEFAULT_RESPONSE_BATCH_SIZE))
				.each(batch -> {
					getCounter().count(NAME_scan_batches);
					getCounter().count(NAME_scanDatabeans_batches);
					getCounter().count(NAME_scan_rows, batch.size());
					getCounter().count(NAME_scanDatabeans_rows, batch.size());
				})
				.concat(Scanner::of);
	}

}

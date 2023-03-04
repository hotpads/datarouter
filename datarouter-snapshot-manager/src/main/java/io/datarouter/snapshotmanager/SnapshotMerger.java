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
package io.datarouter.snapshotmanager;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.filesystem.snapshot.group.SnapshotGroup;
import io.datarouter.filesystem.snapshot.group.dto.SnapshotKeyAndNumRecords;
import io.datarouter.filesystem.snapshot.group.dto.SnapshotWriteResult;
import io.datarouter.filesystem.snapshot.key.SnapshotKey;
import io.datarouter.filesystem.snapshot.reader.ScanningSnapshotReader;
import io.datarouter.filesystem.snapshot.reader.record.SnapshotLeafRecord;
import io.datarouter.filesystem.snapshot.writer.SnapshotWriterConfig;
import io.datarouter.scanner.Scanner;
import io.datarouter.scanner.Threads;

/**
 * Merge snapshots in the mergeGroup until there are fewer than the mergeFactor, then merge the remaining into
 * the destinationGroup.
 */
public class SnapshotMerger{
	private static final Logger logger = LoggerFactory.getLogger(SnapshotMerger.class);

	public record SnapshotMergerParams(
			Supplier<Boolean> shouldStop,
			SnapshotGroup mergeGroup,
			SnapshotGroup destinationGroup,
			ExecutorService readExec,
			ExecutorService writeExec,
			SnapshotWriterConfig mergeWriterConfig,
			SnapshotWriterConfig destinationWriterConfig,
			int prefetchThreads,
			int prefetchBlocks,
			int mergeFactor){
	}

	private final SnapshotMergerParams params;

	public SnapshotMerger(SnapshotMergerParams params){
		this.params = params;
	}

	public void merge(){
		Map<SnapshotKey,SnapshotKeyAndNumRecords> summaryByKey = params.mergeGroup.keyReadOps(false)
				.scanSnapshotKeysAndRootBlocks(new Threads(params.readExec, 10))
				.map(SnapshotKeyAndNumRecords::new)
				.toMap(SnapshotKeyAndNumRecords::key);
		while(summaryByKey.size() > 1){
			boolean isFinalSnapshot = summaryByKey.size() <= params.mergeFactor;
			SnapshotGroup outputGroup = isFinalSnapshot
					? params.destinationGroup
					: params.mergeGroup;
			SnapshotWriterConfig writerConfig = isFinalSnapshot
					? params.destinationWriterConfig
					: params.mergeWriterConfig;
			Scanner.of(summaryByKey.values())
					.minN(SnapshotKeyAndNumRecords.BY_NUM_RECORDS, params.mergeFactor)
					.map(SnapshotKeyAndNumRecords::key)
					.flush(keys -> {
						SnapshotWriteResult result = combineSnapshots(keys, outputGroup, writerConfig);
						var newSummary = new SnapshotKeyAndNumRecords(result.toSnapshotKeyAndRoot());
						summaryByKey.put(result.key, newSummary);
					})
					.forEach(summaryByKey::remove);
		}
	}

	private SnapshotWriteResult combineSnapshots(
			List<SnapshotKey> keys,
			SnapshotGroup outputGroup,
			SnapshotWriterConfig writerConfig){
		SnapshotWriteResult result = Scanner.of(keys)
				.map(key -> new ScanningSnapshotReader(
						key,
						new Threads(params.readExec, params.prefetchThreads),
						params.mergeGroup,
						params.prefetchBlocks))
				.collate(reader -> reader.scanLeafRecords(0), SnapshotLeafRecord.KEY_COMPARATOR)
				.deduplicateConsecutiveBy(SnapshotLeafRecord::key, Arrays::equals)
				.map(SnapshotLeafRecord::entry)
				.batch(10_000)
				.apply(batches -> outputGroup.writeOps().write(
						writerConfig,
						batches,
						params.writeExec,
						params.shouldStop));
		keys.forEach(key -> params.mergeGroup.deleteOps().deleteSnapshot(key, new Threads(params.writeExec, 10)));
		logger.warn("combined {}, {}", keys.size(), keys);
		return result;
	}

}

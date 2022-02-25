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
package io.datarouter.filesystem.snapshot.combine;

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

/**
 * Merge snapshots in the mergeGroup until there are fewer than the mergeFactor, then merge the remaining into
 * the destinationGroup.
 */
public class SnapshotMerger{
	private static final Logger logger = LoggerFactory.getLogger(SnapshotMerger.class);

	private final Supplier<Boolean> shouldStop;
	private final SnapshotGroup mergeGroup;
	private final SnapshotGroup destinationGroup;
	private final ExecutorService readExec;
	private final ExecutorService writeExec;
	private final SnapshotWriterConfig writerConfig;
	private final int scanNumBlocks;
	private final int mergeFactor;

	public SnapshotMerger(
			Supplier<Boolean> shouldStop,
			SnapshotGroup mergeGroup,
			SnapshotGroup destinationGroup,
			ExecutorService readExec,
			ExecutorService writeExec,
			SnapshotWriterConfig writerConfig,
			int scanNumBlocks,
			int mergeFactor){
		this.shouldStop = shouldStop;
		this.mergeGroup = mergeGroup;
		this.destinationGroup = destinationGroup;
		this.readExec = readExec;
		this.writeExec = writeExec;
		this.writerConfig = writerConfig;
		this.scanNumBlocks = scanNumBlocks;
		this.mergeFactor = mergeFactor;
	}

	public void merge(){
		Map<SnapshotKey,SnapshotKeyAndNumRecords> summaryByKey = mergeGroup.keyReadOps(false)
				.scanSnapshotKeysAndRootBlocks(readExec, 10)
				.map(SnapshotKeyAndNumRecords::new)
				.toMap(summary -> summary.key);
		while(summaryByKey.size() > 1){
			SnapshotGroup outputGroup = summaryByKey.size() <= mergeFactor ? destinationGroup : mergeGroup;
			Scanner.of(summaryByKey.values())
					.minN(SnapshotKeyAndNumRecords.BY_NUM_RECORDS, mergeFactor)
					.map(summary -> summary.key)
					.flush(keys -> {
						SnapshotWriteResult result = combineSnapshots(keys, outputGroup);
						var newSummary = new SnapshotKeyAndNumRecords(result.toSnapshotKeyAndRoot());
						summaryByKey.put(result.key, newSummary);
					})
					.forEach(summaryByKey::remove);
		}
	}

	private SnapshotWriteResult combineSnapshots(List<SnapshotKey> keys, SnapshotGroup outputGroup){
		SnapshotWriteResult result = Scanner.of(keys)
				.map(key -> new ScanningSnapshotReader(key, readExec, 10, mergeGroup, scanNumBlocks))
				.collate(reader -> reader.scanLeafRecords(0), SnapshotLeafRecord.KEY_COMPARATOR)
				.deduplicateConsecutiveBy(leafRecord -> leafRecord.key, Arrays::equals)
				.map(SnapshotLeafRecord::entry)
				.batch(10_000)
				.apply(batches -> outputGroup.writeOps().write(
						writerConfig,
						batches,
						writeExec,
						shouldStop));
		keys.forEach(key -> mergeGroup.deleteOps().deleteSnapshot(key, writeExec, 10));
		logger.warn("combined {}, {}", keys.size(), keys);
		return result;
	}

}

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

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.filesystem.snapshot.entry.SnapshotEntry;
import io.datarouter.filesystem.snapshot.group.SnapshotGroup;
import io.datarouter.filesystem.snapshot.group.dto.SnapshotKeyAndNumRecords;
import io.datarouter.filesystem.snapshot.key.SnapshotKey;
import io.datarouter.filesystem.snapshot.reader.ScanningSnapshotReader;
import io.datarouter.filesystem.snapshot.reader.record.SnapshotLeafRecord;
import io.datarouter.filesystem.snapshot.writer.SnapshotWriterConfig;
import io.datarouter.scanner.Scanner;

public class SnapshotCombiner{
	private static final Logger logger = LoggerFactory.getLogger(SnapshotCombiner.class);

	private final Supplier<Boolean> shouldStop;
	private final SnapshotGroup group;
	private final ExecutorService readExec;
	private final ExecutorService writeExec;
	private final SnapshotWriterConfig writerConfig;
	private final int targetNumSnapshots;
	private final int maxNewSnapshotsPerIteration;

	public SnapshotCombiner(
			Supplier<Boolean> shouldStop,
			SnapshotGroup group,
			ExecutorService readExec,
			ExecutorService writeExec,
			SnapshotWriterConfig writerConfig,
			int targetNumSnapshots,
			int maxNewSnapshotsPerIteration){
		this.shouldStop = shouldStop;
		this.group = group;
		this.readExec = readExec;
		this.writeExec = writeExec;
		this.writerConfig = writerConfig;
		this.targetNumSnapshots = targetNumSnapshots;
		this.maxNewSnapshotsPerIteration = maxNewSnapshotsPerIteration;
	}

	public void combine(){
		while(tryCombineSmallestN(targetNumSnapshots, maxNewSnapshotsPerIteration)){
		}
	}

	private boolean tryCombineSmallestN(
			int targetNumSnapshots,
			long maxNewSnapshots){
		return group.keyReadOps(false).scanSnapshotKeysAndRootBlocks(readExec, 10)
				.map(SnapshotKeyAndNumRecords::new)
				.listTo(dtos -> SnapshotCombineTool.scanSmallestGroups(
						dtos,
						targetNumSnapshots,
						10))
				.map(batch -> Scanner.of(batch).map(dto -> dto.key).list())
				.advanceUntil($ -> shouldStop.get())
				.each(keys -> combineSnapshots(keys))
				.each(keys -> logger.warn("combined {}, {}", keys.size(), keys))
				.limit(maxNewSnapshots)
				.hasAny();
	}

	private void combineSnapshots(List<SnapshotKey> keys){
		Scanner.of(keys)
				.map(key -> new ScanningSnapshotReader(key, readExec, 10, group))
				.collate(reader -> reader.scanLeafRecords(0), SnapshotLeafRecord.KEY_COMPARATOR)
				.map(SnapshotLeafRecord::entry)
				.batch(10_000)
				.then(this::writeSnapshot);
		keys.forEach(key -> group.deleteOps().deleteSnapshot(key, writeExec, 10));
	}

	private void writeSnapshot(Scanner<List<SnapshotEntry>> entryBatches){
		entryBatches.then(batches -> group.writeOps().write(
				writerConfig,
				batches,
				writeExec,
				shouldStop));
	}

}

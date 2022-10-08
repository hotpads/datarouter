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

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

import io.datarouter.filesystem.snapshot.entry.SnapshotEntry;
import io.datarouter.filesystem.snapshot.group.SnapshotGroup;
import io.datarouter.filesystem.snapshot.writer.SnapshotWriterConfig;
import io.datarouter.scanner.ObjectScanner;
import io.datarouter.scanner.ParallelScannerContext;
import io.datarouter.scanner.Scanner;
import io.datarouter.snapshotmanager.SnapshotMerger.SnapshotMergerParams;
import io.datarouter.util.Require;

/**
 * - Accept a Scanner of lists of SnapshotEntry
 * - Each list:
 *   - should be large but fit in memory
 *   - is sorted in memory
 *   - is flushed to a temporary snapshot to be merged
 * - Merge all the snapshots into a final snapshot in the destinationGroup
 */
public class SortingSnapshotWriter{

	public record SortingSnapshotWriterParams(
			Supplier<Boolean> shouldStop,
			Scanner<List<SnapshotEntry>> entries,
			SnapshotGroup mergeGroup,
			SnapshotWriterConfig mergeWriterConfig,
			SnapshotGroup destinationGroup,
			SnapshotWriterConfig destinationWriterConfig,
			ExecutorService sortExec,
			int sortThreads,
			ExecutorService readExec,
			ExecutorService writeExec,
			int prefetchThreads,
			int prefetchBlocks,
			int mergeFactor){
	}

	private final SortingSnapshotWriterParams params;

	public SortingSnapshotWriter(SortingSnapshotWriterParams params){
		this.params = params;
		Require.isTrue(params.mergeWriterConfig.sorted());
	}

	public void sort(){
		createSortedSplits();
		merge();
	}

	private void createSortedSplits(){
		params.entries
				.advanceUntil($ -> params.shouldStop.get())
				.parallel(new ParallelScannerContext(
						params.sortExec,
						params.sortThreads,
						false,
						params.sortThreads > 0))
				.each(split -> Collections.sort(split, SnapshotEntry.KEY_COMPARATOR))
				.forEach(split -> params.mergeGroup.writeOps().write(
						params.mergeWriterConfig,
						ObjectScanner.of(split),
						params.writeExec,
						params.shouldStop));
	}

	private void merge(){
		var mergerParams = new SnapshotMergerParams(
				params.shouldStop,
				params.mergeGroup,
				params.destinationGroup,
				params.readExec,
				params.writeExec,
				params.mergeWriterConfig,
				params.destinationWriterConfig,
				params.prefetchThreads,
				params.prefetchBlocks,
				params.mergeFactor);
		new SnapshotMerger(mergerParams).merge();
	}

}

/**
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
package io.datarouter.filesystem.snapshot.benchmark;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.filesystem.snapshot.block.root.RootBlock;
import io.datarouter.filesystem.snapshot.compress.PassthroughBlockCompressor;
import io.datarouter.filesystem.snapshot.entry.SnapshotEntry;
import io.datarouter.filesystem.snapshot.group.SnapshotGroup;
import io.datarouter.filesystem.snapshot.group.dto.SnapshotWriteResult;
import io.datarouter.filesystem.snapshot.key.SnapshotKey;
import io.datarouter.filesystem.snapshot.writer.SnapshotWriterConfig;
import io.datarouter.filesystem.snapshot.writer.SnapshotWriterConfigBuilder;
import io.datarouter.scanner.ParallelScannerContext;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.bytes.ByteTool;
import io.datarouter.util.bytes.LongByteTool;
import io.datarouter.util.concurrent.ExecutorServiceTool;
import io.datarouter.util.number.NumberFormatter;
import io.datarouter.util.timer.PhaseTimer;

public class SnapshotBenchmark{
	private static final Logger logger = LoggerFactory.getLogger(SnapshotBenchmark.class);

	private static final int KEY_LENGTH = 8;
	private static final int VALUE_LENGTH = 8;
	public static final int WRITE_BATCH_SIZE = 10_000;

	public final SnapshotGroup group;

	private final int numInputThreads;
	private final ExecutorService scannerExec;
	private final int numWriterThreads;
	private final ExecutorService writerExec;

	public final long numEntries;
	public final int writeBatchSize;

	public final boolean persist;

	public SnapshotKey snapshotKey;

	public SnapshotBenchmark(
			SnapshotGroup group,
			int numInputThreads,
			int numWriterThreads,
			long numEntries,
			int writeBatchSize,
			boolean persist){
		this.group = group;
		this.scannerExec = Executors.newFixedThreadPool(numInputThreads);
		this.numInputThreads = numInputThreads;
		this.writerExec = Executors.newFixedThreadPool(numWriterThreads);
		this.numWriterThreads = numWriterThreads;
		this.numEntries = numEntries;
		this.writeBatchSize = writeBatchSize;
		this.persist = persist;
	}

	public RootBlock execute(){
		var timer = new PhaseTimer("writeSnapshot");
		SnapshotWriteResult result = makeEntryScanner(scannerExec, numInputThreads)
				.apply(entries -> group.writeOps().write(
						makeSnapshotWriterConfig(),
						entries,
						writerExec,
						() -> false));
		snapshotKey = result.key;
		timer.add("wrote " + NumberFormatter.addCommas(result.root.numRecords()));
		logger.warn("{} @{}/s", timer, NumberFormatter.addCommas(timer.getItemsPerSecond(numEntries)));
		return result.root;
	}

	public void cleanup(){
		group.deleteOps().deleteSnapshot(snapshotKey, writerExec, numWriterThreads);
		group.deleteOps().deleteGroup(writerExec, numWriterThreads);
	}

	public void shutdown(){
		ExecutorServiceTool.shutdown(scannerExec, Duration.ofSeconds(2));
		ExecutorServiceTool.shutdown(writerExec, Duration.ofSeconds(2));
	}

	private SnapshotWriterConfig makeSnapshotWriterConfig(){
		return new SnapshotWriterConfigBuilder(true, 0)
				.withPersist(persist)
				.withCompressor(new PassthroughBlockCompressor())
				.withNumThreads(numWriterThreads)
				.build();
	}

	public Scanner<List<SnapshotEntry>> makeEntryScanner(ExecutorService exec, int numThreads){
		return Scanner.iterate(0L, id -> id + writeBatchSize)
				.advanceWhile(id -> id < numEntries)
				.parallel(new ParallelScannerContext(exec, numThreads, false))
				.map(from -> makeEntries(from, writeBatchSize));
	}

	public static List<SnapshotEntry> makeEntries(long from, int limit){
		byte[] keySlab = new byte[KEY_LENGTH * limit];
		byte[] valueSlab = new byte[VALUE_LENGTH * limit];
		SnapshotEntry[] entries = new SnapshotEntry[limit];
		for(int i = 0; i < limit; ++i){
			long id = from + i;
			int keyFrom = i * KEY_LENGTH;
			int keyTo = keyFrom + KEY_LENGTH;
			LongByteTool.toRawBytes(id, keySlab, keyFrom);
			int valueFrom = i * VALUE_LENGTH;
			int valueTo = valueFrom + VALUE_LENGTH;
			LongByteTool.toRawBytes(id, valueSlab, valueFrom);
			entries[i] = new SnapshotEntry(
					keySlab, keyFrom, keyTo,
					valueSlab, valueFrom, valueTo,
					ByteTool.EMPTY_ARRAY_2);
		}
		return Arrays.asList(entries);
	}

	public static byte[] makeKey(long id){
		return LongByteTool.getRawBytes(id);
	}

	public static byte[] makeValue(long id){
		return LongByteTool.getRawBytes(id);
	}

}

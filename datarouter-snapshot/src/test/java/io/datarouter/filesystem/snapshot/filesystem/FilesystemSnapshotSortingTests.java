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
package io.datarouter.filesystem.snapshot.filesystem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import io.datarouter.bytes.ByteTool;
import io.datarouter.bytes.EmptyArray;
import io.datarouter.bytes.codec.intcodec.RawIntCodec;
import io.datarouter.filesystem.DatarouterSnapshotModuleFactory;
import io.datarouter.filesystem.snapshot.block.BlockKey;
import io.datarouter.filesystem.snapshot.block.root.RootBlock;
import io.datarouter.filesystem.snapshot.compress.PassthroughBlockCompressor;
import io.datarouter.filesystem.snapshot.entry.SnapshotEntry;
import io.datarouter.filesystem.snapshot.group.SnapshotGroup;
import io.datarouter.filesystem.snapshot.group.dto.SnapshotWriteResult;
import io.datarouter.filesystem.snapshot.key.SnapshotKey;
import io.datarouter.filesystem.snapshot.reader.ScanningSnapshotReader;
import io.datarouter.filesystem.snapshot.reader.record.SnapshotRecord;
import io.datarouter.filesystem.snapshot.writer.SnapshotWriterConfig;
import io.datarouter.filesystem.snapshot.writer.SnapshotWriterConfigBuilder;
import io.datarouter.scanner.Scanner;
import io.datarouter.scanner.Threads;
import io.datarouter.util.Count;
import io.datarouter.util.concurrent.ScalingThreadPoolExecutor;
import io.datarouter.util.number.NumberFormatter;
import io.datarouter.util.timer.PhaseTimer;

@Guice(moduleFactory = DatarouterSnapshotModuleFactory.class)
public class FilesystemSnapshotSortingTests{
	private static final Logger logger = LoggerFactory.getLogger(FilesystemSnapshotSortingTests.class);

	private static final int NUM_ENTRIES = 10_000_000;
	private static final int CHUNK_SIZE = 100_000;
	private static final int SCAN_NUM_BLOCKS = 200;
	private static final boolean CLEANUP = true;
	private static final RawIntCodec RAW_INT_CODEC = RawIntCodec.INSTANCE;

	private final SnapshotGroup inputGroup;
	private final SnapshotGroup chunkGroup;
	private final SnapshotGroup outputGroup;
	private final int numThreads;
	private final Threads defaultThreads;
	private final Threads sortThreads;
	private SnapshotKey inputSnapshotKey;
	private List<SnapshotKey> chunkSnapshotKeys;
	private SnapshotKey outputSnapshotKey;


	@Inject
	public FilesystemSnapshotSortingTests(FilesystemSnapshotTestGroups groups){
		this.inputGroup = groups.sortingInput;
		this.chunkGroup = groups.sortingChunk;
		this.outputGroup = groups.sortingOutput;
		numThreads = Runtime.getRuntime().availableProcessors();
		defaultThreads = new Threads(new ScalingThreadPoolExecutor("default", numThreads), numThreads);
		sortThreads = new Threads(new ScalingThreadPoolExecutor("sort", numThreads), numThreads);
		chunkSnapshotKeys = new ArrayList<>();
	}

	@AfterClass
	public void afterClass(){
		if(CLEANUP){
			//for debugging individual snapshot deletion.  could otherwise just delete the groups
			deleteSnapshot(inputGroup, inputSnapshotKey);
			chunkSnapshotKeys.forEach(key -> deleteSnapshot(chunkGroup, key));
			deleteSnapshot(outputGroup, outputSnapshotKey);

			inputGroup.deleteOps().deleteGroup(defaultThreads);
			chunkGroup.deleteOps().deleteGroup(defaultThreads);
			outputGroup.deleteOps().deleteGroup(defaultThreads);
		}
	}

	@Test
	public void testSorting(){
		var timer = new PhaseTimer("testSorting");

		//write unsorted snapshot
		writeInputSnapshot();
		RootBlock inputRootBlock = inputGroup.root(BlockKey.root(inputSnapshotKey));
		Assert.assertEquals(inputRootBlock.numItems(), NUM_ENTRIES);
		timer.add("writeInputSnapshot");

		//write sorted chunks
		var inputReader = new ScanningSnapshotReader(inputSnapshotKey, defaultThreads, inputGroup, SCAN_NUM_BLOCKS);
		var chunkId = new AtomicInteger();
		chunkSnapshotKeys = inputReader.scan(0)
				.batch(CHUNK_SIZE)
				.parallelUnordered(sortThreads)
				.map(batch -> {
					Collections.sort(batch, SnapshotRecord.KEY_COMPARATOR);
					return writeChunkSnapshot(chunkId.getAndIncrement(), batch);
				})
				.list();
		timer.add("writeSortedChunks");

		RootBlock outputRootBlock = Scanner.of(chunkSnapshotKeys)
				.map(chunkKey -> new ScanningSnapshotReader(chunkKey, defaultThreads, chunkGroup, SCAN_NUM_BLOCKS))
				.collate(reader -> reader.scan(0), SnapshotRecord.KEY_COMPARATOR)
				.apply(this::writeOutputSnapshot);
		Assert.assertEquals(outputRootBlock.numItems(), NUM_ENTRIES);
		timer.add("writeOutputSnapshot");

		var outputReader = new ScanningSnapshotReader(
				outputSnapshotKey,
				defaultThreads,
				outputGroup,
				SCAN_NUM_BLOCKS);
		var outputCount = new Count("output");
		outputReader.scanKeys()
				.map(FilesystemSnapshotSortingTests::parseKey)
				.each(id -> Assert.assertEquals(id.intValue(), outputCount.intValue()))
				.forEach(outputCount::increment);
		timer.add("assert output sorted");

		logger.warn("{}", timer);
	}

	private RootBlock writeInputSnapshot(){
		var timer = new PhaseTimer("writeInputSnapshot");
		SnapshotWriterConfig config = makeSnapshotWriterConfig(false);
		SnapshotWriteResult result = Scanner.iterate(0, i -> i + 1)
				.limit(NUM_ENTRIES)
				.shuffle()
				.map(FilesystemSnapshotSortingTests::makeEntry)
				.batch(1000)
				.apply(entries -> inputGroup.writeOps().write(config, entries, defaultThreads.exec(), () -> false));
		inputSnapshotKey = result.key;
		timer.add("wrote " + NumberFormatter.addCommas(result.optRoot.get().numItems()));
		logger.warn("{}", timer);
		return result.optRoot.get();
	}

	private SnapshotKey writeChunkSnapshot(int chunkId, List<SnapshotRecord> records){
		var timer = new PhaseTimer("writeChunkSnapshot " + chunkId);
		SnapshotWriterConfig config = makeSnapshotWriterConfig(true);
		SnapshotWriteResult result = Scanner.of(records)
				.map(SnapshotRecord::entry)
				.batch(1000)
				.apply(entries -> chunkGroup.writeOps().write(config, entries, defaultThreads.exec(), () -> false));
		timer.add("wrote " + NumberFormatter.addCommas(result.optRoot.get().numItems()));
		logger.warn("{}", timer);
		return result.key;
	}

	private RootBlock writeOutputSnapshot(Scanner<SnapshotRecord> records){
		var timer = new PhaseTimer("writeOutputSnapshot");
		SnapshotWriterConfig config = makeSnapshotWriterConfig(true);
		SnapshotWriteResult result = records
				.map(SnapshotRecord::entry)
				.batch(1000)
				.apply(entries -> outputGroup.writeOps().write(config, entries, defaultThreads.exec(), () -> false));
		outputSnapshotKey = result.key;
		timer.add("wrote " + NumberFormatter.addCommas(result.optRoot.get().numItems()));
		logger.warn("{}", timer);
		return result.optRoot.get();
	}

	private SnapshotWriterConfig makeSnapshotWriterConfig(boolean sorted){
		return new SnapshotWriterConfigBuilder(sorted, 0)
				.withCompressor(new PassthroughBlockCompressor())
				.withBlocksPerFile(10_000)
				.withNumThreads(numThreads)
				.build();
	}

	private static SnapshotEntry makeEntry(int id){
		return new SnapshotEntry(makeKey(id), EmptyArray.BYTE, ByteTool.EMPTY_ARRAY_2);
	}

	private static byte[] makeKey(int id){
		return RAW_INT_CODEC.encode(id);
	}

	private static int parseKey(byte[] key){
		return RAW_INT_CODEC.decode(key, 0);
	}

	private void deleteSnapshot(SnapshotGroup group, SnapshotKey key){
		group.deleteOps().deleteSnapshot(key, defaultThreads);
	}

}

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

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import javax.inject.Inject;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import io.datarouter.filesystem.DatarouterFilesystemModuleFactory;
import io.datarouter.filesystem.snapshot.benchmark.SnapshotBenchmark;
import io.datarouter.filesystem.snapshot.block.root.RootBlock;
import io.datarouter.filesystem.snapshot.cache.MemoryBlockCache;
import io.datarouter.filesystem.snapshot.group.SnapshotGroup;
import io.datarouter.filesystem.snapshot.reader.ScanningSnapshotReader;
import io.datarouter.filesystem.snapshot.reader.SnapshotIdReader;
import io.datarouter.filesystem.snapshot.reader.block.BlockLoader;
import io.datarouter.filesystem.snapshot.reader.record.SnapshotLeafRecord;
import io.datarouter.util.concurrent.ExecutorServiceTool;

/**
 * For testing large snapshot write throughput.  Adjust NUM_ENTRIES into the billions for a large test, but that may be
 * too much for the build system to run often.
 */
@Guice(moduleFactory = DatarouterFilesystemModuleFactory.class)
public class FilesystemSnapshotLargeTests{

	private static final long NUM_ENTRIES = 10_000_000L;
	private static final int NUM_INPUT_THREADS = Runtime.getRuntime().availableProcessors();
	private static final int NUM_WRITER_THREADS = Runtime.getRuntime().availableProcessors();
	private static final int WRITE_BATCH_SIZE = 10_000;
	private static final boolean SCAN_WITH_CACHE = false;
	private static final int SCAN_NUM_BLOCKS = 200;
	private static final boolean PERSIST = true;
	private static final boolean CLEANUP = true;

	private final SnapshotGroup group;
	private final BlockLoader cache;
	private final SnapshotBenchmark benchmark;

	private RootBlock rootBlock;

	@Inject
	public FilesystemSnapshotLargeTests(FilesystemSnapshotTestGroups groups){
		group = groups.large;
		cache = new MemoryBlockCache(64 * 1024 * 1024, group);
		benchmark = new SnapshotBenchmark(
				group,
				NUM_INPUT_THREADS,
				NUM_WRITER_THREADS,
				NUM_ENTRIES,
				WRITE_BATCH_SIZE,
				PERSIST);
	}

	@BeforeClass
	public void beforeClass(){
		rootBlock = benchmark.execute();
	}

	@AfterClass
	public void afterClass(){
		if(PERSIST){
			if(CLEANUP){
				benchmark.cleanup();
			}
		}
		benchmark.shutdown();
	}

	@Test
	public void testRoot(){
		Assert.assertEquals(rootBlock.numItems(), benchmark.numEntries);
	}

	@Test
	public void testScan(){
		if(!PERSIST){
			return;
		}
		BlockLoader blockLoader = SCAN_WITH_CACHE ? cache : group;
		int numThreads = Runtime.getRuntime().availableProcessors();
		var exec = Executors.newFixedThreadPool(numThreads);
		var reader = new ScanningSnapshotReader(benchmark.snapshotKey, exec, numThreads, blockLoader, SCAN_NUM_BLOCKS);
		var count = new AtomicLong();
		reader.scan(0)
				.forEach(record -> {
					long id = count.getAndIncrement();
					Assert.assertEquals(record.id, id);
					Assert.assertEquals(record.key, SnapshotBenchmark.makeKey(id));
					Assert.assertEquals(record.value, SnapshotBenchmark.makeValue(id));
				});
		Assert.assertEquals(count.get(), benchmark.numEntries);
		ExecutorServiceTool.shutdown(exec, Duration.ofSeconds(2));
	}

	@Test
	public void testGets(){
		if(!PERSIST){
			return;
		}
		var reader = new SnapshotIdReader(benchmark.snapshotKey, cache);
		for(long id = 0; id < benchmark.numEntries; ++id){
			SnapshotLeafRecord leafRecord = reader.leafRecord(id);
			Assert.assertEquals(leafRecord.key, SnapshotBenchmark.makeKey(id));
			Assert.assertEquals(leafRecord.value, SnapshotBenchmark.makeValue(id));
		}
	}

}

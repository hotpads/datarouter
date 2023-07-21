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
package io.datarouter.filesystem.snapshot;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import io.datarouter.bytes.ByteTool;
import io.datarouter.bytes.Bytes;
import io.datarouter.filesystem.DatarouterSnapshotModuleFactory;
import io.datarouter.filesystem.snapshot.block.BlockKey;
import io.datarouter.filesystem.snapshot.cache.MemoryBlockCache;
import io.datarouter.filesystem.snapshot.compress.GzipBlockCompressor;
import io.datarouter.filesystem.snapshot.entry.SnapshotEntry;
import io.datarouter.filesystem.snapshot.group.SnapshotGroup;
import io.datarouter.filesystem.snapshot.group.dto.SnapshotWriteResult;
import io.datarouter.filesystem.snapshot.key.SnapshotKey;
import io.datarouter.filesystem.snapshot.reader.ScanningSnapshotReader;
import io.datarouter.filesystem.snapshot.reader.SnapshotIdReader;
import io.datarouter.filesystem.snapshot.reader.SnapshotKeyReader;
import io.datarouter.filesystem.snapshot.reader.block.BlockLoader;
import io.datarouter.filesystem.snapshot.reader.record.SnapshotLeafRecord;
import io.datarouter.filesystem.snapshot.reader.record.SnapshotRecord;
import io.datarouter.filesystem.snapshot.writer.SnapshotWriterConfig;
import io.datarouter.filesystem.snapshot.writer.SnapshotWriterConfigBuilder;
import io.datarouter.scanner.Scanner;
import io.datarouter.scanner.Threads;
import io.datarouter.util.Require;
import io.datarouter.util.collection.ListTool;
import io.datarouter.util.number.NumberFormatter;
import io.datarouter.util.timer.PhaseTimer;

@Guice(moduleFactory = DatarouterSnapshotModuleFactory.class)
public abstract class BaseSnapshotTests{
	private static final Logger logger = LoggerFactory.getLogger(BaseSnapshotTests.class);

	private enum TestId{
		INDEX_LEVELS,
		PARTIAL_SCANS,
		RANDOM_MULTI_EXISTS,
		RANDOM_MULTI_FIND,
		RANDOM_MULTI_GET_ENTRY,
		RANDOM_MULTI_GET_KEY,
		RANDOM_SINGLE_EXISTS,
		RANDOM_SINGLE_FIND,
		RANDOM_SINGLE_GET_ENTRY,
		RANDOM_SINGLE_GET_KEY,
		SCAN,
		SCAN_COLUMN_VALUES,
		SCAN_KEYS,
		SCAN_LEAF_RECORDS,
		SCAN_VALUES,
		SEARCHES,
		SORTED_MULTI_EXISTS,
		SORTED_MULTI_FIND,
		SORTED_MULTI_GET_ENTRY,
		SORTED_MULTI_GET_KEY,
		SORTED_SINGLE_EXISTS,
		SORTED_SINGLE_FIND,
		SORTED_SINGLE_GET_ENTRY,
		SORTED_SINGLE_GET_KEY
	}

	private static final Set<TestId> ENABLED_TESTS = Set.of(
			TestId.INDEX_LEVELS,
			TestId.PARTIAL_SCANS,
			TestId.RANDOM_MULTI_EXISTS,
			TestId.RANDOM_MULTI_FIND,
			TestId.RANDOM_MULTI_GET_ENTRY,
			TestId.RANDOM_MULTI_GET_KEY,
			TestId.RANDOM_SINGLE_EXISTS,
			TestId.RANDOM_SINGLE_FIND,
			TestId.RANDOM_SINGLE_GET_ENTRY,
			TestId.RANDOM_SINGLE_GET_KEY,
			TestId.SCAN,
			TestId.SCAN_COLUMN_VALUES,
			TestId.SCAN_KEYS,
			TestId.SCAN_LEAF_RECORDS,
			TestId.SCAN_VALUES,
			TestId.SEARCHES,
			TestId.SORTED_MULTI_EXISTS,
			TestId.SORTED_MULTI_FIND,
			TestId.SORTED_MULTI_GET_ENTRY,
			TestId.SORTED_MULTI_GET_KEY,
			TestId.SORTED_SINGLE_EXISTS,
			TestId.SORTED_SINGLE_FIND,
			TestId.SORTED_SINGLE_GET_ENTRY,
			TestId.SORTED_SINGLE_GET_KEY);

	private static final int NUM_COLUMNS = 2;
	private static final int SCAN_NUM_BLOCKS = 100;

	private static class Input{

		final long id;
		final SnapshotEntry entry;

		public Input(long id, SnapshotEntry entry){
			this.id = id;
			this.entry = entry;
		}

	}

	private enum Operation{
		GET_LEAF_RECORD, GET_RECORD, FIND_ID, FIND_RECORD;
	}

	protected List<Input> sortedInputs;
	private List<Input> randomInputs;
	private SnapshotKey snapshotKey;
	protected ExecutorService exec;
	protected ExecutorService scanExec;
	private BlockLoader sharedMemoryCache;

	protected abstract SnapshotGroup getGroup();
	protected abstract List<String> getInputs();
	protected abstract int getNumThreads();

	// for overriding (for example, when testing against S3 is too slow)
	protected boolean useMemoryCache(){
		return false;
	}

	// for overriding (for example, when testing against S3 is too slow)
	protected boolean shareMemoryCache(){
		return false;
	}

	protected boolean shouldCleanup(){
		return true;
	}

	@BeforeClass
	public void beforeClass(){
		var wordId = new AtomicLong();
		sortedInputs = Scanner.of(getInputs())
				.map(str -> str.getBytes(StandardCharsets.UTF_8))
				.map(BaseSnapshotTests::makeEntry)
				.map(entry -> new Input(wordId.getAndIncrement(), entry))
				.list();
		Require.equals(sortedInputs.get(0).entry.columnValues.length, NUM_COLUMNS);
		randomInputs = Scanner.of(sortedInputs)
				.shuffle()
				.list();
		exec = Executors.newFixedThreadPool(getNumThreads());
		scanExec = Executors.newFixedThreadPool(getNumThreads());
		snapshotKey = writeSnapshot();
		sharedMemoryCache = new MemoryBlockCache(64 * 1024 * 1024, getGroup());
	}

	@AfterClass
	public void afterClass(){
		if(shouldCleanup()){
			getGroup().deleteOps().deleteSnapshot(snapshotKey, new Threads(exec, getNumThreads()));
			getGroup().deleteOps().deleteGroup(new Threads(exec, getNumThreads()));
		}
	}

	@Test
	public void testIndexLevels(){
		if(!ENABLED_TESTS.contains(TestId.INDEX_LEVELS)){
			return;
		}
		if(sortedInputs.size() > 100_000){
			Assert.assertTrue(
					getGroup().root(BlockKey.root(snapshotKey)).maxBranchLevel() > 0,
					"large input sizes should test multiple index levels");
		}
	}

	@Test
	public void testScanLeafRecords(){
		if(!ENABLED_TESTS.contains(TestId.SCAN_LEAF_RECORDS)){
			return;
		}
		BlockLoader blockLoader = makeBlockLoader(useMemoryCache(), shareMemoryCache());
		var reader = new ScanningSnapshotReader(
				snapshotKey,
				new Threads(exec, getNumThreads()),
				blockLoader,
				SCAN_NUM_BLOCKS);
		List<SnapshotLeafRecord> actuals = reader.scanLeafRecords(0).list();
		Assert.assertEquals(actuals.size(), sortedInputs.size());
		for(int i = 0; i < sortedInputs.size(); ++i){
			Input input = sortedInputs.get(i);
			Assert.assertEquals(input.entry.key(), actuals.get(i).key());
			Assert.assertEquals(input.entry.value(), actuals.get(i).value());
		}
	}

	@Test
	public void testScanKeys(){
		if(!ENABLED_TESTS.contains(TestId.SCAN_KEYS)){
			return;
		}
		BlockLoader blockLoader = makeBlockLoader(useMemoryCache(), shareMemoryCache());
		var reader = new ScanningSnapshotReader(
				snapshotKey,
				new Threads(exec, getNumThreads()),
				blockLoader,
				SCAN_NUM_BLOCKS);
		List<byte[]> actuals = reader.scanKeys().list();
		Assert.assertEquals(actuals.size(), sortedInputs.size());
		for(int i = 0; i < sortedInputs.size(); ++i){
			Input input = sortedInputs.get(i);
			Assert.assertEquals(input.entry.key(), actuals.get(i));
		}
	}

	@Test
	public void testScanValues(){
		if(!ENABLED_TESTS.contains(TestId.SCAN_VALUES)){
			return;
		}
		BlockLoader blockLoader = makeBlockLoader(useMemoryCache(), shareMemoryCache());
		var reader = new ScanningSnapshotReader(
				snapshotKey,
				new Threads(exec, getNumThreads()),
				blockLoader,
				SCAN_NUM_BLOCKS);
		List<byte[]> actuals = reader.scanValues().list();
		Assert.assertEquals(actuals.size(), sortedInputs.size());
		for(int i = 0; i < sortedInputs.size(); ++i){
			Input input = sortedInputs.get(i);
			Assert.assertEquals(input.entry.value(), actuals.get(i));
		}
	}

	@Test
	public void testScanColumnValues(){
		if(!ENABLED_TESTS.contains(TestId.SCAN_COLUMN_VALUES)){
			return;
		}
		BlockLoader blockLoader = makeBlockLoader(useMemoryCache(), shareMemoryCache());
		var reader = new ScanningSnapshotReader(
				snapshotKey,
				new Threads(exec, getNumThreads()),
				blockLoader,
				SCAN_NUM_BLOCKS);
		IntStream.range(0, NUM_COLUMNS).forEach(column -> {
			List<byte[]> actuals = reader.scanColumnValues(column).list();
			Assert.assertEquals(actuals.size(), sortedInputs.size());
			for(int i = 0; i < sortedInputs.size(); ++i){
				Input input = sortedInputs.get(i);
				//TODO test all values after reader returns them
				if(!Arrays.equals(input.entry.columnValues[column], actuals.get(i))){
					String message = String.format("%s, actual=%s, expected=%s", i,
							utf8(actuals.get(i)),
							utf8(input.entry.columnValues[column]));
					throw new RuntimeException(message);
				}
			}
		});
	}

	@Test
	public void testScan(){
		if(!ENABLED_TESTS.contains(TestId.SCAN)){
			return;
		}
		BlockLoader blockLoader = makeBlockLoader(useMemoryCache(), shareMemoryCache());
		var reader = new ScanningSnapshotReader(
				snapshotKey,
				new Threads(exec, getNumThreads()),
				blockLoader,
				SCAN_NUM_BLOCKS);
		List<SnapshotRecord> outputs = reader.scan(0).list();
		Assert.assertEquals(outputs.size(), sortedInputs.size());
		for(int i = 0; i < sortedInputs.size(); ++i){
			Input input = sortedInputs.get(i);
			SnapshotRecord output = outputs.get(i);
			Assert.assertEquals(i, output.id());
			for(int column = 0; column < input.entry.columnValues.length; ++column){
				if(!SnapshotEntry.equalColumnValue(input.entry, output.entry(), column)){
					String message = String.format("%s, actual=%s, expected=%s",
							i,
							utf8(output.columnValues()[column]),
							utf8(input.entry.columnValues[column]));
					throw new RuntimeException(message);
				}
			}
		}
	}

	@Test
	public void testParialScans(){
		if(!ENABLED_TESTS.contains(TestId.PARTIAL_SCANS)){
			return;
		}
		BlockLoader blockLoader = makeBlockLoader(useMemoryCache(), shareMemoryCache());
		var reader = new ScanningSnapshotReader(
				snapshotKey,
				new Threads(exec, getNumThreads()),
				blockLoader,
				SCAN_NUM_BLOCKS);
		int step = 1000;
		int limit = 1000;
		Scanner.iterate(0, fromId -> fromId += step)
				.advanceWhile(fromId -> fromId < sortedInputs.size() - limit)
				.parallelUnordered(new Threads(scanExec, getNumThreads()))
				.forEach(fromId -> {
					var timer = new PhaseTimer(fromId + "");
					List<SnapshotRecord> outputs = reader.scan(fromId)
							.limit(limit)
							.list();
					timer.add("got " + outputs.size());
					for(int i = 0; i < limit; ++i){
						Input input = sortedInputs.get(fromId + i);
						SnapshotRecord output = outputs.get(i);
						Assert.assertEquals(fromId + i, output.id());
						Assert.assertEquals(new Bytes(input.entry.key()), new Bytes(output.key()));
						for(int column = 0; column < input.entry.columnValues.length; ++column){
							if(!SnapshotEntry.equalColumnValue(input.entry, output.entry(), column)){
								String message = String.format("%s, actual=%s, expected=%s",
										i,
										utf8(output.columnValues()[column]),
										utf8(input.entry.columnValues[column]));
								throw new RuntimeException(message);
							}
						}
					}
					timer.add("assert");
					logger.info("{}", timer);
				});
	}

	@Test
	public void testSearches(){
		if(!ENABLED_TESTS.contains(TestId.SEARCHES)){
			return;
		}
		BlockLoader blockLoader = makeBlockLoader(useMemoryCache(), shareMemoryCache());
		var reader = new ScanningSnapshotReader(
				snapshotKey,
				new Threads(exec, getNumThreads()),
				blockLoader,
				SCAN_NUM_BLOCKS);
		int step = 1000;
		int limit = 1000;
		Scanner.iterate(0, fromId -> fromId += step)
				.advanceWhile(fromId -> fromId < sortedInputs.size() - limit)
				.parallelUnordered(new Threads(scanExec, getNumThreads()))
				.forEach(fromId -> {
					var idReader = new SnapshotIdReader(snapshotKey, blockLoader);

					//known first key inclusive
					byte[] searchKey = idReader.getRecord(fromId).key();
					List<SnapshotLeafRecord> outputsInclusive = reader.scanLeafRecords(searchKey, true)
							.limit(limit)
							.list();
					for(int i = 0; i < limit; ++i){
						Input input = sortedInputs.get(fromId + i);
						SnapshotLeafRecord output = outputsInclusive.get(i);
						Assert.assertEquals(fromId + i, output.id());
						Assert.assertEquals(new Bytes(input.entry.key()), new Bytes(output.key()));
					}

					//known first key exclusive
					List<SnapshotLeafRecord> outputsExclusive = reader.scanLeafRecords(searchKey, false)
							.limit(limit)
							.list();
					for(int i = 0; i < limit; ++i){
						Input input = sortedInputs.get(fromId + i + 1);//plus one because exclusive
						SnapshotLeafRecord output = outputsExclusive.get(i);
						Assert.assertEquals(input.id, output.id());
						Assert.assertEquals(new Bytes(input.entry.key()), new Bytes(output.key()));
					}

					//fake first key (should act like exclusive)
					byte[] nonExistentKey = ByteTool.concat(searchKey, new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0});
					List<SnapshotLeafRecord> outputsNonExistentKey = reader.scanLeafRecords(nonExistentKey, true)
							.limit(limit)
							.list();
					for(int i = 0; i < limit; ++i){
						Input input = sortedInputs.get(fromId + i + 1);//plus one because the first key didn't exist
						SnapshotLeafRecord output = outputsNonExistentKey.get(i);
						Assert.assertEquals(input.id, output.id());
						Assert.assertEquals(new Bytes(input.entry.key()), new Bytes(output.key()));
					}
				});
	}

	@Test
	public void testSortedSingleGetKey(){
		if(!ENABLED_TESTS.contains(TestId.SORTED_SINGLE_GET_KEY)){
			return;
		}
		testOperationInternal(
				makeBlockLoader(useMemoryCache(), shareMemoryCache()),
				false,
				false,
				Operation.GET_LEAF_RECORD);
	}

	@Test
	public void testSortedMultiGetKey(){
		if(!ENABLED_TESTS.contains(TestId.SORTED_MULTI_GET_KEY)){
			return;
		}
		testOperationInternal(
				makeBlockLoader(useMemoryCache(), shareMemoryCache()),
				false,
				true,
				Operation.GET_LEAF_RECORD);
	}

	@Test
	public void testRandomSingleGetKey(){
		if(!ENABLED_TESTS.contains(TestId.RANDOM_SINGLE_GET_KEY)){
			return;
		}
		testOperationInternal(
				makeBlockLoader(useMemoryCache(), shareMemoryCache()),
				true,
				false,
				Operation.GET_LEAF_RECORD);
	}

	@Test
	public void testRandomMultiGetKey(){
		if(!ENABLED_TESTS.contains(TestId.RANDOM_MULTI_GET_KEY)){
			return;
		}
		testOperationInternal(
				makeBlockLoader(useMemoryCache(), shareMemoryCache()),
				true,
				true,
				Operation.GET_LEAF_RECORD);
	}

	@Test
	public void testSortedSingleGetEntry(){
		if(!ENABLED_TESTS.contains(TestId.SORTED_SINGLE_GET_ENTRY)){
			return;
		}
		testOperationInternal(
				makeBlockLoader(useMemoryCache(), shareMemoryCache()),
				false,
				false,
				Operation.GET_RECORD);
	}

	@Test
	public void testSortedMultiGetEntry(){
		if(!ENABLED_TESTS.contains(TestId.SORTED_MULTI_GET_ENTRY)){
			return;
		}
		testOperationInternal(
				makeBlockLoader(useMemoryCache(), shareMemoryCache()),
				false,
				true,
				Operation.GET_RECORD);
	}

	@Test
	public void testRandomSingleGetEntry(){
		if(!ENABLED_TESTS.contains(TestId.RANDOM_SINGLE_GET_ENTRY)){
			return;
		}
		testOperationInternal(
				makeBlockLoader(useMemoryCache(), shareMemoryCache()),
				true,
				false,
				Operation.GET_RECORD);
	}

	@Test
	public void testRandomMultiGetEntry(){
		if(!ENABLED_TESTS.contains(TestId.RANDOM_MULTI_GET_ENTRY)){
			return;
		}
		testOperationInternal(
				makeBlockLoader(useMemoryCache(), shareMemoryCache()),
				true,
				true,
				Operation.GET_RECORD);
	}

	@Test
	public void testSortedSingleExists(){
		if(!ENABLED_TESTS.contains(TestId.SORTED_SINGLE_EXISTS)){
			return;
		}
		testOperationInternal(
				makeBlockLoader(useMemoryCache(), shareMemoryCache()),
				false,
				false,
				Operation.FIND_ID);
	}

	@Test
	public void testSortedMultiExists(){
		if(!ENABLED_TESTS.contains(TestId.SORTED_MULTI_EXISTS)){
			return;
		}
		testOperationInternal(
				makeBlockLoader(useMemoryCache(), shareMemoryCache()),
				false,
				true,
				Operation.FIND_ID);
	}

	@Test
	public void testRandomSingleExists(){
		if(!ENABLED_TESTS.contains(TestId.RANDOM_SINGLE_EXISTS)){
			return;
		}
		testOperationInternal(
				makeBlockLoader(useMemoryCache(), shareMemoryCache()),
				true,
				false,
				Operation.FIND_ID);
	}

	@Test
	public void testRandomMultiExists(){
		if(!ENABLED_TESTS.contains(TestId.RANDOM_MULTI_EXISTS)){
			return;
		}
		testOperationInternal(
				makeBlockLoader(useMemoryCache(), shareMemoryCache()),
				true,
				true,
				Operation.FIND_ID);
	}

	@Test
	public void testSortedSingleFind(){
		if(!ENABLED_TESTS.contains(TestId.SORTED_SINGLE_FIND)){
			return;
		}
		testOperationInternal(
				makeBlockLoader(useMemoryCache(), shareMemoryCache()),
				false,
				false,
				Operation.FIND_RECORD);
	}

	@Test
	public void testSortedMultiFind(){
		if(!ENABLED_TESTS.contains(TestId.SORTED_MULTI_FIND)){
			return;
		}
		testOperationInternal(
				makeBlockLoader(useMemoryCache(), shareMemoryCache()),
				false,
				true,
				Operation.FIND_RECORD);
	}

	@Test
	public void testRandomSingleFind(){
		if(!ENABLED_TESTS.contains(TestId.RANDOM_SINGLE_FIND)){
			return;
		}
		testOperationInternal(
				makeBlockLoader(useMemoryCache(), shareMemoryCache()),
				true,
				false,
				Operation.FIND_RECORD);
	}

	@Test
	public void testRandomMultiFind(){
		if(!ENABLED_TESTS.contains(TestId.RANDOM_MULTI_FIND)){
			return;
		}
		testOperationInternal(makeBlockLoader(useMemoryCache(), shareMemoryCache()), true, true, Operation.FIND_RECORD);
	}

	/*------------- private ---------------*/

	private SnapshotKey writeSnapshot(){
		var timer = new PhaseTimer("writeSnapshot");
		SnapshotWriteResult result = Scanner.of(sortedInputs)
				.map(input -> makeEntry(input.entry.key()))
				.batch(1000)
				.apply(entries -> getGroup().writeOps().write(makeSnapshotWriterConfig(), entries, exec, () -> false));
		timer.add("wrote " + result.optRoot.get().numItems());
		logger.warn("{}", timer);
		return result.key;
	}

	protected SnapshotWriterConfig makeSnapshotWriterConfig(){
		return new SnapshotWriterConfigBuilder(true, 2)
				.withBranchBlockSize(4 * 1024)
				.withBranchBlockCompressor(new GzipBlockCompressor())
				.withLeafBlockSize(8 * 1024)//should be small enough to cause multiple index levels
				.withLeafBlockCompressor(new GzipBlockCompressor())
				.withValueBlockSize(32 * 1024)
				.withValueBlockCompressor(new GzipBlockCompressor())
				.withNumThreads(getNumThreads())
				.build();
	}

	private BlockLoader makeBlockLoader(boolean useMemoryCache, boolean useSharedMemoryCache){
		if(!useMemoryCache){
			return getGroup();
		}
		if(!useSharedMemoryCache){
			return new MemoryBlockCache(64 * 1024 * 1024, getGroup());
		}
		return sharedMemoryCache;
	}

	private void testOperationInternal(
			BlockLoader threadSafeBlockLoader,
			boolean random,
			boolean multiThreaded,
			Operation operation){
		List<Input> searchKeys = random ? randomInputs : sortedInputs;
		int batchSize = 10_000;
		var threads = new Threads(exec, multiThreaded ? getNumThreads() : 1);
		var count = new AtomicLong();
		Scanner.of(searchKeys)
				.batch(batchSize)
				.parallelUnordered(threads)
				.forEach(batch -> {
					var idReader = new SnapshotIdReader(snapshotKey, threadSafeBlockLoader);
					var keyReader = new SnapshotKeyReader(snapshotKey, threadSafeBlockLoader);
					for(int i = 0; i < batch.size(); ++i){
						Input input = batch.get(i);
						long id = input.id;
						byte[] key = input.entry.key();
						byte[] value = input.entry.value();
						if(Operation.GET_LEAF_RECORD == operation){
							SnapshotLeafRecord leafRecord = idReader.leafRecord(id);
							if(!Arrays.equals(key, leafRecord.key())){
								String message = String.format("%s, expected=%s, actual=%s",
										id,
										utf8(key),
										utf8(leafRecord.key()));
								throw new RuntimeException(message);
							}
							if(!Arrays.equals(value, leafRecord.value())){
								String message = String.format("%s, expected=%s, actual=%s",
										id,
										utf8(value),
										utf8(leafRecord.value()));
								throw new RuntimeException(message);
							}
						}else if(Operation.GET_RECORD == operation){
							SnapshotRecord result = idReader.getRecord(id);
							if(id != result.id()){
								String message = String.format("%s, expected=%s, actual=%s",
										id,
										id,
										result.id());
								throw new RuntimeException(message);
							}
							if(!Arrays.equals(key, result.key())){
								String message = String.format("%s, expected=%s, actual=%s",
										id,
										utf8(key),
										utf8(result.key()));
								throw new RuntimeException(message);
							}
							if(!SnapshotEntry.equal(input.entry, result.entry())){
								String message = String.format("%s, expected=%s, actual=%s",
										i,
										//TODO print more than column 0
										utf8(input.entry.columnValues[0]),
										utf8(result.columnValues()[0]));
								throw new RuntimeException(message);
							}
						}else if(Operation.FIND_ID == operation){
							if(keyReader.findRecordId(key).isEmpty()){
								String message = String.format("%s, %s not found", i, utf8(key));
								throw new RuntimeException(message);
							}
							if(id != keyReader.findRecordId(key).get().longValue()){
								String message = String.format("%s, %s not found", i, utf8(key));
								throw new RuntimeException(message);
							}
						}else if(Operation.FIND_RECORD == operation){
							Optional<SnapshotRecord> output = keyReader.findRecord(key);
							if(output.isEmpty()){
								String message = String.format("%s, %s not found", i, utf8(key));
								throw new RuntimeException(message);
							}
							if(!SnapshotEntry.equal(input.entry, output.get().entry())){
								String message = String.format("%s, expected=%s, actual=%s",
										i,
										//TODO print more than column 0
										utf8(batch.get(i).entry.columnValues[0]),
										utf8(output.get().columnValues()[0]));
								throw new RuntimeException(message);
							}
						}
					}
					count.addAndGet(batch.size());
					logger.warn("{}, {}, {} for {}/{} {}",
							random ? "random" : "sorted",
							multiThreaded ? "multi" : "single",
							operation.toString().toLowerCase(),
							NumberFormatter.addCommas(count.get()),
							NumberFormatter.addCommas(searchKeys.size()),
							utf8(ListTool.getLast(batch).entry.key()));
				});
	}

	/*------------- static --------------------*/

	protected static int getNumVcpus(){
		return Runtime.getRuntime().availableProcessors();
	}

	protected static SnapshotEntry makeEntry(byte[] key){
		byte[] value = ByteTool.concat("v".getBytes(), key);
		byte[] columnValue0 = ByteTool.concat(key, key, key);
		byte[] columnValue1 = {key[0], key[0]};
		byte[][] columnValues = {columnValue0, columnValue1};
		return new SnapshotEntry(key, value, columnValues);
	}

	private static String utf8(byte[] bytes){
		return new String(bytes, StandardCharsets.UTF_8);
	}

}

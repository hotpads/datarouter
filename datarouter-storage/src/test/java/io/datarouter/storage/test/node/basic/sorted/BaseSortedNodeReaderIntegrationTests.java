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
package io.datarouter.storage.test.node.basic.sorted;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import io.datarouter.model.databean.Databean;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.node.op.combo.reader.SortedMapStorageReader;
import io.datarouter.storage.op.scan.stride.StrideScanner.StrideScannerBuilder;
import io.datarouter.storage.test.DatarouterStorageTestNgModuleFactory;
import io.datarouter.storage.util.KeyRangeTool;
import io.datarouter.util.ComparableTool;
import io.datarouter.util.collection.ListTool;
import io.datarouter.util.timer.PhaseTimer;
import io.datarouter.util.tuple.Range;
import jakarta.inject.Inject;

@Guice(moduleFactory = DatarouterStorageTestNgModuleFactory.class)
public abstract class BaseSortedNodeReaderIntegrationTests{
	private static final Logger logger = LoggerFactory.getLogger(BaseSortedNodeReaderIntegrationTests.class);

	@Inject
	protected Datarouter datarouter;

	protected final SortedMapStorageReader<SortedBeanKey,SortedBean> readerNode;

	protected BaseSortedNodeReaderIntegrationTests(
			SortedMapStorageReader<SortedBeanKey,SortedBean> readerNode){
		this.readerNode = readerNode;
	}

	protected long count(){
		return readerNode.count(Range.everything());
	}

	@Test
	public void testGetKeys(){
		var key1 = new SortedBeanKey(SortedBeans.S_aardvark, SortedBeans.S_aardvark, 0, SortedBeans.S_alpaca);
		var key2 = new SortedBeanKey("blah", "blah", 1000, "blah");
		var key3 = new SortedBeanKey(SortedBeans.S_aardvark, SortedBeans.S_albatross, 2, SortedBeans.S_emu);
		List<SortedBeanKey> keysToGet = List.of(key1, key2, key3);
		List<SortedBeanKey> keysGotten = readerNode.getKeys(keysToGet);
		Assert.assertTrue(keysGotten.contains(key1));
		Assert.assertFalse(keysGotten.contains(key2));
		Assert.assertTrue(keysGotten.contains(key3));
	}

	@Test
	public void testSimpleScanFromInclusive(){
		var startKey = new SortedBeanKey(SortedBeans.S_pelican, SortedBeans.S_pelican, 7, SortedBeans.S_emu);
		var range = new Range<>(startKey, true, null, true);
		List<SortedBean> result = readerNode.scan(range).list();
		Assert.assertEquals(result.getFirst().getKey(), startKey);
		Assert.assertEquals(result.size(), 4);
	}

	@Test
	public void testSimpleScanFromExclusive(){
		var startKey = new SortedBeanKey(SortedBeans.S_pelican, SortedBeans.S_pelican, 7, SortedBeans.S_emu);
		var range = new Range<>(startKey, false, null, true);
		List<SortedBean> result = readerNode.scan(range).list();
		var expectedFirst = new SortedBeanKey(SortedBeans.S_pelican, SortedBeans.S_pelican, 7, SortedBeans.S_gopher);
		Assert.assertEquals(result.getFirst().getKey(), expectedFirst);
		Assert.assertEquals(result.size(), 3);
	}

	@Test
	public void testSimpleScanToExclusive(){
		var endKey = new SortedBeanKey(SortedBeans.S_aardvark, SortedBeans.S_aardvark, 0, SortedBeans.S_emu);
		var range = new Range<>(null, false, endKey, false);
		List<SortedBean> result = readerNode.scan(range).list();
		var expectedLast = new SortedBeanKey(SortedBeans.S_aardvark, SortedBeans.S_aardvark, 0,
				SortedBeans.S_chinchilla);
		Assert.assertEquals(ListTool.getLastOrNull(result).getKey(), expectedLast);
		Assert.assertEquals(result.size(), 4);
	}

	@Test
	public void testSimpleScanToInclusive(){
		var endKey = new SortedBeanKey(SortedBeans.S_aardvark, SortedBeans.S_aardvark, 0, SortedBeans.S_emu);
		var range = new Range<>(null, false, endKey, true);
		List<SortedBean> result = readerNode.scan(range).list();
		var expectedLast = endKey;
		Assert.assertEquals(ListTool.getLastOrNull(result).getKey(), expectedLast);
		Assert.assertEquals(result.size(), 5);
	}

	@Test //first 3 fields fixed
	public void testScanWithPrefix1(){
		var prefix1 = new SortedBeanKey(
				SortedBeans.STRINGS.first(),
				SortedBeans.STRINGS.last(),
				2,
				null);
		List<SortedBean> result1 = readerNode.scanWithPrefix(prefix1).list();
		Assert.assertEquals(result1.size(), SortedBeans.NUM_ELEMENTS);
		Assert.assertTrue(ComparableTool.isSorted(result1));
	}

	@Test //first 3 fields fixed, last field wildcard
	public void testScanWithPrefix2(){
		Range<SortedBeanKey> prefix2 = KeyRangeTool.forPrefixWithWildcard(
				SortedBeans.PREFIX_a,
				suffix -> new SortedBeanKey(SortedBeans.STRINGS.first(), SortedBeans.STRINGS.last(), 2, suffix));
		List<SortedBean> result2 = readerNode.scan(prefix2).list();
		Assert.assertEquals(result2.size(), SortedBeans.NUM_PREFIX_a);
		Assert.assertTrue(ComparableTool.isSorted(result2));
	}

	@Test //first field fixed, second field wildcard
	public void testScanWithPrefix3(){
		Range<SortedBeanKey> prefix3 = KeyRangeTool.forPrefixWithWildcard(
				SortedBeans.PREFIX_a,
				suffix -> new SortedBeanKey(SortedBeans.STRINGS.first(), suffix, null, null));
		List<SortedBean> result3 = readerNode.scan(prefix3).list();
		int expectedSize3 = SortedBeans.NUM_PREFIX_a * SortedBeans.NUM_ELEMENTS * SortedBeans.NUM_ELEMENTS;
		Assert.assertEquals(result3.size(), expectedSize3);
		Assert.assertTrue(ComparableTool.isSorted(result3));
	}

	@Test //first two fields given, third field is null, fourth field exists and should be ignored
	public void testScanWithPrefix4(){
		var prefix4 = new SortedBeanKey(
				SortedBeans.STRINGS.first(),
				SortedBeans.STRINGS.last(),
				null,
				"Ignore");
		List<SortedBean> result4 = readerNode.scanWithPrefix(prefix4).list();
		Assert.assertEquals(result4.size(), SortedBeans.NUM_ELEMENTS * SortedBeans.NUM_ELEMENTS);
		Assert.assertTrue(ComparableTool.isSorted(result4));
	}

	@Test
	public void testGetWithPrefixes(){
		List<Range<SortedBeanKey>> ranges = Stream.of(SortedBeans.PREFIX_a, SortedBeans.PREFIX_ch)
				.map(prefix -> KeyRangeTool.forPrefixWithWildcard(prefix, suffix -> new SortedBeanKey(
						SortedBeans.STRINGS.first(), suffix, null, null)))
				.toList();
		List<SortedBean> result = readerNode.scanRanges(ranges).list();
		int expectedSizeA = SortedBeans.NUM_PREFIX_a * SortedBeans.NUM_ELEMENTS * SortedBeans.NUM_ELEMENTS;
		int expectedSizeCh = SortedBeans.NUM_PREFIX_ch * SortedBeans.NUM_ELEMENTS * SortedBeans.NUM_ELEMENTS;
		int expectedSizeTotal = expectedSizeA + expectedSizeCh;
		Assert.assertEquals(result.size(), expectedSizeTotal);
		Assert.assertTrue(ComparableTool.isSorted(result));
	}

	@Test
	public void testPrefixStartExclusive(){
		SortedBeanKey startPrefix = new SortedBeanKey(SortedBeans.S_aardvark, null, null, null);
		Range<SortedBeanKey> range = new Range<>(startPrefix, false, null, false);
		List<SortedBean> result = readerNode.scan(range).list();
		int expectedSizeTotal = (SortedBeans.NUM_ELEMENTS - 1) * SortedBeans.NUM_ELEMENTS * SortedBeans.NUM_ELEMENTS
				* SortedBeans.NUM_ELEMENTS;
		Assert.assertEquals(result.size(), expectedSizeTotal);
	}

	@Test
	public void testSingleDatabeanPrefixes(){
		int numToTest = 10;
		long count = Scanner.of(SortedBeans.generatedSortedBeans())
				.sample(77, true)
				.limit(numToTest)
				.map(Databean::getKey)
				.listTo(readerNode::scanWithPrefixes)
				.count();
		Assert.assertEquals(count, numToTest);
	}

	@Test
	public void testGetKeysInRange(){
		testGetKeysOrDatabeanInRange(readerNode::scanKeys);
	}

	@Test
	public void testGetInRange(){
		testGetKeysOrDatabeanInRange(readerNode::scan);
	}

	private interface ScanBySortedBeanKeyProvider<T>{
		public Scanner<T> scan(Range<SortedBeanKey> range, Config config);
	}

	private static <T extends Comparable<? super T>> void testGetKeysOrDatabeanInRange(
			ScanBySortedBeanKeyProvider<T> scanProvider){
		var alp1 = new SortedBeanKey(SortedBeans.RANGE_alp, null, null, null);
		var emu1 = new SortedBeanKey(SortedBeans.RANGE_emu, null, null, null);
		var range1 = new Range<>(alp1, true, emu1, true);
		List<T> result1 = scanProvider.scan(range1, new Config()).list();
		int expectedSize1 = SortedBeans.RANGE_LENGTH_alp_emu_inc
				* SortedBeans.NUM_ELEMENTS
				* SortedBeans.NUM_ELEMENTS
				* SortedBeans.NUM_ELEMENTS;
		Assert.assertEquals(result1.size(), expectedSize1);
		Assert.assertTrue(ComparableTool.isSorted(result1));

		var range1b = new Range<>(alp1, true, emu1, false);
		List<T> result1b = scanProvider.scan(range1b, new Config()).list();
		int expectedSize1b = (SortedBeans.RANGE_LENGTH_alp_emu_inc - 1)
				* SortedBeans.NUM_ELEMENTS
				* SortedBeans.NUM_ELEMENTS
				* SortedBeans.NUM_ELEMENTS;
		Assert.assertEquals(result1b.size(), expectedSize1b);
		Assert.assertTrue(ComparableTool.isSorted(result1b));

		var alp2 = new SortedBeanKey(SortedBeans.STRINGS.first(), SortedBeans.RANGE_alp, null, null);
		var emu2 = new SortedBeanKey(SortedBeans.STRINGS.first(), SortedBeans.RANGE_emu, null, null);
		var range2 = new Range<>(alp2, true, emu2, true);
		List<T> result2 = scanProvider.scan(range2, new Config()).list();
		int expectedSize2 = SortedBeans.RANGE_LENGTH_alp_emu_inc
				* SortedBeans.NUM_ELEMENTS
				* SortedBeans.NUM_ELEMENTS;
		Assert.assertEquals(result2.size(), expectedSize2);
		Assert.assertTrue(ComparableTool.isSorted(result2));
	}

	@Test //small batch sizes to make sure we're resuming each batch from the correct spot
	public void testIncrementalScan(){
		var smallIterateBatchSize = new Config().setResponseBatchSize(3);
		int expectedSize1 = SortedBeans.RANGE_LENGTH_alp_emu_inc
				* SortedBeans.NUM_ELEMENTS
				* SortedBeans.NUM_ELEMENTS
				* SortedBeans.NUM_ELEMENTS;
		var alp1 = new SortedBeanKey(SortedBeans.RANGE_alp, null, null, null);
		var emu1 = new SortedBeanKey(SortedBeans.RANGE_emu, null, null, null);
		List<SortedBeanKey> result1 = readerNode.scanKeys(new Range<>(alp1, true, emu1, true), smallIterateBatchSize)
				.list();
		Assert.assertEquals(result1.size(), expectedSize1);
		Assert.assertTrue(ComparableTool.isSorted(result1));
	}

	@Test
	public void testIncrementalScan2(){
		var smallIterateBatchSize = new Config().setResponseBatchSize(3);
		var alp1 = new SortedBeanKey(SortedBeans.RANGE_alp, null, null, null);
		var emu1 = new SortedBeanKey(SortedBeans.RANGE_emu, null, null, null);
		int expectedSize1b = (SortedBeans.RANGE_LENGTH_alp_emu_inc - 1)
				* SortedBeans.NUM_ELEMENTS
				* SortedBeans.NUM_ELEMENTS
				* SortedBeans.NUM_ELEMENTS;
		List<SortedBeanKey> result1b = readerNode.scanKeys(new Range<>(alp1, true, emu1, false), smallIterateBatchSize)
				.list();
		Assert.assertEquals(result1b.size(), expectedSize1b);
		Assert.assertTrue(ComparableTool.isSorted(result1b));
	}

	@Test
	public void testIncrementalScan3(){
		var smallIterateBatchSize = new Config().setResponseBatchSize(3);
		int expectedSize2 = SortedBeans.RANGE_LENGTH_alp_emu_inc
				* SortedBeans.NUM_ELEMENTS
				* SortedBeans.NUM_ELEMENTS;
		var alp2 = new SortedBeanKey(SortedBeans.STRINGS.first(), SortedBeans.RANGE_alp, null, null);
		var emu2 = new SortedBeanKey(SortedBeans.STRINGS.first(), SortedBeans.RANGE_emu, null, null);
		List<SortedBeanKey> result2 = readerNode.scanKeys(new Range<>(alp2, true, emu2, true), smallIterateBatchSize)
				.list();
		Assert.assertEquals(result2.size(), expectedSize2);
		Assert.assertTrue(ComparableTool.isSorted(result2));
	}

	@Test
	public void testGet(){
		List<SortedBean> all = readerNode.scan().list();
		final int sampleEveryN = 89;
		for(int i = 0; i < all.size(); i += sampleEveryN){
			SortedBean sortedBeanFromScan = all.get(i);
			SortedBean sortedBeanFromGet = readerNode.get(sortedBeanFromScan.getKey());
			Assert.assertEquals(sortedBeanFromGet, sortedBeanFromScan, "error on index=" + i);
		}
	}

	@Test
	public void testGetMulti(){
		Set<SortedBean> allBeans = readerNode.scan().collect(HashSet::new);
		Assert.assertEquals(allBeans.size(), SortedBeans.TOTAL_RECORDS);
		List<SortedBean> getMultiResult = Scanner.of(allBeans)
				.map(Databean::getKey)
				.listTo(keys -> readerNode.getMulti(keys, new Config().setRequestBatchSize(1_000)));
		Assert.assertEquals(getMultiResult.size(), SortedBeans.TOTAL_RECORDS);
		getMultiResult.forEach(result -> Assert.assertTrue(allBeans.contains(result)));
	}

	@Test
	public void testFullScanKeys(){
		long numKeys = readerNode.scanKeys().count();
		Assert.assertEquals(numKeys, SortedBeans.TOTAL_RECORDS);
	}

	@Test
	public void testFullScan(){
		long numDatabeans = readerNode.scan().count();
		Assert.assertEquals(numDatabeans, SortedBeans.TOTAL_RECORDS);
	}

	@Test
	protected void testLimitedScanKeys(){
		long count = SortedBeans.TOTAL_RECORDS;
		Assert.assertNotEquals(0, count);
		Assert.assertEquals(scanKeysAndCountWithConfig(new Config().setResponseBatchSize(555)), count);
		Assert.assertEquals(scanKeysAndCountWithConfig(new Config().setLimit((int)count)), count);
		Assert.assertEquals(scanKeysAndCountWithConfig(new Config().setLimit(10)), 10);
		Assert.assertEquals(scanKeysAndCountWithConfig(new Config().setLimit((int)(2 * count))), count);
		Assert.assertEquals(scanKeysAndCountWithConfig(new Config().setResponseBatchSize(25).setLimit(100)), 100);
		Assert.assertEquals(scanKeysAndCountWithConfig(new Config().setResponseBatchSize(15).setLimit(23)), 23);
	}

	private long scanKeysAndCountWithConfig(Config config){
		return readerNode.scanKeys(config).count();
	}

	@Test
	protected void testLimitedScan(){
		long count = SortedBeans.TOTAL_RECORDS;
		Assert.assertNotEquals(0, count);
		var timer = new PhaseTimer("testLimitedScan");
		Assert.assertEquals(scanAndCountWithConfig(new Config().setResponseBatchSize(555)), count);
		timer.add("1");
		Assert.assertEquals(scanAndCountWithConfig(new Config().setLimit((int)count)), count);
		timer.add("2");
		Assert.assertEquals(scanAndCountWithConfig(new Config().setLimit(10)), 10);
		timer.add("3");
		Assert.assertEquals(scanAndCountWithConfig(new Config().setLimit((int)(2 * count))), count);
		timer.add("4");
		Assert.assertEquals(scanAndCountWithConfig(new Config().setResponseBatchSize(25).setLimit(100)), 100);
		timer.add("5");
		Assert.assertEquals(scanAndCountWithConfig(new Config().setResponseBatchSize(15).setLimit(23)), 23);
		timer.add("6");
		logger.warn("{}", timer);
		//[total:633ms]<testLimitedScan>[1:112ms][2:107ms][3:90ms][4:143ms][5:74ms][6:107ms]
	}

	private long scanAndCountWithConfig(Config config){
		return readerNode.scan(config).count();
	}

	@Test
	public void testNullStartKeyScan(){
		var range = new Range<SortedBeanKey>(null, false, null, true);
		long numKeys = readerNode.scanKeys(range).count();
		Assert.assertEquals(numKeys, SortedBeans.TOTAL_RECORDS);
	}

	@Test
	public void testNullEndKeyScan(){
		var range = new Range<SortedBeanKey>(null, true, null, true);
		long numKeys = readerNode.scanKeys(range).count();
		Assert.assertEquals(numKeys, SortedBeans.TOTAL_RECORDS);
	}

	@Test
	public void testScanOffset(){
		int offset = 10;
		long count = readerNode.scan(new Config().setOffset(offset)).count();
		Assert.assertEquals(count, SortedBeans.TOTAL_RECORDS - offset);
		SortedBean firstByOffset = readerNode.scan(new Config().setOffset(offset).setLimit(1))
				.findFirst()
				.get();
		SortedBean firstBySkip = readerNode.scan(new Config().setLimit(offset + 1))
				.skip(offset)
				.findFirst()
				.get();
		Assert.assertEquals(firstByOffset, firstBySkip);
	}

	@Test
	public void testScanKeysOffset(){
		int offset = 10;
		long count = readerNode.scanKeys(new Config().setOffset(offset)).count();
		Assert.assertEquals(count, SortedBeans.TOTAL_RECORDS - offset);
		SortedBeanKey firstByOffset = readerNode.scanKeys(new Config().setOffset(offset).setLimit(1))
				.findFirst()
				.get();
		SortedBeanKey firstBySkip = readerNode.scanKeys(new Config().setLimit(offset + 1))
				.skip(offset)
				.findFirst()
				.get();
		Assert.assertEquals(firstByOffset, firstBySkip);
	}

	@Test
	public void testEmptyLastBatchRangeScan(){
		var startKey = new SortedBeanKey(SortedBeans.S_alpaca, SortedBeans.S_ostrich, 7, SortedBeans.S_emu);
		var endKey = new SortedBeanKey(SortedBeans.S_alpaca, SortedBeans.S_ostrich, 7, SortedBeans.S_pelican);
		var rangeWithFourRows = new Range<>(startKey, true, endKey, true);
		long count = readerNode.scanKeys(rangeWithFourRows, new Config().setResponseBatchSize(2)).count();
		Assert.assertEquals(count, 4);
	}

	@Test
	public void testEmptyRangeScan(){
		var key = new SortedBeanKey(SortedBeans.S_alpaca, SortedBeans.S_ostrich, 7, SortedBeans.S_emu);
		var emptyRange = new Range<>(key, true, key, false);//Range defines this as empty
		long count = readerNode.scanKeys(emptyRange).count();
		Assert.assertEquals(count, 0);
	}

	@Test
	public void testStrideScanner(){
		int stride = SortedBeans.TOTAL_RECORDS / 10;//trigger multiple small operations
		long count = new StrideScannerBuilder<>(readerNode)
				.withLog(true)
				.withStride(stride)
				.build()
				.findLast()
				.map(sample -> sample.totalCount)
				.orElse(0L);
		Assert.assertEquals(count, SortedBeans.TOTAL_RECORDS);
	}

	@Test
	public void testExclusiveStartKey(){
		// single entity case
		var startKey = new SortedBeanKey(SortedBeans.S_alpaca, SortedBeans.S_ostrich, 7, SortedBeans.S_emu);
		var endKey = new SortedBeanKey(SortedBeans.S_alpaca, SortedBeans.S_ostrich, null, null);
		List<SortedBean> result = readerNode.scan(new Range<>(startKey, false, endKey, true)).list();
		Assert.assertEquals(
				result.getFirst().getKey(),
				new SortedBeanKey(SortedBeans.S_alpaca, SortedBeans.S_ostrich, 7, SortedBeans.S_gopher));
		Assert.assertEquals(result.size(), 3); // because 7 is the last and gopher+ostrich+pelican
		// TODO multiple entity case DATAROUTER-259
	}

	@Test
	public void testScanRanges(){
		var startKey1 = new SortedBeanKey(SortedBeans.S_albatross, SortedBeans.S_ostrich, 0, SortedBeans.S_albatross);
		var endKey1 = new SortedBeanKey(SortedBeans.S_albatross, SortedBeans.S_ostrich, 0, SortedBeans.S_ostrich);
		var range1 = new Range<>(startKey1, endKey1);
		var startKey2 = new SortedBeanKey(SortedBeans.S_albatross, SortedBeans.S_ostrich, 3, SortedBeans.S_aardvark);
		var endKey2 = new SortedBeanKey(SortedBeans.S_albatross, SortedBeans.S_ostrich, 3, SortedBeans.S_emu);
		var range2 = new Range<>(startKey2, endKey2);
		Set<SortedBean> beans = readerNode.scanRanges(
				Arrays.asList(range1, range2),
				new Config().setResponseBatchSize(4))
				.collect(HashSet::new);
		Set<SortedBean> expected = Scanner.of(range1, range2)
				.concat(readerNode::scan)
				.collect(HashSet::new);
		Assert.assertFalse(expected.isEmpty());
		Assert.assertEquals(beans, expected);
	}

	@Test
	public void testSurviveKeyMutation(){
		readerNode.scanKeys()
				.forEach(key -> key.setFoo("z"));
		long zCount = readerNode.scanKeys()
				.include(key -> key.getFoo().equals("z"))
				.count();
		Assert.assertEquals(zCount, 0);
	}


}

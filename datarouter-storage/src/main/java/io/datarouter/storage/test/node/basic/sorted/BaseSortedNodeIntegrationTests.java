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
package io.datarouter.storage.test.node.basic.sorted;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.model.databean.DatabeanTool;
import io.datarouter.model.field.Field;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.test.node.basic.sorted.SortedBean.SortedBeanFielder;
import io.datarouter.storage.util.KeyRangeTool;
import io.datarouter.util.collection.ListTool;
import io.datarouter.util.concurrent.ThreadTool;
import io.datarouter.util.iterable.IterableTool;
import io.datarouter.util.timer.PhaseTimer;
import io.datarouter.util.tuple.Range;

public abstract class BaseSortedNodeIntegrationTests extends BaseSortedBeanIntegrationTests{
	private static final Logger logger = LoggerFactory.getLogger(BaseSortedNodeIntegrationTests.class);

	protected void postTestTests(){
		testSortedDelete();
		testBlankDatabeanPut(new Config().setIgnoreNullFields(false));
		testBlankDatabeanPut(new Config().setIgnoreNullFields(true));
		testIgnoreNull();
	}

	private void testSortedDelete(){
		resetTable(true);
		int remainingElements = SortedBeans.TOTAL_RECORDS;

		//delete
		Assert.assertEquals(IterableTool.count(sortedNode.scan()).intValue(), remainingElements);
		SortedBeanKey key = new SortedBeanKey(SortedBeans.STRINGS.last(), SortedBeans.STRINGS.last(), 0,
				SortedBeans.STRINGS.last());
		sortedNode.delete(key);
		--remainingElements;
		Assert.assertEquals(sortedNode.scan().count(), remainingElements);

		//deleteMulti
		Assert.assertEquals(sortedNode.scan().count(), remainingElements);
		List<SortedBeanKey> keys = Arrays.asList(
				new SortedBeanKey(SortedBeans.STRINGS.last(), SortedBeans.STRINGS.last(), 1,
						SortedBeans.STRINGS.last()),
				new SortedBeanKey(SortedBeans.STRINGS.last(), SortedBeans.STRINGS.last(), 2,
						SortedBeans.STRINGS.last()),
				new SortedBeanKey(SortedBeans.STRINGS.last(), SortedBeans.STRINGS.last(), 3,
						SortedBeans.STRINGS.last()));
		sortedNode.deleteMulti(keys);
		remainingElements -= 3;
		Assert.assertEquals(sortedNode.scan().count(), remainingElements);

		//deleteWithPrefix
		Assert.assertEquals(sortedNode.scan().count(), remainingElements);
		SortedBeanKey prefix = new SortedBeanKey(SortedBeans.S_aardvark, null, null, null);
		sortedNode.deleteWithPrefix(prefix);
		remainingElements -= SortedBeans.NUM_ELEMENTS * SortedBeans.NUM_ELEMENTS * SortedBeans.NUM_ELEMENTS;
		Assert.assertEquals(sortedNode.scan().count(), remainingElements);
	}

	private void testBlankDatabeanPut(Config config){
		SortedBean blankDatabean = new SortedBean("a", "b", 1, "d1", null, null, null, null);
		SortedBean nonBlankDatabean = new SortedBean("a", "b", 1, "d2", "non blank", null, null, null);
		sortedNode.putMulti(Arrays.asList(nonBlankDatabean, blankDatabean), config);
		SortedBean blankDatabeanFromDb = sortedNode.get(blankDatabean.getKey(), config);
		Assert.assertNotNull(blankDatabeanFromDb);
		new SortedBeanFielder().getNonKeyFields(blankDatabeanFromDb).stream()
				.map(Field::getValue)
				.forEach(Assert::assertNull);
		sortedNode.deleteMulti(DatabeanTool.getKeys(Arrays.asList(blankDatabean, nonBlankDatabean)), config);
		Assert.assertFalse(sortedNode.exists(blankDatabean.getKey(), config));
	}

	protected void testIgnoreNull(){
		SortedBeanKey pk = new SortedBeanKey("a", "b", 3, "d");
		String f1 = "Degermat";
		String f3 = "Kenavo";
		SortedBean databean = new SortedBean(pk, f1, null, null, null);
		sortedNode.put(databean);
		ThreadTool.sleep(1);//fix flaky test. see DATAROUTER-487
		databean = new SortedBean(pk, null, null, f3, null);
		sortedNode.put(databean, new Config().setIgnoreNullFields(true));
		databean = sortedNode.get(pk);
		Assert.assertEquals(databean.getF1(), f1);
		Assert.assertEquals(databean.getF3(), f3);
		sortedNode.delete(pk);
		Assert.assertFalse(sortedNode.exists(pk));
	}

	@Test
	public void testGetKeys(){
		SortedBeanKey key1 = new SortedBeanKey(SortedBeans.S_aardvark, SortedBeans.S_aardvark, 0, SortedBeans.S_alpaca);
		SortedBeanKey key2 = new SortedBeanKey("blah", "blah", 1000, "blah");
		SortedBeanKey key3 = new SortedBeanKey(SortedBeans.S_aardvark, SortedBeans.S_albatross, 2, SortedBeans.S_emu);
		List<SortedBeanKey> keysToGet = ListTool.create(key1, key2, key3);
		List<SortedBeanKey> keysGotten = sortedNode.getKeys(keysToGet);
		Assert.assertTrue(keysGotten.contains(key1));
		Assert.assertFalse(keysGotten.contains(key2));
		Assert.assertTrue(keysGotten.contains(key3));
	}

	@Test
	public void testGetAll(){
		List<SortedBean> allBeans = sortedNode.scanKeys()
				.batch(100)
				.map(sortedNode::getMulti)
				.mapToScanner(Scanner::of)
				.concatenate()
				.list();
		Assert.assertEquals(allBeans.size(), SortedBeans.TOTAL_RECORDS);
	}

	@Test
	public void testScanWithPrefix(){
		//first 3 fields fixed
		SortedBeanKey prefix1 = new SortedBeanKey(SortedBeans.STRINGS.first(), SortedBeans.STRINGS.last(), 2, null);
		List<SortedBean> result1 = sortedNode.scanWithPrefix(prefix1).list();
		Assert.assertEquals(result1.size(), SortedBeans.NUM_ELEMENTS);
		Assert.assertTrue(ListTool.isSorted(result1));

		//first 3 fields fixed, last field wildcard
		Range<SortedBeanKey> prefix2 = KeyRangeTool.forPrefixWithWildcard(SortedBeans.PREFIX_a,
				suffix -> new SortedBeanKey(SortedBeans.STRINGS.first(), SortedBeans.STRINGS.last(), 2, suffix));
		List<SortedBean> result2 = sortedNode.scan(prefix2).list();
		Assert.assertEquals(result2.size(), SortedBeans.NUM_PREFIX_a);
		Assert.assertTrue(ListTool.isSorted(result2));

		//first field fixed, second field wildcard
		Range<SortedBeanKey> prefix3 = KeyRangeTool.forPrefixWithWildcard(SortedBeans.PREFIX_a,
				suffix -> new SortedBeanKey(SortedBeans.STRINGS.first(), suffix, null, null));
		List<SortedBean> result3 = sortedNode.scan(prefix3).list();
		int expectedSize3 = SortedBeans.NUM_PREFIX_a * SortedBeans.NUM_ELEMENTS * SortedBeans.NUM_ELEMENTS;
		Assert.assertEquals(result3.size(), expectedSize3);
		Assert.assertTrue(ListTool.isSorted(result3));
	}

	@Test
	public void testGetWithPrefixes(){
		List<Range<SortedBeanKey>> ranges = Stream.of(SortedBeans.PREFIX_a, SortedBeans.PREFIX_ch)
				.map(prefix -> KeyRangeTool.forPrefixWithWildcard(prefix, suffix -> new SortedBeanKey(
						SortedBeans.STRINGS.first(), suffix, null, null)))
				.collect(Collectors.toList());
		List<SortedBean> result = sortedNode.scanMulti(ranges).list();
		int expectedSizeA = SortedBeans.NUM_PREFIX_a * SortedBeans.NUM_ELEMENTS * SortedBeans.NUM_ELEMENTS;
		int expectedSizeCh = SortedBeans.NUM_PREFIX_ch * SortedBeans.NUM_ELEMENTS * SortedBeans.NUM_ELEMENTS;
		int expectedSizeTotal = expectedSizeA + expectedSizeCh;
		Assert.assertEquals(result.size(), expectedSizeTotal);
		Assert.assertTrue(ListTool.isSorted(result));

		long count = sortedNode.scanWithPrefixes(DatabeanTool.getKeys(allBeans)).count();
		Assert.assertEquals(count, allBeans.size());
	}

	@Test
	public void testGetKeysInRange(){
		testGetKeysOrDatabeanInRange(sortedNode::scanKeys);
	}

	@Test
	public void testGetInRange(){
		testGetKeysOrDatabeanInRange(sortedNode::scan);
	}

	private interface ScanBySortedBeanKeyProvider<T>{
		public Scanner<T> scan(Range<SortedBeanKey> range, Config config);
	}

	private static <T extends Comparable<? super T>> void testGetKeysOrDatabeanInRange(ScanBySortedBeanKeyProvider<T>
			scanProvider){
		SortedBeanKey alp1 = new SortedBeanKey(SortedBeans.RANGE_alp, null, null, null);
		SortedBeanKey emu1 = new SortedBeanKey(SortedBeans.RANGE_emu, null, null, null);
		Range<SortedBeanKey> range1 = new Range<>(alp1, true, emu1, true);
		List<T> result1 = scanProvider.scan(range1, new Config()).list();
		int expectedSize1 = SortedBeans.RANGE_LENGTH_alp_emu_inc * SortedBeans.NUM_ELEMENTS * SortedBeans.NUM_ELEMENTS
				* SortedBeans.NUM_ELEMENTS;
		Assert.assertEquals(result1.size(), expectedSize1);
		Assert.assertTrue(ListTool.isSorted(result1));

		Range<SortedBeanKey> range1b = new Range<>(alp1, true, emu1, false);
		List<T> result1b = scanProvider.scan(range1b, new Config()).list();
		int expectedSize1b = (SortedBeans.RANGE_LENGTH_alp_emu_inc - 1) * SortedBeans.NUM_ELEMENTS
				* SortedBeans.NUM_ELEMENTS * SortedBeans.NUM_ELEMENTS;
		Assert.assertEquals(result1b.size(), expectedSize1b);
		Assert.assertTrue(ListTool.isSorted(result1b));

		SortedBeanKey alp2 = new SortedBeanKey(SortedBeans.STRINGS.first(), SortedBeans.RANGE_alp, null, null);
		SortedBeanKey emu2 = new SortedBeanKey(SortedBeans.STRINGS.first(), SortedBeans.RANGE_emu, null, null);
		Range<SortedBeanKey> range2 = new Range<>(alp2, true, emu2, true);
		List<T> result2 = scanProvider.scan(range2, new Config()).list();
		int expectedSize2 = SortedBeans.RANGE_LENGTH_alp_emu_inc * SortedBeans.NUM_ELEMENTS * SortedBeans.NUM_ELEMENTS;
		Assert.assertEquals(result2.size(), expectedSize2);
		Assert.assertTrue(ListTool.isSorted(result2));
	}

	@Test //small batch sizes to make sure we're resuming each batch from the correct spot
	public void testIncrementalScan(){
		Config smallIterateBatchSize = new Config().setOutputBatchSize(3);

		int expectedSize1 = SortedBeans.RANGE_LENGTH_alp_emu_inc * SortedBeans.NUM_ELEMENTS * SortedBeans.NUM_ELEMENTS
				* SortedBeans.NUM_ELEMENTS;
		SortedBeanKey alp1 = new SortedBeanKey(SortedBeans.RANGE_alp, null, null, null);
		SortedBeanKey emu1 = new SortedBeanKey(SortedBeans.RANGE_emu, null, null, null);
		List<SortedBeanKey> result1 = sortedNode.scanKeys(new Range<>(alp1, true, emu1, true), smallIterateBatchSize)
				.list();
		Assert.assertEquals(result1.size(), expectedSize1);
		Assert.assertTrue(ListTool.isSorted(result1));

		int expectedSize1b = (SortedBeans.RANGE_LENGTH_alp_emu_inc - 1) * SortedBeans.NUM_ELEMENTS
				* SortedBeans.NUM_ELEMENTS * SortedBeans.NUM_ELEMENTS;
		List<SortedBeanKey> result1b = sortedNode.scanKeys(new Range<>(alp1, true, emu1, false), smallIterateBatchSize)
				.list();
		Assert.assertEquals(result1b.size(), expectedSize1b);
		Assert.assertTrue(ListTool.isSorted(result1b));

		int expectedSize2 = SortedBeans.RANGE_LENGTH_alp_emu_inc * SortedBeans.NUM_ELEMENTS * SortedBeans.NUM_ELEMENTS;
		SortedBeanKey alp2 = new SortedBeanKey(SortedBeans.STRINGS.first(), SortedBeans.RANGE_alp, null, null);
		SortedBeanKey emu2 = new SortedBeanKey(SortedBeans.STRINGS.first(), SortedBeans.RANGE_emu, null, null);
		List<SortedBeanKey> result2 = sortedNode.scanKeys(new Range<>(alp2, true, emu2, true), smallIterateBatchSize)
				.list();
		Assert.assertEquals(result2.size(), expectedSize2);
		Assert.assertTrue(ListTool.isSorted(result2));
	}

	@Test
	public void testGet(){
		List<SortedBean> all = sortedNode.scan().list();
		final int sampleEveryN = 29;
		for(int i = 0; i < all.size(); i += sampleEveryN){
			SortedBean sortedBeanFromScan = all.get(i);
			SortedBean sortedBeanFromGet = sortedNode.get(sortedBeanFromScan.getKey());
			Assert.assertEquals(sortedBeanFromGet, sortedBeanFromScan);
		}
	}

	@Test
	public void testGetMulti(){
		Set<SortedBean> allBeans = sortedNode.scan()
				.collect(Collectors.toSet());
		Assert.assertEquals(allBeans.size(), SortedBeans.TOTAL_RECORDS);
		List<SortedBean> getMultiResult = sortedNode.getMulti(DatabeanTool.getKeys(allBeans));
		Assert.assertEquals(getMultiResult.size(), SortedBeans.TOTAL_RECORDS);
		getMultiResult.forEach(result -> Assert.assertTrue(allBeans.contains(result)));
	}

	@Test
	public void testFullScanKeys(){
		long numKeys = sortedNode.scanKeys().count();
		Assert.assertEquals(numKeys, SortedBeans.TOTAL_RECORDS);
	}

	@Test
	public void testFullScan(){
		long numDatabeans = sortedNode.scan().count();
		Assert.assertEquals(numDatabeans, SortedBeans.TOTAL_RECORDS);
	}

	@Test
	protected void testLimitedScanKeys(){
		long count = SortedBeans.TOTAL_RECORDS;
		Assert.assertNotEquals(0, count);
		Assert.assertEquals(scanKeysAndCountWithConfig(new Config().setOutputBatchSize(555)), count);
		Assert.assertEquals(scanKeysAndCountWithConfig(new Config().setLimit((int)count)), count);
		Assert.assertEquals(scanKeysAndCountWithConfig(new Config().setLimit(10)), 10);
		Assert.assertEquals(scanKeysAndCountWithConfig(new Config().setLimit((int)(2 * count))), count);
		Assert.assertEquals(scanKeysAndCountWithConfig(new Config().setOutputBatchSize(25).setLimit(100)), 100);
		Assert.assertEquals(scanKeysAndCountWithConfig(new Config().setOutputBatchSize(15).setLimit(23)), 23);
	}

	private long scanKeysAndCountWithConfig(Config config){
		return sortedNode.scanKeys(config).count();
	}

	@Test
	protected void testLimitedScan(){
		long count = SortedBeans.TOTAL_RECORDS;
		Assert.assertNotEquals(0, count);
		PhaseTimer timer = new PhaseTimer("testLimitedScan");
		Assert.assertEquals(scanAndCountWithConfig(new Config().setOutputBatchSize(555)), count);
		timer.add("1");
		Assert.assertEquals(scanAndCountWithConfig(new Config().setLimit((int)count)), count);
		timer.add("2");
		Assert.assertEquals(scanAndCountWithConfig(new Config().setLimit(10)), 10);
		timer.add("3");
		Assert.assertEquals(scanAndCountWithConfig(new Config().setLimit((int)(2 * count))), count);
		timer.add("4");
		Assert.assertEquals(scanAndCountWithConfig(new Config().setOutputBatchSize(25).setLimit(100)), 100);
		timer.add("5");
		Assert.assertEquals(scanAndCountWithConfig(new Config().setOutputBatchSize(15).setLimit(23)), 23);
		timer.add("6");
		logger.warn(timer.toString());
		//[total:633ms]<testLimitedScan>[1:112ms][2:107ms][3:90ms][4:143ms][5:74ms][6:107ms]
	}

	private long scanAndCountWithConfig(Config config){
		return sortedNode.scan(config).count();
	}

	@Test
	public void testNullStartKeyScan(){
		Range<SortedBeanKey> range = new Range<>(null, false, null, true);
		long numKeys = sortedNode.scanKeys(range).count();
		Assert.assertEquals(numKeys, SortedBeans.TOTAL_RECORDS);
	}

	@Test
	public void testNullEndKeyScan(){
		Range<SortedBeanKey> range = new Range<>(null, true, null, true);
		long numKeys = sortedNode.scanKeys(range).count();
		Assert.assertEquals(numKeys, SortedBeans.TOTAL_RECORDS);
	}

	@Test
	public void testScanOffset(){
		int offset = 10;
		long count = sortedNode.scan(new Config().setOffset(offset)).count();
		Assert.assertEquals(count, SortedBeans.TOTAL_RECORDS - offset);
		SortedBean firstByOffset = sortedNode.scan(new Config().setOffset(offset).setLimit(1))
				.findFirst()
				.get();
		SortedBean firstBySkip = sortedNode.scan(new Config().setLimit(offset + 1))
				.skip(offset)
				.findFirst()
				.get();
		Assert.assertEquals(firstByOffset, firstBySkip);
	}

	@Test
	public void testScanKeysOffset(){
		int offset = 10;
		long count = sortedNode.scanKeys(new Config().setOffset(offset)).count();
		Assert.assertEquals(count, SortedBeans.TOTAL_RECORDS - offset);
		SortedBeanKey firstByOffset = sortedNode.scanKeys(new Config().setOffset(offset).setLimit(
				1)).findFirst().get();
		SortedBeanKey firstBySkip = sortedNode.scanKeys(new Config().setLimit(offset + 1))
				.skip(offset).findFirst().get();
		Assert.assertEquals(firstByOffset, firstBySkip);
	}

	@Test
	public void testEmptyLastBatchRangeScan(){
		SortedBeanKey startKey = new SortedBeanKey(SortedBeans.S_alpaca, SortedBeans.S_ostrich, 7, SortedBeans.S_emu);
		SortedBeanKey endKey = new SortedBeanKey(SortedBeans.S_alpaca, SortedBeans.S_ostrich, 7, SortedBeans.S_pelican);
		Range<SortedBeanKey> rangeWithFourRows = new Range<>(startKey, true, endKey, true);
		long count = sortedNode.scanKeys(rangeWithFourRows, new Config().setOutputBatchSize(2)).count();
		Assert.assertEquals(count, 4);
	}

	@Test
	public void testEmptyRangeScan(){
		SortedBeanKey key = new SortedBeanKey(SortedBeans.S_alpaca, SortedBeans.S_ostrich, 7, SortedBeans.S_emu);
		Range<SortedBeanKey> emptyRange = new Range<>(key, true, key, false);//Range defines this as empty
		long count = sortedNode.scanKeys(emptyRange).count();
		Assert.assertEquals(count, 0);
	}

	@Test
	public void testSortedStorageCountingTool(){
		long count = sortedNode.count(Range.everything());
		Assert.assertEquals(count, SortedBeans.TOTAL_RECORDS);
	}

	@Test
	public void testExclusiveStartKey(){
		// single entity case
		SortedBeanKey startKey = new SortedBeanKey(SortedBeans.S_alpaca, SortedBeans.S_ostrich, 7, SortedBeans.S_emu);
		SortedBeanKey endKey = new SortedBeanKey(SortedBeans.S_alpaca, SortedBeans.S_ostrich, null, null);
		List<SortedBean> result = sortedNode.scan(new Range<>(startKey, false, endKey, true)).list();
		Assert.assertEquals(result.get(0).getKey(), new SortedBeanKey(SortedBeans.S_alpaca, SortedBeans.S_ostrich, 7,
				SortedBeans.S_gopher));
		Assert.assertEquals(result.size(), 3); // because 7 is the last and gopher+strich+pelican
		// TODO multiple entity case DATAROUTER-259
	}

	@Test
	public void testScanMulti(){
		SortedBeanKey startKey1 = new SortedBeanKey(SortedBeans.S_albatross, SortedBeans.S_ostrich, 0,
				SortedBeans.S_albatross);
		SortedBeanKey endKey1 = new SortedBeanKey(SortedBeans.S_albatross, SortedBeans.S_ostrich, 0,
				SortedBeans.S_ostrich);
		Range<SortedBeanKey> range1 = new Range<>(startKey1, endKey1);
		SortedBeanKey startKey2 = new SortedBeanKey(SortedBeans.S_albatross, SortedBeans.S_ostrich, 3,
				SortedBeans.S_aardvark);
		SortedBeanKey endKey2 = new SortedBeanKey(SortedBeans.S_albatross, SortedBeans.S_ostrich, 3, SortedBeans.S_emu);
		Range<SortedBeanKey> range2 = new Range<>(startKey2, endKey2);
		Set<SortedBean> beans = sortedNode.scanMulti(Arrays.asList(range1, range2),
				new Config().setOutputBatchSize(4))
				.collect(Collectors.toSet());
		Set<SortedBean> expected = Scanner.of(range1, range2)
				.mapToScanner(sortedNode::scan)
				.concatenate()
				.collect(Collectors.toSet());
		Assert.assertTrue(expected.size() > 0);
		Assert.assertEquals(beans, expected);
	}

	@Test
	public void testSurviveKeyMutation(){
		long count = sortedNode.scanKeys()
				.peek(key -> key.setFoo("z"))
				.count();
		Assert.assertEquals(count, SortedBeans.TOTAL_RECORDS);
	}

}
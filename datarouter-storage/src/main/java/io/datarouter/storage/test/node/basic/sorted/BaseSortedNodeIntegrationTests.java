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

import com.google.common.collect.Sets;

import io.datarouter.model.databean.DatabeanTool;
import io.datarouter.model.field.Field;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.op.util.SortedStorageCountingTool;
import io.datarouter.storage.test.node.basic.sorted.SortedBean.SortedBeanFielder;
import io.datarouter.storage.util.KeyRangeTool;
import io.datarouter.util.collection.CollectionTool;
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
		Assert.assertEquals(IterableTool.count(sortedNode.scan(null, null)).intValue(), remainingElements);
		SortedBeanKey key = new SortedBeanKey(SortedBeans.STRINGS.last(), SortedBeans.STRINGS.last(), 0,
				SortedBeans.STRINGS.last());
		sortedNode.delete(key, null);
		--remainingElements;
		Assert.assertEquals(IterableTool.count(sortedNode.scan(null, null)).intValue(), remainingElements);

		//deleteMulti
		Assert.assertEquals(IterableTool.count(sortedNode.scan(null, null)).intValue(), remainingElements);
		List<SortedBeanKey> keys = Arrays.asList(
			new SortedBeanKey(SortedBeans.STRINGS.last(), SortedBeans.STRINGS.last(), 1, SortedBeans.STRINGS.last()),
			new SortedBeanKey(SortedBeans.STRINGS.last(), SortedBeans.STRINGS.last(), 2, SortedBeans.STRINGS.last()),
			new SortedBeanKey(SortedBeans.STRINGS.last(), SortedBeans.STRINGS.last(), 3, SortedBeans.STRINGS.last()));
		sortedNode.deleteMulti(keys, null);
		remainingElements -= 3;
		Assert.assertEquals(IterableTool.count(sortedNode.scan(null, null)).intValue(), remainingElements);

		//deleteWithPrefix
		Assert.assertEquals(IterableTool.count(sortedNode.scan(null, null)).intValue(), remainingElements);
		SortedBeanKey prefix = new SortedBeanKey(SortedBeans.S_aardvark, null, null, null);
		sortedNode.deleteWithPrefix(prefix, null);
		remainingElements -= SortedBeans.NUM_ELEMENTS * SortedBeans.NUM_ELEMENTS * SortedBeans.NUM_ELEMENTS;
		Assert.assertEquals(IterableTool.count(sortedNode.scan(null, null)).intValue(), remainingElements);
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
		Assert.assertNull(sortedNode.get(blankDatabean.getKey(), config));
	}

	protected void testIgnoreNull(){
		SortedBeanKey pk = new SortedBeanKey("a", "b", 3, "d");
		String f1 = "Degermat";
		String f3 = "Kenavo";
		SortedBean databean = new SortedBean(pk, f1, null, null, null);
		sortedNode.put(databean, null);
		ThreadTool.sleep(1);//fix flaky test. see DATAROUTER-487
		databean = new SortedBean(pk, null, null, f3, null);
		sortedNode.put(databean, new Config().setIgnoreNullFields(true));
		databean = sortedNode.get(pk, null);
		Assert.assertEquals(databean.getF1(), f1);
		Assert.assertEquals(databean.getF3(), f3);
		sortedNode.delete(pk, null);
		Assert.assertNull(sortedNode.get(pk, null));
	}

	@Test
	public void testGetKeys(){
		SortedBeanKey key1 = new SortedBeanKey(SortedBeans.S_aardvark, SortedBeans.S_aardvark, 0, SortedBeans.S_alpaca);
		SortedBeanKey key2 = new SortedBeanKey("blah", "blah", 1000, "blah");
		SortedBeanKey key3 = new SortedBeanKey(SortedBeans.S_aardvark, SortedBeans.S_albatross, 2, SortedBeans.S_emu);
		List<SortedBeanKey> keysToGet = ListTool.create(key1, key2, key3);
		List<SortedBeanKey> keysGotten = sortedNode.getKeys(keysToGet, null);
		Assert.assertTrue(keysGotten.contains(key1));
		Assert.assertFalse(keysGotten.contains(key2));
		Assert.assertTrue(keysGotten.contains(key3));
	}

	@Test
	public void testGetAll(){
		List<SortedBean> allBeans = ListTool.createArrayList(sortedNode.scan(null, null));
		Assert.assertEquals(CollectionTool.size(allBeans), SortedBeans.TOTAL_RECORDS);
	}

	@Test
	public void testStreamWithPrefix(){
		//first 3 fields fixed
		SortedBeanKey prefix1 = new SortedBeanKey(SortedBeans.STRINGS.first(), SortedBeans.STRINGS.last(), 2, null);
		List<SortedBean> result1 = sortedNode.streamWithPrefix(prefix1, null)
				.collect(Collectors.toList());
		Assert.assertEquals(CollectionTool.size(result1), SortedBeans.NUM_ELEMENTS);
		Assert.assertTrue(ListTool.isSorted(result1));

		//first 3 fields fixed, last field wildcard
		List<SortedBean> result2 = sortedNode.stream(KeyRangeTool.forPrefixWithWildcard(SortedBeans.PREFIX_a,
				suffix -> new SortedBeanKey(SortedBeans.STRINGS.first(), SortedBeans.STRINGS.last(), 2, suffix)), null)
				.collect(Collectors.toList());
		Assert.assertEquals(CollectionTool.size(result2), SortedBeans.NUM_PREFIX_a);
		Assert.assertTrue(ListTool.isSorted(result2));

		//first field fixed, second field wildcard
		List<SortedBean> result3 = sortedNode.stream(KeyRangeTool.forPrefixWithWildcard(SortedBeans.PREFIX_a,
				suffix -> new SortedBeanKey(SortedBeans.STRINGS.first(), suffix, null, null)), null)
				.collect(Collectors.toList());
		int expectedSize3 = SortedBeans.NUM_PREFIX_a * SortedBeans.NUM_ELEMENTS * SortedBeans.NUM_ELEMENTS;
		Assert.assertEquals(CollectionTool.size(result3), expectedSize3);
		Assert.assertTrue(ListTool.isSorted(result3));
	}

	@Test
	public void testGetWithPrefixes(){
		List<Range<SortedBeanKey>> ranges = Stream.of(SortedBeans.PREFIX_a, SortedBeans.PREFIX_ch)
				.map(prefix -> KeyRangeTool.forPrefixWithWildcard(prefix, suffix -> new SortedBeanKey(
						SortedBeans.STRINGS.first(), suffix, null, null)))
				.collect(Collectors.toList());
		List<SortedBean> result = sortedNode.streamMulti(ranges, null)
				.collect(Collectors.toList());
		int expectedSizeA = SortedBeans.NUM_PREFIX_a * SortedBeans.NUM_ELEMENTS * SortedBeans.NUM_ELEMENTS;
		int expectedSizeCh = SortedBeans.NUM_PREFIX_ch * SortedBeans.NUM_ELEMENTS * SortedBeans.NUM_ELEMENTS;
		int expectedSizeTotal = expectedSizeA + expectedSizeCh;
		Assert.assertEquals(CollectionTool.size(result), expectedSizeTotal);
		Assert.assertTrue(ListTool.isSorted(result));
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
		public Iterable<T> scan(Range<SortedBeanKey> range, Config config);
	}

	private static <T extends Comparable<? super T>> void testGetKeysOrDatabeanInRange(ScanBySortedBeanKeyProvider<T>
			scanProvider){
		SortedBeanKey alp1 = new SortedBeanKey(SortedBeans.RANGE_alp, null, null, null);
		SortedBeanKey emu1 = new SortedBeanKey(SortedBeans.RANGE_emu, null, null, null);
		Range<SortedBeanKey> range1 = new Range<>(alp1, true, emu1, true);
		List<T> result1 = ListTool.createArrayList(scanProvider.scan(range1, null));
		int expectedSize1 = SortedBeans.RANGE_LENGTH_alp_emu_inc * SortedBeans.NUM_ELEMENTS * SortedBeans.NUM_ELEMENTS
				* SortedBeans.NUM_ELEMENTS;
		Assert.assertEquals(CollectionTool.size(result1), expectedSize1);
		Assert.assertTrue(ListTool.isSorted(result1));

		Range<SortedBeanKey> range1b = new Range<>(alp1, true, emu1, false);
		List<T> result1b = ListTool.createArrayList(scanProvider.scan(range1b, null));
		int expectedSize1b = (SortedBeans.RANGE_LENGTH_alp_emu_inc - 1) * SortedBeans.NUM_ELEMENTS
				* SortedBeans.NUM_ELEMENTS * SortedBeans.NUM_ELEMENTS;
		Assert.assertEquals(CollectionTool.size(result1b), expectedSize1b);
		Assert.assertTrue(ListTool.isSorted(result1b));

		SortedBeanKey alp2 = new SortedBeanKey(SortedBeans.STRINGS.first(), SortedBeans.RANGE_alp, null, null);
		SortedBeanKey emu2 = new SortedBeanKey(SortedBeans.STRINGS.first(), SortedBeans.RANGE_emu, null, null);
		Range<SortedBeanKey> range2 = new Range<>(alp2, true, emu2, true);
		List<T> result2 = ListTool.createArrayList(scanProvider.scan(range2, null));
		int expectedSize2 = SortedBeans.RANGE_LENGTH_alp_emu_inc * SortedBeans.NUM_ELEMENTS * SortedBeans.NUM_ELEMENTS;
		Assert.assertEquals(CollectionTool.size(result2), expectedSize2);
		Assert.assertTrue(ListTool.isSorted(result2));
	}

	@Test //small batch sizes to make sure we're resuming each batch from the correct spot
	public void testIncrementalScan(){
		Config smallIterateBatchSize = new Config().setIterateBatchSize(3);

		int expectedSize1 = SortedBeans.RANGE_LENGTH_alp_emu_inc * SortedBeans.NUM_ELEMENTS * SortedBeans.NUM_ELEMENTS
				* SortedBeans.NUM_ELEMENTS;
//		logger.warn("expecting "+expectedSize1);
		SortedBeanKey alp1 = new SortedBeanKey(SortedBeans.RANGE_alp, null, null, null);
		SortedBeanKey emu1 = new SortedBeanKey(SortedBeans.RANGE_emu, null, null, null);
		List<SortedBeanKey> result1 = IterableTool.asList(sortedNode.scanKeys(new Range<>(alp1, true, emu1, true),
				smallIterateBatchSize));
		Assert.assertEquals(CollectionTool.size(result1), expectedSize1);
		Assert.assertTrue(ListTool.isSorted(result1));

		int expectedSize1b = (SortedBeans.RANGE_LENGTH_alp_emu_inc - 1) * SortedBeans.NUM_ELEMENTS
				* SortedBeans.NUM_ELEMENTS * SortedBeans.NUM_ELEMENTS;
//		logger.warn("expecting "+expectedSize1b);
		List<SortedBeanKey> result1b = IterableTool.asList(sortedNode.scanKeys(new Range<>(alp1, true, emu1, false),
				smallIterateBatchSize));
		Assert.assertEquals(CollectionTool.size(result1b), expectedSize1b);
		Assert.assertTrue(ListTool.isSorted(result1b));

		int expectedSize2 = SortedBeans.RANGE_LENGTH_alp_emu_inc * SortedBeans.NUM_ELEMENTS * SortedBeans.NUM_ELEMENTS;
//		logger.warn("expecting "+expectedSize2);
		SortedBeanKey alp2 = new SortedBeanKey(SortedBeans.STRINGS.first(), SortedBeans.RANGE_alp, null, null);
		SortedBeanKey emu2 = new SortedBeanKey(SortedBeans.STRINGS.first(), SortedBeans.RANGE_emu, null, null);
		List<SortedBeanKey> result2 = IterableTool.asList(sortedNode.scanKeys(new Range<>(alp2, true, emu2, true),
				smallIterateBatchSize));
		Assert.assertEquals(CollectionTool.size(result2), expectedSize2);
		Assert.assertTrue(ListTool.isSorted(result2));
//		logger.warn("finished incremental scan");
	}

	@Test
	public void testGet(){
		List<SortedBean> all = sortedNode.stream(null, null).collect(Collectors.toList());
		final int sampleEveryN = 29;
		for(int i = 0; i < all.size(); i += sampleEveryN){
			SortedBean sortedBeanFromScan = all.get(i);
			SortedBean sortedBeanFromGet = sortedNode.get(sortedBeanFromScan.getKey(), null);
			Assert.assertEquals(sortedBeanFromGet, sortedBeanFromScan);
		}
	}

	@Test
	public void testGetMulti(){
		Iterable<SortedBean> iterable = sortedNode.scan(null, null);
		Set<SortedBean> allBeans = Sets.newHashSet(iterable);
		Assert.assertEquals(allBeans.size(), SortedBeans.TOTAL_RECORDS);
		List<SortedBean> getMultiResult = sortedNode.getMulti(DatabeanTool.getKeys(allBeans), null);
		Assert.assertEquals(getMultiResult.size(), SortedBeans.TOTAL_RECORDS);
		for(SortedBean sortedBeanResult : getMultiResult){
			Assert.assertTrue(allBeans.contains(sortedBeanResult));
		}
	}

	@Test
	public void testFullScanKeys(){
		Iterable<SortedBeanKey> iterable = sortedNode.scanKeys(null, null);
		long numKeys = IterableTool.count(iterable);
		Assert.assertEquals(numKeys, SortedBeans.TOTAL_RECORDS);
	}

	@Test
	public void testFullScan(){
		Iterable<SortedBean> iterable = sortedNode.scan(null, null);
		long numDatabeans = IterableTool.count(iterable);
		Assert.assertEquals(numDatabeans, SortedBeans.TOTAL_RECORDS);
	}

	@Test
	protected void testLimitedScanKeys(){
		long count = SortedBeans.TOTAL_RECORDS;
		Assert.assertNotEquals(0, count);
		Assert.assertEquals(scanKeysAndCountWithConfig(new Config().setIterateBatchSize(555)), count);
		Assert.assertEquals(scanKeysAndCountWithConfig(new Config().setLimit((int)count)), count);
		Assert.assertEquals(scanKeysAndCountWithConfig(new Config().setLimit(10)), 10);
		Assert.assertEquals(scanKeysAndCountWithConfig(new Config().setLimit((int)(2 * count))), count);
		Assert.assertEquals(scanKeysAndCountWithConfig(new Config().setIterateBatchSize(25).setLimit(100)), 100);
		Assert.assertEquals(scanKeysAndCountWithConfig(new Config().setIterateBatchSize(15).setLimit(23)), 23);
	}

	private long scanKeysAndCountWithConfig(Config config){
		return sortedNode.streamKeys(null, config).count();
	}

	@Test
	protected void testLimitedScan(){
		long count = SortedBeans.TOTAL_RECORDS;
		Assert.assertNotEquals(0, count);
		PhaseTimer timer = new PhaseTimer("testLimitedScan");
		Assert.assertEquals(scanAndCountWithConfig(new Config().setIterateBatchSize(555)), count);
		timer.add("1");
		Assert.assertEquals(scanAndCountWithConfig(new Config().setLimit((int)count)), count);
		timer.add("2");
		Assert.assertEquals(scanAndCountWithConfig(new Config().setLimit(10)), 10);
		timer.add("3");
		Assert.assertEquals(scanAndCountWithConfig(new Config().setLimit((int)(2 * count))), count);
		timer.add("4");
		Assert.assertEquals(scanAndCountWithConfig(new Config().setIterateBatchSize(25).setLimit(100)), 100);
		timer.add("5");
		Assert.assertEquals(scanAndCountWithConfig(new Config().setIterateBatchSize(15).setLimit(23)), 23);
		timer.add("6");
		logger.warn(timer.toString());
		//[total:633ms]<testLimitedScan>[1:112ms][2:107ms][3:90ms][4:143ms][5:74ms][6:107ms]
	}

	private long scanAndCountWithConfig(Config config){
		return sortedNode.stream(null, config).count();
	}

	@Test
	public void testNullStartKeyScan(){
		Range<SortedBeanKey> range = new Range<>(null, false, null, true);
		Iterable<SortedBeanKey> iterable = sortedNode.scanKeys(range, null);
		long numDatabeans = IterableTool.count(iterable);
		Assert.assertEquals(numDatabeans, SortedBeans.TOTAL_RECORDS);
	}

	@Test
	public void testNullEndKeyScan(){
		Range<SortedBeanKey> range = new Range<>(null, true, null, true);
		Iterable<SortedBeanKey> iterable = sortedNode.scanKeys(range, null);
		long numDatabeans = IterableTool.count(iterable);
		Assert.assertEquals(numDatabeans, SortedBeans.TOTAL_RECORDS);
	}

	@Test
	public void testScanOffset(){
		int offset = 10;
		Assert.assertEquals(sortedNode.stream(Range.everything(), new Config().setOffset(offset)).count(),
				SortedBeans.TOTAL_RECORDS - offset);
		Assert.assertEquals(sortedNode.stream(Range.everything(), new Config().setOffset(offset).setLimit(1))
				.findFirst().get(), sortedNode.stream(Range.everything(), new Config().setLimit(offset + 1))
				.skip(offset).findFirst().get());
	}

	@Test
	public void testScanKeysOffset(){
		int offset = 10;
		Assert.assertEquals(sortedNode.streamKeys(Range.everything(), new Config().setOffset(offset)).count(),
				SortedBeans.TOTAL_RECORDS - offset);
		Assert.assertEquals(sortedNode.streamKeys(Range.everything(), new Config().setOffset(offset).setLimit(1))
				.findFirst().get(), sortedNode.streamKeys(Range.everything(), new Config().setLimit(offset + 1))
				.skip(offset).findFirst().get());
	}

	@Test
	public void testEmptyLastBatchRangeScan(){
		SortedBeanKey startKey = new SortedBeanKey(SortedBeans.S_alpaca, SortedBeans.S_ostrich, 7, SortedBeans.S_emu);
		SortedBeanKey endKey = new SortedBeanKey(SortedBeans.S_alpaca, SortedBeans.S_ostrich, 7, SortedBeans.S_pelican);
		Range<SortedBeanKey> rangeWithFourRows = new Range<>(startKey, true, endKey, true);
		Assert.assertEquals(sortedNode.streamKeys(rangeWithFourRows, new Config().setIterateBatchSize(2)).count(), 4);
	}

	@Test
	public void testEmptyRangeScan(){
		SortedBeanKey key = new SortedBeanKey(SortedBeans.S_alpaca, SortedBeans.S_ostrich, 7, SortedBeans.S_emu);
		Range<SortedBeanKey> emptyRange = new Range<>(key, key);
		Assert.assertEquals(sortedNode.streamKeys(emptyRange, null).count(), 0);
	}

	@Test
	public void testSortedStorageCountingTool(){
		Assert.assertEquals(SortedStorageCountingTool.count(sortedNode, Range.everything()), SortedBeans.TOTAL_RECORDS);
	}

	@Test
	public void testExclusiveStartKey(){
		// signle entity case
		SortedBeanKey startKey = new SortedBeanKey(SortedBeans.S_alpaca, SortedBeans.S_ostrich, 7, SortedBeans.S_emu);
		SortedBeanKey endKey = new SortedBeanKey(SortedBeans.S_alpaca, SortedBeans.S_ostrich, null, null);
		List<SortedBean> result = sortedNode.stream(new Range<>(startKey, false, endKey, true), null)
				.collect(Collectors.toList());
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
		Set<SortedBean> beans = sortedNode.streamMulti(Arrays.asList(range1, range2),
				new Config().setIterateBatchSize(4)).collect(Collectors.toSet());
		Set<SortedBean> expected = Stream.of(range1, range2).flatMap(range -> sortedNode.stream(range, null))
				.collect(Collectors.toSet());
		Assert.assertTrue(expected.size() > 0);
		Assert.assertEquals(beans, expected);
	}

}
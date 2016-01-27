package com.hotpads.datarouter.test.node.basic.sorted;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.testng.Assert;
import org.testng.AssertJUnit;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import com.google.common.collect.Sets;
import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.config.PutMethod;
import com.hotpads.datarouter.node.factory.EntityNodeFactory;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage;
import com.hotpads.datarouter.op.util.SortedStorageCountingTool;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.storage.databean.DatabeanTool;
import com.hotpads.datarouter.test.DatarouterTestModuleFactory;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrIterableTool;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.core.iterable.BatchingIterable;

@Guice(moduleFactory=DatarouterTestModuleFactory.class)
public abstract class BaseSortedNodeIntegrationTests{

	/***************************** fields **************************************/

	@Inject
	protected Datarouter datarouter;
	@Inject
	private EntityNodeFactory entityNodeFactory;
	@Inject
	private NodeFactory nodeFactory;

	protected SortedNodeTestRouter router;
	protected SortedMapStorage<SortedBeanKey,SortedBean> sortedNode;


	/***************************** setup/teardown **************************************/

	protected void setup(ClientId clientId, boolean useFielder, boolean entity){
		router = new SortedNodeTestRouter(datarouter, entityNodeFactory, nodeFactory, clientId, useFielder, entity);
		sortedNode = router.sortedBean();

		resetTable(true);
	}

	protected void resetTable(boolean force){
		long numExistingDatabeans = DrIterableTool.count(sortedNode.scan(null, null));
		if(!force && SortedBeans.TOTAL_RECORDS == numExistingDatabeans){
			return;
		}

		sortedNode.deleteAll(null);
		List<SortedBean> remainingAfterDelete = DrListTool.createArrayList(sortedNode.scan(null, null));
		AssertJUnit.assertEquals(0, DrCollectionTool.size(remainingAfterDelete));

		List<SortedBean> allBeans = SortedBeans.generatedSortedBeans();
		for(List<SortedBean> batch : new BatchingIterable<>(allBeans, 1000)){
			sortedNode.putMulti(batch, new Config().setPutMethod(PutMethod.INSERT_OR_BUST));
		}

		List<SortedBean> roundTripped = DrListTool.createArrayList(sortedNode.scan(null, null));
		AssertJUnit.assertEquals(SortedBeans.TOTAL_RECORDS, roundTripped.size());
	}

	protected void testSortedDelete(){
		resetTable(true);
		int remainingElements = SortedBeans.TOTAL_RECORDS;

		//delete
		AssertJUnit.assertEquals(remainingElements, DrIterableTool.count(sortedNode.scan(null, null)).intValue());
		SortedBeanKey key = new SortedBeanKey(SortedBeans.STRINGS.last(), SortedBeans.STRINGS.last(), 0,
				SortedBeans.STRINGS.last());
		sortedNode.delete(key, null);
		--remainingElements;
		AssertJUnit.assertEquals(remainingElements, DrIterableTool.count(sortedNode.scan(null, null)).intValue());

		//deleteMulti
		AssertJUnit.assertEquals(remainingElements, DrIterableTool.count(sortedNode.scan(null, null)).intValue());
		List<SortedBeanKey> keys = Arrays.asList(
			new SortedBeanKey(SortedBeans.STRINGS.last(), SortedBeans.STRINGS.last(), 1, SortedBeans.STRINGS.last()),
			new SortedBeanKey(SortedBeans.STRINGS.last(), SortedBeans.STRINGS.last(), 2, SortedBeans.STRINGS.last()),
			new SortedBeanKey(SortedBeans.STRINGS.last(), SortedBeans.STRINGS.last(), 3, SortedBeans.STRINGS.last()));
		sortedNode.deleteMulti(keys, null);
		remainingElements -= 3;
		AssertJUnit.assertEquals(remainingElements, DrIterableTool.count(sortedNode.scan(null, null)).intValue());


		//deleteWithPrefix
		AssertJUnit.assertEquals(remainingElements, DrIterableTool.count(sortedNode.scan(null, null)).intValue());
		SortedBeanKey prefix = new SortedBeanKey(SortedBeans.PREFIX_a, null, null, null);
		sortedNode.deleteRangeWithPrefix(prefix, true, null);
		remainingElements -= SortedBeans.NUM_PREFIX_a * SortedBeans.NUM_ELEMENTS * SortedBeans.NUM_ELEMENTS
				* SortedBeans.NUM_ELEMENTS;
		AssertJUnit.assertEquals(remainingElements, DrIterableTool.count(sortedNode.scan(null, null)).intValue());
	}

	/********************** junit methods *********************************************/

	//hbase 0.94.2 is failing this test
	@Test
	public void testNullKey(){
		SortedBeanKey emptyKey = new SortedBeanKey(null, null, null, null);
		SortedBean shouldBeNull = sortedNode.get(emptyKey, null);
		Assert.assertNull(shouldBeNull);
	}

	@Test
	public void testGetKeys(){
		SortedBeanKey key1 = new SortedBeanKey(SortedBeans.S_aardvark, SortedBeans.S_aardvark, 0, SortedBeans.S_alpaca);
		SortedBeanKey key2 = new SortedBeanKey("blah", "blah", 1000, "blah");
		SortedBeanKey key3 = new SortedBeanKey(SortedBeans.S_aardvark, SortedBeans.S_albatross, 2, SortedBeans.S_emu);
		List<SortedBeanKey> keysToGet = DrListTool.create(key1, key2, key3);
		List<SortedBeanKey> keysGotten = sortedNode.getKeys(keysToGet, null);
		AssertJUnit.assertTrue(keysGotten.contains(key1));
		AssertJUnit.assertFalse(keysGotten.contains(key2));
		AssertJUnit.assertTrue(keysGotten.contains(key3));
	}

	@Test
	public void testGetAll(){
		List<SortedBean> allBeans = DrListTool.createArrayList(sortedNode.scan(null, null));
		AssertJUnit.assertEquals(SortedBeans.TOTAL_RECORDS, DrCollectionTool.size(allBeans));
	}

	@Test
	public void testGetWithPrefix(){
		//first 3 fields fixed
		SortedBeanKey prefix1 = new SortedBeanKey(SortedBeans.STRINGS.first(), SortedBeans.STRINGS.last(), 2, null);
		List<SortedBean> result1 = sortedNode.getWithPrefix(prefix1, false, null);
		AssertJUnit.assertEquals(SortedBeans.NUM_ELEMENTS, DrCollectionTool.size(result1));
		AssertJUnit.assertTrue(DrListTool.isSorted(result1));

		//first 3 fields fixed, last field wildcard
		SortedBeanKey prefix2 = new SortedBeanKey(SortedBeans.STRINGS.first(), SortedBeans.STRINGS.last(), 2,
				SortedBeans.PREFIX_a);
		List<SortedBean> result2 = sortedNode.getWithPrefix(prefix2, true, null);
		AssertJUnit.assertEquals(SortedBeans.NUM_PREFIX_a, DrCollectionTool.size(result2));
		AssertJUnit.assertTrue(DrListTool.isSorted(result2));

		//first field fixed, second field wildcard
		SortedBeanKey prefix3 = new SortedBeanKey(SortedBeans.STRINGS.first(), SortedBeans.PREFIX_a, null, null);
		List<SortedBean> result3 = sortedNode.getWithPrefix(prefix3, true, null);
		int expectedSize3 = SortedBeans.NUM_PREFIX_a * SortedBeans.NUM_ELEMENTS * SortedBeans.NUM_ELEMENTS;
		AssertJUnit.assertEquals(expectedSize3, DrCollectionTool.size(result3));
		AssertJUnit.assertTrue(DrListTool.isSorted(result3));
	}

	@Test
	public void testGetWithPrefixes(){
		SortedBeanKey prefixA = new SortedBeanKey(SortedBeans.STRINGS.first(), SortedBeans.PREFIX_a, null, null);
		SortedBeanKey prefixCh = new SortedBeanKey(SortedBeans.STRINGS.first(), SortedBeans.PREFIX_ch, null, null);
		List<SortedBeanKey> prefixes = DrListTool.create(prefixA, prefixCh);
		List<SortedBean> result = sortedNode.getWithPrefixes(prefixes, true, null);
		int expectedSizeA = SortedBeans.NUM_PREFIX_a * SortedBeans.NUM_ELEMENTS * SortedBeans.NUM_ELEMENTS;
		int expectedSizeCh = SortedBeans.NUM_PREFIX_ch * SortedBeans.NUM_ELEMENTS * SortedBeans.NUM_ELEMENTS;
		int expectedSizeTotal = expectedSizeA + expectedSizeCh;
		AssertJUnit.assertEquals(expectedSizeTotal, DrCollectionTool.size(result));
		AssertJUnit.assertTrue(DrListTool.isSorted(result));
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
		List<T> result1 = DrListTool.createArrayList(scanProvider.scan(range1, null));
		int expectedSize1 = SortedBeans.RANGE_LENGTH_alp_emu_inc * SortedBeans.NUM_ELEMENTS * SortedBeans.NUM_ELEMENTS
				* SortedBeans.NUM_ELEMENTS;
		AssertJUnit.assertEquals(expectedSize1, DrCollectionTool.size(result1));
		AssertJUnit.assertTrue(DrListTool.isSorted(result1));

		Range<SortedBeanKey> range1b = new Range<>(alp1, true, emu1, false);
		List<T> result1b = DrListTool.createArrayList(scanProvider.scan(range1b, null));
		int expectedSize1b = (SortedBeans.RANGE_LENGTH_alp_emu_inc-1) * SortedBeans.NUM_ELEMENTS
				* SortedBeans.NUM_ELEMENTS * SortedBeans.NUM_ELEMENTS;
		AssertJUnit.assertEquals(expectedSize1b, DrCollectionTool.size(result1b));
		AssertJUnit.assertTrue(DrListTool.isSorted(result1b));

		SortedBeanKey alp2 = new SortedBeanKey(SortedBeans.STRINGS.first(), SortedBeans.RANGE_alp, null, null);
		SortedBeanKey emu2 = new SortedBeanKey(SortedBeans.STRINGS.first(), SortedBeans.RANGE_emu, null, null);
		Range<SortedBeanKey> range2 = new Range<>(alp2, true, emu2, true);
		List<T> result2 = DrListTool.createArrayList(scanProvider.scan(range2, null));
		int expectedSize2 = SortedBeans.RANGE_LENGTH_alp_emu_inc * SortedBeans.NUM_ELEMENTS * SortedBeans.NUM_ELEMENTS;
		AssertJUnit.assertEquals(expectedSize2, DrCollectionTool.size(result2));
		AssertJUnit.assertTrue(DrListTool.isSorted(result2));
	}

	@Test //small batch sizes to make sure we're resuming each batch from the correct spot
	public void testIncrementalScan(){
		Config smallIterateBatchSize = new Config().setIterateBatchSize(3);

		int expectedSize1 = SortedBeans.RANGE_LENGTH_alp_emu_inc * SortedBeans.NUM_ELEMENTS * SortedBeans.NUM_ELEMENTS
				* SortedBeans.NUM_ELEMENTS;
//		logger.warn("expecting "+expectedSize1);
		SortedBeanKey alp1 = new SortedBeanKey(SortedBeans.RANGE_alp, null, null, null);
		SortedBeanKey emu1 = new SortedBeanKey(SortedBeans.RANGE_emu, null, null, null);
		List<SortedBeanKey> result1 = DrIterableTool.asList(sortedNode.scanKeys(Range.create(alp1, true, emu1, true),
				smallIterateBatchSize));
		AssertJUnit.assertEquals(expectedSize1, DrCollectionTool.size(result1));
		AssertJUnit.assertTrue(DrListTool.isSorted(result1));

		int expectedSize1b = (SortedBeans.RANGE_LENGTH_alp_emu_inc - 1) * SortedBeans.NUM_ELEMENTS
				* SortedBeans.NUM_ELEMENTS * SortedBeans.NUM_ELEMENTS;
//		logger.warn("expecting "+expectedSize1b);
		List<SortedBeanKey> result1b = DrIterableTool.asList(sortedNode.scanKeys(Range.create(alp1, true, emu1, false),
				smallIterateBatchSize));
		AssertJUnit.assertEquals(expectedSize1b, DrCollectionTool.size(result1b));
		AssertJUnit.assertTrue(DrListTool.isSorted(result1b));

		int expectedSize2 = SortedBeans.RANGE_LENGTH_alp_emu_inc * SortedBeans.NUM_ELEMENTS * SortedBeans.NUM_ELEMENTS;
//		logger.warn("expecting "+expectedSize2);
		SortedBeanKey alp2 = new SortedBeanKey(SortedBeans.STRINGS.first(), SortedBeans.RANGE_alp, null, null);
		SortedBeanKey emu2 = new SortedBeanKey(SortedBeans.STRINGS.first(), SortedBeans.RANGE_emu, null, null);
		List<SortedBeanKey> result2 = DrIterableTool.asList(sortedNode.scanKeys(Range.create(alp2, true, emu2, true),
				smallIterateBatchSize));
		AssertJUnit.assertEquals(expectedSize2, DrCollectionTool.size(result2));
		AssertJUnit.assertTrue(DrListTool.isSorted(result2));
//		logger.warn("finished incremental scan");
	}

	@Test
	public void testGet(){
		Iterable<SortedBean> iterable = sortedNode.scan(null, null);
		for(SortedBean sortedBeanFromScan : iterable){
			SortedBean sortedBeanFromGet = sortedNode.get(sortedBeanFromScan.getKey(), null);
			AssertJUnit.assertEquals(sortedBeanFromScan, sortedBeanFromGet);
		}
	}

	@Test
	public void testGetMulti(){
		Iterable<SortedBean> iterable = sortedNode.scan(null, null);
		Set<SortedBean> allBeans = Sets.newHashSet(iterable);
		AssertJUnit.assertEquals(SortedBeans.TOTAL_RECORDS, allBeans.size());
		List<SortedBean> getMultiResult = sortedNode.getMulti(DatabeanTool.getKeys(allBeans), null);
		AssertJUnit.assertEquals(SortedBeans.TOTAL_RECORDS, getMultiResult.size());
		for(SortedBean sortedBeanResult : getMultiResult){
			AssertJUnit.assertTrue(allBeans.contains(sortedBeanResult));
		}
	}

	@Test
	public void testFullScanKeys(){
		Iterable<SortedBeanKey> iterable = sortedNode.scanKeys(null, null);
		long numKeys = DrIterableTool.count(iterable);
		AssertJUnit.assertEquals(SortedBeans.TOTAL_RECORDS, numKeys);
	}

	@Test
	public void testFullScan(){
		Iterable<SortedBean> iterable = sortedNode.scan(null, null);
		long numDatabeans = DrIterableTool.count(iterable);
		AssertJUnit.assertEquals(SortedBeans.TOTAL_RECORDS, numDatabeans);
	}

	@Test
	protected void testLimitedScanKeys(){
		long count = scanKeysAndCountWithConfig(null);
		Assert.assertNotEquals(0, count);
		Assert.assertEquals(scanKeysAndCountWithConfig(new Config().setIterateBatchSize(10)), count);
		Assert.assertEquals(scanKeysAndCountWithConfig(new Config().setLimit((int)count)), count);
		Assert.assertEquals(scanKeysAndCountWithConfig(new Config().setLimit(10)), 10);
		Assert.assertEquals(scanKeysAndCountWithConfig(new Config().setLimit((int)(2*count))), count);
		Assert.assertEquals(scanKeysAndCountWithConfig(new Config().setIterateBatchSize(10).setLimit(100)), 100);
		Assert.assertEquals(scanKeysAndCountWithConfig(new Config().setIterateBatchSize(15).setLimit(23)), 23);
	}

	private long scanKeysAndCountWithConfig(Config config){
		return router.sortedBean().streamKeys(null, config).count();
	}

	@Test
	protected void testLimitedScan(){
		long count = scanAndCountWithConfig(null);
		Assert.assertNotEquals(0, count);
		Assert.assertEquals(scanAndCountWithConfig(new Config().setIterateBatchSize(10)), count);
		Assert.assertEquals(scanAndCountWithConfig(new Config().setLimit((int)count)), count);
		Assert.assertEquals(scanAndCountWithConfig(new Config().setLimit(10)), 10);
		Assert.assertEquals(scanAndCountWithConfig(new Config().setLimit((int)(2*count))), count);
		Assert.assertEquals(scanAndCountWithConfig(new Config().setIterateBatchSize(10).setLimit(100)), 100);
		Assert.assertEquals(scanAndCountWithConfig(new Config().setIterateBatchSize(15).setLimit(23)), 23);
	}

	private long scanAndCountWithConfig(Config config){
		return router.sortedBean().stream(null, config).count();
	}

	@Test
	public void testNullStartKeyScan(){
		Range<SortedBeanKey> range = new Range<>(null, false, null, true);
		Iterable<SortedBeanKey> iterable = sortedNode.scanKeys(range, null);
		long numDatabeans = DrIterableTool.count(iterable);
		AssertJUnit.assertEquals(SortedBeans.TOTAL_RECORDS, numDatabeans);
	}

	@Test
	public void testNullEndKeyScan(){
		Range<SortedBeanKey> range = new Range<>(null, true, null, true);
		Iterable<SortedBeanKey> iterable = sortedNode.scanKeys(range, null);
		long numDatabeans = DrIterableTool.count(iterable);
		AssertJUnit.assertEquals(SortedBeans.TOTAL_RECORDS, numDatabeans);
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
	public void testSortedStorageCountingTool(){
		Assert.assertEquals(SortedStorageCountingTool.count(sortedNode, Range.everything()).intValue(),
				SortedBeans.TOTAL_RECORDS);
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
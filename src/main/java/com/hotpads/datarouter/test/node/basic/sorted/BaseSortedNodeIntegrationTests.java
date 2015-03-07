package com.hotpads.datarouter.test.node.basic.sorted;

import java.util.List;
import java.util.Set;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import com.google.common.collect.Sets;
import com.google.inject.Injector;
import com.hotpads.datarouter.client.imp.hbase.node.HBaseSubEntityReaderNode;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.config.PutMethod;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage;
import com.hotpads.datarouter.routing.DatarouterContext;
import com.hotpads.datarouter.storage.key.KeyTool;
import com.hotpads.datarouter.test.TestDatarouterInjectorProvider;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrIterableTool;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.core.iterable.BatchingIterable;

public abstract class BaseSortedNodeIntegrationTests{
//	private static final Logger logger = LoggerFactory.getLogger(BaseSortedNodeIntegrationTests.class);

	/***************************** fields **************************************/

	protected static DatarouterContext drContext;
	protected static SortedNodeTestRouter router;
	protected static SortedMapStorage<SortedBeanKey,SortedBean> sortedNode;
	private static SortedBeanEntityNode entityNode;


	/***************************** setup/teardown **************************************/

	protected static void setup(String clientName, boolean useFielder, boolean entity){
		Injector injector = new TestDatarouterInjectorProvider().get();
		drContext = injector.getInstance(DatarouterContext.class);
		NodeFactory nodeFactory = injector.getInstance(NodeFactory.class);
		router = new SortedNodeTestRouter(drContext, nodeFactory, clientName, useFielder, entity);
		sortedNode = router.sortedBean();
		entityNode = router.sortedBeanEntity();

		resetTable(true);
	}

	protected static void resetTable(boolean force){
		long numExistingDatabeans = DrIterableTool.count(sortedNode.scan(null, null));
		if(!force && SortedBeans.TOTAL_RECORDS == numExistingDatabeans){ return; }

		sortedNode.deleteAll(null);
		List<SortedBean> remainingAfterDelete = DrListTool.createArrayList(sortedNode.scan(null, null));
		AssertJUnit.assertEquals(0, DrCollectionTool.size(remainingAfterDelete));

		List<SortedBean> allBeans = SortedBeans.generatedSortedBeans();
		for(List<SortedBean> batch : new BatchingIterable<SortedBean>(allBeans, 1000)){
			sortedNode.putMulti(batch, new Config().setPutMethod(PutMethod.INSERT_OR_BUST));
		}

		List<SortedBean> roundTripped = DrListTool.createArrayList(sortedNode.scan(null, null));
		AssertJUnit.assertEquals(SortedBeans.TOTAL_RECORDS, roundTripped.size());
	}

	@Test
	protected static void testSortedDelete(){
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
		List<SortedBeanKey> keys = DrListTool.create(
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
	public void testGetEntity(){
		if(!isHBaseEntity()){ return; }
		SortedBeanEntityKey ek1 = new SortedBeanEntityKey(SortedBeans.S_albatross, SortedBeans.S_ostrich);
		SortedBeanEntity albatrossOstrich = entityNode.getEntity(ek1, null);
		int numExpected = SortedBeans.NUM_ELEMENTS * SortedBeans.NUM_ELEMENTS;
		AssertJUnit.assertEquals(numExpected, albatrossOstrich.getSortedBeans().size());
		AssertJUnit.assertEquals(SortedBeans.S_albatross, DrCollectionTool.getFirst(albatrossOstrich.getSortedBeans()).getA());
		AssertJUnit.assertEquals(SortedBeans.S_ostrich, DrCollectionTool.getFirst(albatrossOstrich.getSortedBeans()).getB());
	}

	@Test
	public void testGetAll(){
		List<SortedBean> allBeans = DrListTool.createArrayList(sortedNode.scan(null, null));
		AssertJUnit.assertEquals(SortedBeans.TOTAL_RECORDS, DrCollectionTool.size(allBeans));
	}

	@Test
	public void testGetFirstKey(){
		SortedBeanKey firstKey = sortedNode.getFirstKey(null);
		AssertJUnit.assertEquals(SortedBeans.STRINGS.first(), firstKey.getA());
		AssertJUnit.assertEquals(SortedBeans.STRINGS.first(), firstKey.getB());
		AssertJUnit.assertEquals(new Integer(0), firstKey.getC());
		AssertJUnit.assertEquals(SortedBeans.STRINGS.first(), firstKey.getD());
	}

	@Test
	public void testGetFirst(){
		SortedBean firstBean = sortedNode.getFirst(null);
		AssertJUnit.assertEquals(SortedBeans.STRINGS.first(), firstBean.getKey().getA());
		AssertJUnit.assertEquals(SortedBeans.STRINGS.first(), firstBean.getKey().getB());
		AssertJUnit.assertEquals(new Integer(0), firstBean.getKey().getC());
		AssertJUnit.assertEquals(SortedBeans.STRINGS.first(), firstBean.getKey().getD());
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
		SortedBeanKey alp1 = new SortedBeanKey(SortedBeans.RANGE_alp, null, null, null);
		SortedBeanKey emu1 = new SortedBeanKey(SortedBeans.RANGE_emu, null, null, null);
		Range<SortedBeanKey> range1 = new Range<>(alp1, true, emu1, true);
		List<SortedBeanKey> result1 = DrListTool.createArrayList(sortedNode.scanKeys(range1, null));
		int expectedSize1 = SortedBeans.RANGE_LENGTH_alp_emu_inc * SortedBeans.NUM_ELEMENTS * SortedBeans.NUM_ELEMENTS 
				* SortedBeans.NUM_ELEMENTS;
		AssertJUnit.assertEquals(expectedSize1, DrCollectionTool.size(result1));
		AssertJUnit.assertTrue(DrListTool.isSorted(result1));

		Range<SortedBeanKey> range1b = new Range<>(alp1, true, emu1, false);
		List<SortedBeanKey> result1b = DrListTool.createArrayList(sortedNode.scanKeys(range1b, null));
		int expectedSize1b = (SortedBeans.RANGE_LENGTH_alp_emu_inc - 1) * SortedBeans.NUM_ELEMENTS 
				* SortedBeans.NUM_ELEMENTS * SortedBeans.NUM_ELEMENTS;
		AssertJUnit.assertEquals(expectedSize1b, DrCollectionTool.size(result1b));
		AssertJUnit.assertTrue(DrListTool.isSorted(result1b));

		SortedBeanKey alp2 = new SortedBeanKey(SortedBeans.STRINGS.first(), SortedBeans.RANGE_alp, null, null);
		SortedBeanKey emu2 = new SortedBeanKey(SortedBeans.STRINGS.first(), SortedBeans.RANGE_emu, null, null);
		Range<SortedBeanKey> range2 = new Range<>(alp2, true, emu2, true);
		List<SortedBeanKey> result2 = DrListTool.createArrayList(sortedNode.scanKeys(range2, null));
		int expectedSize2 = SortedBeans.RANGE_LENGTH_alp_emu_inc * SortedBeans.NUM_ELEMENTS * SortedBeans.NUM_ELEMENTS;
		AssertJUnit.assertEquals(expectedSize2, DrCollectionTool.size(result2));
		AssertJUnit.assertTrue(DrListTool.isSorted(result2));
	}

	@Test
	public void testGetInRange(){
		SortedBeanKey alp1 = new SortedBeanKey(SortedBeans.RANGE_alp, null, null, null);
		SortedBeanKey emu1 = new SortedBeanKey(SortedBeans.RANGE_emu, null, null, null);
		Range<SortedBeanKey> range1 = new Range<>(alp1, true, emu1, true);
		List<SortedBean> result1 = DrListTool.createArrayList(sortedNode.scan(range1, null));
		int expectedSize1 = SortedBeans.RANGE_LENGTH_alp_emu_inc * SortedBeans.NUM_ELEMENTS * SortedBeans.NUM_ELEMENTS 
				* SortedBeans.NUM_ELEMENTS;
		AssertJUnit.assertEquals(expectedSize1, DrCollectionTool.size(result1));
		AssertJUnit.assertTrue(DrListTool.isSorted(result1));
		
		Range<SortedBeanKey> range1b = new Range<>(alp1, true, emu1, false);
		List<SortedBean> result1b = DrListTool.createArrayList(sortedNode.scan(range1b, null));
		int expectedSize1b = (SortedBeans.RANGE_LENGTH_alp_emu_inc-1) * SortedBeans.NUM_ELEMENTS 
				* SortedBeans.NUM_ELEMENTS * SortedBeans.NUM_ELEMENTS;
		AssertJUnit.assertEquals(expectedSize1b, DrCollectionTool.size(result1b));
		AssertJUnit.assertTrue(DrListTool.isSorted(result1b));

		SortedBeanKey alp2 = new SortedBeanKey(SortedBeans.STRINGS.first(), SortedBeans.RANGE_alp, null, null);
		SortedBeanKey emu2 = new SortedBeanKey(SortedBeans.STRINGS.first(), SortedBeans.RANGE_emu, null, null);
		Range<SortedBeanKey> range2 = new Range<>(alp2, true, emu2, true);
		List<SortedBean> result2 = DrListTool.createArrayList(sortedNode.scan(range2, null));
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
		List<SortedBeanKey> result1 = DrListTool.createArrayList(sortedNode.scanKeys(Range.create(alp1, true, emu1, true),
				smallIterateBatchSize));
		AssertJUnit.assertEquals(expectedSize1, DrCollectionTool.size(result1));
		AssertJUnit.assertTrue(DrListTool.isSorted(result1));

		int expectedSize1b = (SortedBeans.RANGE_LENGTH_alp_emu_inc - 1) * SortedBeans.NUM_ELEMENTS
				* SortedBeans.NUM_ELEMENTS * SortedBeans.NUM_ELEMENTS;
//		logger.warn("expecting "+expectedSize1b);
		List<SortedBeanKey> result1b = DrListTool.createArrayList(sortedNode.scanKeys(Range.create(alp1, true, emu1, false),
				smallIterateBatchSize));
		AssertJUnit.assertEquals(expectedSize1b, DrCollectionTool.size(result1b));
		AssertJUnit.assertTrue(DrListTool.isSorted(result1b));

		int expectedSize2 = SortedBeans.RANGE_LENGTH_alp_emu_inc * SortedBeans.NUM_ELEMENTS * SortedBeans.NUM_ELEMENTS;
//		logger.warn("expecting "+expectedSize2);
		SortedBeanKey alp2 = new SortedBeanKey(SortedBeans.STRINGS.first(), SortedBeans.RANGE_alp, null, null);
		SortedBeanKey emu2 = new SortedBeanKey(SortedBeans.STRINGS.first(), SortedBeans.RANGE_emu, null, null);
		List<SortedBeanKey> result2 = DrListTool.createArrayList(sortedNode.scanKeys(Range.create(alp2, true, emu2, true),
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
		List<SortedBean> getMultiResult = sortedNode.getMulti(KeyTool.getKeys(allBeans), null);
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


	/************************* helper ****************************/

	public boolean isHBaseEntity(){
		return sortedNode instanceof HBaseSubEntityReaderNode;
	}
}





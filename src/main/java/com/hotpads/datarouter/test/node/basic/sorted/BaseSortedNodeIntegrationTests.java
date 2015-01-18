package com.hotpads.datarouter.test.node.basic.sorted;

import java.util.List;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Sets;
import com.google.inject.Injector;
import com.hotpads.datarouter.client.imp.hbase.node.HBaseSubEntityReaderNode;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.config.PutMethod;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage;
import com.hotpads.datarouter.routing.DatarouterContext;
import com.hotpads.datarouter.storage.key.KeyTool;
import com.hotpads.datarouter.test.DatarouterTestInjectorProvider;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.core.iterable.BatchingIterable;

public abstract class BaseSortedNodeIntegrationTests{
//	private static final Logger logger = LoggerFactory.getLogger(BaseSortedNodeIntegrationTests.class);
	
	/***************************** fields **************************************/
	
	private static DatarouterContext drContext;
	private static SortedMapStorage<SortedBeanKey,SortedBean> node;
	private static SortedBeanEntityNode entityNode;

	
	/***************************** setup/teardown **************************************/
	
	@AfterClass
	public static void afterClass(){
		testDelete();
		drContext.shutdown();
	}
	
	protected static void setup(String clientName, boolean useFielder, boolean entity, String tableSuffix){
		Injector injector = new DatarouterTestInjectorProvider().get();
		drContext = injector.getInstance(DatarouterContext.class);
		NodeFactory nodeFactory = injector.getInstance(NodeFactory.class);
		SortedNodeTestRouter router = new SortedNodeTestRouter(drContext, nodeFactory, clientName, useFielder, entity,
				tableSuffix);
		node = router.sortedBean();
		entityNode = router.sortedBeanEntity();

		resetTable(true);
	}

	private static void resetTable(boolean force){
		long numExistingDatabeans = IterableTool.count(node.scan(null, null));
		if(!force && SortedBeans.TOTAL_RECORDS == numExistingDatabeans){ return; }
		
		node.deleteAll(null);
		List<SortedBean> remainingAfterDelete = ListTool.createArrayList(node.scan(null, null));
		Assert.assertEquals(0, CollectionTool.size(remainingAfterDelete));
		
		List<SortedBean> allBeans = SortedBeans.generatedSortedBeans();
		for(List<SortedBean> batch : new BatchingIterable<SortedBean>(allBeans, 1000)){
			node.putMulti(batch, new Config().setPutMethod(PutMethod.INSERT_OR_BUST));
		}
		
		List<SortedBean> roundTripped = ListTool.createArrayList(node.scan(null, null));
		Assert.assertEquals(SortedBeans.TOTAL_RECORDS, roundTripped.size());
	}
	
	
	/********************** junit methods *********************************************/
	
	@Test
	public void testGetKeys(){
		SortedBeanKey key1 = new SortedBeanKey(SortedBeans.S_aardvark, SortedBeans.S_aardvark, 0, SortedBeans.S_alpaca);
		SortedBeanKey key2 = new SortedBeanKey("blah", "blah", 1000, "blah");
		SortedBeanKey key3 = new SortedBeanKey(SortedBeans.S_aardvark, SortedBeans.S_albatross, 2, SortedBeans.S_emu);
		List<SortedBeanKey> keysToGet = ListTool.create(key1, key2, key3);
		List<SortedBeanKey> keysGotten = node.getKeys(keysToGet, null);
		Assert.assertTrue(keysGotten.contains(key1));
		Assert.assertFalse(keysGotten.contains(key2));
		Assert.assertTrue(keysGotten.contains(key3));
	}
	
	@Test
	public void testGetEntity(){
		if(!isHBaseEntity()){ return; }
		SortedBeanEntityKey ek1 = new SortedBeanEntityKey(SortedBeans.S_albatross, SortedBeans.S_ostrich);
		SortedBeanEntity albatrossOstrich = entityNode.getEntity(ek1, null);
		int numExpected = SortedBeans.NUM_ELEMENTS * SortedBeans.NUM_ELEMENTS;
		Assert.assertEquals(numExpected, albatrossOstrich.getSortedBeans().size());
		Assert.assertEquals(SortedBeans.S_albatross, CollectionTool.getFirst(albatrossOstrich.getSortedBeans()).getA());
		Assert.assertEquals(SortedBeans.S_ostrich, CollectionTool.getFirst(albatrossOstrich.getSortedBeans()).getB());
	}
	
	@Test
	public void testGetAll(){
		List<SortedBean> allBeans = ListTool.createArrayList(node.scan(null, null));
		Assert.assertEquals(SortedBeans.TOTAL_RECORDS, CollectionTool.size(allBeans));
	}
	
	@Test
	public void testGetFirstKey(){
		SortedBeanKey firstKey = node.getFirstKey(null);
		Assert.assertEquals(SortedBeans.STRINGS.first(), firstKey.getA());
		Assert.assertEquals(SortedBeans.STRINGS.first(), firstKey.getB());
		Assert.assertEquals(new Integer(0), firstKey.getC());
		Assert.assertEquals(SortedBeans.STRINGS.first(), firstKey.getD());
	}
	
	@Test
	public void testGetFirst(){
		SortedBean firstBean = node.getFirst(null);
		Assert.assertEquals(SortedBeans.STRINGS.first(), firstBean.getKey().getA());
		Assert.assertEquals(SortedBeans.STRINGS.first(), firstBean.getKey().getB());
		Assert.assertEquals(new Integer(0), firstBean.getKey().getC());
		Assert.assertEquals(SortedBeans.STRINGS.first(), firstBean.getKey().getD());
	}
	
	@Test
	public void testGetWithPrefix(){
		//first 3 fields fixed
		SortedBeanKey prefix1 = new SortedBeanKey(SortedBeans.STRINGS.first(), SortedBeans.STRINGS.last(), 2, null);
		List<SortedBean> result1 = node.getWithPrefix(prefix1, false, null);
		Assert.assertEquals(SortedBeans.NUM_ELEMENTS, CollectionTool.size(result1));
		Assert.assertTrue(ListTool.isSorted(result1));

		//first 3 fields fixed, last field wildcard
		SortedBeanKey prefix2 = new SortedBeanKey(SortedBeans.STRINGS.first(), SortedBeans.STRINGS.last(), 2, 
				SortedBeans.PREFIX_a);
		List<SortedBean> result2 = node.getWithPrefix(prefix2, true, null);
		Assert.assertEquals(SortedBeans.NUM_PREFIX_a, CollectionTool.size(result2));
		Assert.assertTrue(ListTool.isSorted(result2));

		//first field fixed, second field wildcard
		SortedBeanKey prefix3 = new SortedBeanKey(SortedBeans.STRINGS.first(), SortedBeans.PREFIX_a, null, null);
		List<SortedBean> result3 = node.getWithPrefix(prefix3, true, null);
		int expectedSize3 = SortedBeans.NUM_PREFIX_a * SortedBeans.NUM_ELEMENTS * SortedBeans.NUM_ELEMENTS;
		Assert.assertEquals(expectedSize3, CollectionTool.size(result3));
		Assert.assertTrue(ListTool.isSorted(result3));
	}
	
	@Test
	public void testGetWithPrefixes(){
		SortedBeanKey prefixA = new SortedBeanKey(SortedBeans.STRINGS.first(), SortedBeans.PREFIX_a, null, null);
		SortedBeanKey prefixCh = new SortedBeanKey(SortedBeans.STRINGS.first(), SortedBeans.PREFIX_ch, null, null);
		List<SortedBeanKey> prefixes = ListTool.create(prefixA, prefixCh);
		List<SortedBean> result = node.getWithPrefixes(prefixes, true, null);
		int expectedSizeA = SortedBeans.NUM_PREFIX_a * SortedBeans.NUM_ELEMENTS * SortedBeans.NUM_ELEMENTS;
		int expectedSizeCh = SortedBeans.NUM_PREFIX_ch * SortedBeans.NUM_ELEMENTS * SortedBeans.NUM_ELEMENTS;
		int expectedSizeTotal = expectedSizeA + expectedSizeCh;
		Assert.assertEquals(expectedSizeTotal, CollectionTool.size(result));
		Assert.assertTrue(ListTool.isSorted(result));
	}
	
	@Test
	public void testGetKeysInRange(){
		SortedBeanKey alp1 = new SortedBeanKey(SortedBeans.RANGE_alp, null, null, null);
		SortedBeanKey emu1 = new SortedBeanKey(SortedBeans.RANGE_emu, null, null, null);
		List<SortedBeanKey> result1 = node.getKeysInRange(alp1, true, emu1, true, null);
		int expectedSize1 = SortedBeans.RANGE_LENGTH_alp_emu_inc * SortedBeans.NUM_ELEMENTS * SortedBeans.NUM_ELEMENTS 
				* SortedBeans.NUM_ELEMENTS;
		Assert.assertEquals(expectedSize1, CollectionTool.size(result1));
		Assert.assertTrue(ListTool.isSorted(result1));
		
		List<SortedBeanKey> result1b = node.getKeysInRange(alp1, true, emu1, false, null);
		int expectedSize1b = (SortedBeans.RANGE_LENGTH_alp_emu_inc - 1) * SortedBeans.NUM_ELEMENTS 
				* SortedBeans.NUM_ELEMENTS * SortedBeans.NUM_ELEMENTS;
		Assert.assertEquals(expectedSize1b, CollectionTool.size(result1b));
		Assert.assertTrue(ListTool.isSorted(result1b));
		
		SortedBeanKey alp2 = new SortedBeanKey(SortedBeans.STRINGS.first(), SortedBeans.RANGE_alp, null, null);
		SortedBeanKey emu2 = new SortedBeanKey(SortedBeans.STRINGS.first(), SortedBeans.RANGE_emu, null, null);
		List<SortedBeanKey> result2 = node.getKeysInRange(alp2, true, emu2, true, null);
		int expectedSize2 = SortedBeans.RANGE_LENGTH_alp_emu_inc * SortedBeans.NUM_ELEMENTS * SortedBeans.NUM_ELEMENTS;
		Assert.assertEquals(expectedSize2, CollectionTool.size(result2));
		Assert.assertTrue(ListTool.isSorted(result2));
	}
	
	@Test
	public void testGetInRange(){
		SortedBeanKey alp1 = new SortedBeanKey(SortedBeans.RANGE_alp, null, null, null);
		SortedBeanKey emu1 = new SortedBeanKey(SortedBeans.RANGE_emu, null, null, null);
		List<SortedBean> result1 = node.getRange(alp1, true, emu1, true, null);
		int expectedSize1 = SortedBeans.RANGE_LENGTH_alp_emu_inc * SortedBeans.NUM_ELEMENTS * SortedBeans.NUM_ELEMENTS 
				* SortedBeans.NUM_ELEMENTS;
		Assert.assertEquals(expectedSize1, CollectionTool.size(result1));
		Assert.assertTrue(ListTool.isSorted(result1));
		
		List<SortedBean> result1b = node.getRange(alp1, true, emu1, false, null);
		int expectedSize1b = (SortedBeans.RANGE_LENGTH_alp_emu_inc-1) * SortedBeans.NUM_ELEMENTS 
				* SortedBeans.NUM_ELEMENTS * SortedBeans.NUM_ELEMENTS;
		Assert.assertEquals(expectedSize1b, CollectionTool.size(result1b));
		Assert.assertTrue(ListTool.isSorted(result1b));
		
		SortedBeanKey alp2 = new SortedBeanKey(SortedBeans.STRINGS.first(), SortedBeans.RANGE_alp, null, null);
		SortedBeanKey emu2 = new SortedBeanKey(SortedBeans.STRINGS.first(), SortedBeans.RANGE_emu, null, null);
		List<SortedBean> result2 = node.getRange(alp2, true, emu2, true, null);
		int expectedSize2 = SortedBeans.RANGE_LENGTH_alp_emu_inc * SortedBeans.NUM_ELEMENTS * SortedBeans.NUM_ELEMENTS;
		Assert.assertEquals(expectedSize2, CollectionTool.size(result2));
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
		List<SortedBeanKey> result1 = ListTool.createArrayList(node.scanKeys(Range.create(alp1, true, emu1, true), 
				smallIterateBatchSize));
		Assert.assertEquals(expectedSize1, CollectionTool.size(result1));
		Assert.assertTrue(ListTool.isSorted(result1));

		int expectedSize1b = (SortedBeans.RANGE_LENGTH_alp_emu_inc - 1) * SortedBeans.NUM_ELEMENTS 
				* SortedBeans.NUM_ELEMENTS * SortedBeans.NUM_ELEMENTS;
//		logger.warn("expecting "+expectedSize1b);
		List<SortedBeanKey> result1b = ListTool.createArrayList(node.scanKeys(Range.create(alp1, true, emu1, false), 
				smallIterateBatchSize));
		Assert.assertEquals(expectedSize1b, CollectionTool.size(result1b));
		Assert.assertTrue(ListTool.isSorted(result1b));

		int expectedSize2 = SortedBeans.RANGE_LENGTH_alp_emu_inc * SortedBeans.NUM_ELEMENTS * SortedBeans.NUM_ELEMENTS;
//		logger.warn("expecting "+expectedSize2);
		SortedBeanKey alp2 = new SortedBeanKey(SortedBeans.STRINGS.first(), SortedBeans.RANGE_alp, null, null);
		SortedBeanKey emu2 = new SortedBeanKey(SortedBeans.STRINGS.first(), SortedBeans.RANGE_emu, null, null);
		List<SortedBeanKey> result2 = ListTool.createArrayList(node.scanKeys(Range.create(alp2, true, emu2, true), 
				smallIterateBatchSize));
		Assert.assertEquals(expectedSize2, CollectionTool.size(result2));
		Assert.assertTrue(ListTool.isSorted(result2));
//		logger.warn("finished incremental scan");
	}
	
	@Test
	public void testPrefixedRange(){
		if(isHBaseEntity()){ return; }//not implemented
		SortedBeanKey prefix = new SortedBeanKey(SortedBeans.PREFIX_a, null, null, null);
		SortedBeanKey al = new SortedBeanKey(SortedBeans.RANGE_al, null, null, null);
		List<SortedBean> result1 = node.getPrefixedRange(prefix, true, al, true, null);
		int expectedSize1 = SortedBeans.RANGE_LENGTH_al_b * SortedBeans.NUM_ELEMENTS * SortedBeans.NUM_ELEMENTS 
				* SortedBeans.NUM_ELEMENTS;
		Assert.assertEquals(expectedSize1, CollectionTool.size(result1));
		Assert.assertTrue(ListTool.isSorted(result1));
	}
	
	@Test
	public void testGet(){
		Iterable<SortedBean> iterable = node.scan(null, null);
		for(SortedBean sortedBeanFromScan : iterable){
			SortedBean sortedBeanFromGet = node.get(sortedBeanFromScan.getKey(), null);
			Assert.assertEquals(sortedBeanFromScan, sortedBeanFromGet);
		}
	}
	
	@Test
	public void testGetMulti(){
		Iterable<SortedBean> iterable = node.scan(null, null);
		Set<SortedBean> allBeans = Sets.newHashSet(iterable);
		Assert.assertEquals(SortedBeans.TOTAL_RECORDS, allBeans.size());
		List<SortedBean> getMultiResult = node.getMulti(KeyTool.getKeys(allBeans), null);
		Assert.assertEquals(SortedBeans.TOTAL_RECORDS, getMultiResult.size());
		for(SortedBean sortedBeanResult : getMultiResult){
			Assert.assertTrue(allBeans.contains(sortedBeanResult));
		}
	}
	
	@Test
	public void testFullScanKeys(){
		Iterable<SortedBeanKey> iterable = node.scanKeys(null, null);
		long numKeys = IterableTool.count(iterable);
		Assert.assertEquals(SortedBeans.TOTAL_RECORDS, numKeys);
	}
	
	@Test
	public void testFullScan(){
		Iterable<SortedBean> iterable = node.scan(null, null);
		long numDatabeans = IterableTool.count(iterable);
		Assert.assertEquals(SortedBeans.TOTAL_RECORDS, numDatabeans);
	}

//	@Test
	public static void testDelete(){
		int remainingElements = SortedBeans.TOTAL_RECORDS;
		
		//delete
		Assert.assertEquals(remainingElements, IterableTool.count(node.scan(null, null)).intValue());
		SortedBeanKey key = new SortedBeanKey(SortedBeans.STRINGS.last(), SortedBeans.STRINGS.last(), 0, 
				SortedBeans.STRINGS.last());
		node.delete(key, null);
		--remainingElements;
		Assert.assertEquals(remainingElements, IterableTool.count(node.scan(null, null)).intValue());

		//deleteMulti
		Assert.assertEquals(remainingElements, IterableTool.count(node.scan(null, null)).intValue());
		List<SortedBeanKey> keys = ListTool.create(
				new SortedBeanKey(SortedBeans.STRINGS.last(), SortedBeans.STRINGS.last(), 1, SortedBeans.STRINGS.last()),
				new SortedBeanKey(SortedBeans.STRINGS.last(), SortedBeans.STRINGS.last(), 2, SortedBeans.STRINGS.last()),
				new SortedBeanKey(SortedBeans.STRINGS.last(), SortedBeans.STRINGS.last(), 3, SortedBeans.STRINGS.last()));
		node.deleteMulti(keys, null);
		remainingElements -= 3;
		Assert.assertEquals(remainingElements, IterableTool.count(node.scan(null, null)).intValue());
		
		
		//deleteWithPrefix
		Assert.assertEquals(remainingElements, IterableTool.count(node.scan(null, null)).intValue());
		SortedBeanKey prefix = new SortedBeanKey(SortedBeans.PREFIX_a, null, null, null);
		node.deleteRangeWithPrefix(prefix, true, null);
		remainingElements -= SortedBeans.NUM_PREFIX_a * SortedBeans.NUM_ELEMENTS * SortedBeans.NUM_ELEMENTS 
				* SortedBeans.NUM_ELEMENTS;
		Assert.assertEquals(remainingElements, IterableTool.count(node.scan(null, null)).intValue());

		resetTable(false);//leave the table full
	}
	
	

	/************************* helper ****************************/
	
	public boolean isHBaseEntity(){
		return node instanceof HBaseSubEntityReaderNode;
	}
}





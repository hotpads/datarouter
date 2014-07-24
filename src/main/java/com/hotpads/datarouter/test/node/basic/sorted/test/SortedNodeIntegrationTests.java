package com.hotpads.datarouter.test.node.basic.sorted.test;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.collect.Sets;
import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.client.imp.hbase.HBaseClientType;
import com.hotpads.datarouter.client.imp.hbase.node.HBaseSubEntityReaderNode;
import com.hotpads.datarouter.client.imp.hibernate.HibernateClientType;
import com.hotpads.datarouter.client.imp.jdbc.JdbcClientType;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.config.PutMethod;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage;
import com.hotpads.datarouter.storage.key.KeyTool;
import com.hotpads.datarouter.test.DRTestConstants;
import com.hotpads.datarouter.test.node.basic.BasicNodeTestRouter.SortedBasicNodeTestRouter;
import com.hotpads.datarouter.test.node.basic.sorted.SortedBean;
import com.hotpads.datarouter.test.node.basic.sorted.SortedBeanEntity;
import com.hotpads.datarouter.test.node.basic.sorted.SortedBeanEntityKey;
import com.hotpads.datarouter.test.node.basic.sorted.SortedBeanEntityNode;
import com.hotpads.datarouter.test.node.basic.sorted.SortedBeanKey;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.SetTool;
import com.hotpads.util.core.collections.Range;
import com.hotpads.util.core.iterable.BatchingIterable;

@RunWith(Parameterized.class)
public class SortedNodeIntegrationTests{
	private static Logger logger = Logger.getLogger(SortedNodeIntegrationTests.class);
	
	
	/***************************** fields **************************************/
	
	private SortedBasicNodeTestRouter router;
	private SortedMapStorage<SortedBeanKey,SortedBean> node;
	private SortedBeanEntityNode entityNode;

	
	/***************************** construct **************************************/

	@Parameters
	public static Collection<Object[]> parameters(){
		List<Object[]> params = ListTool.create();
//		params.add(new Object[]{DRTestConstants.CLIENT_drTestHibernate0, HibernateClientType.INSTANCE, false});
//		params.add(new Object[]{DRTestConstants.CLIENT_drTestJdbc0, JdbcClientType.INSTANCE, false});
//		params.add(new Object[]{DRTestConstants.CLIENT_drTestHBase, HBaseClientType.INSTANCE, false});
		params.add(new Object[]{DRTestConstants.CLIENT_drTestHBase, HBaseClientType.INSTANCE, true});
		return params;
	}
	
	
	public SortedNodeIntegrationTests(String clientName, ClientType clientType, boolean entity){
		this.router = new SortedBasicNodeTestRouter(clientName, getClass(), entity);
		this.node = router.sortedBeanSorted();
		this.entityNode = router.sortedBeanEntity();
		resetTable();
	}
	

	public void resetTable(){
		node.deleteAll(null);
		List<SortedBean> remainingAfterDelete = ListTool.createArrayList(node.scan(null, null));
		Assert.assertEquals(0, CollectionTool.size(remainingAfterDelete));
		
		List<SortedBean> allBeans = generatedSortedBeans();
		for(List<SortedBean> batch : new BatchingIterable<SortedBean>(allBeans, 1000)){
			node.putMulti(batch, new Config().setPutMethod(PutMethod.INSERT_OR_BUST));
		}
		
		List<SortedBean> roundTripped = ListTool.createArrayList(node.scan(null, null));
		Assert.assertEquals(TOTAL_RECORDS, roundTripped.size());
	}
	
	
	/****************************** testing vars ***********************************/
	
	public static final String 
			S_aardvark = "aardvark",
			S_albatross = "albatross",
			S_alpaca = "alpaca",
			S_chinchilla = "chinchilla",
			S_emu = "emu",
			S_gopher = "gopher",
			S_ostrich = "ostrich",
			S_pelican = "pelican";
	
	public static final SortedSet<String> STRINGS = SetTool.createTreeSet(
			S_aardvark,
			S_albatross,
			S_alpaca,
			S_chinchilla,
			S_emu,
			S_gopher,
			S_ostrich,
			S_pelican);

	public static final String PREFIX_a = "a";
	public static final int NUM_PREFIX_a = 3;

	public static final String PREFIX_ch = "ch";
	public static final int NUM_PREFIX_ch = 1;

	public static final	String 
			RANGE_al = "al",
			RANGE_alp = "alp",
			RANGE_emu = "emu";
	
	public static final int 
			RANGE_LENGTH_alp = 6,
			RANGE_LENGTH_al_b = 2,
			RANGE_LENGTH_alp_emu_inc = 3,//exclude things that begin with emu without the other 3 key fields
			RANGE_LENGTH_emu = 4;
	
	public static final int NUM_ELEMENTS = STRINGS.size();
	public static final List<Integer> INTEGERS = ListTool.createArrayList(NUM_ELEMENTS);
	static{
		for(int i=0; i < NUM_ELEMENTS; ++i){
			INTEGERS.add(i);
		}
	}
	
	public static final int TOTAL_RECORDS = NUM_ELEMENTS*NUM_ELEMENTS*NUM_ELEMENTS*NUM_ELEMENTS;
	
	public static List<SortedBean> generatedSortedBeans(){
		List<String> as = ListTool.createArrayList(STRINGS);
		List<String> bs = ListTool.createArrayList(STRINGS);
		List<Integer> cs = ListTool.createArrayList(INTEGERS);
		List<String> ds = ListTool.createArrayList(STRINGS);
		//shuffle them for fun.  they should end up sorted in the table
		Collections.shuffle(as);
		Collections.shuffle(bs);
		Collections.shuffle(cs);
		Collections.shuffle(ds);
		
		List<SortedBean> beans = ListTool.createArrayList();//save in periodic batches
		for(int a=0; a < NUM_ELEMENTS; ++a){
			for(int b=0; b < NUM_ELEMENTS; ++b){
				for(int c=0; c < NUM_ELEMENTS; ++c){
					for(int d=0; d < NUM_ELEMENTS; ++d){
						SortedBean bean = new SortedBean(
								as.get(a), bs.get(b), cs.get(c), ds.get(d), 
								"string so hbase has at least one field", null, null, null);
						beans.add(bean);
					}
				}
			}
		}
		return beans;
	}
	
	/********************** junit methods *********************************************/
	
	@Test
	public synchronized void testGetEntity(){
		if(!isHBaseEntity()){ return; }
		SortedBeanEntityKey ek1 = new SortedBeanEntityKey(S_albatross, S_ostrich);
		SortedBeanEntity albatrossOstrich = entityNode.getEntity(ek1, null);
		int numExpected = NUM_ELEMENTS * NUM_ELEMENTS;
		Assert.assertEquals(numExpected, albatrossOstrich.getSortedBeans().size());
		Assert.assertEquals(S_albatross, CollectionTool.getFirst(albatrossOstrich.getSortedBeans()).getA());
		Assert.assertEquals(S_ostrich, CollectionTool.getFirst(albatrossOstrich.getSortedBeans()).getB());
	}
	
	@Test
	public synchronized void testGetAll(){
		List<SortedBean> allBeans = ListTool.createArrayList(node.scan(null, null));
		Assert.assertEquals(TOTAL_RECORDS, CollectionTool.size(allBeans));
	}
	
	@Test
	public synchronized void testGetFirstKey(){
		SortedBeanKey firstKey = node.getFirstKey(null);
		Assert.assertEquals(STRINGS.first(), firstKey.getA());
		Assert.assertEquals(STRINGS.first(), firstKey.getB());
		Assert.assertEquals(new Integer(0), firstKey.getC());
		Assert.assertEquals(STRINGS.first(), firstKey.getD());
	}
	
	@Test
	public synchronized void testGetFirst(){
		SortedBean firstBean = node.getFirst(null);
		Assert.assertEquals(STRINGS.first(), firstBean.getKey().getA());
		Assert.assertEquals(STRINGS.first(), firstBean.getKey().getB());
		Assert.assertEquals(new Integer(0), firstBean.getKey().getC());
		Assert.assertEquals(STRINGS.first(), firstBean.getKey().getD());
	}
	
	@Test
	public synchronized void testGetWithPrefix(){
		//first 3 fields fixed
		SortedBeanKey prefix1 = new SortedBeanKey(STRINGS.first(), STRINGS.last(), 2, null);
		List<SortedBean> result1 = node.getWithPrefix(prefix1, false, null);
		Assert.assertEquals(NUM_ELEMENTS, CollectionTool.size(result1));
		Assert.assertTrue(ListTool.isSorted(result1));

		//first 3 fields fixed, last field wildcard
		SortedBeanKey prefix2 = new SortedBeanKey(STRINGS.first(), STRINGS.last(), 2, PREFIX_a);
		List<SortedBean> result2 = node.getWithPrefix(prefix2, true, null);
		Assert.assertEquals(NUM_PREFIX_a, CollectionTool.size(result2));
		Assert.assertTrue(ListTool.isSorted(result2));

		//first field fixed, second field wildcard
		SortedBeanKey prefix3 = new SortedBeanKey(STRINGS.first(), PREFIX_a, null, null);
		List<SortedBean> result3 = node.getWithPrefix(prefix3, true, null);
		int expectedSize3 = NUM_PREFIX_a * NUM_ELEMENTS * NUM_ELEMENTS;
		Assert.assertEquals(expectedSize3, CollectionTool.size(result3));
		Assert.assertTrue(ListTool.isSorted(result3));
	}
	
	@Test
	public synchronized void testGetWithPrefixes(){
		SortedBeanKey prefixA = new SortedBeanKey(STRINGS.first(), PREFIX_a, null, null);
		SortedBeanKey prefixCh = new SortedBeanKey(STRINGS.first(), PREFIX_ch, null, null);
		List<SortedBeanKey> prefixes = ListTool.create(prefixA, prefixCh);
		List<SortedBean> result = node.getWithPrefixes(prefixes, true, null);
		int expectedSizeA = NUM_PREFIX_a * NUM_ELEMENTS * NUM_ELEMENTS;
		int expectedSizeCh = NUM_PREFIX_ch * NUM_ELEMENTS * NUM_ELEMENTS;
		int expectedSizeTotal = expectedSizeA + expectedSizeCh;
		Assert.assertEquals(expectedSizeTotal, CollectionTool.size(result));
		Assert.assertTrue(ListTool.isSorted(result));
	}
	
	@Test
	public synchronized void testGetKeysInRange(){
		SortedBeanKey alp1 = new SortedBeanKey(RANGE_alp, null, null, null);
		SortedBeanKey emu1 = new SortedBeanKey(RANGE_emu, null, null, null);
		List<SortedBeanKey> result1 = node.getKeysInRange(alp1, true, emu1, true, null);
		int expectedSize1 = RANGE_LENGTH_alp_emu_inc * NUM_ELEMENTS * NUM_ELEMENTS * NUM_ELEMENTS;
		Assert.assertEquals(expectedSize1, CollectionTool.size(result1));
		Assert.assertTrue(ListTool.isSorted(result1));
		
		List<SortedBeanKey> result1b = node.getKeysInRange(alp1, true, emu1, false, null);
		int expectedSize1b = (RANGE_LENGTH_alp_emu_inc - 1) * NUM_ELEMENTS * NUM_ELEMENTS * NUM_ELEMENTS;
		Assert.assertEquals(expectedSize1b, CollectionTool.size(result1b));
		Assert.assertTrue(ListTool.isSorted(result1b));
		
		SortedBeanKey alp2 = new SortedBeanKey(STRINGS.first(), RANGE_alp, null, null);
		SortedBeanKey emu2 = new SortedBeanKey(STRINGS.first(), RANGE_emu, null, null);
		List<SortedBeanKey> result2 = node.getKeysInRange(alp2, true, emu2, true, null);
		int expectedSize2 = RANGE_LENGTH_alp_emu_inc * NUM_ELEMENTS * NUM_ELEMENTS;
		Assert.assertEquals(expectedSize2, CollectionTool.size(result2));
		Assert.assertTrue(ListTool.isSorted(result2));
	}
	
	@Test
	public synchronized void testGetInRange(){
		SortedBeanKey alp1 = new SortedBeanKey(RANGE_alp, null, null, null);
		SortedBeanKey emu1 = new SortedBeanKey(RANGE_emu, null, null, null);
		List<SortedBean> result1 = node.getRange(alp1, true, emu1, true, null);
		int expectedSize1 = RANGE_LENGTH_alp_emu_inc * NUM_ELEMENTS * NUM_ELEMENTS * NUM_ELEMENTS;
		Assert.assertEquals(expectedSize1, CollectionTool.size(result1));
		Assert.assertTrue(ListTool.isSorted(result1));
		
		List<SortedBean> result1b = node.getRange(alp1, true, emu1, false, null);
		int expectedSize1b = (RANGE_LENGTH_alp_emu_inc-1) * NUM_ELEMENTS * NUM_ELEMENTS * NUM_ELEMENTS;
		Assert.assertEquals(expectedSize1b, CollectionTool.size(result1b));
		Assert.assertTrue(ListTool.isSorted(result1b));
		
		SortedBeanKey alp2 = new SortedBeanKey(STRINGS.first(), RANGE_alp, null, null);
		SortedBeanKey emu2 = new SortedBeanKey(STRINGS.first(), RANGE_emu, null, null);
		List<SortedBean> result2 = node.getRange(alp2, true, emu2, true, null);
		int expectedSize2 = RANGE_LENGTH_alp_emu_inc * NUM_ELEMENTS * NUM_ELEMENTS;
		Assert.assertEquals(expectedSize2, CollectionTool.size(result2));
		Assert.assertTrue(ListTool.isSorted(result2));
	}
	
	@Test //small batch sizes to make sure we're resuming each batch from the correct spot
	public synchronized void testIncrementalScan(){
		Config smallIterateBatchSize = new Config().setIterateBatchSize(3);

		int expectedSize1 = RANGE_LENGTH_alp_emu_inc * NUM_ELEMENTS * NUM_ELEMENTS * NUM_ELEMENTS;
//		logger.warn("expecting "+expectedSize1);
		SortedBeanKey alp1 = new SortedBeanKey(RANGE_alp, null, null, null);
		SortedBeanKey emu1 = new SortedBeanKey(RANGE_emu, null, null, null);
		List<SortedBeanKey> result1 = ListTool.createArrayList(node.scanKeys(Range.create(alp1, true, emu1, true), 
				smallIterateBatchSize));
		Assert.assertEquals(expectedSize1, CollectionTool.size(result1));
		Assert.assertTrue(ListTool.isSorted(result1));

		int expectedSize1b = (RANGE_LENGTH_alp_emu_inc - 1) * NUM_ELEMENTS * NUM_ELEMENTS * NUM_ELEMENTS;
//		logger.warn("expecting "+expectedSize1b);
		List<SortedBeanKey> result1b = ListTool.createArrayList(node.scanKeys(Range.create(alp1, true, emu1, false), 
				smallIterateBatchSize));
		Assert.assertEquals(expectedSize1b, CollectionTool.size(result1b));
		Assert.assertTrue(ListTool.isSorted(result1b));

		int expectedSize2 = RANGE_LENGTH_alp_emu_inc * NUM_ELEMENTS * NUM_ELEMENTS;
//		logger.warn("expecting "+expectedSize2);
		SortedBeanKey alp2 = new SortedBeanKey(STRINGS.first(), RANGE_alp, null, null);
		SortedBeanKey emu2 = new SortedBeanKey(STRINGS.first(), RANGE_emu, null, null);
		List<SortedBeanKey> result2 = ListTool.createArrayList(node.scanKeys(Range.create(alp2, true, emu2, true), 
				smallIterateBatchSize));
		Assert.assertEquals(expectedSize2, CollectionTool.size(result2));
		Assert.assertTrue(ListTool.isSorted(result2));
//		logger.warn("finished incremental scan");
	}
	
	@Test
	public synchronized void testPrefixedRange(){
		if(isHBaseEntity()){ return; }//not implemented
		SortedBeanKey prefix = new SortedBeanKey(PREFIX_a, null, null, null);
		SortedBeanKey al = new SortedBeanKey(RANGE_al, null, null, null);
		List<SortedBean> result1 = node.getPrefixedRange(prefix, true, al, true, null);
		int expectedSize1 = RANGE_LENGTH_al_b * NUM_ELEMENTS * NUM_ELEMENTS * NUM_ELEMENTS;
		Assert.assertEquals(expectedSize1, CollectionTool.size(result1));
		Assert.assertTrue(ListTool.isSorted(result1));
	}
	
	@Test
	public synchronized void testGet(){
		Iterable<SortedBean> iterable = node.scan(null, null);
		for(SortedBean sortedBean : iterable){
			SortedBean sortedBean2 = node.get(sortedBean.getKey(), null);
			Assert.assertEquals(sortedBean, sortedBean2);
		}
	}
	
	@Test
	public synchronized void testGetMulti(){
		Iterable<SortedBean> iterable = node.scan(null, null);
		Set<SortedBean> allBeans = Sets.newHashSet(iterable);
		Assert.assertEquals(TOTAL_RECORDS, allBeans.size());
		List<SortedBean> getMultiResult = node.getMulti(KeyTool.getKeys(allBeans), null);
		Assert.assertEquals(TOTAL_RECORDS, getMultiResult.size());
		for(SortedBean sortedBeanResult : getMultiResult){
			Assert.assertTrue(allBeans.contains(sortedBeanResult));
		}
	}
	
	@Test
	public synchronized void testFullScanKeys(){
		Iterable<SortedBeanKey> iterable = node.scanKeys(null, null);
		long numKeys = IterableTool.count(iterable);
		Assert.assertEquals(TOTAL_RECORDS, numKeys);
	}
	
	@Test
	public synchronized void testFullScan(){
		Iterable<SortedBean> iterable = node.scan(null, null);
		long numDatabeans = IterableTool.count(iterable);
		Assert.assertEquals(TOTAL_RECORDS, numDatabeans);
	}

	@Test
	public synchronized void testDelete(){
		int remainingElements = TOTAL_RECORDS;
		
		//delete
		Assert.assertEquals(remainingElements, IterableTool.count(node.scan(null, null)).intValue());
		SortedBeanKey key = new SortedBeanKey(STRINGS.last(), STRINGS.last(), 0, STRINGS.last());
		node.delete(key, null);
		--remainingElements;
		Assert.assertEquals(remainingElements, IterableTool.count(node.scan(null, null)).intValue());

		//deleteMulti
		Assert.assertEquals(remainingElements, IterableTool.count(node.scan(null, null)).intValue());
		List<SortedBeanKey> keys = ListTool.create(
				new SortedBeanKey(STRINGS.last(), STRINGS.last(), 1, STRINGS.last()),
				new SortedBeanKey(STRINGS.last(), STRINGS.last(), 2, STRINGS.last()),
				new SortedBeanKey(STRINGS.last(), STRINGS.last(), 3, STRINGS.last()));
		node.deleteMulti(keys, null);
		remainingElements -= 3;
		Assert.assertEquals(remainingElements, IterableTool.count(node.scan(null, null)).intValue());
		
		
		//deleteWithPrefix
		Assert.assertEquals(remainingElements, IterableTool.count(node.scan(null, null)).intValue());
		SortedBeanKey prefix = new SortedBeanKey(PREFIX_a, null, null, null);
		node.deleteRangeWithPrefix(prefix, true, null);
		remainingElements -= NUM_PREFIX_a * NUM_ELEMENTS * NUM_ELEMENTS * NUM_ELEMENTS;
		Assert.assertEquals(remainingElements, IterableTool.count(node.scan(null, null)).intValue());

		resetTable();//in case this one doesn't run last
	}
	
	

	/************************* helper ****************************/
	
	public boolean isHBaseEntity(){
		return node instanceof HBaseSubEntityReaderNode;
	}
}





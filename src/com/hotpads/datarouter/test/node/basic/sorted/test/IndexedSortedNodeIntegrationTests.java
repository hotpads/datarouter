package com.hotpads.datarouter.test.node.basic.sorted.test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.config.PutMethod;
import com.hotpads.datarouter.test.node.basic.BasicNodeTestRouter;
import com.hotpads.datarouter.test.node.basic.sorted.SortedBean;
import com.hotpads.datarouter.test.node.basic.sorted.SortedBeanKey;
import com.hotpads.datarouter.test.node.basic.sorted.SortedBean.SortedBeanByDCBLookup;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.SetTool;

public class IndexedSortedNodeIntegrationTests{
	
	static BasicNodeTestRouter router;
	static List<SortedBeanKey> keys = ListTool.create();
	
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
			RANGE_LENGTH_alp_emu = 3,
			RANGE_LENGTH_emu = 4;
	
	public static final int NUM_ELEMENTS = STRINGS.size();
	public static final List<Integer> INTEGERS = ListTool.createArrayList(NUM_ELEMENTS);
	static{
		for(int i=0; i < NUM_ELEMENTS; ++i){
			INTEGERS.add(i);
		}
	}
	
	public static final int TOTAL_RECORDS = NUM_ELEMENTS*NUM_ELEMENTS*NUM_ELEMENTS*NUM_ELEMENTS;
	
	public static int remainingElements;
	
	
	@BeforeClass
	public static void init() throws IOException{
		Injector injector = Guice.createInjector();
		router = injector.getInstance(BasicNodeTestRouter.class);
		
		router.sortedBean().deleteAll(null);
		Assert.assertEquals(0, CollectionTool.size(router.sortedBean().getAll(null)));
	}
	
	@Test
	public void testRandomInserts(){//create every permutation
		List<String> as = ListTool.createArrayList(STRINGS);
		List<String> bs = ListTool.createArrayList(STRINGS);
		List<Integer> cs = ListTool.createArrayList(INTEGERS);
		List<String> ds = ListTool.createArrayList(STRINGS);
		Collections.shuffle(as);
		Collections.shuffle(bs);
		Collections.shuffle(cs);
		Collections.shuffle(ds);
		
		for(int a=0; a < NUM_ELEMENTS; ++a){
			for(int b=0; b < NUM_ELEMENTS; ++b){
				for(int c=0; c < NUM_ELEMENTS; ++c){
					for(int d=0; d < NUM_ELEMENTS; ++d){
						SortedBean bean = new SortedBean(
								as.get(a), bs.get(b), cs.get(c), ds.get(d), 
								null, null, null, null);
						router.sortedBean().put(bean, 
								new Config().setPutMethod(PutMethod.INSERT_OR_BUST));
					}
				}
			}
		}
	}
	
	@Test
	public void testGetAll(){
		List<SortedBean> allBeans = router.sortedBean().getAll(null);
		Assert.assertEquals(TOTAL_RECORDS, CollectionTool.size(allBeans));
	}
	
	@Test
	public void testGetFirstKey(){
		SortedBeanKey firstKey = router.sortedBean().getFirstKey(null);
		Assert.assertEquals(STRINGS.first(), firstKey.getA());
		Assert.assertEquals(STRINGS.first(), firstKey.getB());
		Assert.assertEquals(new Integer(0), firstKey.getC());
		Assert.assertEquals(STRINGS.first(), firstKey.getD());
	}
	
	@Test
	public void testGetFirst(){
		SortedBean firstBean = router.sortedBean().getFirst(null);
		Assert.assertEquals(STRINGS.first(), firstBean.getKey().getA());
		Assert.assertEquals(STRINGS.first(), firstBean.getKey().getB());
		Assert.assertEquals(new Integer(0), firstBean.getKey().getC());
		Assert.assertEquals(STRINGS.first(), firstBean.getKey().getD());
	}
	
	@Test
	public void testGetWithPrefix(){
		//first 3 fields fixed
		SortedBeanKey prefix1 = new SortedBeanKey(STRINGS.first(), STRINGS.last(), 2, null);
		List<SortedBean> result1 = router.sortedBean().getWithPrefix(prefix1, false, null);
		Assert.assertEquals(NUM_ELEMENTS, CollectionTool.size(result1));
		Assert.assertTrue(ListTool.isSorted(result1));

		//first 3 fields fixed, last field wildcard
		SortedBeanKey prefix2 = new SortedBeanKey(STRINGS.first(), STRINGS.last(), 2, PREFIX_a);
		List<SortedBean> result2 = router.sortedBean().getWithPrefix(prefix2, true, null);
		Assert.assertEquals(NUM_PREFIX_a, CollectionTool.size(result2));
		Assert.assertTrue(ListTool.isSorted(result2));

		//first field fixed, second field wildcard
		SortedBeanKey prefix3 = new SortedBeanKey(STRINGS.first(), PREFIX_a, null, null);
		List<SortedBean> result3 = router.sortedBean().getWithPrefix(prefix3, true, null);
		int expectedSize3 = NUM_PREFIX_a * NUM_ELEMENTS * NUM_ELEMENTS;
		Assert.assertEquals(expectedSize3, CollectionTool.size(result3));
		Assert.assertTrue(ListTool.isSorted(result3));
	}
	
	@Test
	public void testGetWithPrefixes(){
		SortedBeanKey prefixA = new SortedBeanKey(STRINGS.first(), PREFIX_a, null, null);
		SortedBeanKey prefixCh = new SortedBeanKey(STRINGS.first(), PREFIX_ch, null, null);
		List<SortedBeanKey> prefixes = ListTool.create(prefixA, prefixCh);
		List<SortedBean> result = router.sortedBean().getWithPrefixes(prefixes, true, null);
		int expectedSizeA = NUM_PREFIX_a * NUM_ELEMENTS * NUM_ELEMENTS;
		int expectedSizeCh = NUM_PREFIX_ch * NUM_ELEMENTS * NUM_ELEMENTS;
		int expectedSizeTotal = expectedSizeA + expectedSizeCh;
		Assert.assertEquals(expectedSizeTotal, CollectionTool.size(result));
		Assert.assertTrue(ListTool.isSorted(result));
	}
	
	@Test
	public void testGetKeysInRange(){
		SortedBeanKey alp1 = new SortedBeanKey(RANGE_alp, null, null, null);
		SortedBeanKey emu1 = new SortedBeanKey(RANGE_emu, null, null, null);
		List<SortedBeanKey> result1 = router.sortedBean().getKeysInRange(
				alp1, true, emu1, true, null);
		int expectedSize1 = RANGE_LENGTH_alp_emu * NUM_ELEMENTS * NUM_ELEMENTS * NUM_ELEMENTS;
		Assert.assertEquals(expectedSize1, CollectionTool.size(result1));
		Assert.assertTrue(ListTool.isSorted(result1));
		
		List<SortedBeanKey> result1b = router.sortedBean().getKeysInRange(
				alp1, true, emu1, false, null);
		int expectedSize1b = (RANGE_LENGTH_alp_emu-1) * NUM_ELEMENTS * NUM_ELEMENTS * NUM_ELEMENTS;
		Assert.assertEquals(expectedSize1b, CollectionTool.size(result1b));
		Assert.assertTrue(ListTool.isSorted(result1b));
		
		SortedBeanKey alp2 = new SortedBeanKey(STRINGS.first(), RANGE_alp, null, null);
		SortedBeanKey emu2 = new SortedBeanKey(STRINGS.first(), RANGE_emu, null, null);
		List<SortedBeanKey> result2 = router.sortedBean().getKeysInRange(
				alp2, true, emu2, true, null);
		int expectedSize2 = RANGE_LENGTH_alp_emu * NUM_ELEMENTS * NUM_ELEMENTS;
		Assert.assertEquals(expectedSize2, CollectionTool.size(result2));
		Assert.assertTrue(ListTool.isSorted(result2));
	}
	
	@Test
	public void testGetInRange(){
		SortedBeanKey alp1 = new SortedBeanKey(RANGE_alp, null, null, null);
		SortedBeanKey emu1 = new SortedBeanKey(RANGE_emu, null, null, null);
		List<SortedBean> result1 = router.sortedBean().getRange(
				alp1, true, emu1, true, null);
		int expectedSize1 = RANGE_LENGTH_alp_emu * NUM_ELEMENTS * NUM_ELEMENTS * NUM_ELEMENTS;
		Assert.assertEquals(expectedSize1, CollectionTool.size(result1));
		Assert.assertTrue(ListTool.isSorted(result1));
		
		List<SortedBean> result1b = router.sortedBean().getRange(
				alp1, true, emu1, false, null);
		int expectedSize1b = (RANGE_LENGTH_alp_emu-1) * NUM_ELEMENTS * NUM_ELEMENTS * NUM_ELEMENTS;
		Assert.assertEquals(expectedSize1b, CollectionTool.size(result1b));
		Assert.assertTrue(ListTool.isSorted(result1b));
		
		SortedBeanKey alp2 = new SortedBeanKey(STRINGS.first(), RANGE_alp, null, null);
		SortedBeanKey emu2 = new SortedBeanKey(STRINGS.first(), RANGE_emu, null, null);
		List<SortedBean> result2 = router.sortedBean().getRange(
				alp2, true, emu2, true, null);
		int expectedSize2 = RANGE_LENGTH_alp_emu * NUM_ELEMENTS * NUM_ELEMENTS;
		Assert.assertEquals(expectedSize2, CollectionTool.size(result2));
		Assert.assertTrue(ListTool.isSorted(result2));
	}
	
	@Test
	public void testLookup(){
		SortedBeanByDCBLookup lookup = new SortedBeanByDCBLookup(STRINGS.last(), 1, STRINGS.first());
		List<SortedBean> result = router.sortedBean().lookup(lookup, null);
		Assert.assertEquals(NUM_ELEMENTS, CollectionTool.size(result));
		Assert.assertTrue(ListTool.isSorted(result));
	}
	
	@Test
	public void testLookups(){
		List<SortedBeanByDCBLookup> lookups = ListTool.create(
				new SortedBeanByDCBLookup(STRINGS.last(), 1, STRINGS.first()),
				new SortedBeanByDCBLookup(STRINGS.first(), 2, null),//should currently match nulls in 3, which we don't have
				new SortedBeanByDCBLookup(STRINGS.last(), 0, STRINGS.first()));
		List<SortedBean> result = router.sortedBean().lookup(lookups, null);
		int expected = 2 * NUM_ELEMENTS;
		Assert.assertEquals(expected, CollectionTool.size(result));
		Assert.assertTrue(ListTool.isSorted(result));
	}
	
	@Test
	public void testPrefixedRange(){
		SortedBeanKey prefix = new SortedBeanKey(PREFIX_a, null, null, null);
		SortedBeanKey emu1 = new SortedBeanKey(RANGE_al, null, null, null);
		List<SortedBean> result1 = router.sortedBean().getPrefixedRange(
				prefix, true, emu1, true, null);
		int expectedSize1 = RANGE_LENGTH_al_b * NUM_ELEMENTS * NUM_ELEMENTS * NUM_ELEMENTS;
		Assert.assertEquals(expectedSize1, CollectionTool.size(result1));
		Assert.assertTrue(ListTool.isSorted(result1));
	}

	@Test
	public void testDelete(){
		remainingElements = TOTAL_RECORDS;
		Assert.assertEquals(remainingElements, CollectionTool.size(router.sortedBean().getAll(null)));
		SortedBeanKey key = new SortedBeanKey(STRINGS.last(), STRINGS.last(), 0, STRINGS.last());
		router.sortedBean().delete(key, null);
		--remainingElements;
		Assert.assertEquals(remainingElements, CollectionTool.size(router.sortedBean().getAll(null)));
	}

	@Test
	public void testDeleteMulti(){
		Assert.assertEquals(remainingElements, CollectionTool.size(router.sortedBean().getAll(null)));
		List<SortedBeanKey> keys = ListTool.create(
				new SortedBeanKey(STRINGS.last(), STRINGS.last(), 1, STRINGS.last()),
				new SortedBeanKey(STRINGS.last(), STRINGS.last(), 2, STRINGS.last()),
				new SortedBeanKey(STRINGS.last(), STRINGS.last(), 3, STRINGS.last()));
		router.sortedBean().deleteMulti(keys, null);
		remainingElements -= 3;
		Assert.assertEquals(remainingElements, CollectionTool.size(router.sortedBean().getAll(null)));
	}

	@Test
	public void testDeleteWithPrefix(){
		Assert.assertEquals(remainingElements, CollectionTool.size(router.sortedBean().getAll(null)));
		SortedBeanKey prefix = new SortedBeanKey(PREFIX_a, null, null, null);
		router.sortedBean().deleteRangeWithPrefix(prefix, true, null);
		remainingElements -= NUM_PREFIX_a * NUM_ELEMENTS * NUM_ELEMENTS * NUM_ELEMENTS;
		Assert.assertEquals(remainingElements, CollectionTool.size(router.sortedBean().getAll(null)));
	}

	@Test
	public void testDeleteViaLookup(){
		Assert.assertEquals(remainingElements, CollectionTool.size(router.sortedBean().getAll(null)));
		SortedBeanByDCBLookup lookup = new SortedBeanByDCBLookup(S_gopher, 0, S_gopher);
		router.sortedBean().delete(lookup, null);
		remainingElements -= (NUM_ELEMENTS - 3);//we deleted the 3 "where key.a like 'a%'" in the previous test
		Assert.assertEquals(remainingElements, CollectionTool.size(router.sortedBean().getAll(null)));
	}
	
}





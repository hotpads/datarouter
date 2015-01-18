package com.hotpads.datarouter.test.node.basic.sorted;

import java.util.List;
import java.util.SortedSet;

import org.junit.Assert;
import org.junit.Test;

import com.hotpads.datarouter.test.node.basic.sorted.SortedBean.SortedBeanByDCBLookup;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;

public abstract class BaseIndexedNodeIntegrationTests extends BaseSortedNodeIntegrationTests{
//	private static final Logger logger = LoggerFactory.getLogger(IndexedNodeIntegrationTests.class);

	

	/***************************** setup/teardown **************************************/

	protected static void testIndexedDelete(){
		testSortedDelete();
		resetTable(false);//leave the table full
		
		int remainingElements = TOTAL_RECORDS;
		
		//delete via lookup
		Assert.assertEquals(remainingElements, IterableTool.count(sortedNode.scan(null, null)).intValue());
		SortedBeanByDCBLookup lookup = new SortedBeanByDCBLookup(
				SortedBeans.S_gopher, 0, SortedBeans.S_gopher);
		router.indexedSortedBean().delete(lookup, null);
		remainingElements -= (NUM_ELEMENTS);
		Assert.assertEquals(remainingElements, IterableTool.count(sortedNode.scan(null, null)).intValue());
	}
	
	
	/****************************** testing vars ***********************************/
	
	//shortcuts
	public static final SortedSet<String> STRINGS = SortedBeans.STRINGS;
	public static final int 
		NUM_ELEMENTS = SortedBeans.NUM_ELEMENTS,
		TOTAL_RECORDS = SortedBeans.TOTAL_RECORDS;
	
	/********************** junit methods *********************************************/
	
	@Test
	public void testLookup(){
		SortedBeanByDCBLookup lookup = new SortedBeanByDCBLookup(STRINGS.last(), 1, STRINGS.first());
		List<SortedBean> result = router.indexedSortedBean().lookup(lookup, false, null);
		Assert.assertEquals(NUM_ELEMENTS, CollectionTool.size(result));
		Assert.assertTrue(ListTool.isSorted(result));
		
		lookup = new SortedBeanByDCBLookup(STRINGS.first(), 2, null);//matches d=aardvark && c=2 (64 rows)
		result = router.indexedSortedBean().lookup(lookup, false, null);
		Assert.assertEquals(NUM_ELEMENTS*NUM_ELEMENTS, CollectionTool.size(result));
		Assert.assertTrue(ListTool.isSorted(result));
	}
	
	@Test
	public void testLookups(){
		List<SortedBeanByDCBLookup> lookups = ListTool.create(
				new SortedBeanByDCBLookup(STRINGS.last(), 1, STRINGS.first()), //8 rows
				new SortedBeanByDCBLookup(STRINGS.first(), 2, null),//matches d=aardvark && c=2 (64 rows)
				new SortedBeanByDCBLookup(STRINGS.last(), 0, STRINGS.first())); //8 rows
		
		List<SortedBean> result = router.indexedSortedBean().lookup(lookups, null);
		int expected = NUM_ELEMENTS + NUM_ELEMENTS*NUM_ELEMENTS + NUM_ELEMENTS;
		Assert.assertEquals(expected, CollectionTool.size(result));
		Assert.assertTrue(ListTool.isSorted(result));
	}
	
}


package com.hotpads.datarouter.test.node.basic.sorted;

import java.util.List;
import java.util.SortedSet;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;

import com.google.inject.Injector;
import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.config.PutMethod;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage;
import com.hotpads.datarouter.routing.DatarouterContext;
import com.hotpads.datarouter.test.DatarouterTestInjectorProvider;
import com.hotpads.datarouter.test.node.basic.sorted.SortedBean.SortedBeanByDCBLookup;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.iterable.BatchingIterable;

public abstract class BaseIndexedNodeIntegrationTests{
//	private static final Logger logger = LoggerFactory.getLogger(IndexedNodeIntegrationTests.class);

	
	/***************************** fields **************************************/
	
	private static DatarouterContext drContext;
	private static SortedNodeTestRouter router;
	private static SortedMapStorage<SortedBeanKey,SortedBean> node;

	/***************************** setup/teardown **************************************/

	@AfterClass
	public static void afterClass(){
		testDelete();
		drContext.shutdown();
	}
	
	protected static void setup(String clientName, ClientType clientType, boolean useFielder){
		Injector injector = new DatarouterTestInjectorProvider().get();
		drContext = injector.getInstance(DatarouterContext.class);
		NodeFactory nodeFactory = injector.getInstance(NodeFactory.class);
		router = new SortedNodeTestRouter(drContext, nodeFactory, clientName, BaseIndexedNodeIntegrationTests.class, 
				useFielder, false);
		node = router.sortedBean();
		
		resetTable();
	}


	private static void resetTable(){
		node.deleteAll(null);
		List<SortedBean> remainingAfterDelete = ListTool.createArrayList(node.scan(null, null));
		Assert.assertEquals(0, CollectionTool.size(remainingAfterDelete));
		
		List<SortedBean> allBeans = SortedBeans.generatedSortedBeans();
		for(List<SortedBean> batch : new BatchingIterable<SortedBean>(allBeans, 1000)){
			node.putMulti(batch, new Config().setPutMethod(PutMethod.INSERT_OR_BUST));
		}
		
		List<SortedBean> roundTripped = ListTool.createArrayList(node.scan(null, null));
		Assert.assertEquals(TOTAL_RECORDS, roundTripped.size());
	}

	private static void testDelete(){
		int remainingElements = TOTAL_RECORDS;
		
		//delete via lookup
		Assert.assertEquals(remainingElements, IterableTool.count(router.sortedBean().scan(null, null)).intValue());
		SortedBeanByDCBLookup lookup = new SortedBeanByDCBLookup(
				SortedBeans.S_gopher, 0, SortedBeans.S_gopher);
		router.sortedBeanIndexed().delete(lookup, null);
		remainingElements -= (NUM_ELEMENTS);
		Assert.assertEquals(remainingElements, IterableTool.count(router.sortedBean().scan(null, null)).intValue());
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
		List<SortedBean> result = router.sortedBeanIndexed().lookup(lookup, false, null);
		Assert.assertEquals(NUM_ELEMENTS, CollectionTool.size(result));
		Assert.assertTrue(ListTool.isSorted(result));
		
		lookup = new SortedBeanByDCBLookup(STRINGS.first(), 2, null);//matches d=aardvark && c=2 (64 rows)
		result = router.sortedBeanIndexed().lookup(lookup, false, null);
		Assert.assertEquals(NUM_ELEMENTS*NUM_ELEMENTS, CollectionTool.size(result));
		Assert.assertTrue(ListTool.isSorted(result));
	}
	
	@Test
	public void testLookups(){
		List<SortedBeanByDCBLookup> lookups = ListTool.create(
				new SortedBeanByDCBLookup(STRINGS.last(), 1, STRINGS.first()), //8 rows
				new SortedBeanByDCBLookup(STRINGS.first(), 2, null),//matches d=aardvark && c=2 (64 rows)
				new SortedBeanByDCBLookup(STRINGS.last(), 0, STRINGS.first())); //8 rows
		
		List<SortedBean> result = router.sortedBeanIndexed().lookup(lookups, null);
		int expected = NUM_ELEMENTS + NUM_ELEMENTS*NUM_ELEMENTS + NUM_ELEMENTS;
		Assert.assertEquals(expected, CollectionTool.size(result));
		Assert.assertTrue(ListTool.isSorted(result));
	}
	
}


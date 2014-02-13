package com.hotpads.datarouter.test.node.basic.sorted.test;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.hotpads.datarouter.client.DClientType;
import com.hotpads.datarouter.client.imp.hibernate.HibernateClientType;
import com.hotpads.datarouter.test.DRTestConstants;
import com.hotpads.datarouter.test.node.basic.BasicNodeTestRouter;
import com.hotpads.datarouter.test.node.basic.BasicNodeTestRouter.IndexedBasicNodeTestRouter;
import com.hotpads.datarouter.test.node.basic.sorted.SortedBean;
import com.hotpads.datarouter.test.node.basic.sorted.SortedBean.SortedBeanByDCBLookup;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;

@RunWith(Parameterized.class)
public class IndexedNodeIntegrationTests{
	static Logger logger = Logger.getLogger(IndexedNodeIntegrationTests.class);
	
	/****************************** static setup ***********************************/

	static Map<DClientType,IndexedBasicNodeTestRouter> routerByClientType = MapTool.create();
	
	@Parameters
	public static Collection<Object[]> parameters(){
		return ListTool.wrap(new Object[]{HibernateClientType.INSTANCE});
//		return ListTool.create(
//				new Object[]{ClientType.hibernate},
//				new Object[]{ClientType.hbase});
	}
	
	@BeforeClass
	public static void init() throws IOException{
		Class<?> cls = IndexedNodeIntegrationTests.class;

		IndexedBasicNodeTestRouter hibernateRouter = new IndexedBasicNodeTestRouter(
				DRTestConstants.CLIENT_drTestHibernate0, cls);
		routerByClientType.put(HibernateClientType.INSTANCE, hibernateRouter);
		
//		BasicNodeTestRouter hbaseRouter = new BasicNodeTestRouter(
//				DRTestConstants.CLIENT_drTestHBase, 
//				DRTestConstants.CLIENT_drTestHibernate0);
//		routerByClientType.put(ClientType.hbase, hbaseRouter);
		
		for(BasicNodeTestRouter router : routerByClientType.values()){
			SortedNodeIntegrationTests.resetTable(router);
		}
	}
	
	
	/***************************** fields **************************************/
	
	protected DClientType clientType;
	protected IndexedBasicNodeTestRouter router;

	/***************************** constructors **************************************/
	
	public IndexedNodeIntegrationTests(DClientType clientType){
		this.clientType = clientType;
		this.router = routerByClientType.get(clientType);
	}
	
	
	/****************************** testing vars ***********************************/
	
	//shortcuts
	public static final SortedSet<String> STRINGS = SortedNodeIntegrationTests.STRINGS;
	public static final int 
		NUM_ELEMENTS = SortedNodeIntegrationTests.NUM_ELEMENTS,
		TOTAL_RECORDS = SortedNodeIntegrationTests.TOTAL_RECORDS;
	
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

	@Test
	public void testDelete(){
		SortedNodeIntegrationTests.resetTable(router);
		
		int remainingElements = TOTAL_RECORDS;
		
		//delete via lookup
		Assert.assertEquals(remainingElements, CollectionTool.size(router.sortedBean().getAll(null)));
		SortedBeanByDCBLookup lookup = new SortedBeanByDCBLookup(
				SortedNodeIntegrationTests.S_gopher, 0, SortedNodeIntegrationTests.S_gopher);
		router.sortedBeanIndexed().delete(lookup, null);
		remainingElements -= (NUM_ELEMENTS);
		Assert.assertEquals(remainingElements, CollectionTool.size(router.sortedBean().getAll(null)));
	}
	
}


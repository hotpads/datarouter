package com.hotpads.datarouter.test.node.basic.sorted.test;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.client.imp.hibernate.HibernateClientType;
import com.hotpads.datarouter.client.imp.jdbc.JdbcClientType;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.config.PutMethod;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage;
import com.hotpads.datarouter.routing.DataRouterContext;
import com.hotpads.datarouter.test.DRTestConstants;
import com.hotpads.datarouter.test.DatarouterTestInjectorProvider;
import com.hotpads.datarouter.test.node.basic.BasicNodeTestRouter.IndexedBasicNodeTestRouter;
import com.hotpads.datarouter.test.node.basic.sorted.SortedBean;
import com.hotpads.datarouter.test.node.basic.sorted.SortedBean.SortedBeanByDCBLookup;
import com.hotpads.datarouter.test.node.basic.sorted.SortedBeanKey;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;
import com.hotpads.util.core.iterable.BatchingIterable;

@RunWith(Parameterized.class)
public class IndexedNodeIntegrationTests{
	static Logger logger = LoggerFactory.getLogger(IndexedNodeIntegrationTests.class);
	
	/****************************** static setup ***********************************/

	static Map<ClientType,IndexedBasicNodeTestRouter> routerByClientType = MapTool.create();
	
	@Parameters
	public static Collection<Object[]> parameters(){
		List<Object[]> params = ListTool.create();
		params.add(new Object[]{DRTestConstants.CLIENT_drTestHibernate0, HibernateClientType.INSTANCE, false});
		params.add(new Object[]{DRTestConstants.CLIENT_drTestJdbc0, JdbcClientType.INSTANCE, true});
		return params;
	}
	
	@BeforeClass
	public static void init() throws IOException{
//		Class<?> cls = IndexedNodeIntegrationTests.class;
//
//		IndexedBasicNodeTestRouter hibernateRouter = new IndexedBasicNodeTestRouter(
//				DRTestConstants.CLIENT_drTestHibernate0, cls, false);
//		routerByClientType.put(HibernateClientType.INSTANCE, hibernateRouter);
//		
////		BasicNodeTestRouter hbaseRouter = new BasicNodeTestRouter(
////				DRTestConstants.CLIENT_drTestHBase, 
////				DRTestConstants.CLIENT_drTestHibernate0);
////		routerByClientType.put(ClientType.hbase, hbaseRouter);
//		
//		for(BasicNodeTestRouter router : routerByClientType.values()){
//			SortedNodeIntegrationTests.resetTable();
//		}
	}
	
	
	/***************************** fields **************************************/
	
	private IndexedBasicNodeTestRouter router;
	private SortedMapStorage<SortedBeanKey,SortedBean> node;

	/***************************** constructors **************************************/

	public IndexedNodeIntegrationTests(String clientName, ClientType clientType, boolean useFielder){
		Injector injector = new DatarouterTestInjectorProvider().get();
		DataRouterContext drContext = injector.getInstance(DataRouterContext.class);
		NodeFactory nodeFactory = injector.getInstance(NodeFactory.class);
		this.router = new IndexedBasicNodeTestRouter(drContext, nodeFactory, clientName, getClass(), useFielder, false);
		this.node = router.sortedBean();
		resetTable();
	}


	public void resetTable(){
		node.deleteAll(null);
		List<SortedBean> remainingAfterDelete = ListTool.createArrayList(node.scan(null, null));
		Assert.assertEquals(0, CollectionTool.size(remainingAfterDelete));
		
		List<SortedBean> allBeans = SortedNodeIntegrationTests.generatedSortedBeans();
		for(List<SortedBean> batch : new BatchingIterable<SortedBean>(allBeans, 1000)){
			node.putMulti(batch, new Config().setPutMethod(PutMethod.INSERT_OR_BUST));
		}
		
		List<SortedBean> roundTripped = ListTool.createArrayList(node.scan(null, null));
		Assert.assertEquals(TOTAL_RECORDS, roundTripped.size());
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
		resetTable();
		
		int remainingElements = TOTAL_RECORDS;
		
		//delete via lookup
		Assert.assertEquals(remainingElements, IterableTool.count(router.sortedBean().scan(null, null)).intValue());
		SortedBeanByDCBLookup lookup = new SortedBeanByDCBLookup(
				SortedNodeIntegrationTests.S_gopher, 0, SortedNodeIntegrationTests.S_gopher);
		router.sortedBeanIndexed().delete(lookup, null);
		remainingElements -= (NUM_ELEMENTS);
		Assert.assertEquals(remainingElements, IterableTool.count(router.sortedBean().scan(null, null)).intValue());
	}
	
}


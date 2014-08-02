package com.hotpads.datarouter.test.node.basic.prefixed.test;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.client.imp.hbase.HBaseClientType;
import com.hotpads.datarouter.client.imp.hibernate.HibernateClientType;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.config.PutMethod;
import com.hotpads.datarouter.test.DRTestConstants;
import com.hotpads.datarouter.test.node.basic.BasicNodeTestRouter;
import com.hotpads.datarouter.test.node.basic.BasicNodeTestRouter.SortedBasicNodeTestRouter;
import com.hotpads.datarouter.test.node.basic.prefixed.ScatteringPrefixBean;
import com.hotpads.datarouter.test.node.basic.prefixed.ScatteringPrefixBean.ScatteringPrefixBeanFielder.ScatteringPrefixBeanScatterer;
import com.hotpads.datarouter.test.node.basic.prefixed.ScatteringPrefixBeanKey;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ComparableTool;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;
import com.hotpads.util.core.collections.Range;

@RunWith(Parameterized.class)
public class ScatteringPrefixIntegrationTests{
	static Logger logger = Logger.getLogger(ScatteringPrefixIntegrationTests.class);
	
	/****************************** client types ***********************************/

	public static List<ClientType> clientTypes = ListTool.create();
	public static List<Object[]> clientTypeObjectArrays = ListTool.create();
	static{
		clientTypes.add(HBaseClientType.INSTANCE);
		clientTypes.add(HibernateClientType.INSTANCE);
		for(ClientType clientType : clientTypes){
			clientTypeObjectArrays.add(new Object[]{clientType});
		}
	}
	
	/****************************** static setup ***********************************/

	static Map<ClientType,SortedBasicNodeTestRouter> routerByClientType = MapTool.create();
	
	@BeforeClass
	public static void init() throws IOException{	
		
		if(clientTypes.contains(HibernateClientType.INSTANCE)){
			routerByClientType.put(HibernateClientType.INSTANCE, new SortedBasicNodeTestRouter(
					DRTestConstants.CLIENT_drTestHibernate0, ScatteringPrefixIntegrationTests.class, true, false));
		}

		if(clientTypes.contains(HBaseClientType.INSTANCE)){
			routerByClientType.put(HBaseClientType.INSTANCE, new SortedBasicNodeTestRouter(DRTestConstants.CLIENT_drTestHBase,
					ScatteringPrefixIntegrationTests.class, true, false));
		}
		
		for(BasicNodeTestRouter router : routerByClientType.values()){
			resetTable(router);
		}
	}
	
	public static final List<String> PREFIXES = ListTool.create("a","b","c","d","e");
	
	public static final int 
		NUM_BATCHES = 2,
		BATCH_SIZE = 100,
		TOTAL_ROWS = NUM_BATCHES * BATCH_SIZE,
		NUM_OF_EACH_PREFIX = TOTAL_ROWS / PREFIXES.size();
	
	public static final int TOTAL_RECORDS = NUM_BATCHES * BATCH_SIZE;
	
	public static void resetTable(BasicNodeTestRouter routerToReset){
		routerToReset.scatteringPrefixBean().deleteAll(null);
		Assert.assertEquals(0, IterableTool.count(routerToReset.scatteringPrefixBean().scan(null, null)).intValue());
		
		List<ScatteringPrefixBean> toSave = ListTool.createArrayList();
		for(int a=0; a < NUM_BATCHES; ++a){
			for(int b=0; b < BATCH_SIZE; ++b){
				String prefix = PREFIXES.get(b % PREFIXES.size());
				long id = a * BATCH_SIZE + b;
				toSave.add(new ScatteringPrefixBean(prefix, id, "abc", (int)id
						% ScatteringPrefixBeanScatterer.NUM_SHARDS));
			}
			//save them every batch to avoid a huge put
			routerToReset.scatteringPrefixBean().putMulti(toSave, 
					new Config().setPutMethod(PutMethod.INSERT_OR_BUST));
			toSave.clear();
		}
	}
	
	/***************************** fields **************************************/
	
	protected ClientType clientType;
	protected SortedBasicNodeTestRouter router;

	/***************************** constructors **************************************/

	@Parameters
	public static Collection<Object[]> parameters(){
		return clientTypeObjectArrays;
	}
	
	public ScatteringPrefixIntegrationTests(ClientType clientType){
		this.clientType = clientType;
		this.router = routerByClientType.get(clientType);
	}
	
	
	/********************** setup methods *********************************************/
	
	@Before
	public void setUp(){
		resetTable(router);
	}
	
	/*************************** tests *********************************************/
	
	@Test
	public synchronized void testGetAll(){
		int iterateBatchSize = 2; 
		Iterable<ScatteringPrefixBeanKey> iter = router.scatteringPrefixBean().scanKeys(
				null, new Config().setIterateBatchSize(iterateBatchSize));
		Iterable<ScatteringPrefixBeanKey> all = IterableTool.createArrayListFromIterable(iter);
		long count = IterableTool.count(all);
		Assert.assertTrue(TOTAL_RECORDS == count);
		Assert.assertTrue(ComparableTool.isSorted(all));
	}
	
	@Test
	public synchronized void testGetFirstKey(){
		ScatteringPrefixBeanKey firstKey = router.scatteringPrefixBean().getFirstKey(null);
		Assert.assertTrue(0==firstKey.getId());
	}
	
	@Test
	public synchronized void testGetFirst(){
		ScatteringPrefixBean firstBean = router.scatteringPrefixBean().getFirst(null);
		Assert.assertTrue(0==firstBean.getKey().getId());
	}
	
	@Test
	public synchronized void testGetWithPrefix(){
		//first 3 fields fixed
		ScatteringPrefixBeanKey prefix1 = new ScatteringPrefixBeanKey(CollectionTool.getFirst(PREFIXES), null);
		List<ScatteringPrefixBean> result1 = router.scatteringPrefixBeanSorted().getWithPrefix(prefix1, false, null);
		Assert.assertEquals(NUM_OF_EACH_PREFIX, CollectionTool.size(result1));
		Assert.assertTrue(ListTool.isSorted(result1));
	}
	
	@Test
	public synchronized void testGetWithPrefixes(){
		ScatteringPrefixBeanKey prefix1 = new ScatteringPrefixBeanKey(CollectionTool.getFirst(PREFIXES), null);
		ScatteringPrefixBeanKey prefix2 = new ScatteringPrefixBeanKey(PREFIXES.get(2), null);
		List<ScatteringPrefixBeanKey> prefixes = ListTool.create(prefix1, prefix2);
		List<ScatteringPrefixBean> result = router.scatteringPrefixBeanSorted().getWithPrefixes(prefixes, true, null);
		int expectedSizeTotal = 2 * NUM_OF_EACH_PREFIX;
		Assert.assertEquals(expectedSizeTotal, CollectionTool.size(result));
		Assert.assertTrue(ListTool.isSorted(result));
	}
	
//	@Test
//	public synchronized void testGetKeysInRange(){
//		ScatteringPrefixBeanKey a190 = new ScatteringPrefixBeanKey("a", 190L);
//		ScatteringPrefixBeanKey b6 = new ScatteringPrefixBeanKey("b", 6L);
//		int expectedSize1 = 4;//confusing... just looked at mysql
//		
//		Iterable<ScatteringPrefixBeanKey> scanner0 = router.scatteringPrefixBean().scanKeys(
//				a190, true, b6, true, null);
//		List<ScatteringPrefixBeanKey> result0 = ListTool.createArrayList(scanner0);
//		Assert.assertEquals(expectedSize1, CollectionTool.size(result0));
//		Assert.assertTrue(ListTool.isSorted(result0));
//		
//		List<ScatteringPrefixBeanKey> result1 = router.scatteringPrefixBean().getKeysInRange(
//				a190, true, b6, true, null);
//		Assert.assertEquals(expectedSize1, CollectionTool.size(result1));
//		Assert.assertTrue(ListTool.isSorted(result1));
//	}
	
	@Test
	public synchronized void testGetKeysInRange(){
		ScatteringPrefixBeanKey a190 = new ScatteringPrefixBeanKey("a", 195L);
		ScatteringPrefixBeanKey b6 = new ScatteringPrefixBeanKey("b", 1L);
		int expectedSize1 = 2;//confusing... just looked at mysql
		
		Iterable<ScatteringPrefixBeanKey> scanner0 = router.scatteringPrefixBean().scanKeys(
				Range.create(a190, true, b6, true), null);
		List<ScatteringPrefixBeanKey> result0 = ListTool.createArrayList(scanner0);
		Assert.assertEquals(expectedSize1, CollectionTool.size(result0));
		Assert.assertTrue(ListTool.isSorted(result0));
		
		List<ScatteringPrefixBeanKey> result1 = router.scatteringPrefixBean().getKeysInRange(
				a190, true, b6, true, null);
		Assert.assertEquals(expectedSize1, CollectionTool.size(result1));
		Assert.assertTrue(ListTool.isSorted(result1));
	}
	
	@Test
	public synchronized void testGetInRange(){
		ScatteringPrefixBeanKey a190 = new ScatteringPrefixBeanKey("a", 190L);
		ScatteringPrefixBeanKey b6 = new ScatteringPrefixBeanKey("b", 6L);
		List<ScatteringPrefixBean> result1 = router.scatteringPrefixBean().getRange(
				a190, true, b6, true, null);
		int expectedSize1 = 4;//confusing... just looked at mysql
		Assert.assertEquals(expectedSize1, CollectionTool.size(result1));
		Assert.assertTrue(ListTool.isSorted(result1));
	}
	
	@Test
	public synchronized void testPrefixedRange(){
		ScatteringPrefixBeanKey prefix = new ScatteringPrefixBeanKey("a", null);
		ScatteringPrefixBeanKey startKey = new ScatteringPrefixBeanKey("a", 173L);
		int batchSize = 2;
		List<ScatteringPrefixBean> all = ListTool.create();
		ScatteringPrefixBeanKey batchStartKey = startKey;
		while(true){	
			boolean startInclusive = CollectionTool.isEmpty(all);//only inclusive on first batch
			List<ScatteringPrefixBean> batch = router.scatteringPrefixBeanSorted().getPrefixedRange(
					prefix, true, batchStartKey, startInclusive, new Config().setLimit(batchSize));
			all.addAll(CollectionTool.nullSafe(batch));
			if(CollectionTool.size(batch) < batchSize){ break; }
			batchStartKey = CollectionTool.getLast(batch).getKey();
		}
		int expectedSize1 = 5;//confusing... just looked at mysql
		Assert.assertEquals(expectedSize1, CollectionTool.size(all));
		Assert.assertTrue(ListTool.isSorted(all));
	}

	@Test
	public synchronized void testDelete(){
		router.scatteringPrefixBean().delete(new ScatteringPrefixBeanKey("a", 10L), null);
		Assert.assertEquals(TOTAL_RECORDS - 1, IterableTool.count(router.scatteringPrefixBean().scan(null, null)).intValue());
	}
	
}





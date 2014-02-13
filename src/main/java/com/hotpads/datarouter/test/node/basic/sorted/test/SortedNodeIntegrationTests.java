package com.hotpads.datarouter.test.node.basic.sorted.test;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
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

import com.hotpads.datarouter.client.ClientType;
import com.hotpads.datarouter.client.imp.hbase.HBaseClientType;
import com.hotpads.datarouter.client.imp.hibernate.HibernateClientType;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.config.PutMethod;
import com.hotpads.datarouter.test.DRTestConstants;
import com.hotpads.datarouter.test.node.basic.BasicNodeTestRouter;
import com.hotpads.datarouter.test.node.basic.BasicNodeTestRouter.SortedBasicNodeTestRouter;
import com.hotpads.datarouter.test.node.basic.sorted.SortedBean;
import com.hotpads.datarouter.test.node.basic.sorted.SortedBeanKey;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;
import com.hotpads.util.core.MapTool;
import com.hotpads.util.core.SetTool;

@RunWith(Parameterized.class)
public class SortedNodeIntegrationTests{
	static Logger logger = Logger.getLogger(SortedNodeIntegrationTests.class);
	
	/****************************** client types ***********************************/

	public static List<ClientType> clientTypes = ListTool.create();
	public static List<Object[]> clientTypeObjectArrays = ListTool.create();
	static{
		clientTypes.add(HibernateClientType.INSTANCE);
		clientTypes.add(HBaseClientType.INSTANCE);
		for(ClientType clientType : clientTypes){
			clientTypeObjectArrays.add(new Object[]{clientType});
		}
	}
	
	/****************************** static setup ***********************************/

	static Map<ClientType,SortedBasicNodeTestRouter> routerByClientType = MapTool.create();
	
	@BeforeClass
	public static void init() throws IOException{	
		Class<?> cls = SortedNodeIntegrationTests.class;
		
		if(clientTypes.contains(HibernateClientType.INSTANCE)){
			routerByClientType.put(
					HibernateClientType.INSTANCE, 
					new SortedBasicNodeTestRouter(DRTestConstants.CLIENT_drTestHibernate0, cls));
		}

		if(clientTypes.contains(HBaseClientType.INSTANCE)){
			routerByClientType.put(
					HBaseClientType.INSTANCE, 
					new SortedBasicNodeTestRouter(DRTestConstants.CLIENT_drTestHBase, cls));
		}
		
		for(BasicNodeTestRouter router : routerByClientType.values()){
			resetTable(router);
		}
	}
	
	public static void resetTable(BasicNodeTestRouter routerToReset){
		routerToReset.sortedBean().deleteAll(null);
		List<SortedBean> remainingAfterDelete = routerToReset.sortedBean().getAll(null);
		Assert.assertEquals(0, CollectionTool.size(remainingAfterDelete));
		
		List<String> as = ListTool.createArrayList(STRINGS);
		List<String> bs = ListTool.createArrayList(STRINGS);
		List<Integer> cs = ListTool.createArrayList(INTEGERS);
		List<String> ds = ListTool.createArrayList(STRINGS);
		Collections.shuffle(as);
		Collections.shuffle(bs);
		Collections.shuffle(cs);
		Collections.shuffle(ds);
		
		List<SortedBean> toSave = ListTool.createArrayList();
		for(int a=0; a < NUM_ELEMENTS; ++a){
			for(int b=0; b < NUM_ELEMENTS; ++b){
				for(int c=0; c < NUM_ELEMENTS; ++c){
					for(int d=0; d < NUM_ELEMENTS; ++d){
						SortedBean bean = new SortedBean(
								as.get(a), bs.get(b), cs.get(c), ds.get(d), 
								"string so hbase has at least one field", null, null, null);
						toSave.add(bean);
					}
				}
			}
		}
		routerToReset.sortedBean().putMulti(toSave, 
				new Config().setPutMethod(PutMethod.INSERT_OR_BUST));
		Assert.assertEquals(TOTAL_RECORDS, CollectionTool.size(routerToReset.sortedBean().getAll(null)));
	}
	
	/***************************** fields **************************************/
	
	protected ClientType clientType;
	protected SortedBasicNodeTestRouter router;

	/***************************** constructors **************************************/

	@Parameters
	public static Collection<Object[]> parameters(){
		return clientTypeObjectArrays;
	}
	
	public SortedNodeIntegrationTests(ClientType clientType){
		this.clientType = clientType;
		this.router = routerByClientType.get(clientType);
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
	
	/********************** junit methods *********************************************/
	
	@Test
	public synchronized void testGetAll(){
		List<SortedBean> allBeans = router.sortedBean().getAll(null);
		Assert.assertEquals(TOTAL_RECORDS, CollectionTool.size(allBeans));
	}
	
	@Test
	public synchronized void testGetFirstKey(){
		SortedBeanKey firstKey = router.sortedBeanSorted().getFirstKey(null);
		Assert.assertEquals(STRINGS.first(), firstKey.getA());
		Assert.assertEquals(STRINGS.first(), firstKey.getB());
		Assert.assertEquals(new Integer(0), firstKey.getC());
		Assert.assertEquals(STRINGS.first(), firstKey.getD());
	}
	
	@Test
	public synchronized void testGetFirst(){
		SortedBean firstBean = router.sortedBeanSorted().getFirst(null);
		Assert.assertEquals(STRINGS.first(), firstBean.getKey().getA());
		Assert.assertEquals(STRINGS.first(), firstBean.getKey().getB());
		Assert.assertEquals(new Integer(0), firstBean.getKey().getC());
		Assert.assertEquals(STRINGS.first(), firstBean.getKey().getD());
	}
	
	@Test
	public synchronized void testGetWithPrefix(){
		//first 3 fields fixed
		SortedBeanKey prefix1 = new SortedBeanKey(STRINGS.first(), STRINGS.last(), 2, null);
		List<SortedBean> result1 = router.sortedBeanSorted().getWithPrefix(prefix1, false, null);
		Assert.assertEquals(NUM_ELEMENTS, CollectionTool.size(result1));
		Assert.assertTrue(ListTool.isSorted(result1));

		//first 3 fields fixed, last field wildcard
		SortedBeanKey prefix2 = new SortedBeanKey(STRINGS.first(), STRINGS.last(), 2, PREFIX_a);
		List<SortedBean> result2 = router.sortedBeanSorted().getWithPrefix(prefix2, true, null);
		Assert.assertEquals(NUM_PREFIX_a, CollectionTool.size(result2));
		Assert.assertTrue(ListTool.isSorted(result2));

		//first field fixed, second field wildcard
		SortedBeanKey prefix3 = new SortedBeanKey(STRINGS.first(), PREFIX_a, null, null);
		List<SortedBean> result3 = router.sortedBeanSorted().getWithPrefix(prefix3, true, null);
		int expectedSize3 = NUM_PREFIX_a * NUM_ELEMENTS * NUM_ELEMENTS;
		Assert.assertEquals(expectedSize3, CollectionTool.size(result3));
		Assert.assertTrue(ListTool.isSorted(result3));
	}
	
	@Test
	public synchronized void testGetWithPrefixes(){
		SortedBeanKey prefixA = new SortedBeanKey(STRINGS.first(), PREFIX_a, null, null);
		SortedBeanKey prefixCh = new SortedBeanKey(STRINGS.first(), PREFIX_ch, null, null);
		List<SortedBeanKey> prefixes = ListTool.create(prefixA, prefixCh);
		List<SortedBean> result = router.sortedBeanSorted().getWithPrefixes(prefixes, true, null);
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
		List<SortedBeanKey> result1 = router.sortedBeanSorted().getKeysInRange(
				alp1, true, emu1, true, null);
		int expectedSize1 = RANGE_LENGTH_alp_emu_inc * NUM_ELEMENTS * NUM_ELEMENTS * NUM_ELEMENTS;
		Assert.assertEquals(expectedSize1, CollectionTool.size(result1));
		Assert.assertTrue(ListTool.isSorted(result1));
		
		List<SortedBeanKey> result1b = router.sortedBeanSorted().getKeysInRange(
				alp1, true, emu1, false, null);
		int expectedSize1b = (RANGE_LENGTH_alp_emu_inc - 1) * NUM_ELEMENTS * NUM_ELEMENTS * NUM_ELEMENTS;
		Assert.assertEquals(expectedSize1b, CollectionTool.size(result1b));
		Assert.assertTrue(ListTool.isSorted(result1b));
		
		SortedBeanKey alp2 = new SortedBeanKey(STRINGS.first(), RANGE_alp, null, null);
		SortedBeanKey emu2 = new SortedBeanKey(STRINGS.first(), RANGE_emu, null, null);
		List<SortedBeanKey> result2 = router.sortedBeanSorted().getKeysInRange(
				alp2, true, emu2, true, null);
		int expectedSize2 = RANGE_LENGTH_alp_emu_inc * NUM_ELEMENTS * NUM_ELEMENTS;
		Assert.assertEquals(expectedSize2, CollectionTool.size(result2));
		Assert.assertTrue(ListTool.isSorted(result2));
	}
	
	@Test
	public synchronized void testGetInRange(){
		SortedBeanKey alp1 = new SortedBeanKey(RANGE_alp, null, null, null);
		SortedBeanKey emu1 = new SortedBeanKey(RANGE_emu, null, null, null);
		List<SortedBean> result1 = router.sortedBeanSorted().getRange(
				alp1, true, emu1, true, null);
		int expectedSize1 = RANGE_LENGTH_alp_emu_inc * NUM_ELEMENTS * NUM_ELEMENTS * NUM_ELEMENTS;
		Assert.assertEquals(expectedSize1, CollectionTool.size(result1));
		Assert.assertTrue(ListTool.isSorted(result1));
		
		List<SortedBean> result1b = router.sortedBeanSorted().getRange(
				alp1, true, emu1, false, null);
		int expectedSize1b = (RANGE_LENGTH_alp_emu_inc-1) * NUM_ELEMENTS * NUM_ELEMENTS * NUM_ELEMENTS;
		Assert.assertEquals(expectedSize1b, CollectionTool.size(result1b));
		Assert.assertTrue(ListTool.isSorted(result1b));
		
		SortedBeanKey alp2 = new SortedBeanKey(STRINGS.first(), RANGE_alp, null, null);
		SortedBeanKey emu2 = new SortedBeanKey(STRINGS.first(), RANGE_emu, null, null);
		List<SortedBean> result2 = router.sortedBeanSorted().getRange(
				alp2, true, emu2, true, null);
		int expectedSize2 = RANGE_LENGTH_alp_emu_inc * NUM_ELEMENTS * NUM_ELEMENTS;
		Assert.assertEquals(expectedSize2, CollectionTool.size(result2));
		Assert.assertTrue(ListTool.isSorted(result2));
	}
	
	@Test
	public synchronized void testPrefixedRange(){
		SortedBeanKey prefix = new SortedBeanKey(PREFIX_a, null, null, null);
		SortedBeanKey al = new SortedBeanKey(RANGE_al, null, null, null);
		List<SortedBean> result1 = router.sortedBeanSorted().getPrefixedRange(
				prefix, true, al, true, null);
		int expectedSize1 = RANGE_LENGTH_al_b * NUM_ELEMENTS * NUM_ELEMENTS * NUM_ELEMENTS;
		Assert.assertEquals(expectedSize1, CollectionTool.size(result1));
		Assert.assertTrue(ListTool.isSorted(result1));
	}

	@Test
	public synchronized void testDelete(){
		int remainingElements = TOTAL_RECORDS;
		
		//delete
		Assert.assertEquals(remainingElements, CollectionTool.size(router.sortedBean().getAll(null)));
		SortedBeanKey key = new SortedBeanKey(STRINGS.last(), STRINGS.last(), 0, STRINGS.last());
		router.sortedBean().delete(key, null);
		--remainingElements;
		Assert.assertEquals(remainingElements, CollectionTool.size(router.sortedBean().getAll(null)));

		//deleteMulti
		Assert.assertEquals(remainingElements, CollectionTool.size(router.sortedBean().getAll(null)));
		List<SortedBeanKey> keys = ListTool.create(
				new SortedBeanKey(STRINGS.last(), STRINGS.last(), 1, STRINGS.last()),
				new SortedBeanKey(STRINGS.last(), STRINGS.last(), 2, STRINGS.last()),
				new SortedBeanKey(STRINGS.last(), STRINGS.last(), 3, STRINGS.last()));
		router.sortedBean().deleteMulti(keys, null);
		remainingElements -= 3;
		Assert.assertEquals(remainingElements, CollectionTool.size(router.sortedBean().getAll(null)));
		
		
		//deleteWithPrefix
		Assert.assertEquals(remainingElements, CollectionTool.size(router.sortedBean().getAll(null)));
		SortedBeanKey prefix = new SortedBeanKey(PREFIX_a, null, null, null);
		router.sortedBeanSorted().deleteRangeWithPrefix(prefix, true, null);
		remainingElements -= NUM_PREFIX_a * NUM_ELEMENTS * NUM_ELEMENTS * NUM_ELEMENTS;
		Assert.assertEquals(remainingElements, CollectionTool.size(router.sortedBean().getAll(null)));

		resetTable(router);//in case this one doesn't run last
	}
	
}





package com.hotpads.datarouter.test.node.basic.prefixed.test;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.config.PutMethod;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.routing.DatarouterContext;
import com.hotpads.datarouter.test.DatarouterTestModuleFactory;
import com.hotpads.datarouter.test.node.basic.prefixed.ScatteringPrefixBean;
import com.hotpads.datarouter.test.node.basic.prefixed.ScatteringPrefixBean.ScatteringPrefixBeanFielder.ScatteringPrefixBeanScatterer;
import com.hotpads.datarouter.test.node.basic.prefixed.ScatteringPrefixBeanKey;
import com.hotpads.datarouter.test.node.basic.prefixed.ScatteringPrefixTestRouter;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrComparableTool;
import com.hotpads.datarouter.util.core.DrIterableTool;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.util.core.collections.Range;

@Guice(moduleFactory=DatarouterTestModuleFactory.class)
public abstract class BaseScatteringPrefixIntegrationTests{

	@Inject
	private DatarouterContext datarouterContext;
	@Inject
	private NodeFactory nodeFactory;
	
	private ScatteringPrefixTestRouter router;

	public void setup(String clientName){
		router = new ScatteringPrefixTestRouter(datarouterContext, nodeFactory, clientName);

		resetTable();
	}


	@AfterClass
	public void afterClass(){
		testDelete();
		datarouterContext.shutdown();
	}

	public void testDelete(){
		router.scatteringPrefixBean().delete(new ScatteringPrefixBeanKey("a", 10L), null);
		AssertJUnit.assertEquals(TOTAL_RECORDS - 1, DrIterableTool.count(router.scatteringPrefixBean().scan(null, null))
				.intValue());
	}

	/********************** setup methods *********************************************/

	public static final List<String> PREFIXES = DrListTool.create("a", "b", "c", "d", "e");

	public static final int
		NUM_BATCHES = 2,
		BATCH_SIZE = 100,
		TOTAL_ROWS = NUM_BATCHES * BATCH_SIZE,
		NUM_OF_EACH_PREFIX = TOTAL_ROWS / PREFIXES.size();

	public static final int TOTAL_RECORDS = NUM_BATCHES * BATCH_SIZE;

	public void resetTable(){
		router.scatteringPrefixBean().deleteAll(null);
		AssertJUnit.assertEquals(0, DrIterableTool.count(router.scatteringPrefixBean().scan(null, null)).intValue());

		List<ScatteringPrefixBean> toSave = new ArrayList<>();
		for(int a = 0; a < NUM_BATCHES; ++a){
			for(int b = 0; b < BATCH_SIZE; ++b){
				String prefix = PREFIXES.get(b % PREFIXES.size());
				long id = a * BATCH_SIZE + b;
				toSave.add(new ScatteringPrefixBean(prefix, id, "abc", (int)id
						% ScatteringPrefixBeanScatterer.NUM_SHARDS));
			}
			// save them every batch to avoid a huge put
			router.scatteringPrefixBean().putMulti(toSave, new Config().setPutMethod(PutMethod.INSERT_OR_BUST));
			toSave.clear();
		}
	}

	/*************************** tests *********************************************/

	@Test
	public void testGetAll(){
		int iterateBatchSize = 2;
		Iterable<ScatteringPrefixBeanKey> iter = router.scatteringPrefixBean().scanKeys(
				null, new Config().setIterateBatchSize(iterateBatchSize));
		Iterable<ScatteringPrefixBeanKey> all = DrIterableTool.createArrayListFromIterable(iter);
		long count = DrIterableTool.count(all);
		AssertJUnit.assertEquals(TOTAL_RECORDS, count);
		AssertJUnit.assertTrue(DrComparableTool.isSorted(all));
	}

	@Test
	public void testGetFirstKey(){
		ScatteringPrefixBeanKey firstKey = router.scatteringPrefixBean().getFirstKey(null);
		AssertJUnit.assertTrue(0==firstKey.getId());
	}

	@Test
	public void testGetFirst(){
		ScatteringPrefixBean firstBean = router.scatteringPrefixBean().getFirst(null);
		AssertJUnit.assertTrue(0==firstBean.getKey().getId());
	}

	@Test
	public void testGetWithPrefix(){
		//first 3 fields fixed
		ScatteringPrefixBeanKey prefix1 = new ScatteringPrefixBeanKey(DrCollectionTool.getFirst(PREFIXES), null);
		List<ScatteringPrefixBean> result1 = router.scatteringPrefixBeanSorted().getWithPrefix(prefix1, false, null);
		AssertJUnit.assertEquals(NUM_OF_EACH_PREFIX, DrCollectionTool.size(result1));
		AssertJUnit.assertTrue(DrListTool.isSorted(result1));
	}

	@Test
	public void testGetWithPrefixes(){
		ScatteringPrefixBeanKey prefix1 = new ScatteringPrefixBeanKey(DrCollectionTool.getFirst(PREFIXES), null);
		ScatteringPrefixBeanKey prefix2 = new ScatteringPrefixBeanKey(PREFIXES.get(2), null);
		List<ScatteringPrefixBeanKey> prefixes = DrListTool.create(prefix1, prefix2);
		List<ScatteringPrefixBean> result = router.scatteringPrefixBeanSorted().getWithPrefixes(prefixes, true, null);
		int expectedSizeTotal = 2 * NUM_OF_EACH_PREFIX;
		AssertJUnit.assertEquals(expectedSizeTotal, DrCollectionTool.size(result));
		AssertJUnit.assertTrue(DrListTool.isSorted(result));
	}

	@Test
	public void testGetKeysInRange(){
		ScatteringPrefixBeanKey a190 = new ScatteringPrefixBeanKey("a", 195L);
		ScatteringPrefixBeanKey b6 = new ScatteringPrefixBeanKey("b", 1L);
		int expectedSize1 = 2;//confusing... just looked at mysql

		Iterable<ScatteringPrefixBeanKey> scanner0 = router.scatteringPrefixBean().scanKeys(
				Range.create(a190, true, b6, true), null);
		List<ScatteringPrefixBeanKey> result0 = DrListTool.createArrayList(scanner0);
		AssertJUnit.assertEquals(expectedSize1, DrCollectionTool.size(result0));
		AssertJUnit.assertTrue(DrListTool.isSorted(result0));

		Range<ScatteringPrefixBeanKey> range1 = new Range<>(a190, true, b6, true);
		List<ScatteringPrefixBeanKey> result1 = DrListTool.createArrayList(router.scatteringPrefixBean().scanKeys(
				range1, null));
		AssertJUnit.assertEquals(expectedSize1, DrCollectionTool.size(result1));
		AssertJUnit.assertTrue(DrListTool.isSorted(result1));
	}

	@Test
	public void testGetInRange(){
		ScatteringPrefixBeanKey a190 = new ScatteringPrefixBeanKey("a", 190L);
		ScatteringPrefixBeanKey b6 = new ScatteringPrefixBeanKey("b", 6L);
		Range<ScatteringPrefixBeanKey> range1 = new Range<>(a190, true, b6, true);
		List<ScatteringPrefixBean> result1 = DrListTool.createArrayList(router.scatteringPrefixBean().scan(
				range1, null));
		int expectedSize1 = 4;//confusing... just looked at mysql
		AssertJUnit.assertEquals(expectedSize1, DrCollectionTool.size(result1));
		AssertJUnit.assertTrue(DrListTool.isSorted(result1));
	}

}





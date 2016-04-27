package com.hotpads.datarouter.test;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.factory.EntityNodeFactory;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.test.node.basic.sorted.SortedBean;
import com.hotpads.datarouter.test.node.basic.sorted.SortedBeanEntityNode;
import com.hotpads.datarouter.test.node.basic.sorted.SortedBeans;
import com.hotpads.datarouter.test.node.basic.sorted.SortedNodeTestRouter;
import com.hotpads.datarouter.util.core.DrIterableTool;
import com.hotpads.util.core.iterable.BatchingIterable;

@Guice(moduleFactory=DatarouterStorageTestModuleFactory.class)
public class HBaseEntityNodeIntegrationTests{
	@Inject
	private Datarouter datarouter;
	@Inject
	private EntityNodeFactory entityNodeFactory;
	@Inject
	private NodeFactory nodeFactory;

	private SortedBeanEntityNode sortedBeanEntityNode;
	private List<SortedBean> sortedBeans;

	@BeforeClass
	public void beforeClass(){
		SortedNodeTestRouter router = new SortedNodeTestRouter(datarouter, entityNodeFactory,
				SortedBeanEntityNode.ENTITY_NODE_PARAMS_3, nodeFactory, DrTestConstants.CLIENT_drTestHBase, true, true);
		sortedBeanEntityNode = router.sortedBeanEntity();

		for(List<SortedBean> batch : new BatchingIterable<>(SortedBeans.generatedSortedBeans(), 1000)){
			sortedBeanEntityNode.sortedBean().putMulti(batch, null);
		}

		sortedBeans = new LinkedList<>();
		String prefix = "testScanForRowDatabean1to1";
		for(int i = 1; i< 6; i++){
			sortedBeans.add(new SortedBean(prefix + "-" + i, prefix + "-2-" + i, i, prefix + "-4-" + i,
					"string so hbase has at least one field", null, null, null));
		}

		//inserted 5 new rows with 1-1 mapping with datarouter databeans
		sortedBeanEntityNode.sortedBean().putMulti(sortedBeans, null);
	}

	@AfterClass
	public void afterClass(){
		sortedBeanEntityNode.sortedBean().deleteAll(null);
		datarouter.shutdown();
	}

	@Test
	//this tests scan when there are hbase rows which have 1-1 mapping with datarouter (entity) databeans
	public void testScanForRowDatabean1to1(){
		Config config = new Config().setIterateBatchSize(2);
		Iterable<SortedBean> iterable = sortedBeanEntityNode.sortedBean().scan(null, config);
		long count = DrIterableTool.count(iterable);
		Assert.assertEquals(count, SortedBeans.TOTAL_RECORDS + sortedBeans.size());
	}

}
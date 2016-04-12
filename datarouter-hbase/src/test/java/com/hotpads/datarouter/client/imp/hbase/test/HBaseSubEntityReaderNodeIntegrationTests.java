package com.hotpads.datarouter.client.imp.hbase.test;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import com.hotpads.datarouter.node.factory.EntityNodeFactory;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.storage.databean.DatabeanTool;
import com.hotpads.datarouter.test.DatarouterStorageTestModuleFactory;
import com.hotpads.datarouter.test.DrTestConstants;
import com.hotpads.datarouter.test.node.basic.sorted.SortedBean;
import com.hotpads.datarouter.test.node.basic.sorted.SortedBeanEntityNode;
import com.hotpads.datarouter.test.node.basic.sorted.SortedBeanKey;
import com.hotpads.datarouter.test.node.basic.sorted.SortedNodeTestRouter;
import com.hotpads.util.core.collections.Range;

@Guice(moduleFactory=DatarouterStorageTestModuleFactory.class)
public class HBaseSubEntityReaderNodeIntegrationTests{

	@Inject
	protected Datarouter datarouter;
	@Inject
	private EntityNodeFactory entityNodeFactory;
	@Inject
	private NodeFactory nodeFactory;

	private SortedMapStorage<SortedBeanKey,SortedBean> sortedBean;
	private List<SortedBean> sortedBeans;

	@BeforeClass
	public void setup(){
		//Use SortedBeanEntityNode.ENTITY_NODE_PARAMS_2 to avoid conflicting with HBaseEntitySortedNodeIntegrationTests
		SortedNodeTestRouter router = new SortedNodeTestRouter(datarouter, entityNodeFactory,
				SortedBeanEntityNode.ENTITY_NODE_PARAMS_2, nodeFactory, DrTestConstants.CLIENT_drTestHBase, true, true);
		sortedBean = router.sortedBean();

		sortedBeans = new ArrayList<>();
		sortedBeans.add(new SortedBean("a", "b", 1, "d", "f1", 2L, "f3", 4D));
		sortedBeans.add(new SortedBean("a", "b", 2, "d", "f1", 2L, "f3", 4D));
		sortedBeans.add(new SortedBean("a", "b", 25, "d", "f1", 2L, "f3", 4D));
		sortedBeans.add(new SortedBean("a", "b", 3, "d", "f1", 2L, "f3", 4D));
		sortedBeans.add(new SortedBean("a", "b", 4, "d", "f1", 2L, "f3", 4D));
		sortedBeans.add(new SortedBean("a", "b", 4, "dj", "f1", 2L, "f3", 4D));
		sortedBeans.add(new SortedBean("a", "b", 4, "e", "f1", 2L, "f3", 4D));
		sortedBeans.add(new SortedBean("a", "c", 1, "d", "f1", 2L, "f3", 4D));
		sortedBeans.add(new SortedBean("a", "c", 2, "d", "f1", 2L, "f3", 4D));
		sortedBeans.add(new SortedBean("a", "c", 2, "dd", "f1", 2L, "f3", 4D));
		sortedBeans.add(new SortedBean("b", "b", 1, "d", "f1", 2L, "f3", 4D));
		sortedBeans.add(new SortedBean("c", "b", 1, "d", "f1", 2L, "f3", 4D));
		sortedBean.putMulti(sortedBeans, null);

	}

	@Test
	public void testNotDefinedEntityScan(){
		Assert.assertEquals(sortedBean.stream(new Range<>(new SortedBeanKey("a", null, null, null), true,
				new SortedBeanKey("b", null, null, null), true), null).count(), 11);

		Assert.assertEquals(sortedBean.stream(new Range<>(new SortedBeanKey("a", null, null, null), new SortedBeanKey(
				"a", "c", null, null)), null).count(), 7);

		Assert.assertEquals(sortedBean.streamWithPrefix(new SortedBeanKey("a", null, null, null), null).count(), 10);
	}

	@Test
	public void testSingleEntityScan(){
		Assert.assertEquals(sortedBean.streamWithPrefix(new SortedBeanKey("a", "c", null, null), null).count(), 3);
		Assert.assertEquals(sortedBean.streamWithPrefix(new SortedBeanKey("a", "c", 2, null), null).count(), 2);
		Assert.assertEquals(sortedBean.streamWithPrefix(new SortedBeanKey("a", "b", 2, null), null).count(), 1);
		Assert.assertEquals(sortedBean.streamWithPrefix(new SortedBeanKey("a", "b", 4, "d"), null).count(), 1);
		Assert.assertEquals(sortedBean.stream(new Range<>(new SortedBeanKey("a", "c", 1, null), new SortedBeanKey("a",
				"c", 2, null)), null).count(), 1);
		Assert.assertEquals(sortedBean.stream(new Range<>(new SortedBeanKey("a", "c", 1, null), true, new SortedBeanKey(
				"a", "c", 2, null), true), null).count(), 3);

		//test wildcardLastField
		Assert.assertEquals(sortedBean.getWithPrefix(new SortedBeanKey("a", "b", 4, "d"), false, null).size(), 1);
		Assert.assertEquals(sortedBean.getWithPrefix(new SortedBeanKey("a", "b", 4, "d"), true, null).size(), 2);
	}

	@AfterClass
	public void cleanup(){
		sortedBean.deleteMulti(DatabeanTool.getKeys(sortedBeans), null);
	}

}

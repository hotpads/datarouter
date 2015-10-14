package com.hotpads.datarouter.client.imp.hbase.test;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.testng.Assert;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.node.entity.EntityNode;
import com.hotpads.datarouter.test.DrTestConstants;
import com.hotpads.datarouter.test.node.basic.sorted.BaseSortedNodeIntegrationTests;
import com.hotpads.datarouter.test.node.basic.sorted.SortedBean;
import com.hotpads.datarouter.test.node.basic.sorted.SortedBeanEntity;
import com.hotpads.datarouter.test.node.basic.sorted.SortedBeanEntityKey;
import com.hotpads.datarouter.test.node.basic.sorted.SortedBeanEntityNode;
import com.hotpads.datarouter.test.node.basic.sorted.SortedBeanKey;
import com.hotpads.datarouter.test.node.basic.sorted.SortedBeans;
import com.hotpads.datarouter.util.core.DrCollectionTool;

public class HBaseEntitySortedNodeIntegrationTests extends BaseSortedNodeIntegrationTests{

	private SortedBeanEntityNode sortedBeanEntityNode;

	@BeforeClass
	public void beforeClass(){
		setup(DrTestConstants.CLIENT_drTestHBase, true, true);
		this.sortedBeanEntityNode = router.sortedBeanEntity();
	}

	@AfterClass
	public void afterClass(){
		testSortedDelete();
		datarouter.shutdown();
	}

	@Test
	public void testGetEntity(){
		SortedBeanEntityKey ek1 = new SortedBeanEntityKey(SortedBeans.S_albatross, SortedBeans.S_ostrich);
		EntityNode<SortedBeanEntityKey,SortedBeanEntity> entityNode = sortedBeanEntityNode.entity();
		SortedBeanEntity albatrossOstrich = entityNode.getEntity(ek1, null);
		int numExpected = SortedBeans.NUM_ELEMENTS * SortedBeans.NUM_ELEMENTS;
		Collection<SortedBean> results = albatrossOstrich.getSortedBeans();
		AssertJUnit.assertEquals(numExpected, results.size());
		AssertJUnit.assertEquals(SortedBeans.S_albatross, DrCollectionTool.getFirst(results).getA());
		AssertJUnit.assertEquals(SortedBeans.S_ostrich, DrCollectionTool.getFirst(results).getB());
	}

	@Test
	private void testSingleEntityScan(){
		SortedBeanKey twoFieldsPk = new SortedBeanKey(SortedBeans.S_albatross, SortedBeans.S_ostrich, null, null);
		SortedBeanKey threeFieldsPk = new SortedBeanKey(SortedBeans.S_albatross, SortedBeans.S_ostrich, 0, null);

		final int limit = 23;
		Assert.assertEquals(sortedNode.streamKeysWithPrefix(threeFieldsPk, new Config().setLimit(limit)).count(),
				SortedBeans.NUM_ELEMENTS);
		Assert.assertEquals(sortedNode.streamWithPrefix(threeFieldsPk, new Config().setLimit(limit)).count(),
				SortedBeans.NUM_ELEMENTS);

		Assert.assertEquals(sortedNode.streamKeysWithPrefix(twoFieldsPk, new Config().setLimit(limit)).count(), limit);
		Assert.assertEquals(sortedNode.streamWithPrefix(twoFieldsPk, new Config().setLimit(limit)).count(), limit);

		final int offset = 57;
		List<SortedBeanKey> limitedOffset = sortedNode
				.streamKeysWithPrefix(twoFieldsPk, new Config().setOffset(offset).setLimit(1))
				.collect(Collectors.toList());
		Assert.assertEquals(limitedOffset.size(), 1);
		Assert.assertEquals(limitedOffset.get(0), sortedNode.streamKeysWithPrefix(twoFieldsPk, new Config()
				.setOffset(offset)).findFirst().get());
	}

}
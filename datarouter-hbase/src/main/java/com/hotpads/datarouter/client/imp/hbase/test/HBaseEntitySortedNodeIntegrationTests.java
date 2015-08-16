package com.hotpads.datarouter.client.imp.hbase.test;

import java.util.Collection;

import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

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
import com.hotpads.datarouter.util.core.DrIterableTool;
import com.hotpads.util.core.collections.Range;


public class HBaseEntitySortedNodeIntegrationTests extends BaseSortedNodeIntegrationTests{

	protected SortedBeanEntityNode sortedBeanEntityNode;
	
	@BeforeClass
	public void beforeClass(){
		setup(DrTestConstants.CLIENT_drTestHBase, true, true);
		this.sortedBeanEntityNode = router.sortedBeanEntity();
	}

	@AfterClass
	public void afterClass(){
		testSortedDelete();
		drContext.shutdown();
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
	

	@Test //regression test for HBaseSubEntityQueryBuilder.getRowRange which loaded results from the next partition
	public void testNullEndKey(){
		Range<SortedBeanKey> range = new Range<>(null, true, null, true);
		Iterable<SortedBeanKey> iterable = sortedNode.scanKeys(range, null);
		long numDatabeans = DrIterableTool.count(iterable);
		AssertJUnit.assertEquals(SortedBeans.TOTAL_RECORDS, numDatabeans);
	}
}
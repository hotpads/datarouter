package com.hotpads.datarouter.client.imp.hbase.test;

import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.hotpads.datarouter.test.DRTestConstants;
import com.hotpads.datarouter.test.node.basic.sorted.BaseSortedNodeIntegrationTests;
import com.hotpads.datarouter.test.node.basic.sorted.SortedBeanEntity;
import com.hotpads.datarouter.test.node.basic.sorted.SortedBeanEntityKey;
import com.hotpads.datarouter.test.node.basic.sorted.SortedBeans;
import com.hotpads.datarouter.util.core.DrCollectionTool;


public class HBaseEntitySortedNodeIntegrationTests extends BaseSortedNodeIntegrationTests{

	@BeforeClass
	public static void beforeClass(){
		setup(DRTestConstants.CLIENT_drTestHBase, true, true);
	}

	@AfterClass
	public static void afterClass(){
		testSortedDelete();
		drContext.shutdown();
	}
	


	@Test
	public void testGetEntity(){
		SortedBeanEntityKey ek1 = new SortedBeanEntityKey(SortedBeans.S_albatross, SortedBeans.S_ostrich);
		SortedBeanEntity albatrossOstrich = entityNode.getEntity(ek1, null);
		int numExpected = SortedBeans.NUM_ELEMENTS * SortedBeans.NUM_ELEMENTS;
		AssertJUnit.assertEquals(numExpected, albatrossOstrich.getSortedBeans().size());
		AssertJUnit.assertEquals(SortedBeans.S_albatross, DrCollectionTool.getFirst(albatrossOstrich.getSortedBeans()).getA());
		AssertJUnit.assertEquals(SortedBeans.S_ostrich, DrCollectionTool.getFirst(albatrossOstrich.getSortedBeans()).getB());
	}
}
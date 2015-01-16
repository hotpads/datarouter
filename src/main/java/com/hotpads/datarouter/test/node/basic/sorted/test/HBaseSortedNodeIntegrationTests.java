package com.hotpads.datarouter.test.node.basic.sorted.test;

import org.junit.BeforeClass;

import com.hotpads.datarouter.client.imp.hbase.HBaseClientType;
import com.hotpads.datarouter.test.DRTestConstants;
import com.hotpads.datarouter.test.node.basic.sorted.BaseSortedNodeIntegrationTests;


public class HBaseSortedNodeIntegrationTests extends BaseSortedNodeIntegrationTests{
	
	@BeforeClass
	public static void beforeClass(){
		setup(DRTestConstants.CLIENT_drTestHBase, HBaseClientType.INSTANCE, true, false);
	}
	
}
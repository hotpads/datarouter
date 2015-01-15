package com.hotpads.datarouter.test.node.basic.sorted.test;

import org.junit.BeforeClass;

import com.hotpads.datarouter.client.imp.hibernate.HibernateClientType;
import com.hotpads.datarouter.test.DRTestConstants;

/************************** subclasses ***********************************/

public class HibernateSortedNodeIntegrationTests extends BaseSortedNodeIntegrationTests{
	
	@BeforeClass
	public static void beforeClass(){
		setup(DRTestConstants.CLIENT_drTestHibernate0, HibernateClientType.INSTANCE, false, false);
	}
	
}
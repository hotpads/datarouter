package com.hotpads.datarouter.test.client.txn.test;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.hotpads.datarouter.config.Isolation;
import com.hotpads.datarouter.routing.DataRouterContext;
import com.hotpads.datarouter.test.client.BasicClientTestRouter;
import com.hotpads.datarouter.test.client.txn.TxnBean;
import com.hotpads.datarouter.test.client.txn.txnapp.InsertRollback;
import com.hotpads.datarouter.test.client.txn.txnapp.MultiInsertRollback;
import com.hotpads.datarouter.test.client.txn.txnapp.NestedTxn;
import com.hotpads.util.core.CollectionTool;

public class TxnIntegrationTests {
	private Logger logger = Logger.getLogger(TxnIntegrationTests.class);
	
	private BasicClientTestRouter router;
	private DataRouterContext drContext;
	
	
	
	public TxnIntegrationTests(){
		Injector injector = Guice.createInjector();
		drContext = injector.getInstance(DataRouterContext.class);
		router = injector.getInstance(BasicClientTestRouter.class);
	}

//	@BeforeClass
//	public static void init(){
//		Injector injector = Guice.createInjector();
//		router = injector.getInstance(BasicClientTestRouter.class);
//		resetTable();
//	}
	
	public void resetTable(){
		router.txnBeanHibernate().deleteAll(null);
		Assert.assertEquals(0, CollectionTool.size(router.txnBeanHibernate().getAll(null)));
	}
	

	/************ InsertRollback *********************/
	
	@Test 
	public void testInsertRollbackNoFlush(){	
		resetTable();	
		int numExceptions = 0;
		try{
			router.run(new InsertRollback(drContext, drContext.getClientPool().getClientNames(), Isolation.readCommitted, 
					router, false));
		}catch(RuntimeException re){
			++numExceptions;
		}
		Assert.assertEquals(1, numExceptions);
		Assert.assertEquals(0, CollectionTool.size(router.txnBeanHibernate().getAll(null)));
	}
	
	@Test 
	public void testInsertRollbackWithFlush(){		
		resetTable();
		int numExceptions = 0;
		try{
			router.run(new InsertRollback(drContext, drContext.getClientPool().getClientNames(), Isolation.readCommitted,
					router, true));
		}catch(RuntimeException re){
			++numExceptions;
		}
		Assert.assertEquals(1, numExceptions);
		Assert.assertEquals(0, CollectionTool.size(router.txnBeanHibernate().getAll(null)));
	}

	
	/************ MultiInsertRollback *********************/
	
	@Test 
	public void testMoreComplexInsertRollbackNoFlush(){		
		resetTable();
		int numExceptions = 0;
		TxnBean b = new TxnBean("b");
		router.txnBeanHibernate().put(b, null);
		Assert.assertEquals(1, CollectionTool.size(router.txnBeanHibernate().getAll(null)));
		try{
			router.run(new MultiInsertRollback(drContext, drContext.getClientPool().getClientNames(), 
					Isolation.readCommitted, router, false));
		}catch(RuntimeException re){
			++numExceptions;
		}
		Assert.assertEquals(1, numExceptions);
		Assert.assertEquals(1, CollectionTool.size(router.txnBeanHibernate().getAll(null)));
	}
	
	@Test 
	public void testMoreComplexInsertRollbackWithFlush(){		
		resetTable();
		int numExceptions = 0;
		TxnBean b = new TxnBean("b");
		router.txnBeanHibernate().put(b, null);
		Assert.assertEquals(1, CollectionTool.size(router.txnBeanHibernate().getAll(null)));
		try{
			router.run(new MultiInsertRollback(drContext, drContext.getClientPool().getClientNames(), 
					Isolation.readCommitted, router, true));
		}catch(RuntimeException re){
			++numExceptions;
		}
		Assert.assertEquals(1, numExceptions);
		Assert.assertEquals(1, CollectionTool.size(router.txnBeanHibernate().getAll(null)));
	}

	
	/************ NestedTxn *********************/
	
	@Test 
	public void testNestedTxn(){
		logger.warn("testNestedTxn()");
		resetTable();	
		int numExceptions = 0;
		try{
			router.run(new NestedTxn(drContext, drContext.getClientPool().getClientNames(), Isolation.readCommitted,
					router, false));
		}catch(RuntimeException re){
			++numExceptions;
		}
		Assert.assertEquals(1, numExceptions);
		Assert.assertEquals(0, CollectionTool.size(router.txnBeanHibernate().getAll(null)));
	}
}








package com.hotpads.datarouter.test.client.txn.test;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.hotpads.datarouter.client.imp.hibernate.op.BaseHibernateOp;
import com.hotpads.datarouter.config.Isolation;
import com.hotpads.datarouter.routing.DataRouterContext;
import com.hotpads.datarouter.test.DRTestConstants;
import com.hotpads.datarouter.test.client.BasicClientTestRouter;
import com.hotpads.datarouter.test.client.BasicClientTestRouterImp;
import com.hotpads.datarouter.test.client.txn.TxnBean;
import com.hotpads.datarouter.test.client.txn.txnapp.InsertRollback;
import com.hotpads.datarouter.test.client.txn.txnapp.MultiInsertRollback;
import com.hotpads.datarouter.test.client.txn.txnapp.NestedTxn;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;

public class TxnIntegrationTests {
	private static Logger logger = LoggerFactory.getLogger(TxnIntegrationTests.class);
	
	private BasicClientTestRouter router;
	private DataRouterContext drContext;
	private String clientName = DRTestConstants.CLIENT_drTestHibernate0;
//	private String clientName = DRTestConstants.CLIENT_drTestJdbc0;
	
	
	
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
		Assert.assertEquals(0, IterableTool.count(router.txnBeanHibernate().scan(null, null)).intValue());
		logger.warn("resetTable complete");
	}
	

	/************ InsertRollback *********************/
	
	@Test 
	public void testInsertRollbackNoFlush(){	
		resetTable();	
		int numExceptions = 0;
		try{
			router.run(new InsertRollback(drContext, ListTool.wrap(clientName), Isolation.readCommitted, 
					router, false));
		}catch(RuntimeException re){
			++numExceptions;
		}
		Assert.assertEquals(1, numExceptions);
		Assert.assertEquals(0, IterableTool.count(router.txnBeanHibernate().scan(null, null)).intValue());
	}
	
	@Test 
	public void testInsertRollbackWithFlush(){		
		resetTable();
		int numExceptions = 0;
		try{
			BaseHibernateOp<Void> op = new InsertRollback(drContext, ListTool.wrap(clientName), Isolation.readCommitted,
					router, true);
			router.run(op);
		}catch(RuntimeException re){
			logger.warn("", re);
			++numExceptions;
		}
		Assert.assertEquals(1, numExceptions);
		Assert.assertEquals(0, IterableTool.count(router.txnBeanHibernate().scan(null, null)).intValue());
	}

	
	/************ MultiInsertRollback *********************/
	
	@Test 
	public void testMoreComplexInsertRollbackNoFlush(){		
		resetTable();
		int numExceptions = 0;
		TxnBean b = new TxnBean("b");
		router.txnBeanHibernate().put(b, null);
		Assert.assertEquals(1, IterableTool.count(router.txnBeanHibernate().scan(null, null)).intValue());
		try{
			router.run(new MultiInsertRollback(drContext, ListTool.wrap(clientName), 
					Isolation.readCommitted, router, false));
		}catch(RuntimeException re){
			++numExceptions;
		}
		Assert.assertEquals(1, numExceptions);
		Assert.assertEquals(1, IterableTool.count(router.txnBeanHibernate().scan(null, null)).intValue());
	}
	
	@Test 
	public void testMoreComplexInsertRollbackWithFlush(){		
		resetTable();
		int numExceptions = 0;
		TxnBean b = new TxnBean("b");
		router.txnBeanHibernate().put(b, null);
		Assert.assertEquals(1, IterableTool.count(router.txnBeanHibernate().scan(null, null)).intValue());
		try{
			router.run(new MultiInsertRollback(drContext, ListTool.wrap(clientName), 
					Isolation.readCommitted, router, true));
		}catch(RuntimeException re){
			++numExceptions;
		}
		Assert.assertEquals(1, numExceptions);
		Assert.assertEquals(1, IterableTool.count(router.txnBeanHibernate().scan(null, null)).intValue());
	}

	
	/************ NestedTxn *********************/
	
	@Test 
	public void testNestedTxn(){
		logger.warn("testNestedTxn()");
		resetTable();	
		int numExceptions = 0;
		try{
			router.run(new NestedTxn(drContext, ListTool.wrap(clientName), 
					Isolation.readCommitted, false, router, false));
		}catch(RuntimeException re){
			++numExceptions;
		}
		Assert.assertEquals(1, numExceptions);
		Assert.assertEquals(0, IterableTool.count(router.txnBeanHibernate().scan(null, null)).intValue());
	}
	
	public static void main(String... args){
		BasicClientTestRouter router = new BasicClientTestRouterImp(new DataRouterContext());
//		router.txnBeanHibernate().get(new TxnBeanKey("abc"), null);
		TxnBean b = new TxnBean("b");
		router.txnBeanHibernate().put(b, null);
	}
}








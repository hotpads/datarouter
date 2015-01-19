package com.hotpads.datarouter.test.client.txn.test;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;
import com.hotpads.datarouter.client.imp.hibernate.op.BaseHibernateOp;
import com.hotpads.datarouter.config.Isolation;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import com.hotpads.datarouter.routing.DatarouterContext;
import com.hotpads.datarouter.test.DatarouterTestInjectorProvider;
import com.hotpads.datarouter.test.client.txn.TxnBean;
import com.hotpads.datarouter.test.client.txn.TxnBeanKey;
import com.hotpads.datarouter.test.client.txn.TxnTestRouter;
import com.hotpads.datarouter.test.client.txn.txnapp.InsertRollback;
import com.hotpads.datarouter.test.client.txn.txnapp.MultiInsertRollback;
import com.hotpads.datarouter.test.client.txn.txnapp.NestedTxn;
import com.hotpads.util.core.IterableTool;
import com.hotpads.util.core.ListTool;

public abstract class BaseTxnIntegrationTests {
	private static final Logger logger = LoggerFactory.getLogger(BaseTxnIntegrationTests.class);

	private static DatarouterContext drContext;
	private static String clientName;
	private static TxnTestRouter router;
	private static SortedMapStorageNode<TxnBeanKey,TxnBean> node;
	

	protected static void setup(String pClientName, boolean useFielder){
		Injector injector = new DatarouterTestInjectorProvider().get();
		drContext = injector.getInstance(DatarouterContext.class);
		clientName = pClientName;
		NodeFactory nodeFactory = injector.getInstance(NodeFactory.class);
		router = new TxnTestRouter(drContext, nodeFactory, clientName, useFielder);
		node = router.txnBean();
	}
	
	@AfterClass
	public static void afterClass(){
		drContext.shutdown();
	}
	
	protected static void resetTable(){
		router.txnBean().deleteAll(null);
		Assert.assertEquals(0, IterableTool.count(node.scan(null, null)).intValue());
	}
	

	/************ InsertRollback *********************/
	
	@Test 
	public void testInsertRollbackNoFlush(){	
		int numExceptions = 0;
		String beanPrefix = "a";
		try{
			router.run(new InsertRollback(drContext, ListTool.wrap(clientName), Isolation.readCommitted, 
					router, false, beanPrefix));
		}catch(RuntimeException re){
			++numExceptions;
		}
		Assert.assertEquals(1, numExceptions);
		Assert.assertFalse(node.exists(new TxnBeanKey(beanPrefix + "1"), null));
	}
	
	@Test 
	public void testInsertRollbackWithFlush(){		
		int numExceptions = 0;
		String beanPrefix = "b";
		try{
			BaseHibernateOp<Void> op = new InsertRollback(drContext, ListTool.wrap(clientName), Isolation.readCommitted,
					router, true, beanPrefix);
			router.run(op);
		}catch(RuntimeException re){
			logger.warn("", re);
			++numExceptions;
		}
		Assert.assertEquals(1, numExceptions);
		Assert.assertFalse(node.exists(new TxnBeanKey(beanPrefix + "1"), null));
	}

	
	/************ MultiInsertRollback *********************/
	
	@Test 
	public void testMoreComplexInsertRollbackNoFlush(){		
		int numExceptions = 0;
		String beanPrefix = "c";
		TxnBean b = new TxnBean(beanPrefix + "1");
		node.put(b, null);
		Assert.assertTrue(router.txnBean().exists(b.getKey(), null));
		try{
			router.run(new MultiInsertRollback(drContext, ListTool.wrap(clientName), 
					Isolation.readCommitted, router, false, beanPrefix));
		}catch(RuntimeException re){
			++numExceptions;
		}
		Assert.assertEquals(1, numExceptions);
		Assert.assertTrue(router.txnBean().exists(b.getKey(), null));
	}
	
	@Test 
	public void testMoreComplexInsertRollbackWithFlush(){		
		int numExceptions = 0;
		String beanPrefix = "d";
		TxnBean b = new TxnBean(beanPrefix + "1");
		node.put(b, null);
		Assert.assertTrue(router.txnBean().exists(b.getKey(), null));
		try{
			router.run(new MultiInsertRollback(drContext, ListTool.wrap(clientName), 
					Isolation.readCommitted, router, true, beanPrefix));
		}catch(RuntimeException re){
			++numExceptions;
		}
		Assert.assertEquals(1, numExceptions);
		Assert.assertTrue(router.txnBean().exists(b.getKey(), null));
		Assert.assertFalse(router.txnBean().exists(new TxnBeanKey(beanPrefix + "2"), null));
		Assert.assertFalse(router.txnBean().exists(new TxnBeanKey(beanPrefix + "3"), null));
	}

	
	/************ NestedTxn *********************/
	
	@Test 
	public void testNestedTxn(){
		int numExceptions = 0;
		try{
			router.run(new NestedTxn(drContext, ListTool.wrap(clientName), 
					Isolation.readCommitted, false, router, false));
		}catch(RuntimeException re){
			++numExceptions;
		}
		Assert.assertEquals(1, numExceptions);
		Assert.assertFalse(router.txnBean().exists(new TxnBeanKey("outer"), null));
	}
}








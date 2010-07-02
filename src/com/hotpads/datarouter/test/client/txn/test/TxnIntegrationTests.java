package com.hotpads.datarouter.test.client.txn.test;

import java.io.IOException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.hotpads.datarouter.test.client.BasicClientTestRouter;
import com.hotpads.datarouter.test.client.txn.InsertRollback;
import com.hotpads.datarouter.test.client.txn.MultiInsertRollback;
import com.hotpads.datarouter.test.client.txn.TxnBean;
import com.hotpads.util.core.CollectionTool;

public class TxnIntegrationTests {
	
	static BasicClientTestRouter router;

	public static void resetTable(){
		router.txnBean().deleteAll(null);
		Assert.assertEquals(0, CollectionTool.size(router.txnBean().getAll(null)));
	}
	
	@BeforeClass
	public static void init() throws IOException{
		Injector injector = Guice.createInjector();
		router = injector.getInstance(BasicClientTestRouter.class);
		
		resetTable();
	}
	
	
	
	@Test 
	public void testInsertRollbackNoFlush(){	
		resetTable();	
		int numExceptions = 0;
		try{
			router.run(new InsertRollback(router, false));
		}catch(RuntimeException re){
			++numExceptions;
		}
		Assert.assertTrue(1==numExceptions);
		Assert.assertEquals(0, CollectionTool.size(router.txnBean().getAll(null)));
	}
	
	@Test 
	public void testInsertRollbackWithFlush(){		
		resetTable();
		int numExceptions = 0;
		try{
			router.run(new InsertRollback(router, true));
		}catch(RuntimeException re){
			++numExceptions;
		}
		Assert.assertTrue(1==numExceptions);
		Assert.assertEquals(0, CollectionTool.size(router.txnBean().getAll(null)));
	}

	

	
	
	@Test 
	public void testMoreComplexInsertRollbackNoFlush(){		
		resetTable();
		int numExceptions = 0;
		TxnBean b = new TxnBean("b");
		router.txnBean().put(b, null);
		Assert.assertEquals(1, CollectionTool.size(router.txnBean().getAll(null)));
		try{
			router.run(new MultiInsertRollback(router, false));
		}catch(RuntimeException re){
			++numExceptions;
		}
		Assert.assertTrue(1==numExceptions);
		Assert.assertEquals(1, CollectionTool.size(router.txnBean().getAll(null)));
	}
	
	
	@Test 
	public void testMoreComplexInsertRollbackWithFlush(){		
		resetTable();
		int numExceptions = 0;
		TxnBean b = new TxnBean("b");
		router.txnBean().put(b, null);
		Assert.assertEquals(1, CollectionTool.size(router.txnBean().getAll(null)));
		try{
			router.run(new MultiInsertRollback(router, true));
		}catch(RuntimeException re){
			++numExceptions;
		}
		Assert.assertTrue(1==numExceptions);
		Assert.assertEquals(1, CollectionTool.size(router.txnBean().getAll(null)));
	}
	
}








package com.hotpads.datarouter.test.client.txn.test;

import javax.inject.Inject;

import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.config.Isolation;
import com.hotpads.datarouter.node.factory.NodeFactory;
import com.hotpads.datarouter.node.op.combo.SortedMapStorage.SortedMapStorageNode;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.test.client.txn.TxnBean;
import com.hotpads.datarouter.test.client.txn.TxnBeanKey;
import com.hotpads.datarouter.test.client.txn.TxnTestRouter;
import com.hotpads.datarouter.test.client.txn.txnapp.TestInsertRollback;
import com.hotpads.datarouter.test.client.txn.txnapp.TestMultiInsertRollback;
import com.hotpads.datarouter.test.client.txn.txnapp.TestNestedTxn;
import com.hotpads.datarouter.util.core.DrIterableTool;
import com.hotpads.datarouter.util.core.DrListTool;

public abstract class BaseTxnIntegrationTests {

	@Inject
	private Datarouter datarouter;
	@Inject
	private NodeFactory nodeFactory;

	private ClientId clientId;
	private TxnTestRouter router;
	private SortedMapStorageNode<TxnBeanKey,TxnBean> node;


	protected void setup(ClientId clientId, boolean useFielder){
		this.clientId = clientId;
		router = new TxnTestRouter(datarouter, nodeFactory, clientId, useFielder);
		node = router.txnBean();
	}

	@AfterClass
	public void afterClass(){
		datarouter.shutdown();
	}

	protected void resetTable(){
		router.txnBean().deleteAll(null);
		AssertJUnit.assertEquals(0, DrIterableTool.count(node.scan(null, null)).intValue());
	}

	/***************** override these in subclasses **************/

	protected boolean hasSession(){
		return false;
	}


	/************ InsertRollback *********************/

	@Test
	public void testInsertRollback(){
		int numExceptions = 0;
		String beanPrefix = "a";
		try{
			datarouter.run(new TestInsertRollback(datarouter, DrListTool.wrap(clientId.getName()),
					Isolation.readCommitted, router, beanPrefix));
		}catch(RuntimeException re){
			++numExceptions;
		}
		AssertJUnit.assertEquals(1, numExceptions);
		AssertJUnit.assertFalse(node.exists(new TxnBeanKey(beanPrefix + "1"), null));
	}


	/************ MultiInsertRollback *********************/

	@Test
	public void testMoreComplexInsertRollbackNoFlush(){
		testMoreComplexInsertRollbackWithBeanPrefix("c");
	}

	@Test
	public void testMoreComplexInsertRollback(){
		if(!hasSession()){//can't flush without a session
			return;
		}
		testMoreComplexInsertRollbackWithBeanPrefix("d");
	}

	private void testMoreComplexInsertRollbackWithBeanPrefix(String beanPrefix){
		int numExceptions = 0;
		TxnBean bean = new TxnBean(beanPrefix + "1");
		node.put(bean, null);
		AssertJUnit.assertTrue(router.txnBean().exists(bean.getKey(), null));
		try{
			datarouter.run(new TestMultiInsertRollback(datarouter, DrListTool.wrap(clientId.getName()),
					Isolation.readCommitted, router, beanPrefix));
		}catch(RuntimeException re){
			++numExceptions;
		}
		AssertJUnit.assertEquals(1, numExceptions);
		AssertJUnit.assertTrue(router.txnBean().exists(bean.getKey(), null));
		AssertJUnit.assertFalse(router.txnBean().exists(new TxnBeanKey(beanPrefix + "2"), null));
		AssertJUnit.assertFalse(router.txnBean().exists(new TxnBeanKey(beanPrefix + "3"), null));
	}


	/************ NestedTxn *********************/

	@Test
	public void testNestedTxn(){
		int numExceptions = 0;
		try{
			datarouter.run(new TestNestedTxn(datarouter, DrListTool.wrap(clientId.getName()), Isolation.readCommitted,
					false, router));
		}catch(RuntimeException re){
			++numExceptions;
		}
		AssertJUnit.assertEquals(1, numExceptions);
		AssertJUnit.assertFalse(router.txnBean().exists(new TxnBeanKey("outer"), null));
	}
}
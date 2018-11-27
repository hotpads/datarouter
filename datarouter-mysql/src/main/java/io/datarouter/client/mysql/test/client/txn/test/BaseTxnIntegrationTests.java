/**
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.client.mysql.test.client.txn.test;

import javax.inject.Inject;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import io.datarouter.client.mysql.execution.SessionExecutor;
import io.datarouter.client.mysql.op.Isolation;
import io.datarouter.client.mysql.test.client.txn.DatarouterTxnTestRouter;
import io.datarouter.client.mysql.test.client.txn.TxnBean;
import io.datarouter.client.mysql.test.client.txn.TxnBeanKey;
import io.datarouter.client.mysql.test.client.txn.txnapp.TestInsertRollback;
import io.datarouter.client.mysql.test.client.txn.txnapp.TestMultiInsertRollback;
import io.datarouter.client.mysql.test.client.txn.txnapp.TestNestedTxn;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.setting.DatarouterSettings;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.combo.SortedMapStorage;
import io.datarouter.storage.test.TestDatarouterProperties;
import io.datarouter.util.collection.ListTool;
import io.datarouter.util.iterable.IterableTool;

public abstract class BaseTxnIntegrationTests{

	@Inject
	private TestDatarouterProperties datarouterProperties;
	@Inject
	private Datarouter datarouter;
	@Inject
	private DatarouterSettings datarouterSettings;
	@Inject
	private NodeFactory nodeFactory;

	private ClientId clientId;
	private DatarouterTxnTestRouter router;
	private SortedMapStorage<TxnBeanKey,TxnBean> node;

	protected void setup(ClientId clientId){
		this.clientId = clientId;
		router = new DatarouterTxnTestRouter(datarouterProperties, datarouter, datarouterSettings, nodeFactory,
				clientId);
		node = router.txnBean();
	}

	@AfterClass
	public void afterClass(){
		datarouter.shutdown();
	}

	protected void resetTable(){
		router.txnBean().deleteAll(null);
		Assert.assertEquals(IterableTool.count(node.scan(null, null)).intValue(), 0);
	}

	/*----------------------  override these in subclasses ------------------*/


	protected boolean hasSession(){
		return false;
	}

	/*---------------------------- InsertRollback ---------------------------*/

	@Test
	public void testInsertRollback(){
		int numExceptions = 0;
		String beanPrefix = "a";
		try{
			SessionExecutor.run(new TestInsertRollback(datarouter, ListTool.wrap(clientId.getName()),
					Isolation.readCommitted, router, beanPrefix));
		}catch(RuntimeException re){
			++numExceptions;
		}
		Assert.assertEquals(numExceptions, 1);
		Assert.assertFalse(node.exists(new TxnBeanKey(beanPrefix + "1"), null));
	}

	/*------------------------- MultiInsertRollback -------------------------*/

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
		Assert.assertTrue(router.txnBean().exists(bean.getKey(), null));
		try{
			SessionExecutor.run(new TestMultiInsertRollback(datarouter, ListTool.wrap(clientId.getName()),
					Isolation.readCommitted, router, beanPrefix));
		}catch(RuntimeException re){
			++numExceptions;
		}
		Assert.assertEquals(numExceptions, 1);
		Assert.assertTrue(router.txnBean().exists(bean.getKey(), null));
		Assert.assertFalse(router.txnBean().exists(new TxnBeanKey(beanPrefix + "2"), null));
		Assert.assertFalse(router.txnBean().exists(new TxnBeanKey(beanPrefix + "3"), null));
	}


	/*------------------------------- NestedTxn -----------------------------*/

	@Test
	public void testNestedTxn(){
		int numExceptions = 0;
		try{
			SessionExecutor.run(new TestNestedTxn(datarouter, ListTool.wrap(clientId.getName()),
					Isolation.readCommitted, false, router));
		}catch(RuntimeException re){
			++numExceptions;
		}
		Assert.assertEquals(numExceptions, 1);
		Assert.assertFalse(router.txnBean().exists(new TxnBeanKey("outer"), null));
	}
}
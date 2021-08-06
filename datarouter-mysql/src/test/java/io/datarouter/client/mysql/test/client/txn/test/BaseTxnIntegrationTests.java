/*
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
import io.datarouter.client.mysql.test.client.txn.DatarouterTxnTestDao;
import io.datarouter.client.mysql.test.client.txn.TxnBean;
import io.datarouter.client.mysql.test.client.txn.TxnBeanKey;
import io.datarouter.client.mysql.test.client.txn.txnapp.TestInsertRollback;
import io.datarouter.client.mysql.test.client.txn.txnapp.TestMultiInsertRollback;
import io.datarouter.client.mysql.test.client.txn.txnapp.TestNestedTxn;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.node.factory.NodeFactory;

public abstract class BaseTxnIntegrationTests{

	@Inject
	private Datarouter datarouter;
	@Inject
	private NodeFactory nodeFactory;
	@Inject
	private SessionExecutor sessionExecutor;

	private ClientId clientId;
	private DatarouterTxnTestDao dao;

	protected void setup(ClientId clientId){
		this.clientId = clientId;
		dao = new DatarouterTxnTestDao(datarouter, nodeFactory, clientId);
	}

	@AfterClass
	public void afterClass(){
		datarouter.shutdown();
	}

	protected void resetTable(){
		dao.deleteAll();
		Assert.assertTrue(dao.scan().isEmpty());
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
			sessionExecutor.runWithoutRetries(new TestInsertRollback(
					datarouter,
					clientId,
					Isolation.readCommitted,
					dao, beanPrefix));
		}catch(RuntimeException re){
			++numExceptions;
		}
		Assert.assertEquals(numExceptions, 1);
		Assert.assertFalse(dao.exists(new TxnBeanKey(beanPrefix + "1")));
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
		dao.put(bean);
		Assert.assertTrue(dao.exists(bean.getKey()));
		try{
			sessionExecutor.runWithoutRetries(new TestMultiInsertRollback(
					datarouter,
					clientId,
					Isolation.readCommitted,
					dao, beanPrefix));
		}catch(RuntimeException re){
			++numExceptions;
		}
		Assert.assertEquals(numExceptions, 1);
		Assert.assertTrue(dao.exists(bean.getKey()));
		Assert.assertFalse(dao.exists(new TxnBeanKey(beanPrefix + "2")));
		Assert.assertFalse(dao.exists(new TxnBeanKey(beanPrefix + "3")));
	}

	/*------------------------------- NestedTxn -----------------------------*/

	@Test
	public void testNestedTxn(){
		int numExceptions = 0;
		try{
			sessionExecutor.runWithoutRetries(new TestNestedTxn(
					datarouter,
					clientId,
					Isolation.readCommitted,
					false,
					dao, sessionExecutor));
		}catch(RuntimeException re){
			++numExceptions;
		}
		Assert.assertEquals(numExceptions, 1);
		Assert.assertFalse(dao.exists(new TxnBeanKey("outer")));
	}

}

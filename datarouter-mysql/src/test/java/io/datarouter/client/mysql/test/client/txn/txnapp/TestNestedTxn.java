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
package io.datarouter.client.mysql.test.client.txn.txnapp;

import org.testng.Assert;

import io.datarouter.client.mysql.execution.SessionExecutor;
import io.datarouter.client.mysql.op.BaseMysqlOp;
import io.datarouter.client.mysql.op.Isolation;
import io.datarouter.client.mysql.test.client.txn.DatarouterTxnTestDao;
import io.datarouter.client.mysql.test.client.txn.TxnBean;
import io.datarouter.client.mysql.test.client.txn.TxnBeanKey;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.client.ConnectionHandle;
import io.datarouter.storage.client.type.ConnectionClientManager;

public class TestNestedTxn extends BaseMysqlOp<Void>{

	private final Datarouter datarouter;
	private final ClientId clientId;
	private final Isolation isolation;
	private final DatarouterTxnTestDao dao;
	private final SessionExecutor sessionExecutor;

	public TestNestedTxn(
			Datarouter datarouter,
			ClientId clientId,
			Isolation isolation,
			boolean autoCommit,
			DatarouterTxnTestDao dao,
			SessionExecutor sessionExecutor){
		super(datarouter, clientId, isolation, autoCommit);
		this.datarouter = datarouter;
		this.clientId = clientId;
		this.isolation = isolation;
		this.dao = dao;
		this.sessionExecutor = sessionExecutor;
	}

	@Override
	public Void runOnce(){
		ConnectionClientManager clientManager = (ConnectionClientManager)datarouter.getClientPool().getClientManager(
				clientId);
		ConnectionHandle handle = clientManager.getExistingHandle(clientId);

		var outer = new TxnBean("outer");
		dao.put(outer);
		Assert.assertTrue(dao.exists(outer.getKey()));

		sessionExecutor.runWithoutRetries(new InnerTxn(datarouter, clientId, isolation, false, dao, handle));

		var outer2 = new TxnBean(outer.getKey().getId());
		dao.putOrBust(outer2); //should bust on commit
		return null;
	}


	public static class InnerTxn extends BaseMysqlOp<Void>{

		private final DatarouterTxnTestDao dao;
		private final ConnectionHandle outerHandle;
		private final Datarouter datarouter;
		private final ClientId clientId;

		public InnerTxn(
				Datarouter datarouter,
				ClientId clientId,
				Isolation isolation,
				boolean autoCommit,
				DatarouterTxnTestDao dao,
				ConnectionHandle outerHandle){
			super(datarouter, clientId, isolation, autoCommit);
			this.datarouter = datarouter;
			this.clientId = clientId;
			this.dao = dao;
			this.outerHandle = outerHandle;
		}

		@Override
		public Void runOnce(){
			ConnectionClientManager clientManager = (ConnectionClientManager)datarouter.getClientPool()
					.getClientManager(clientId);
			ConnectionHandle handle = clientManager.getExistingHandle(clientId);
			Assert.assertEquals(handle, outerHandle);

			String name = "inner_txn";
			var inner = new TxnBean(name);
			dao.put(inner);
			Assert.assertTrue(dao.exists(inner.getKey()));
			Assert.assertTrue(dao.exists(new TxnBeanKey("outer")));
			return null;
		}

	}

}
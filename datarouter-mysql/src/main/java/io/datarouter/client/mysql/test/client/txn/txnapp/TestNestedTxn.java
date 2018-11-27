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

import java.util.List;

import org.testng.Assert;

import io.datarouter.client.mysql.execution.SessionExecutor;
import io.datarouter.client.mysql.op.BaseMysqlOp;
import io.datarouter.client.mysql.op.Isolation;
import io.datarouter.client.mysql.test.client.txn.DatarouterTxnTestRouter;
import io.datarouter.client.mysql.test.client.txn.TxnBean;
import io.datarouter.client.mysql.test.client.txn.TxnBeanKey;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.Client;
import io.datarouter.storage.client.ConnectionHandle;
import io.datarouter.storage.client.type.ConnectionClient;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.config.PutMethod;

public class TestNestedTxn extends BaseMysqlOp<Void>{

	private final Datarouter datarouter;
	private final List<String> clientNames;
	private final Isolation isolation;
	private final DatarouterTxnTestRouter router;

	public TestNestedTxn(Datarouter datarouter, List<String> clientNames, Isolation isolation, boolean autoCommit,
			DatarouterTxnTestRouter router){
		super(datarouter, clientNames, isolation, autoCommit);
		this.datarouter = datarouter;
		this.clientNames = clientNames;
		this.isolation = isolation;
		this.router = router;
	}

	@Override
	public Void runOncePerClient(Client client){
		ConnectionClient connectionClient = (ConnectionClient)client;
		ConnectionHandle handle = connectionClient.getExistingHandle();

		TxnBean outer = new TxnBean("outer");
		router.txnBean().put(outer, null);
		Assert.assertTrue(router.txnBean().exists(outer.getKey(), null));

		SessionExecutor.run(new InnerTxn(datarouter, clientNames, isolation, false, router, handle));

		TxnBean outer2 = new TxnBean(outer.getId());
		router.txnBean().put(outer2, new Config().setPutMethod(PutMethod.INSERT_OR_BUST));//should bust on commit
		return null;
	}


	public static class InnerTxn extends BaseMysqlOp<Void>{

		private final DatarouterTxnTestRouter router;
		private final ConnectionHandle outerHandle;

		public InnerTxn(Datarouter datarouter, List<String> clientNames, Isolation isolation, boolean autoCommit,
				DatarouterTxnTestRouter router, ConnectionHandle outerHandle){
			super(datarouter, clientNames, isolation, autoCommit);
			this.router = router;
			this.outerHandle = outerHandle;
		}

		@Override
		public Void runOncePerClient(Client client){
			ConnectionClient connectionClient = (ConnectionClient)client;
			ConnectionHandle handle = connectionClient.getExistingHandle();
			Assert.assertEquals(handle, outerHandle);

			String name = "inner_txn";
			TxnBean inner = new TxnBean(name);
			router.txnBean().put(inner, null);
			Assert.assertTrue(router.txnBean().exists(inner.getKey(), null));
			Assert.assertTrue(router.txnBean().exists(new TxnBeanKey("outer"), null));
			return null;
		}

	}

}
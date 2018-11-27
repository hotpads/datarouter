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

import io.datarouter.client.mysql.op.BaseMysqlOp;
import io.datarouter.client.mysql.op.Isolation;
import io.datarouter.client.mysql.test.client.txn.DatarouterTxnTestRouter;
import io.datarouter.client.mysql.test.client.txn.TxnBean;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.Client;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.config.PutMethod;

public class TestInsertRollback extends BaseMysqlOp<Void>{

	private final DatarouterTxnTestRouter router;
	private final String beanPrefix;

	public TestInsertRollback(Datarouter datarouter, List<String> clientNames, Isolation isolation,
			DatarouterTxnTestRouter router, String beanPrefix){
		super(datarouter, clientNames, isolation, false);
		this.router = router;
		this.beanPrefix = beanPrefix;
	}

	@Override
	public Void runOncePerClient(Client client){
		TxnBean beanA = new TxnBean(beanPrefix + "1");
		router.txnBean().put(beanA, null);
		Assert.assertTrue(router.txnBean().exists(beanA.getKey(), null));//it exists inside the txn
		TxnBean a2 = new TxnBean(beanPrefix + "2");
		a2.setId(beanA.getId());
		router.txnBean().put(a2, new Config().setPutMethod(PutMethod.INSERT_OR_BUST));//should bust on commit
		return null;
	}

}
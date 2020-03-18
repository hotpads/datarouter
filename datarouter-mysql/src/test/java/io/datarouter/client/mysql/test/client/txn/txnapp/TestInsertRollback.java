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

import io.datarouter.client.mysql.op.BaseMysqlOp;
import io.datarouter.client.mysql.op.Isolation;
import io.datarouter.client.mysql.test.client.txn.DatarouterTxnTestDao;
import io.datarouter.client.mysql.test.client.txn.TxnBean;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;

public class TestInsertRollback extends BaseMysqlOp<Void>{

	private final DatarouterTxnTestDao dao;
	private final String beanPrefix;

	public TestInsertRollback(
			Datarouter datarouter,
			ClientId clientId,
			Isolation isolation,
			DatarouterTxnTestDao dao,
			String beanPrefix){
		super(datarouter, clientId, isolation, false);
		this.dao = dao;
		this.beanPrefix = beanPrefix;
	}

	@Override
	public Void runOnce(){
		var beanA = new TxnBean(beanPrefix + "1");
		dao.put(beanA);
		Assert.assertTrue(dao.exists(beanA.getKey()));//it exists inside the txn
		var a2 = new TxnBean(beanA.getKey().getId());
		dao.putOrBust(a2);//should bust on commit
		return null;
	}

}
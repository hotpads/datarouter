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
package io.datarouter.client.mysql.test.client.txn;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.client.mysql.test.client.txn.TxnBean.TxnBeanFielder;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.combo.SortedMapStorage;
import io.datarouter.storage.router.BaseRouter;
import io.datarouter.storage.router.TestRouter;

@Singleton
public class DatarouterTxnTestRouter extends BaseRouter implements TestRouter{

	private final SortedMapStorage<TxnBeanKey,TxnBean> txnBean;

	@Inject
	public DatarouterTxnTestRouter(Datarouter datarouter, NodeFactory nodeFactory, ClientId clientId){
		super(datarouter);

		txnBean = nodeFactory.create(clientId, TxnBean::new, TxnBeanFielder::new).buildAndRegister();
	}

	public SortedMapStorage<TxnBeanKey,TxnBean> txnBean(){
		return txnBean;
	}

}
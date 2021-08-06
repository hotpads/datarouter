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
package io.datarouter.client.mysql.test.client.txn;

import java.util.Collection;

import javax.inject.Singleton;

import io.datarouter.client.mysql.test.client.txn.TxnBean.TxnBeanFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.config.PutMethod;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.TestDao;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.combo.SortedMapStorage;

@Singleton
public class DatarouterTxnTestDao extends BaseDao implements TestDao{

	private final SortedMapStorage<TxnBeanKey,TxnBean> node;

	public DatarouterTxnTestDao(Datarouter datarouter, NodeFactory nodeFactory, ClientId clientId){
		super(datarouter);
		node = nodeFactory.create(
				clientId,
				TxnBean::new,
				TxnBeanFielder::new)
				.buildAndRegister();
	}

	public void put(TxnBean databean){
		node.put(databean);
	}

	public void putMulti(Collection<TxnBean> databeans){
		node.putMulti(databeans);
	}

	public void putOrBust(TxnBean databean){
		node.put(databean, new Config().setPutMethod(PutMethod.INSERT_OR_BUST));
	}

	public boolean exists(TxnBeanKey key){
		return node.exists(key);
	}

	public void deleteAll(){
		node.deleteAll();
	}

	public Scanner<TxnBean> scan(){
		return node.scan();
	}

}

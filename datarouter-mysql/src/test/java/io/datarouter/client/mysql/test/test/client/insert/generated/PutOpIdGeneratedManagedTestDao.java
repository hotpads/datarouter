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
package io.datarouter.client.mysql.test.test.client.insert.generated;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.client.mysql.test.DatarouterMysqlTestClientids;
import io.datarouter.client.mysql.test.client.insert.generated.managed.PutOpIdGeneratedManagedTestBean;
import io.datarouter.client.mysql.test.client.insert.generated.managed.PutOpIdGeneratedManagedTestBean.PutOpIdGeneratedManagedTestBeanFielder;
import io.datarouter.client.mysql.test.client.insert.generated.managed.PutOpIdGeneratedManagedTestBeanKey;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.TestDao;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.combo.SortedMapStorage;

@Singleton
public class PutOpIdGeneratedManagedTestDao
extends BaseDao
implements TestDao, PutOpIdGeneratedTest<PutOpIdGeneratedManagedTestBeanKey,PutOpIdGeneratedManagedTestBean>{

	private final SortedMapStorage<PutOpIdGeneratedManagedTestBeanKey,PutOpIdGeneratedManagedTestBean> node;

	@Inject
	public PutOpIdGeneratedManagedTestDao(Datarouter datarouter, NodeFactory nodeFactory){
		super(datarouter);

		node = nodeFactory.create(
				DatarouterMysqlTestClientids.MYSQL,
				PutOpIdGeneratedManagedTestBean::new,
				PutOpIdGeneratedManagedTestBeanFielder::new)
				.buildAndRegister();
	}

	@Override
	public Scanner<PutOpIdGeneratedManagedTestBeanKey> scanKeys(){
		return node.scanKeys();
	}

	@Override
	public void put(PutOpIdGeneratedManagedTestBean databean){
		node.put(databean);
	}

	@Override
	public void putMulti(Collection<PutOpIdGeneratedManagedTestBean> databeans){
		node.putMulti(databeans);
	}

	@Override
	public void deleteAll(){
		node.deleteAll();
	}

}

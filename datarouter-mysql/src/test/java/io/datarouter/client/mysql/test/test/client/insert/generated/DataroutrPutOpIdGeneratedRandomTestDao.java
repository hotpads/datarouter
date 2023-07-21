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
package io.datarouter.client.mysql.test.test.client.insert.generated;

import java.util.Collection;

import io.datarouter.client.mysql.test.DatarouterMysqlTestClientids;
import io.datarouter.client.mysql.test.client.insert.generated.random.PutOpIdGeneratedRandomTestBean;
import io.datarouter.client.mysql.test.client.insert.generated.random.PutOpIdGeneratedRandomTestBean.PutOpIdGeneratedRandomTestBeanFielder;
import io.datarouter.client.mysql.test.client.insert.generated.random.PutOpIdGeneratedRandomTestBeanKey;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.TestDao;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.combo.SortedMapStorage;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DataroutrPutOpIdGeneratedRandomTestDao
extends BaseDao
implements TestDao, PutOpIdGeneratedTest<PutOpIdGeneratedRandomTestBeanKey,PutOpIdGeneratedRandomTestBean>{

	private final SortedMapStorage<PutOpIdGeneratedRandomTestBeanKey,PutOpIdGeneratedRandomTestBean> node;

	@Inject
	public DataroutrPutOpIdGeneratedRandomTestDao(Datarouter datarouter, NodeFactory nodeFactory){
		super(datarouter);
		node = nodeFactory.create(
				DatarouterMysqlTestClientids.MYSQL,
				PutOpIdGeneratedRandomTestBean::new,
				PutOpIdGeneratedRandomTestBeanFielder::new)
				.buildAndRegister();
	}

	@Override
	public Scanner<PutOpIdGeneratedRandomTestBeanKey> scanKeys(){
		return node.scanKeys();
	}

	@Override
	public void put(PutOpIdGeneratedRandomTestBean databean){
		node.put(databean);
	}

	@Override
	public void putMulti(Collection<PutOpIdGeneratedRandomTestBean> databeans){
		node.putMulti(databeans);
	}

	@Override
	public void deleteAll(){
		node.deleteAll();
	}

}

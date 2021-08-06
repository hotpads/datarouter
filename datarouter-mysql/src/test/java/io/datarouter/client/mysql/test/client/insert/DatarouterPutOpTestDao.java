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
package io.datarouter.client.mysql.test.client.insert;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.client.mysql.test.DatarouterMysqlTestClientids;
import io.datarouter.client.mysql.test.client.insert.PutOpTestBean.PutOpTestBeanFielder;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.TestDao;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.raw.MapStorage;

@Singleton
public class DatarouterPutOpTestDao extends BaseDao implements TestDao{

	private final MapStorage<PutOpTestBeanKey,PutOpTestBean> node;

	@Inject
	public DatarouterPutOpTestDao(Datarouter datarouter, NodeFactory nodeFactory){
		super(datarouter);
		node = nodeFactory.create(
				DatarouterMysqlTestClientids.MYSQL,
				PutOpTestBean::new,
				PutOpTestBeanFielder::new)
				.buildAndRegister();
	}

	public PutOpTestBean get(PutOpTestBeanKey key){
		return node.get(key);
	}

	public void put(PutOpTestBean databean, Config config){
		node.put(databean, config);
	}

	public List<PutOpTestBean> getMulti(Collection<PutOpTestBeanKey> keys){
		return node.getMulti(keys);
	}

	public void putMulti(Collection<PutOpTestBean> databeans, Config config){
		node.putMulti(databeans, config);
	}

	public void deleteAll(){
		node.deleteAll();
	}

}

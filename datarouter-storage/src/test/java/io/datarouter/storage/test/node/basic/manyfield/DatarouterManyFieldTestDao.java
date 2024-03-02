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
package io.datarouter.storage.test.node.basic.manyfield;

import java.util.Optional;
import java.util.Random;
import java.util.function.Supplier;

import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.config.PutMethod;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.TestDao;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.raw.MapStorage.MapStorageNode;
import io.datarouter.storage.test.node.basic.manyfield.ManyFieldBean.ManyFieldTypeBeanFielder;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterManyFieldTestDao extends BaseDao implements TestDao{

	private final MapStorageNode<ManyFieldBeanKey,ManyFieldBean,ManyFieldTypeBeanFielder> node;

	@Inject
	public DatarouterManyFieldTestDao(Datarouter datarouter, NodeFactory nodeFactory, ClientId clientId,
			Supplier<ManyFieldTypeBeanFielder> fielderSupplier, Optional<String> tableName){
		super(datarouter);

		node = nodeFactory.create(clientId, ManyFieldBean::new, fielderSupplier)
				.withSchemaVersion(Integer.toString(new Random().nextInt()))
				.withTableName(tableName)
				.buildAndRegister();
	}

	public MapStorageNode<ManyFieldBeanKey,ManyFieldBean,ManyFieldTypeBeanFielder> getNode(){
		return node;
	}

	public void delete(ManyFieldBeanKey key){
		node.delete(key);
	}

	public void deleteAll(){
		node.deleteAll();
	}

	public void put(ManyFieldBean databean){
		node.put(databean);
	}

	public void putOrBust(ManyFieldBean databean){
		node.put(databean, new Config().setPutMethod(PutMethod.INSERT_OR_BUST));
	}

	public ManyFieldBean get(ManyFieldBeanKey key){
		return node.get(key);
	}

}

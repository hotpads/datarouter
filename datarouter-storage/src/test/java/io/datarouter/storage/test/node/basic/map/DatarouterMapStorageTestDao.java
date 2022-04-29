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
package io.datarouter.storage.test.node.basic.map;

import java.util.Collection;
import java.util.List;

import javax.inject.Singleton;

import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.TestDao;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.raw.MapStorage;
import io.datarouter.storage.test.node.basic.map.databean.MapStorageBean;
import io.datarouter.storage.test.node.basic.map.databean.MapStorageBean.MapStorageBeanFielder;
import io.datarouter.storage.test.node.basic.map.databean.MapStorageBeanEntityKey;
import io.datarouter.storage.test.node.basic.map.databean.MapStorageBeanKey;

@Singleton
public class DatarouterMapStorageTestDao extends BaseDao implements TestDao{

	private final MapStorage<MapStorageBeanKey,MapStorageBean> node;

	public DatarouterMapStorageTestDao(Datarouter datarouter, NodeFactory nodeFactory,
			ClientId clientId){
		super(datarouter);
		node = nodeFactory.create(clientId, MapStorageBeanEntityKey::new, MapStorageBean::new,
				MapStorageBeanFielder::new)
				.withSchemaVersion(1)
				.buildAndRegister();
	}

	public List<MapStorageBeanKey> getKeys(Collection<MapStorageBeanKey> keys){
		return node.getKeys(keys);
	}

	public void put(MapStorageBean databean){
		node.put(databean);
	}

	public void putMulti(Collection<MapStorageBean> databeans){
		node.putMulti(databeans);
	}

	public List<MapStorageBean> getMulti(Collection<MapStorageBeanKey> keys){
		return node.getMulti(keys);
	}

	public MapStorageBean get(MapStorageBeanKey key){
		return node.get(key);
	}

	public boolean exists(MapStorageBeanKey key){
		return node.exists(key);
	}

	public void deleteMulti(Collection<MapStorageBeanKey> keys){
		node.deleteMulti(keys);
	}

	public void delete(MapStorageBeanKey key){
		node.delete(key);
	}

}

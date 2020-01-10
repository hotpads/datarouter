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
package io.datarouter.client.hbase.test.entity.databean;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.client.hbase.test.DatarouterHBaseTestClientIds;
import io.datarouter.client.hbase.test.entity.databean.HBaseBeanTest.HBaseBeanTestFielder;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.TestDao;
import io.datarouter.storage.node.entity.EntityNode;
import io.datarouter.storage.node.entity.EntityNodeParams;
import io.datarouter.storage.node.entity.SubEntitySortedMapStorageNode;
import io.datarouter.storage.node.factory.EntityNodeFactory;
import io.datarouter.storage.node.factory.WideNodeFactory;

@Singleton
public class DatarouterHBaseBeanTestEntityDao extends BaseDao implements TestDao{

	private static final EntityNodeParams<HBaseBeanTestEntityKey,HBaseBeanTestEntity> NODE_PARAMS
			= new EntityNodeParams<>(
			"HBaseBeanTestEntity",
			HBaseBeanTestEntityKey::new,
			HBaseBeanTestEntity::new,
			HBaseBeanTestPartitioner4::new,
			"HBaseBeanTestEntity");

	private final EntityNode<HBaseBeanTestEntityKey,HBaseBeanTestEntity> entity;
	private final SubEntitySortedMapStorageNode<
			HBaseBeanTestEntityKey,
			HBaseBeanTestKey,
			HBaseBeanTest,
			HBaseBeanTestFielder> node;

	@Inject
	public DatarouterHBaseBeanTestEntityDao(Datarouter datarouter, EntityNodeFactory entityNodeFactory,
			WideNodeFactory wideNodeFactory){
		super(datarouter);

		entity = entityNodeFactory.create(DatarouterHBaseTestClientIds.HBASE, NODE_PARAMS);
		node = datarouter.register(wideNodeFactory.subEntityNode(
				NODE_PARAMS,
				DatarouterHBaseTestClientIds.HBASE,
				HBaseBeanTest::new,
				HBaseBeanTestFielder::new,
				HBaseBeanTestEntity.PREFIX,
				"HBaseBeanTestEntity"));
		entity.register(node);
	}

	public HBaseBeanTestEntity getEntity(HBaseBeanTestEntityKey key){
		return entity.getEntity(key);
	}

	public void deleteMultiEntities(Collection<HBaseBeanTestEntityKey> keys){
		entity.deleteMultiEntities(keys);
	}

	public void putMulti(Collection<HBaseBeanTest> databeans){
		node.putMulti(databeans);
	}

	public List<HBaseBeanTestEntityKey> getEntityKeys(HBaseBeanTestEntityKey startKey, boolean startKeyInclusive,
			int limit){
		return entity.listEntityKeys(startKey, startKeyInclusive, new Config().setLimit(limit));
	}

}

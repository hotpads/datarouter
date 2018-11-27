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
package io.datarouter.storage.test.node.basic.map;

import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.node.entity.EntityNodeParams;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.raw.MapStorage;
import io.datarouter.storage.router.Router;
import io.datarouter.storage.test.node.basic.map.databean.MapStorageBean;
import io.datarouter.storage.test.node.basic.map.databean.MapStorageBean.MapStorageBeanFielder;
import io.datarouter.storage.test.node.basic.map.databean.MapStorageBeanEntity;
import io.datarouter.storage.test.node.basic.map.databean.MapStorageBeanEntityKey;
import io.datarouter.storage.test.node.basic.map.databean.MapStorageBeanEntityKey.MapStorageBeanEntityPartitioner;
import io.datarouter.storage.test.node.basic.map.databean.MapStorageBeanKey;

public class MapStorageEntityNode{

	private static final EntityNodeParams<MapStorageBeanEntityKey,MapStorageBeanEntity> ENTITY_NODE_PARAMS_1 =
			new EntityNodeParams<>(
			"MapStorageBeanEntity",
			MapStorageBeanEntityKey.class,
			MapStorageBeanEntity::new,
			MapStorageBeanEntityPartitioner::new,
			"MapStorageBeanEntity");

	public final MapStorage<MapStorageBeanKey,MapStorageBean> mapStorageNode;

	public MapStorageEntityNode(NodeFactory nodeFactory, Router router, ClientId clientId){
		this.mapStorageNode = router.register(nodeFactory.subEntityNode(ENTITY_NODE_PARAMS_1, clientId,
				MapStorageBean::new, MapStorageBeanFielder::new, MapStorageBeanEntity.QUALIFIER_PREFIX_MapStorageBean));
	}

}
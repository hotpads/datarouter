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
package io.datarouter.storage.test.node.basic.sorted;

import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.node.entity.EntityNode;
import io.datarouter.storage.node.entity.EntityNodeParams;
import io.datarouter.storage.node.entity.SubEntitySortedMapStorageNode;
import io.datarouter.storage.node.factory.EntityNodeFactory;
import io.datarouter.storage.node.factory.WideNodeFactory;
import io.datarouter.storage.test.node.basic.sorted.SortedBean.SortedBeanFielder;
import io.datarouter.storage.test.node.basic.sorted.SortedBeanEntityKey.SortedBeanEntityPartitioner4;

public class SortedBeanEntityNode{

	public static final EntityNodeParams<SortedBeanEntityKey,SortedBeanEntity> ENTITY_NODE_PARAMS_1 = createNodeParams(
			1);
	public static final EntityNodeParams<SortedBeanEntityKey,SortedBeanEntity> ENTITY_NODE_PARAMS_2 = createNodeParams(
			2);
	public static final EntityNodeParams<SortedBeanEntityKey,SortedBeanEntity> ENTITY_NODE_PARAMS_3 = createNodeParams(
			3);
	public static final EntityNodeParams<SortedBeanEntityKey,SortedBeanEntity> ENTITY_NODE_PARAMS_4 = createNodeParams(
			4);
	public static final EntityNodeParams<SortedBeanEntityKey,SortedBeanEntity> ENTITY_NODE_PARAMS_5 = createNodeParams(
			5);

	private static EntityNodeParams<SortedBeanEntityKey, SortedBeanEntity> createNodeParams(int index){
		String nodeName = "SortedBeanEntity" + index;
		return new EntityNodeParams<>(
				nodeName,
				SortedBeanEntityKey::new,
				SortedBeanEntity::new,
				SortedBeanEntityPartitioner4::new,
				nodeName);
	}

	public final EntityNode<SortedBeanEntityKey,SortedBeanEntity> entity;
	public final SubEntitySortedMapStorageNode<SortedBeanEntityKey,SortedBeanKey,SortedBean,SortedBeanFielder>
			sortedBean;

	public SortedBeanEntityNode(EntityNodeFactory entityNodeFactory, WideNodeFactory wideNodeFactory,
			Datarouter datarouter, ClientId clientId,
			EntityNodeParams<SortedBeanEntityKey,SortedBeanEntity> entityNodeParams){
		this.entity = entityNodeFactory.create(clientId, entityNodeParams);
		this.sortedBean = datarouter.register(wideNodeFactory.subEntityNode(entityNodeParams, clientId, SortedBean::new,
				SortedBeanFielder::new, SortedBeanEntity.QUALIFIER_PREFIX_SortedBean));
		entity.register(sortedBean);
	}

}

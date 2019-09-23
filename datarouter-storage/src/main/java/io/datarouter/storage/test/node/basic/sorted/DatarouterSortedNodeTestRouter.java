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
import io.datarouter.storage.node.entity.EntityNodeParams;
import io.datarouter.storage.node.factory.EntityNodeFactory;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.factory.WideNodeFactory;
import io.datarouter.storage.node.op.combo.SortedMapStorage;
import io.datarouter.storage.router.BaseRouter;
import io.datarouter.storage.router.TestRouter;
import io.datarouter.storage.test.node.basic.sorted.SortedBean.SortedBeanFielder;

public class DatarouterSortedNodeTestRouter extends BaseRouter implements TestRouter{

	private static final String TABLE_NAME_SortedBean = "SortedBean";

	public final SortedMapStorage<SortedBeanKey,SortedBean> sortedBeanNode;
	public final SortedBeanEntityNode sortedBeanEntityNode;

	public DatarouterSortedNodeTestRouter(
			Datarouter datarouter,
			EntityNodeFactory entityNodeFactory,
			EntityNodeParams<SortedBeanEntityKey,SortedBeanEntity> entityNodeParams,
			NodeFactory nodeFactory,
			WideNodeFactory wideNodeFactory,
			ClientId clientId,
			boolean entity){
		super(datarouter);

		String tableName = TABLE_NAME_SortedBean;
		if(entity){
			sortedBeanEntityNode = new SortedBeanEntityNode(entityNodeFactory, wideNodeFactory, datarouter, clientId,
					entityNodeParams);
			sortedBeanNode = sortedBeanEntityNode.sortedBean;
		}else{
			sortedBeanEntityNode = null;
			sortedBeanNode = nodeFactory.create(clientId, SortedBeanEntityKey::new, SortedBean::new,
					SortedBeanFielder::new)
					.withTableName(tableName)
					.buildAndRegister();
		}
	}

}
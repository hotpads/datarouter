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

import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.setting.DatarouterSettings;
import io.datarouter.storage.node.entity.EntityNodeParams;
import io.datarouter.storage.node.factory.EntityNodeFactory;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.combo.IndexedSortedMapStorage.IndexedSortedMapStorageNode;
import io.datarouter.storage.node.op.combo.SortedMapStorage;
import io.datarouter.storage.routing.BaseRouter;
import io.datarouter.storage.routing.Datarouter;
import io.datarouter.storage.routing.TestRouter;
import io.datarouter.storage.test.TestDatarouterProperties;
import io.datarouter.storage.test.node.basic.sorted.SortedBean.SortedBeanFielder;

public class SortedNodeTestRouter extends BaseRouter implements TestRouter{

	private static final String
			NAME = "SortedNodeTestRouter",
			TABLE_NAME_SortedBean = "SortedBean";


	/********************************** nodes **********************************/

	private SortedMapStorage<SortedBeanKey,SortedBean> sortedBeanNode;
	private SortedBeanEntityNode sortedBeanEntityNode;

	public SortedNodeTestRouter(TestDatarouterProperties datarouterProperties, Datarouter datarouter,
			DatarouterSettings datarouterSettings, EntityNodeFactory entityNodeFactory,
			EntityNodeParams<SortedBeanEntityKey,SortedBeanEntity> entityNodeParams, NodeFactory nodeFactory,
			ClientId clientId, boolean entity){
		super(datarouter, datarouterProperties.getDatarouterTestFileLocation(), NAME, nodeFactory,
				datarouterSettings);

		String tableName = TABLE_NAME_SortedBean;
		if(entity){
			sortedBeanEntityNode = new SortedBeanEntityNode(entityNodeFactory, nodeFactory, this, clientId,
					entityNodeParams);
			sortedBeanNode = sortedBeanEntityNode.sortedBean();
		}else{
			sortedBeanNode = create(clientId, SortedBean::new, SortedBeanFielder::new)
					.withTableName(tableName).buildAndRegister();
		}

	}


	/************************ methods **********************************/

	//we have to do this on-request to avoid trying to cast things like HBaseNode to Indexed Storage
	@SuppressWarnings("unchecked")
	public IndexedSortedMapStorageNode<SortedBeanKey,SortedBean,SortedBeanFielder> indexedSortedBean(){
		return (IndexedSortedMapStorageNode<SortedBeanKey,SortedBean,SortedBeanFielder>)sortedBeanNode;
	}


	/*************************** get/set ***********************************/

	public SortedMapStorage<SortedBeanKey,SortedBean> sortedBean(){
		return sortedBeanNode;
	}

	public SortedBeanEntityNode sortedBeanEntity(){
		return sortedBeanEntityNode;
	}
}
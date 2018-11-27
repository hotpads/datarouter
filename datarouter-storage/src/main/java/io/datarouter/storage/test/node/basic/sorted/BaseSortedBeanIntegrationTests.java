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

import java.util.List;

import javax.inject.Inject;

import org.testng.Assert;
import org.testng.annotations.Guice;

import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.config.PutMethod;
import io.datarouter.storage.config.setting.DatarouterSettings;
import io.datarouter.storage.node.factory.EntityNodeFactory;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.combo.SortedMapStorage;
import io.datarouter.storage.test.DatarouterStorageTestModuleFactory;
import io.datarouter.storage.test.TestDatarouterProperties;
import io.datarouter.util.iterable.BatchingIterable;
import io.datarouter.util.iterable.IterableTool;

@Guice(moduleFactory = DatarouterStorageTestModuleFactory.class)
public abstract class BaseSortedBeanIntegrationTests{

	@Inject
	protected Datarouter datarouter;
	@Inject
	private TestDatarouterProperties datarouterProperties;
	@Inject
	private DatarouterSettings datarouterSettings;
	@Inject
	private EntityNodeFactory entityNodeFactory;
	@Inject
	private NodeFactory nodeFactory;

	protected DatarouterSortedNodeTestRouter router;
	protected SortedMapStorage<SortedBeanKey,SortedBean> sortedNode;
	protected List<SortedBean> allBeans = SortedBeans.generatedSortedBeans();

	protected void setup(ClientId clientId, boolean entity){
		router = new DatarouterSortedNodeTestRouter(datarouterProperties, datarouter, datarouterSettings,
				entityNodeFactory, SortedBeanEntityNode.ENTITY_NODE_PARAMS_1, nodeFactory, clientId, entity);
		sortedNode = router.sortedBeanNode;

		resetTable(true);
	}

	protected void resetTable(boolean force){
		long numExistingDatabeans = IterableTool.count(sortedNode.scan(null, null));
		if(!force && SortedBeans.TOTAL_RECORDS == numExistingDatabeans){
			return;
		}

		sortedNode.deleteAll(null);
		Assert.assertEquals(sortedNode.stream(null, null).count(), 0);

		for(List<SortedBean> batch : new BatchingIterable<>(allBeans, 1000)){
			sortedNode.putMulti(batch, new Config().setPutMethod(PutMethod.INSERT_OR_BUST));
		}
		Assert.assertEquals(sortedNode.stream(null, null).count(), SortedBeans.TOTAL_RECORDS);
	}

}

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
import io.datarouter.storage.node.factory.EntityNodeFactory;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.factory.WideNodeFactory;
import io.datarouter.storage.test.DatarouterStorageTestNgModuleFactory;

@Guice(moduleFactory = DatarouterStorageTestNgModuleFactory.class)
public abstract class BaseSortedBeanIntegrationTests{

	@Inject
	protected Datarouter datarouter;
	@Inject
	private EntityNodeFactory entityNodeFactory;
	@Inject
	private NodeFactory nodeFactory;
	@Inject
	private WideNodeFactory wideNodeFactory;

	protected DatarouterSortedNodeTestDao dao;
	protected List<SortedBean> allBeans = SortedBeans.generatedSortedBeans();

	protected void setup(ClientId clientId, boolean entity){
		dao = new DatarouterSortedNodeTestDao(
				datarouter,
				entityNodeFactory,
				SortedBeanEntityNode.ENTITY_NODE_PARAMS_1,
				nodeFactory,
				wideNodeFactory,
				clientId,
				entity);
		resetTable(true);
	}

	protected void resetTable(boolean force){
		long numExistingDatabeans = count();
		if(!force && SortedBeans.TOTAL_RECORDS == numExistingDatabeans){
			return;
		}
		dao.deleteAll();
		Assert.assertEquals(count(), 0);
		dao.putStream(allBeans.stream());
		Assert.assertEquals(count(), SortedBeans.TOTAL_RECORDS);
	}

	protected long count(){
		return dao.count();
	}

}

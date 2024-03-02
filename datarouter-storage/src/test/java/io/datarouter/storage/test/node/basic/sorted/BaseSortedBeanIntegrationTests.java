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
package io.datarouter.storage.test.node.basic.sorted;

import java.util.List;
import java.util.Optional;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Guice;

import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.test.DatarouterStorageTestNgModuleFactory;
import jakarta.inject.Inject;

@Guice(moduleFactory = DatarouterStorageTestNgModuleFactory.class)
public abstract class BaseSortedBeanIntegrationTests{

	@Inject
	protected Datarouter datarouter;
	@Inject
	private NodeFactory nodeFactory;

	protected DatarouterSortedNodeTestDao dao;
	protected List<SortedBean> allBeans = SortedBeans.generatedSortedBeans();

	protected void setup(ClientId clientId, Optional<String> tableName){
		dao = new DatarouterSortedNodeTestDao(
				datarouter,
				nodeFactory,
				clientId,
				tableName);
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

	@AfterClass
	public void afterClass(){
		datarouter.shutdown();
	}

}

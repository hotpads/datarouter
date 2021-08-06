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
package io.datarouter.client.hbase.test.entity;

import java.util.List;
import java.util.SortedSet;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import io.datarouter.client.hbase.config.DatarouterHBaseTestNgModuleFactory;
import io.datarouter.client.hbase.test.DatarouterHBaseTestClientIds;
import io.datarouter.storage.config.Config;
import io.datarouter.storage.test.node.basic.sorted.BaseSortedNodeIntegrationTests;
import io.datarouter.storage.test.node.basic.sorted.SortedBean;
import io.datarouter.storage.test.node.basic.sorted.SortedBeanEntity;
import io.datarouter.storage.test.node.basic.sorted.SortedBeanEntityKey;
import io.datarouter.storage.test.node.basic.sorted.SortedBeanKey;
import io.datarouter.storage.test.node.basic.sorted.SortedBeans;

@Guice(moduleFactory = DatarouterHBaseTestNgModuleFactory.class)
public class HBaseEntitySortedNodeIntegrationTests extends BaseSortedNodeIntegrationTests{

	@BeforeClass
	public void beforeClass(){
		setup(DatarouterHBaseTestClientIds.HBASE, true);
	}

	@AfterClass
	public void afterClass(){
		postTestTests();
		datarouter.shutdown();
	}

	@Test
	public void testGetEntity(){
		SortedBeanEntityKey ek1 = new SortedBeanEntityKey(SortedBeans.S_albatross, SortedBeans.S_emu);
		SortedBeanEntity albatrossOstrich = dao.getEntity(ek1);
		Assert.assertNotNull(albatrossOstrich);
		int numExpected = SortedBeans.NUM_ELEMENTS * SortedBeans.NUM_ELEMENTS;
		SortedSet<SortedBean> results = albatrossOstrich.getSortedBeans();
		Assert.assertEquals(results.size(), numExpected);
		Assert.assertEquals(results.iterator().next().getKey().getFoo(), SortedBeans.S_albatross);
		Assert.assertEquals(results.iterator().next().getKey().getBar(), SortedBeans.S_emu);
	}

	@Test
	public void testDeleteEntity(){
		SortedBeanEntityKey ek2 = new SortedBeanEntityKey(SortedBeans.S_gopher, SortedBeans.S_pelican);
		dao.deleteEntity(ek2);
		SortedBeanEntity sortedBeans2 = dao.getEntity(ek2);
		Assert.assertNull(sortedBeans2);
		resetTable(true);
	}

	@Test
	private void testSingleEntityScan(){
		SortedBeanKey twoFieldsPk = new SortedBeanKey(SortedBeans.S_albatross, SortedBeans.S_ostrich, null, null);
		SortedBeanKey threeFieldsPk = new SortedBeanKey(SortedBeans.S_albatross, SortedBeans.S_ostrich, 1, null);

		int limit = 23;
		Assert.assertEquals(dao.scanKeysWithPrefix(threeFieldsPk, new Config().setLimit(limit)).count(),
				SortedBeans.NUM_ELEMENTS);
		Assert.assertEquals(dao.scanWithPrefix(threeFieldsPk, new Config().setLimit(limit)).count(),
				SortedBeans.NUM_ELEMENTS);

		Assert.assertEquals(dao.scanKeysWithPrefix(twoFieldsPk, new Config().setLimit(limit)).count(), limit);
		Assert.assertEquals(dao.scanWithPrefix(twoFieldsPk, new Config().setLimit(limit)).count(), limit);

		int offset = 57;
		List<SortedBeanKey> limitedOffset = dao
				.scanKeysWithPrefix(twoFieldsPk, new Config().setOffset(offset).setLimit(1))
				.list();
		Assert.assertEquals(limitedOffset.size(), 1);
		Assert.assertEquals(limitedOffset.get(0), dao.scanKeysWithPrefix(twoFieldsPk, new Config()
				.setOffset(offset)).findFirst().get());
	}

}

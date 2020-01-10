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
package io.datarouter.client.hbase.test.entity;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import io.datarouter.client.hbase.config.DatarouterHBaseTestNgModuleFactory;
import io.datarouter.client.hbase.test.DatarouterHBaseTestClientIds;
import io.datarouter.model.databean.DatabeanTool;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.node.factory.EntityNodeFactory;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.factory.WideNodeFactory;
import io.datarouter.storage.test.node.basic.sorted.DatarouterSortedNodeTestDao;
import io.datarouter.storage.test.node.basic.sorted.SortedBean;
import io.datarouter.storage.test.node.basic.sorted.SortedBeanEntityNode;
import io.datarouter.storage.test.node.basic.sorted.SortedBeanKey;
import io.datarouter.storage.util.KeyRangeTool;
import io.datarouter.util.tuple.Range;

@Guice(moduleFactory = DatarouterHBaseTestNgModuleFactory.class)
public class HBaseSubEntityReaderNodeIntegrationTests{

	@Inject
	protected Datarouter datarouter;
	@Inject
	private EntityNodeFactory entityNodeFactory;
	@Inject
	private NodeFactory nodeFactory;
	@Inject
	private WideNodeFactory wideNodeFactory;

	private DatarouterSortedNodeTestDao dao;
	private List<SortedBean> sortedBeans;

	@BeforeClass
	public void beforeClass(){
		// Use SortedBeanEntityNode.ENTITY_NODE_PARAMS_2 to avoid conflicting with HBaseEntitySortedNodeIntegrationTests
		dao = new DatarouterSortedNodeTestDao(
				datarouter,
				entityNodeFactory,
				SortedBeanEntityNode.ENTITY_NODE_PARAMS_2,
				nodeFactory,
				wideNodeFactory,
				DatarouterHBaseTestClientIds.HBASE, true);

		sortedBeans = new ArrayList<>();
		sortedBeans.add(new SortedBean("a", "b", 1, "d", "f1", 2L, "f3", 4D));
		sortedBeans.add(new SortedBean("a", "b", 2, "d", "f1", 2L, "f3", 4D));
		sortedBeans.add(new SortedBean("a", "b", 25, "d", "f1", 2L, "f3", 4D));
		sortedBeans.add(new SortedBean("a", "b", 3, "d", "f1", 2L, "f3", 4D));
		sortedBeans.add(new SortedBean("a", "b", 4, "d", "f1", 2L, "f3", 4D));
		sortedBeans.add(new SortedBean("a", "b", 4, "dj", "f1", 2L, "f3", 4D));
		sortedBeans.add(new SortedBean("a", "b", 4, "e", "f1", 2L, "f3", 4D));
		sortedBeans.add(new SortedBean("a", "c", 1, "d", "f1", 2L, "f3", 4D));
		sortedBeans.add(new SortedBean("a", "c", 2, "d", "f1", 2L, "f3", 4D));
		sortedBeans.add(new SortedBean("a", "c", 2, "dd", "f1", 2L, "f3", 4D));
		sortedBeans.add(new SortedBean("b", "b", 1, "d", "f1", 2L, "f3", 4D));
		sortedBeans.add(new SortedBean("c", "b", 1, "d", "f1", 2L, "f3", 4D));
		dao.putMulti(sortedBeans);
	}

	@Test
	public void testNotDefinedEntityScan(){
		Assert.assertEquals(dao.scan(new Range<>(new SortedBeanKey("a", null, null, null), true,
				new SortedBeanKey("b", null, null, null), true)).count(), 11);

		Assert.assertEquals(dao.scan(new Range<>(new SortedBeanKey("a", null, null, null), new SortedBeanKey(
				"a", "c", null, null))).count(), 7);

		Assert.assertEquals(dao.scanWithPrefix(new SortedBeanKey("a", null, null, null)).count(), 10);
	}

	@Test
	public void testSingleEntityScan(){
		Assert.assertEquals(dao.scanKeysWithPrefix(new SortedBeanKey("a", "c", null, null)).count(), 3);
		Assert.assertEquals(dao.scanKeysWithPrefix(new SortedBeanKey("a", "c", 2, null)).count(), 2);
		Assert.assertEquals(dao.scanKeysWithPrefix(new SortedBeanKey("a", "b", 2, null)).count(), 1);
		Assert.assertEquals(dao.scanKeysWithPrefix(new SortedBeanKey("a", "b", 4, "d")).count(), 1);
		Assert.assertEquals(dao.scanKeys(new Range<>(new SortedBeanKey("a", "c", 1, null), new SortedBeanKey(
				"a", "c", 2, null))).count(), 1);
		Assert.assertEquals(dao.scanKeys(new Range<>(new SortedBeanKey("a", "c", 1, null), true,
				new SortedBeanKey("a", "c", 2, null), true)).count(), 3);

		//test wildcardLastField
		Assert.assertEquals(dao.scanKeysWithPrefix(new SortedBeanKey("a", "b", 4, "d")).count(), 1);
		Assert.assertEquals(dao.scanKeys(KeyRangeTool.forPrefixWithWildcard("d",
				suffix -> new SortedBeanKey("a", "b", 4, suffix))).count(), 2);
	}

	@AfterClass
	public void afterClass(){
		dao.deleteMulti(DatabeanTool.getKeys(sortedBeans));
		datarouter.shutdown();
	}

}

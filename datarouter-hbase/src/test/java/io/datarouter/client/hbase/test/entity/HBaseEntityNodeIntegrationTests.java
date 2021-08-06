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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import io.datarouter.client.hbase.config.DatarouterHBaseTestNgModuleFactory;
import io.datarouter.client.hbase.test.DatarouterHBaseTestClientIds;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.node.factory.EntityNodeFactory;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.factory.WideNodeFactory;
import io.datarouter.storage.test.node.basic.sorted.DatarouterSortedNodeTestDao;
import io.datarouter.storage.test.node.basic.sorted.SortedBean;
import io.datarouter.storage.test.node.basic.sorted.SortedBeanEntityNode;
import io.datarouter.storage.test.node.basic.sorted.SortedBeans;

@Guice(moduleFactory = DatarouterHBaseTestNgModuleFactory.class)
public class HBaseEntityNodeIntegrationTests{

	@Inject
	private Datarouter datarouter;
	@Inject
	private EntityNodeFactory entityNodeFactory;
	@Inject
	private NodeFactory nodeFactory;
	@Inject
	private WideNodeFactory wideNodeFactory;

	private DatarouterSortedNodeTestDao dao;
	private List<SortedBean> normalBeans;
	private List<SortedBean> extraBeans;

	@BeforeClass
	public void beforeClass(){
		dao = new DatarouterSortedNodeTestDao(
				datarouter,
				entityNodeFactory,
				SortedBeanEntityNode.ENTITY_NODE_PARAMS_3,
				nodeFactory,
				wideNodeFactory,
				DatarouterHBaseTestClientIds.HBASE,
				true);
		normalBeans = SortedBeans.generatedSortedBeans().stream()
				.collect(Collectors.toList());
		Scanner.of(normalBeans)
				.batch(1000)
				.forEach(dao::putMultiEntity);

		extraBeans = new ArrayList<>();
		String prefix = "testScanForRowDatabean1to1";
		for(int i = 1; i < 6; i++){
			String k1 = prefix + "-" + i;
			String k2 = prefix + "-2-" + i;
			int k3 = i;
			String k4 = prefix + "-4-" + i;
			String f1 = "string so hbase has at least one field";
			extraBeans.add(new SortedBean(k1, k2, k3, k4, f1, null, null, null));
		}
		//inserted 5 new rows with 1-1 mapping with datarouter databeans
		dao.putMultiEntity(extraBeans);
	}

	@AfterClass
	public void afterClass(){
		dao.deleteAllEntity();
		datarouter.shutdown();
	}

	//This tests scan when there are hbase rows which have 1-1 mapping with datarouter (entity) databeans. 1-1
	// mapping in this context means hbase scans results (hbaseRows) and the converted entity databeans (outs) in
	// {@link io.datarouter.client.hbase.batching.entity.BaseHBaseEntityBatchLoader#call}
	// have the same count. In other words, after converting hbase scan results to entities, we only get single
	// databean entities in the converted results and the number of the converted results is <= the number of the
	// results returned by the hbase scan.
	@Test
	public void testScanForRowDatabean1to1(){
		List<SortedBean> expectedBeans = Scanner.of(normalBeans, extraBeans)
				.concat(Scanner::of)
				.sort()
				.list();

		//purposefully tiny batch size
		List<SortedBean> actualBeans = dao.scanEntity(2).list();
		Assert.assertEquals(actualBeans, expectedBeans);
	}

}

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

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import io.datarouter.client.hbase.config.DatarouterHBaseTestNgModuleFactory;
import io.datarouter.client.hbase.test.DatarouterHBaseTestClientIds;
import io.datarouter.model.databean.Databean;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.node.factory.EntityNodeFactory;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.factory.WideNodeFactory;
import io.datarouter.storage.test.node.basic.sorted.DatarouterSortedNodeTestDao;
import io.datarouter.storage.test.node.basic.sorted.SortedBean;
import io.datarouter.storage.test.node.basic.sorted.SortedBeanEntity;
import io.datarouter.storage.test.node.basic.sorted.SortedBeanEntityKey;
import io.datarouter.storage.test.node.basic.sorted.SortedBeanEntityNode;
import io.datarouter.storage.test.node.basic.sorted.SortedBeanKey;
import io.datarouter.storage.test.node.basic.sorted.SortedBeans;

@Guice(moduleFactory = DatarouterHBaseTestNgModuleFactory.class)
public class HBaseSubEntityRawScanIntegrationTests{
	private static final Logger logger = LoggerFactory.getLogger(HBaseSubEntityRawScanIntegrationTests.class);

	private static final List<SortedBean> ALL = SortedBeans.generatedSortedBeans();
	private static final SortedBeanKey PREFIX_GOPHER_PELICAN_3 = new SortedBeanKey(SortedBeans.S_gopher,
			SortedBeans.S_pelican, 3, null);
	private static final SortedBeanEntityKey EK_GOPHER_PELICAN = PREFIX_GOPHER_PELICAN_3.getEntityKey();

	@Inject
	protected Datarouter datarouter;
	@Inject
	private EntityNodeFactory entityNodeFactory;
	@Inject
	private NodeFactory nodeFactory;
	@Inject
	private WideNodeFactory wideNodeFactory;

	private DatarouterSortedNodeTestDao dao;

	@BeforeClass
	public void beforeClass(){
		dao = new DatarouterSortedNodeTestDao(
				datarouter,
				entityNodeFactory,
				SortedBeanEntityNode.ENTITY_NODE_PARAMS_5,
				nodeFactory,
				wideNodeFactory,
				DatarouterHBaseTestClientIds.HBASE,
				true);
	}

	@AfterClass
	public void afterClass(){
		datarouter.shutdown();
	}

	@Test
	public void testSequentially(){
		testPartialRowScan();
		testRowLevelTombstones();
		testChurn();
	}

	private void testPartialRowScan(){//mainly for debugging.  should be covered elsewhere
		putAll(1);
		dao.deleteEntity(EK_GOPHER_PELICAN);
		putAll(1);
		for(int outputBatchSize = 1; outputBatchSize <= 8; ++outputBatchSize){
			long count = dao.scanWithPrefix(PREFIX_GOPHER_PELICAN_3, outputBatchSize)
					.map(Databean::getKey)
					.each(pk -> logger.warn("{}", pk))
					.count();
			Assert.assertEquals(count, 8);
		}
	}

	private void testRowLevelTombstones(){
		putAll(1);
		Assert.assertEquals(countEntityGet(EK_GOPHER_PELICAN), 64);
		Assert.assertEquals(countEntityScan(EK_GOPHER_PELICAN, false), 64);
		dao.deleteEntity(EK_GOPHER_PELICAN);
		Assert.assertEquals(countEntityGet(EK_GOPHER_PELICAN), 0);
		Assert.assertEquals(countEntityScan(EK_GOPHER_PELICAN, false), 0);
		putAll(1);//now we have cells that are newer than the row tombstones
		Assert.assertEquals(countEntityGet(EK_GOPHER_PELICAN), 64);
		Assert.assertEquals(countEntityScan(EK_GOPHER_PELICAN, false), 64);
	}

	private void testChurn(){
		deleteAll();
		Assert.assertEquals(count(true), 0);//clear the table, even though there will be tombstones
		Assert.assertEquals(countByEntityScan(true), 0);//clear the table, even though there will be tombstones

		putAll(1);
		Assert.assertEquals(countByEntityScan(false), ALL.size());
		putAll(100);//generate lots of hidden cells
		Assert.assertEquals(count(false), ALL.size());//check we get all of them back even though there are old versions
		deleteAll();
		Assert.assertEquals(count(true), 0);//check we don't pick up deleted databeans
	}

	private void deleteAll(){
		dao.deleteAll();
	}

	private void putAll(int nTimes){
		for(int i = 0; i < nTimes; ++i){
			dao.putStream(ALL.stream());
		}
	}

	private long count(boolean logPk){
		return dao.scan(1)
				.map(Databean::getKey)
				.each(pk -> logUnexpectedPks(logPk, pk))
				.count();
	}

	private long countByEntityScan(boolean logPk){
		return SortedBeans.generateEntityKeys().stream()
				.map(ek -> countEntityScan(ek, logPk))
				.mapToLong(Long::valueOf)
				.sum();
	}

	private long countEntityScan(SortedBeanEntityKey ek, boolean logPk){
		SortedBeanKey prefix = new SortedBeanKey().prefixFromEntityKey(ek);
		return dao.scanWithPrefix(prefix, 1)
				.map(Databean::getKey)
				.each(pk -> logUnexpectedPks(logPk, pk))
				.count();
	}

	private long countEntityGet(SortedBeanEntityKey ek){
		SortedBeanEntity entity = dao.getEntity(ek);
		return entity == null ? 0 : entity.getSortedBeans().size();
	}

	private void logUnexpectedPks(boolean unexpected, SortedBeanKey pk){
		if(unexpected){
			logger.warn("unexpected pk {}", pk);
		}
	}

}

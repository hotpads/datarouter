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

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import org.apache.hadoop.hbase.client.Result;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import io.datarouter.client.hbase.HBaseClientManager;
import io.datarouter.client.hbase.config.DatarouterHBaseTestNgModuleFactory;
import io.datarouter.client.hbase.node.subentity.HBaseSubEntityNode;
import io.datarouter.client.hbase.node.subentity.HBaseSubEntityResultParser;
import io.datarouter.client.hbase.node.subentity.HBaseSubEntityResultParserFactory;
import io.datarouter.client.hbase.test.DatarouterHBaseTestClientIds;
import io.datarouter.client.hbase.test.DirectHBaseSubEntityGetOpsForTest;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.node.adapter.callsite.physical.PhysicalSubEntitySortedMapStorageCallsiteAdapter;
import io.datarouter.storage.node.factory.EntityNodeFactory;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.factory.WideNodeFactory;
import io.datarouter.storage.test.node.basic.sorted.BaseSortedBeanIntegrationTests;
import io.datarouter.storage.test.node.basic.sorted.DatarouterSortedNodeTestDao;
import io.datarouter.storage.test.node.basic.sorted.SortedBean;
import io.datarouter.storage.test.node.basic.sorted.SortedBean.SortedBeanFielder;
import io.datarouter.storage.test.node.basic.sorted.SortedBeanEntityKey;
import io.datarouter.storage.test.node.basic.sorted.SortedBeanEntityNode;
import io.datarouter.storage.test.node.basic.sorted.SortedBeanKey;

@Guice(moduleFactory = DatarouterHBaseTestNgModuleFactory.class)
public class HBaseSubEntityResultParserIntegrationTests extends BaseSortedBeanIntegrationTests{
	private static final int KEY_BAZ_VALUE = (int)System.currentTimeMillis();

	@Inject
	private EntityNodeFactory entityNodeFactory;
	@Inject
	private NodeFactory nodeFactory;
	@Inject
	private WideNodeFactory wideNodeFactory;
	@Inject
	private HBaseClientManager hBaseClientManager;

	private SortedBean savedBean;
	private DirectHBaseSubEntityGetOpsForTest<SortedBeanEntityKey,SortedBeanKey,SortedBean> directHBaseGet;
	private HBaseSubEntityResultParser<SortedBeanEntityKey,SortedBeanKey,SortedBean> resultParser;

	@BeforeClass
	@SuppressWarnings("unchecked")
	public void beforeClass(){
		setup(DatarouterHBaseTestClientIds.HBASE, true);
		savedBean = new SortedBean("a", "b", KEY_BAZ_VALUE, "d", "f1", 2L, "f3", 4D);
		dao.put(savedBean);
		HBaseSubEntityNode<SortedBeanEntityKey,?,SortedBeanKey,SortedBean,?> node =
				((PhysicalSubEntitySortedMapStorageCallsiteAdapter<?,?,?,?,HBaseSubEntityNode<SortedBeanEntityKey,?,
				SortedBeanKey,SortedBean,?>>)dao.getNodeFromEntity()).getUnderlyingNode();
		directHBaseGet = new DirectHBaseSubEntityGetOpsForTest<>(hBaseClientManager, DatarouterHBaseTestClientIds.HBASE,
				node, savedBean.getKey());
	}

	@Override
	protected void setup(ClientId clientId, boolean entity){
		dao = new DatarouterSortedNodeTestDao(
				datarouter,
				entityNodeFactory,
				SortedBeanEntityNode.ENTITY_NODE_PARAMS_4,
				nodeFactory,
				wideNodeFactory,
				DatarouterHBaseTestClientIds.HBASE,
				true);
		resetTable(true);
	}

	@AfterClass
	public void afterClass(){
		dao.delete(savedBean.getKey());
		datarouter.shutdown();
	}

	@Test
	public void testParseHBaseResultToPrimaryKey() throws IOException{
		resultParser = HBaseSubEntityResultParserFactory.create(SortedBeanEntityKey::new, SortedBeanKey.class,
				SortedBean::new, new SortedBeanFielder(), 1, directHBaseGet.fieldInfo.getEntityColumnPrefixBytes());
		doParseHBaseResultToPrimaryKey();
	}

	@Test
	public void testParseHBaseResultToPrimaryKeyUsingFieldInfos() throws IOException{
		resultParser = HBaseSubEntityResultParserFactory.create(directHBaseGet.entityFieldInfo,
				directHBaseGet.fieldInfo);
		doParseHBaseResultToPrimaryKey();
	}

	protected void doParseHBaseResultToPrimaryKey() throws IOException{
		Result result = directHBaseGet.table.get(directHBaseGet.hbaseGetOps.get(0));
		List<SortedBeanKey> keys = resultParser.getPrimaryKeysWithMatchingQualifierPrefix(result, null);
		Assert.assertEquals(keys.size(), 1);
		checkKeys(keys.get(0));
	}

	@Test
	public void testParseHBaseResultToDatabean() throws IOException{
		resultParser = HBaseSubEntityResultParserFactory.create(SortedBeanEntityKey::new, SortedBeanKey.class,
				SortedBean::new, new SortedBeanFielder(), 1, directHBaseGet.fieldInfo.getEntityColumnPrefixBytes());
		doParseHBaseResultToDatabean();
	}

	@Test
	public void testParseHBaseResultToDatabeanUsingFieldInfos() throws IOException{
		resultParser = HBaseSubEntityResultParserFactory.create(directHBaseGet.entityFieldInfo,
				directHBaseGet.fieldInfo);
		doParseHBaseResultToDatabean();
	}

	@Test(enabled = false)
	public void testEmptyTrailingStringInKey(){
		String firstField = "testEmptyTrailingStringInKey";
		String trailingString = "";
		SortedBeanKey pk = new SortedBeanKey(firstField, trailingString, 3, "qux");//EK has two fields
		SortedBean input = new SortedBean(pk, "f1", 2L, "f3", 4D);
		dao.put(input);
		SortedBean getOutput = dao.get(pk);
		Assert.assertEquals(getOutput, input);
		dao.delete(pk);
	}

	protected void doParseHBaseResultToDatabean() throws IOException{
		Result result = directHBaseGet.table.get(directHBaseGet.hbaseGetOps.get(0));
		List<SortedBean> sortedBeans = resultParser.getDatabeansForKvsWithMatchingQualifierPrefix(
				result.listCells(), null);
		Assert.assertEquals(sortedBeans.size(), 1);
		checkKeys(sortedBeans.get(0).getKey());
		Assert.assertEquals(sortedBeans.get(0).getF1(), savedBean.getF1());
		Assert.assertEquals(sortedBeans.get(0).getF3(), savedBean.getF3());
	}

	private void checkKeys(SortedBeanKey key){
		Assert.assertEquals(key.getFoo(), savedBean.getKey().getFoo());
		Assert.assertEquals(key.getBar(), savedBean.getKey().getBar());
		Assert.assertEquals(key.getBaz(), savedBean.getKey().getBaz());
		Assert.assertEquals(key.getQux(), savedBean.getKey().getQux());
	}

}
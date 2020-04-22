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
package io.datarouter.storage.test.node.basic.map;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.inject.Inject;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.field.Field;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.factory.WideNodeFactory;
import io.datarouter.storage.test.node.basic.map.databean.MapStorageBean;
import io.datarouter.storage.test.node.basic.map.databean.MapStorageBean.MapStorageBeanFielder;
import io.datarouter.storage.test.node.basic.map.databean.MapStorageBeanKey;

public abstract class BaseMapStorageIntegrationTests{

	@Inject
	private Datarouter datarouter;
	@Inject
	private NodeFactory nodeFactory;
	@Inject
	private WideNodeFactory wideNodeFactory;

	private DatarouterMapStorageTestDao dao;

	protected void setup(ClientId clientId, boolean entity){
		this.dao = new DatarouterMapStorageTestDao(datarouter, nodeFactory, wideNodeFactory, clientId, entity);
	}

	@AfterClass
	public void afterClass(){
		datarouter.shutdown();
	}

	@Test
	public void testGet(){
		List<MapStorageBean> beans = initBeans(1000);
		dao.putMulti(beans);
		int sampleSize = 29;
		for(int i = 0; i < beans.size(); i += sampleSize){
			MapStorageBean bean = beans.get(i);
			MapStorageBean roundTripped = dao.get(bean.getKey());
			Assert.assertEquals(roundTripped, bean);
		}
		deleteRecords(beans);
	}

	@Test
	public void testGetMulti(){
		List<MapStorageBean> beans = initBeans(10);
		dao.putMulti(beans);
		List<MapStorageBeanKey> keys = Scanner.of(beans).map(Databean::getKey).list();
		List<MapStorageBean> roundTripped = dao.getMulti(keys);
		Assert.assertEquals(roundTripped.size(), beans.size());
		beans.forEach(bean -> Assert.assertTrue(roundTripped.contains(bean)));
		deleteRecords(beans);
	}

	@Test
	public void testPutMulti(){
		List<MapStorageBean> beans = initBeans(10);
		beans.forEach(bean -> Assert.assertFalse(dao.exists(bean.getKey())));
		dao.putMulti(beans);
		beans.forEach(bean -> Assert.assertTrue(dao.exists(bean.getKey())));
		deleteRecords(beans);
	}

	@Test
	public void testBlankDatabeanPut(){
		var blankDatabean = new MapStorageBean(null);
		var nonBlankDatabean = new MapStorageBean("a");
		dao.putMulti(Arrays.asList(nonBlankDatabean, blankDatabean));
		MapStorageBean roundTrippedBlank = dao.get(blankDatabean.getKey());
		new MapStorageBeanFielder().getNonKeyFields(roundTrippedBlank).stream()
				.map(Field::getValue)
				.forEach(Assert::assertNull);
		Scanner.of(blankDatabean, nonBlankDatabean).map(Databean::getKey).flush(dao::deleteMulti);
		Assert.assertNull(dao.get(blankDatabean.getKey()));
	}


	@Test
	public void testGetKeys(){
		var bean0 = new MapStorageBean();
		var bean1 = new MapStorageBean(); // not inserted to db
		var bean2 = new MapStorageBean();

		dao.put(bean0);
		dao.put(bean2);

		List<MapStorageBeanKey> keysToGet = Arrays.asList(bean0.getKey(), bean1.getKey(), bean2.getKey());
		List<MapStorageBeanKey> keysGotten = dao.getKeys(keysToGet);

		Assert.assertTrue(keysGotten.contains(bean0.getKey()));
		Assert.assertFalse(keysGotten.contains(bean1.getKey()));
		Assert.assertTrue(keysGotten.contains(bean2.getKey()));

		deleteRecord(bean0);
		deleteRecord(bean2);
	}

	private List<MapStorageBean> initBeans(int size){
		return IntStream.range(0, size)
				.mapToObj(i -> new MapStorageBean("data" + i))
				.collect(Collectors.toList());
	}

	private void deleteRecord(MapStorageBean databean){
		dao.delete(databean.getKey());
	}

	private void deleteRecords(List<MapStorageBean> databeans){
		Scanner.of(databeans).map(Databean::getKey).flush(dao::deleteMulti);
	}

}

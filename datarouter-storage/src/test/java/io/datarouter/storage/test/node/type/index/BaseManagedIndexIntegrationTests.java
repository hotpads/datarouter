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
package io.datarouter.storage.test.node.type.index;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.datarouter.model.databean.DatabeanTool;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.test.TestDatabean;
import io.datarouter.storage.test.TestDatabeanKey;
import io.datarouter.storage.test.node.type.index.databean.TestDatabeanWithManagedIndexByBar;
import io.datarouter.storage.test.node.type.index.databean.TestDatabeanWithManagedIndexByBarKey;
import io.datarouter.storage.test.node.type.index.node.DatarouterTestDatabeanWithIndexDao;

public abstract class BaseManagedIndexIntegrationTests{

	private static final List<TestDatabean> TEST_DATABEANS = Arrays.asList(
			new TestDatabean("un", "alarc'h", "un"),
			new TestDatabean("alarc'h", "tra", "mor"),
			new TestDatabean("war", "lein", "tour"),
			new TestDatabean("moal", "kastell", "Arvor"),
			new TestDatabean("Neventi vad", "d'ar Vreton", "ed"),
			new TestDatabean("Ha malloz-ru", "d'ar C'hallaou", "ed"),
			new TestDatabean("Erru eul lestr", "e pleg ar m", "or"),
			new TestDatabean("He weliou gwenn", "gant han dig", "or"));

	private final ClientId clientId;

	@Inject
	private Datarouter datarouter;
	@Inject
	private NodeFactory nodeFactory;

	private DatarouterTestDatabeanWithIndexDao dao;
	private DatarouterTestDatabeanWithIndexDao daoTxn;

	public BaseManagedIndexIntegrationTests(ClientId clientId){
		this.clientId = clientId;
	}

	@BeforeClass
	public void setUp(){
		dao = new DatarouterTestDatabeanWithIndexDao(datarouter, nodeFactory, clientId,
				"TestDatabeanWithManagedIndex", false, TestDatabeanWithManagedIndexByBar.class.getSimpleName());
		daoTxn = new DatarouterTestDatabeanWithIndexDao(datarouter, nodeFactory, clientId,
				"TestDatabeanWithTxnManagedIndex", true, "TestDatabeanWithTxnManagedIndexByBar");
		dao.putMulti(TEST_DATABEANS);
		daoTxn.putMulti(TEST_DATABEANS);
	}

	@AfterClass
	public void afterClass(){
		dao.deleteAll();
		daoTxn.deleteAll();
		datarouter.shutdown();
	}

	@Test
	public void testLookupUnique(){
		testLookupUnique(dao);
		testLookupUnique(daoTxn);
	}

	private void testLookupUnique(DatarouterTestDatabeanWithIndexDao genericDao){
		TestDatabean databean = genericDao.lookupUnique(new TestDatabeanWithManagedIndexByBarKey("martolod"));
		Assert.assertNull(databean);
		databean = genericDao.lookupUnique(new TestDatabeanWithManagedIndexByBarKey("tra"));
		Assert.assertEquals(databean.getKey().getFoo(), "alarc'h");
		Assert.assertEquals(databean.getBar(), "tra");
		Assert.assertEquals(databean.getBaz(), "mor");
	}

	@Test
	public void testLookupMultiUnique(){
		testLookupMultiUnique(dao);
		testLookupMultiUnique(daoTxn);
	}

	private void testLookupMultiUnique(DatarouterTestDatabeanWithIndexDao genericDao){
		List<TestDatabeanWithManagedIndexByBarKey> keys = List.of(
				new TestDatabeanWithManagedIndexByBarKey("martolod"),
				new TestDatabeanWithManagedIndexByBarKey("kastell"),
				new TestDatabeanWithManagedIndexByBarKey("lein"));
		List<TestDatabean> databeans = genericDao.lookupMultiUnique(keys);
		Assert.assertEquals(2, databeans.size());
		for(TestDatabean d : databeans){
			Assert.assertTrue("moal".equals(d.getKey().getFoo()) || "war".equals(d.getKey().getFoo()));
			if("moal".equals(d.getKey().getFoo())){
				Assert.assertEquals(d.getBar(), "kastell");
				Assert.assertEquals(d.getBaz(), "Arvor");
			}
			if("war".equals(d.getKey().getFoo())){
				Assert.assertEquals(d.getBar(), "lein");
				Assert.assertEquals(d.getBaz(), "tour");
			}
		}
	}

	@Test
	public void testLookupIndex(){
		testLookupIndex(dao);
		testLookupIndex(daoTxn);
	}

	private void testLookupIndex(DatarouterTestDatabeanWithIndexDao genericDao){
		TestDatabeanWithManagedIndexByBar entry = genericDao.get(new TestDatabeanWithManagedIndexByBarKey("martolod"));
		Assert.assertNull(entry);
		entry = genericDao.get(new TestDatabeanWithManagedIndexByBarKey("tra"));
		Assert.assertNotNull(entry);
		Assert.assertEquals(entry.getFoo(), "alarc'h");
		Assert.assertEquals(entry.getKey().getBar(), "tra");
	}

	@Test
	public void testLookupMultiIndex(){
		testLookupMultiIndex(dao);
		testLookupMultiIndex(daoTxn);
	}

	private void testLookupMultiIndex(DatarouterTestDatabeanWithIndexDao genericDao){
		List<TestDatabeanWithManagedIndexByBarKey> keys = List.of(
				new TestDatabeanWithManagedIndexByBarKey("martolod"),
				new TestDatabeanWithManagedIndexByBarKey("kastell"),
				new TestDatabeanWithManagedIndexByBarKey("lein"));
		List<TestDatabeanWithManagedIndexByBar> entries = genericDao.getMultiByB(keys);
		Assert.assertEquals(2, entries.size());
		for(TestDatabeanWithManagedIndexByBar entry : entries){
			Assert.assertTrue("moal".equals(entry.getFoo()) || "war".equals(entry.getFoo()));
			if("moal".equals(entry.getFoo())){
				Assert.assertEquals(entry.getKey().getBar(), "kastell");
			}
			if("war".equals(entry.getFoo())){
				Assert.assertEquals(entry.getKey().getBar(), "lein");
			}
		}
	}

	@Test
	public void testDeleteUnique(){
		testDeleteUnique(dao);
		testDeleteUnique(daoTxn);
	}

	private void testDeleteUnique(DatarouterTestDatabeanWithIndexDao genericDao){
		var databean = new TestDatabean("tri", "martolod", "yaouank");
		var databeanIndexKey = new TestDatabeanWithManagedIndexByBarKey("martolod");
		Assert.assertNull(genericDao.get(databean.getKey()));
		Assert.assertNull(genericDao.lookupUnique(databeanIndexKey));
		genericDao.put(databean);
		Assert.assertNotNull(genericDao.get(databean.getKey()));
		Assert.assertNotNull(genericDao.lookupUnique(databeanIndexKey));
		genericDao.deleteUnique(databeanIndexKey);
		Assert.assertNull(genericDao.get(databean.getKey()));
		Assert.assertNull(genericDao.lookupUnique(databeanIndexKey));
	}

	@Test
	public void testDeleteMultiUnique(){
		testDeleteMultiUnique(dao);
		testDeleteMultiUnique(daoTxn);
	}

	private void testDeleteMultiUnique(DatarouterTestDatabeanWithIndexDao genericDao){
		List<TestDatabean> databeans = List.of(
				new TestDatabean("tri", "martolod", "yaouank"),
				new TestDatabean("i vonet", "da", "veajiñ"));
		List<TestDatabeanKey> keys = DatabeanTool.getKeys(databeans);
		List<TestDatabeanWithManagedIndexByBarKey> entryKeys = new ArrayList<>();
		for(TestDatabean databean : databeans){
			entryKeys.add(new TestDatabeanWithManagedIndexByBarKey(databean.getBar()));
		}
		Assert.assertEquals(genericDao.getMulti(keys).size(), 0);
		Assert.assertEquals(genericDao.lookupMultiUnique(entryKeys).size(), 0);
		genericDao.putMulti(databeans);
		Assert.assertEquals(genericDao.getMulti(keys).size(), 2);
		Assert.assertEquals(genericDao.lookupMultiUnique(entryKeys).size(), 2);
		genericDao.deleteMultiUnique(entryKeys);
		Assert.assertEquals(genericDao.getMulti(keys).size(), 0);
		Assert.assertEquals(genericDao.lookupMultiUnique(entryKeys).size(), 0);
	}

	@Test
	public void testScanIndex(){
		testScanUniqueIndex(dao);
		testScanUniqueIndex(daoTxn);
	}

	private void testScanUniqueIndex(DatarouterTestDatabeanWithIndexDao genericDao){
		TestDatabeanWithManagedIndexByBar previous = null;
		int count = 0;
		for(TestDatabeanWithManagedIndexByBar indexEntry : genericDao.scanByB().iterable()){
			if(previous != null){
				Assert.assertTrue(indexEntry.getKey().compareTo(previous.getKey()) >= 0);
			}
			previous = indexEntry;
			count++;
		}
		Assert.assertEquals(TEST_DATABEANS.size(), count);
	}

	@Test
	public void testScan(){
		testScanUnique(dao);
		testScanUnique(daoTxn);
	}

	private void testScanUnique(DatarouterTestDatabeanWithIndexDao genericDao){
		TestDatabean previous = null;
		int count = 0;
		for(TestDatabean databean : genericDao.scanDatabeansByB().iterable()){
			if(previous != null){
				Assert.assertTrue(databean.getBar().compareTo(previous.getBar()) >= 0);
			}
			previous = databean;
			count++;
		}
		Assert.assertEquals(TEST_DATABEANS.size(), count);
	}

	@Test
	public void testEquals(){
		var d1 = new TestDatabean("tri", "martolod", "yaouank");
		var d2 = new TestDatabean("lalala", "lalala", "la");
		var d3 = new TestDatabean("tri", "martolod", "yaouank");

		Assert.assertEquals(d1, d3);
		Assert.assertNotSame(d2, d3);
		Assert.assertNotSame(d2, d1);
	}

	@Test(enabled = false) // TODO fix as part of DATAROUTER-705
	public void testScanKeysLimit(){
		checkScanKeys(dao, Optional.of(5));
		checkScanKeys(daoTxn, Optional.of(5));
	}

	@Test
	public void testScanKeys(){
		checkScanKeys(dao, Optional.empty());
		checkScanKeys(daoTxn, Optional.empty());
	}

	private void checkScanKeys(DatarouterTestDatabeanWithIndexDao genericDao, Optional<Integer> limit){
		TestDatabeanWithManagedIndexByBarKey previous = null;
		int count = 0;
		for(TestDatabeanWithManagedIndexByBarKey key : genericDao.scanKeysByB(limit).iterable()){
			if(previous != null){
				Assert.assertTrue(key.getBar().compareTo(previous.getBar()) > 0);
			}
			previous = key;
			count++;
		}
		Assert.assertEquals(count, (int)limit.orElse(TEST_DATABEANS.size()));
	}

}

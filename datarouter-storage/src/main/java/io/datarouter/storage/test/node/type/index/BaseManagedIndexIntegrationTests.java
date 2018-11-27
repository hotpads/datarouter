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

import java.util.LinkedList;
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
import io.datarouter.storage.config.Config;
import io.datarouter.storage.test.TestDatabean;
import io.datarouter.storage.test.TestDatabeanKey;
import io.datarouter.storage.test.node.type.index.databean.TestDatabeanWithManagedIndexByBar;
import io.datarouter.storage.test.node.type.index.databean.TestDatabeanWithManagedIndexByBarKey;
import io.datarouter.storage.test.node.type.index.node.DatarouterTestDatabeanWithIndexRouter;
import io.datarouter.storage.test.node.type.index.node.TestDatabeanWithManagedIndexRouter;
import io.datarouter.storage.test.node.type.index.node.TestDatabeanWithTxnManagedIndexRouter;
import io.datarouter.storage.test.node.type.index.router.ManagedIndexTestRoutersFactory;
import io.datarouter.storage.test.node.type.index.router.ManagedIndexTestRoutersFactory.ManagedIndexTestRouters;
import io.datarouter.util.collection.ListTool;

public abstract class BaseManagedIndexIntegrationTests{

	@Inject
	private Datarouter datarouter;
	@Inject
	private ManagedIndexTestRoutersFactory routersFactory;

	private TestDatabeanWithManagedIndexRouter router;
	private TestDatabeanWithTxnManagedIndexRouter routerWithTxnManaged;

	private LinkedList<TestDatabean> testDatabeans;
	private ManagedIndexTestRouters routers;
	private ClientId clientId;

	public BaseManagedIndexIntegrationTests(ClientId clientId){
		this.clientId = clientId;
	}

	@BeforeClass
	public void setUp(){
		routers = routersFactory.new ManagedIndexTestRouters(clientId);
		router = routers.testDatabeanWithManagedIndex;
		routerWithTxnManaged = routers.testDatabeanWithTxnManagedIndex;

		testDatabeans = ListTool.createLinkedList(
				new TestDatabean("un", "alarc'h", "un"),
				new TestDatabean("alarc'h", "tra", "mor"),
				new TestDatabean("war", "lein", "tour"),
				new TestDatabean("moal", "kastell", "Arvor"),
				new TestDatabean("Neventi vad", "d'ar Vreton", "ed"),
				new TestDatabean("Ha malloz-ru", "d'ar C'hallaou", "ed"),
				new TestDatabean("Erru eul lestr", "e pleg ar m", "or"),
				new TestDatabean("He weliou gwenn", "gant han dig", "or"));
		router.mainNode.putMulti(testDatabeans, null);
		routerWithTxnManaged.mainNode.putMulti(testDatabeans, null);
	}

	@AfterClass
	public void afterClass(){
		router.mainNode.deleteAll(null);
		routerWithTxnManaged.mainNode.deleteAll(null);
		datarouter.shutdown();
	}

	@Test
	public void testLookupUnique(){
		testLookupUnique(router);
		testLookupUnique(routerWithTxnManaged);
	}

	private void testLookupUnique(DatarouterTestDatabeanWithIndexRouter router){
		TestDatabean databean = router.byB.lookupUnique(new TestDatabeanWithManagedIndexByBarKey("martolod"), null);
		Assert.assertNull(databean);
		databean = router.byB.lookupUnique(new TestDatabeanWithManagedIndexByBarKey("tra"), null);
		Assert.assertEquals(databean.getFoo(), "alarc'h");
		Assert.assertEquals(databean.getBar(), "tra");
		Assert.assertEquals(databean.getBaz(), "mor");
	}

	@Test
	public void testLookupMultiUnique(){
		testLookupMultiUnique(router);
		testLookupMultiUnique(routerWithTxnManaged);
	}

	private void testLookupMultiUnique(DatarouterTestDatabeanWithIndexRouter router){
		LinkedList<TestDatabeanWithManagedIndexByBarKey> keys = ListTool.createLinkedList(
				new TestDatabeanWithManagedIndexByBarKey("martolod"),
				new TestDatabeanWithManagedIndexByBarKey("kastell"),
				new TestDatabeanWithManagedIndexByBarKey("lein"));
		List<TestDatabean> databeans = router.byB.lookupMultiUnique(keys, null);
		Assert.assertEquals(2, databeans.size());
		for(TestDatabean d : databeans){
			Assert.assertTrue("moal".equals(d.getFoo()) || "war".equals(d.getFoo()));
			if("moal".equals(d.getFoo())){
				Assert.assertEquals(d.getBar(), "kastell");
				Assert.assertEquals(d.getBaz(), "Arvor");
			}
			if("war".equals(d.getFoo())){
				Assert.assertEquals(d.getBar(), "lein");
				Assert.assertEquals(d.getBaz(), "tour");
			}
		}
	}

	@Test
	public void testLookupIndex(){
		testLookupIndex(router);
		testLookupIndex(routerWithTxnManaged);
	}

	private void testLookupIndex(DatarouterTestDatabeanWithIndexRouter router){
		TestDatabeanWithManagedIndexByBar entry = router.byB.get(new TestDatabeanWithManagedIndexByBarKey(
				"martolod"), null);
		Assert.assertNull(entry);
		entry = router.byB.get(new TestDatabeanWithManagedIndexByBarKey("tra"), null);
		Assert.assertNotNull(entry);
		Assert.assertEquals(entry.getFoo(), "alarc'h");
		Assert.assertEquals(entry.getBar(), "tra");
	}

	@Test
	public void testLookupMultiIndex(){
		testLookupMultiIndex(router);
		testLookupMultiIndex(routerWithTxnManaged);
	}

	private void testLookupMultiIndex(DatarouterTestDatabeanWithIndexRouter router){
		LinkedList<TestDatabeanWithManagedIndexByBarKey> keys = ListTool.createLinkedList(
				new TestDatabeanWithManagedIndexByBarKey("martolod"),
				new TestDatabeanWithManagedIndexByBarKey("kastell"),
				new TestDatabeanWithManagedIndexByBarKey("lein"));
		List<TestDatabeanWithManagedIndexByBar> entries = router.byB.getMulti(keys, null);
		Assert.assertEquals(2, entries.size());
		for(TestDatabeanWithManagedIndexByBar entry : entries){
			Assert.assertTrue("moal".equals(entry.getFoo()) || "war".equals(entry.getFoo()));
			if("moal".equals(entry.getFoo())){
				Assert.assertEquals(entry.getBar(), "kastell");
			}
			if("war".equals(entry.getFoo())){
				Assert.assertEquals(entry.getBar(), "lein");
			}
		}
	}

	@Test
	public void testDeleteUnique(){
		testDeleteUnique(router);
		testDeleteUnique(routerWithTxnManaged);
	}

	private void testDeleteUnique(DatarouterTestDatabeanWithIndexRouter router){
		TestDatabean databean = new TestDatabean("tri", "martolod", "yaouank");
		TestDatabeanWithManagedIndexByBarKey databeanIndexKey = new TestDatabeanWithManagedIndexByBarKey("martolod");
		Assert.assertNull(router.mainNode.get(databean.getKey(), null));
		Assert.assertNull(router.byB.lookupUnique(databeanIndexKey, null));
		router.mainNode.put(databean, null);
		Assert.assertNotNull(router.mainNode.get(databean.getKey(), null));
		Assert.assertNotNull(router.byB.lookupUnique(databeanIndexKey, null));
		router.byB.deleteUnique(databeanIndexKey, null);
		Assert.assertNull(router.mainNode.get(databean.getKey(), null));
		Assert.assertNull(router.byB.lookupUnique(databeanIndexKey, null));
	}

	@Test
	public void testDeleteMultiUnique(){
		testDeleteMultiUnique(router);
		testDeleteMultiUnique(routerWithTxnManaged);
	}

	private void testDeleteMultiUnique(DatarouterTestDatabeanWithIndexRouter router){
		List<TestDatabean> databeans = ListTool.createLinkedList(
				new TestDatabean("tri", "martolod", "yaouank"),
				new TestDatabean("i vonet", "da", "veajiñ"));
		List<TestDatabeanKey> keys = DatabeanTool.getKeys(databeans);
		List<TestDatabeanWithManagedIndexByBarKey> entryKeys = new LinkedList<>();
		for(TestDatabean databean : databeans){
			entryKeys.add(new TestDatabeanWithManagedIndexByBarKey(databean.getBar()));
		}
		Assert.assertEquals(0, router.mainNode.getMulti(keys, null).size());
		Assert.assertEquals(0, router.byB.lookupMultiUnique(entryKeys, null).size());
		router.mainNode.putMulti(databeans, null);
		Assert.assertEquals(2, router.mainNode.getMulti(keys, null).size());
		Assert.assertEquals(2, router.byB.lookupMultiUnique(entryKeys, null).size());
		router.byB.deleteMultiUnique(entryKeys, null);
		Assert.assertEquals(0, router.mainNode.getMulti(keys, null).size());
		Assert.assertEquals(0, router.byB.lookupMultiUnique(entryKeys, null).size());
	}

	@Test
	public void testScanIndex(){
		testScanUniqueIndex(router);
		testScanUniqueIndex(routerWithTxnManaged);
	}

	private void testScanUniqueIndex(DatarouterTestDatabeanWithIndexRouter router){
		TestDatabeanWithManagedIndexByBar previous = null;
		int count = 0;
		for(TestDatabeanWithManagedIndexByBar indexEntry : router.byB.scan(null, null)){
			if(previous != null){
				Assert.assertTrue(indexEntry.getKey().compareTo(previous.getKey()) >= 0);
			}
			previous = indexEntry;
			count++;
		}
		Assert.assertEquals(testDatabeans.size(), count);
	}

	@Test
	public void testScan(){
		testScanUnique(router);
		testScanUnique(routerWithTxnManaged);
	}

	private void testScanUnique(DatarouterTestDatabeanWithIndexRouter router){
		TestDatabean previous = null;
		int count = 0;
		for(TestDatabean databean : router.byB.scanDatabeans(null, null)){
			if(previous != null){
				Assert.assertTrue(databean.getBar().compareTo(previous.getBar()) >= 0);
			}
			previous = databean;
			count++;
		}
		Assert.assertEquals(testDatabeans.size(), count);
	}

	@Test
	public void testEquals(){
		TestDatabean d1 = new TestDatabean("tri", "martolod", "yaouank");
		TestDatabean d2 = new TestDatabean("lalala", "lalala", "la");
		TestDatabean d3 = new TestDatabean("tri", "martolod", "yaouank");

		Assert.assertEquals(d1, d3);
		Assert.assertNotSame(d2, d3);
		Assert.assertNotSame(d2, d1);
	}

	@Test(enabled = false) // TODO fix as part of DATAROUTER-705
	public void testScanKeysLimit(){
		checkScanKeys(Optional.of(5));
	}

	@Test
	public void testScanKeys(){
		checkScanKeys(Optional.empty());
	}

	private void checkScanKeys(Optional<Integer> limit){
		TestDatabeanWithManagedIndexByBarKey previous = null;
		int count = 0;
		Config config = limit.map(intLimit -> new Config().setLimit(intLimit)).orElse(null);
		for(TestDatabeanWithManagedIndexByBarKey key : routers.testDatabeanWithManagedIndex.byB.scanKeys(null, config)){
			if(previous != null){
				Assert.assertTrue(key.getBar().compareTo(previous.getBar()) > 0);
			}
			previous = key;
			count++;
		}
		Assert.assertEquals(count, (int)limit.orElse(testDatabeans.size()));
	}

}

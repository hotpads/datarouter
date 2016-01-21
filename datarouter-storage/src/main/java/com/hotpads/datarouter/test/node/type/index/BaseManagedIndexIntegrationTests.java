package com.hotpads.datarouter.test.node.type.index;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.hotpads.datarouter.client.ClientId;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.storage.databean.DatabeanTool;
import com.hotpads.datarouter.test.TestDatabean;
import com.hotpads.datarouter.test.TestDatabeanKey;
import com.hotpads.datarouter.test.node.type.index.databean.TestDatabeanWithManagedIndexByBar;
import com.hotpads.datarouter.test.node.type.index.databean.TestDatabeanWithManagedIndexByBarKey;
import com.hotpads.datarouter.test.node.type.index.databean.TestDatabeanWithManagedIndexByBaz;
import com.hotpads.datarouter.test.node.type.index.databean.TestDatabeanWithManagedIndexByBazKey;
import com.hotpads.datarouter.test.node.type.index.node.TestDatabeanWithIndexNode;
import com.hotpads.datarouter.test.node.type.index.node.TestDatabeanWithManagedIndexNode;
import com.hotpads.datarouter.test.node.type.index.node.TestDatabeanWithTxnManagedIndexNode;
import com.hotpads.datarouter.test.node.type.index.router.ManagedIndexTestRouter;
import com.hotpads.datarouter.test.node.type.index.router.ManagedIndexTestRouter.ManagedIndexTestRouterFactory;
import com.hotpads.datarouter.util.core.DrListTool;

public abstract class BaseManagedIndexIntegrationTests{

	@Inject
	private Datarouter datarouter;
	@Inject
	private ManagedIndexTestRouterFactory routerFactory;

	private TestDatabeanWithManagedIndexNode node;
	private TestDatabeanWithTxnManagedIndexNode nodeWithTxnManaged;

	private LinkedList<TestDatabean> testDatabeans;
	private ManagedIndexTestRouter router;
	private ClientId clientId;

	public BaseManagedIndexIntegrationTests(ClientId clientId){
		this.clientId = clientId;
	}

	@BeforeClass
	public void setUp(){
		router = routerFactory.createWithClientId(clientId);
		node = router.testDatabeanWithManagedIndex;
		nodeWithTxnManaged = router.testDatabeanWithTxnManagedIndex;

		testDatabeans = DrListTool.createLinkedList(
				new TestDatabean("un", "alarc'h", "un"),
				new TestDatabean("alarc'h", "tra", "mor"),
				new TestDatabean("war", "lein", "tour"),
				new TestDatabean("moal", "kastell", "Arvor"),
				new TestDatabean("Neventi vad", "d'ar Vreton", "ed"),
				new TestDatabean("Ha malloz-ru", "d'ar C'hallaou", "ed"),
				new TestDatabean("Erru eul lestr", "e pleg ar m", "or"),
				new TestDatabean("He weliou gwenn", "gant han dig", "or"));
		node.mainNode.putMulti(testDatabeans, null);
		nodeWithTxnManaged.mainNode.putMulti(testDatabeans, null);
	}

	@AfterClass
	public void afterClass(){
		node.mainNode.deleteAll(null);
		nodeWithTxnManaged.mainNode.deleteAll(null);
		datarouter.shutdown();
	}

	@Test
	public void testLookupUnique(){
		testLookupUnique(node);
		testLookupUnique(nodeWithTxnManaged);
	}

	private void testLookupUnique(TestDatabeanWithIndexNode node){
		TestDatabean databean = node.byB.lookupUnique(new TestDatabeanWithManagedIndexByBarKey("martolod"), null);
		Assert.assertNull(databean);
		databean = node.byB.lookupUnique(new TestDatabeanWithManagedIndexByBarKey("tra"), null);
		Assert.assertEquals(databean.getFoo(), "alarc'h");
		Assert.assertEquals(databean.getBar(), "tra");
		Assert.assertEquals(databean.getBaz(), "mor");
	}

	@Test
	public void testLookupMultiUnique(){
		testLookupMultiUnique(node);
		testLookupMultiUnique(nodeWithTxnManaged);
	}

	private void testLookupMultiUnique(TestDatabeanWithIndexNode node){
		LinkedList<TestDatabeanWithManagedIndexByBarKey> keys = DrListTool.createLinkedList(
				new TestDatabeanWithManagedIndexByBarKey("martolod"),
				new TestDatabeanWithManagedIndexByBarKey("kastell"),
				new TestDatabeanWithManagedIndexByBarKey("lein"));
		List<TestDatabean> databeans = node.byB.lookupMultiUnique(keys, null);
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
		testLookupIndex(node);
		testLookupIndex(nodeWithTxnManaged);
	}

	private void testLookupIndex(TestDatabeanWithIndexNode node){
		TestDatabeanWithManagedIndexByBar entry = node.byB.get(new TestDatabeanWithManagedIndexByBarKey(
				"martolod"), null);
		Assert.assertNull(entry);
		entry = node.byB.get(new TestDatabeanWithManagedIndexByBarKey("tra"), null);
		Assert.assertNotNull(entry);
		Assert.assertEquals(entry.getFoo(), "alarc'h");
		Assert.assertEquals(entry.getBar(), "tra");
	}

	@Test
	public void testLookupMultiIndex(){
		testLookupMultiIndex(node);
		testLookupMultiIndex(nodeWithTxnManaged);
	}

	private void testLookupMultiIndex(TestDatabeanWithIndexNode node){
		LinkedList<TestDatabeanWithManagedIndexByBarKey> keys = DrListTool.createLinkedList(
				new TestDatabeanWithManagedIndexByBarKey("martolod"),
				new TestDatabeanWithManagedIndexByBarKey("kastell"),
				new TestDatabeanWithManagedIndexByBarKey("lein"));
		List<TestDatabeanWithManagedIndexByBar> entries = node.byB.getMulti(keys, null);
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
		testDeleteUnique(node);
		testDeleteUnique(nodeWithTxnManaged);
	}

	private void testDeleteUnique(TestDatabeanWithIndexNode node){
		TestDatabean databean = new TestDatabean("tri", "martolod", "yaouank");
		TestDatabeanWithManagedIndexByBarKey databeanIndexKey = new TestDatabeanWithManagedIndexByBarKey("martolod");
		Assert.assertNull(node.mainNode.get(databean.getKey(), null));
		Assert.assertNull(node.byB.lookupUnique(databeanIndexKey, null));
		node.mainNode.put(databean, null);
		Assert.assertNotNull(node.mainNode.get(databean.getKey(), null));
		Assert.assertNotNull(node.byB.lookupUnique(databeanIndexKey, null));
		node.byB.deleteUnique(databeanIndexKey, null);
		Assert.assertNull(node.mainNode.get(databean.getKey(), null));
		Assert.assertNull(node.byB.lookupUnique(databeanIndexKey, null));
	}

	@Test
	public void testDeleteMultiUnique(){
		testDeleteMultiUnique(node);
		testDeleteMultiUnique(nodeWithTxnManaged);
	}

	private void testDeleteMultiUnique(TestDatabeanWithIndexNode node){
		List<TestDatabean> databeans = DrListTool.createLinkedList(
				new TestDatabean("tri", "martolod", "yaouank"),
				new TestDatabean("i vonet", "da", "veajiñ"));
		List<TestDatabeanKey> keys = DatabeanTool.getKeys(databeans);
		List<TestDatabeanWithManagedIndexByBarKey> entryKeys = new LinkedList<>();
		for(TestDatabean databean : databeans){
			entryKeys.add(new TestDatabeanWithManagedIndexByBarKey(databean.getBar()));
		}
		Assert.assertEquals(0, node.mainNode.getMulti(keys, null).size());
		Assert.assertEquals(0, node.byB.lookupMultiUnique(entryKeys, null).size());
		node.mainNode.putMulti(databeans, null);
		Assert.assertEquals(2, node.mainNode.getMulti(keys, null).size());
		Assert.assertEquals(2, node.byB.lookupMultiUnique(entryKeys, null).size());
		node.byB.deleteMultiUnique(entryKeys, null);
		Assert.assertEquals(0, node.mainNode.getMulti(keys, null).size());
		Assert.assertEquals(0, node.byB.lookupMultiUnique(entryKeys, null).size());
	}

	@Test
	public void testLookupMulti(){
		testLookupMulti(node);
		testLookupMulti(nodeWithTxnManaged);
	}

	private void testLookupMulti(TestDatabeanWithIndexNode node){
		List<TestDatabean> databeans = node.byC.lookupMulti(
				new TestDatabeanWithManagedIndexByBazKey("lala"), null);
		Assert.assertEquals(databeans.size(), 0);
		databeans = node.byC.lookupMulti(
				new TestDatabeanWithManagedIndexByBazKey("ed"), null);
		Assert.assertEquals(databeans.size(), 2);
		List<TestDatabean> expected = DrListTool.create(
				new TestDatabean("Neventi vad", "d'ar Vreton", "ed"),
				new TestDatabean("Ha malloz-ru", "d'ar C'hallaou", "ed"));
		for(TestDatabean d : databeans){
			Assert.assertTrue(expected.remove(d));
		}
	}

	@Test
	public void testLookupMultiMulti(){
		testLookupMultiMulti(node);
		testLookupMultiMulti(nodeWithTxnManaged);
	}

	private void testLookupMultiMulti(TestDatabeanWithIndexNode node){
		List<TestDatabeanWithManagedIndexByBazKey> keys = DrListTool.create(
				new TestDatabeanWithManagedIndexByBazKey("ed"),
				new TestDatabeanWithManagedIndexByBazKey("or"));
		List<TestDatabean> databeans = node.byC.lookupMultiMulti(keys, null);
		Assert.assertEquals(databeans.size(), 4);
		List<TestDatabean> expected = DrListTool.create(
				new TestDatabean("Neventi vad", "d'ar Vreton", "ed"),
				new TestDatabean("Ha malloz-ru", "d'ar C'hallaou", "ed"),
				new TestDatabean("Erru eul lestr", "e pleg ar m", "or"),
				new TestDatabean("He weliou gwenn", "gant han dig", "or"));
		for(TestDatabean d : databeans){
			Assert.assertTrue(expected.remove(d));
		}
	}

	@Test
	public void testScanIndex(){
		testScanUniqueIndex(node);
		testScanUniqueIndex(nodeWithTxnManaged);

		testScanMultiIndex(node);
		testScanMultiIndex(nodeWithTxnManaged);
	}

	private void testScanUniqueIndex(TestDatabeanWithIndexNode node){
		TestDatabeanWithManagedIndexByBar previous = null;
		int count = 0;
		for(TestDatabeanWithManagedIndexByBar indexEntry : node.byB.scan(null, null)){
			if(previous != null){
				Assert.assertTrue(indexEntry.getKey().compareTo(previous.getKey()) >= 0);
			}
			previous = indexEntry;
			count++;
		}
		Assert.assertEquals(testDatabeans.size(), count);
	}

	private void testScanMultiIndex(TestDatabeanWithIndexNode node){
		TestDatabeanWithManagedIndexByBaz previous = null;
		int count = 0;
		for(TestDatabeanWithManagedIndexByBaz indexEntry : node.byC.scan(null, null)){
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
		testScanUnique(node);
		testScanUnique(nodeWithTxnManaged);

		testScanMulti(node);
		testScanMulti(nodeWithTxnManaged);
	}

	private void testScanUnique(TestDatabeanWithIndexNode node){
		TestDatabean previous = null;
		int count = 0;
		for(TestDatabean databean : node.byB.scanDatabeans(null, null)){
			if(previous != null){
				Assert.assertTrue(databean.getBar().compareTo(previous.getBar()) >= 0);
			}
			previous = databean;
			count++;
		}
		Assert.assertEquals(testDatabeans.size(), count);
	}

	private void testScanMulti(TestDatabeanWithIndexNode node){
		TestDatabean previous = null;
		int count = 0;
		for(TestDatabean databean : node.byC.scanDatabeans(null, null)){
			if(previous != null){
				Assert.assertTrue(databean.getBaz().compareTo(previous.getBaz()) >= 0);
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

	@Test
	public void testScanKeys(){
		TestDatabeanWithManagedIndexByBarKey previous = null;
		int count = 0;
		for(TestDatabeanWithManagedIndexByBarKey key : router.testDatabeanWithManagedIndex.byB.scanKeys(null, null)){
			if(previous != null){
				Assert.assertTrue(key.getBar().compareTo(previous.getBar()) > 0);
			}
			previous = key;
			count++;
		}
		Assert.assertEquals(testDatabeans.size(), count);
	}

}

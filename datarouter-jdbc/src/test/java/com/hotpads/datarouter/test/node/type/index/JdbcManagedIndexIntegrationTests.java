package com.hotpads.datarouter.test.node.type.index;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import com.hotpads.datarouter.client.imp.jdbc.TestDatarouterJdbcModuleFactory;
import com.hotpads.datarouter.routing.Datarouter;
import com.hotpads.datarouter.storage.databean.DatabeanTool;
import com.hotpads.datarouter.test.TestDatabean;
import com.hotpads.datarouter.test.TestDatabeanKey;
import com.hotpads.datarouter.test.node.type.index.databean.TestDatabeanWithManagedIndexByB;
import com.hotpads.datarouter.test.node.type.index.databean.TestDatabeanWithManagedIndexByBKey;
import com.hotpads.datarouter.test.node.type.index.databean.TestDatabeanWithManagedIndexByC;
import com.hotpads.datarouter.test.node.type.index.databean.TestDatabeanWithManagedIndexByCKey;
import com.hotpads.datarouter.test.node.type.index.node.TestDatabeanWithIndexNode;
import com.hotpads.datarouter.test.node.type.index.node.TestDatabeanWithManagedIndexNode;
import com.hotpads.datarouter.test.node.type.index.node.TestDatabeanWithTxnManagedIndexNode;
import com.hotpads.datarouter.test.node.type.index.router.ManagedIndexTestRouter;
import com.hotpads.datarouter.util.core.DrListTool;

@Guice(moduleFactory=TestDatarouterJdbcModuleFactory.class)
public class JdbcManagedIndexIntegrationTests{

	@Inject
	private Datarouter datarouter;
	@Inject
	private ManagedIndexTestRouter router;

	private TestDatabeanWithManagedIndexNode node;
	private TestDatabeanWithTxnManagedIndexNode nodeWithTxnManaged;

	private LinkedList<TestDatabean> testDatabeans;

	@BeforeClass
	public void setUp(){
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
		TestDatabean databean = node.byB.lookupUnique(new TestDatabeanWithManagedIndexByBKey("martolod"), null);
		Assert.assertNull(databean);
		databean = node.byB.lookupUnique(new TestDatabeanWithManagedIndexByBKey("tra"), null);
		Assert.assertEquals(databean.getA(), "alarc'h");
		Assert.assertEquals(databean.getB(), "tra");
		Assert.assertEquals(databean.getC(), "mor");
	}

	@Test
	public void testLookupMultiUnique(){
		testLookupMultiUnique(node);
		testLookupMultiUnique(nodeWithTxnManaged);
	}

	private void testLookupMultiUnique(TestDatabeanWithIndexNode node){
		LinkedList<TestDatabeanWithManagedIndexByBKey> keys = DrListTool.createLinkedList(
				new TestDatabeanWithManagedIndexByBKey("martolod"),
				new TestDatabeanWithManagedIndexByBKey("kastell"),
				new TestDatabeanWithManagedIndexByBKey("lein"));
		List<TestDatabean> databeans = node.byB.lookupMultiUnique(keys, null);
		Assert.assertEquals(2, databeans.size());
		for(TestDatabean d : databeans){
			Assert.assertTrue("moal".equals(d.getA()) || "war".equals(d.getA()));
			if("moal".equals(d.getA())){
				Assert.assertEquals(d.getB(), "kastell");
				Assert.assertEquals(d.getC(), "Arvor");
			}
			if("war".equals(d.getA())){
				Assert.assertEquals(d.getB(), "lein");
				Assert.assertEquals(d.getC(), "tour");
			}
		}
	}

	@Test
	public void testLookupIndex(){
		testLookupIndex(node);
		testLookupIndex(nodeWithTxnManaged);
	}

	private void testLookupIndex(TestDatabeanWithIndexNode node){
		TestDatabeanWithManagedIndexByB entry = node.byB.get(new TestDatabeanWithManagedIndexByBKey(
				"martolod"), null);
		Assert.assertNull(entry);
		entry = node.byB.get(new TestDatabeanWithManagedIndexByBKey("tra"), null);
		Assert.assertNotNull(entry);
		Assert.assertEquals(entry.getA(), "alarc'h");
		Assert.assertEquals(entry.getB(), "tra");
	}

	@Test
	public void testLookupMultiIndex(){
		testLookupMultiIndex(node);
		testLookupMultiIndex(nodeWithTxnManaged);
	}

	private void testLookupMultiIndex(TestDatabeanWithIndexNode node){
		LinkedList<TestDatabeanWithManagedIndexByBKey> keys = DrListTool.createLinkedList(
				new TestDatabeanWithManagedIndexByBKey("martolod"),
				new TestDatabeanWithManagedIndexByBKey("kastell"),
				new TestDatabeanWithManagedIndexByBKey("lein"));
		List<TestDatabeanWithManagedIndexByB> entries = node.byB.getMulti(keys, null);
		Assert.assertEquals(2, entries.size());
		for(TestDatabeanWithManagedIndexByB entry : entries){
			Assert.assertTrue("moal".equals(entry.getA()) || "war".equals(entry.getA()));
			if("moal".equals(entry.getA())){
				Assert.assertEquals(entry.getB(), "kastell");
			}
			if("war".equals(entry.getA())){
				Assert.assertEquals(entry.getB(), "lein");
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
		TestDatabeanWithManagedIndexByBKey databeanIndexKey = new TestDatabeanWithManagedIndexByBKey("martolod");
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
		List<TestDatabeanWithManagedIndexByBKey> entryKeys = new LinkedList<>();
		for(TestDatabean databean : databeans){
			entryKeys.add(new TestDatabeanWithManagedIndexByBKey(databean.getB()));
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
				new TestDatabeanWithManagedIndexByCKey("lala"), null);
		Assert.assertEquals(databeans.size(), 0);
		databeans = node.byC.lookupMulti(
				new TestDatabeanWithManagedIndexByCKey("ed"), null);
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
		List<TestDatabeanWithManagedIndexByCKey> keys = DrListTool.create(
				new TestDatabeanWithManagedIndexByCKey("ed"),
				new TestDatabeanWithManagedIndexByCKey("or"));
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
		TestDatabeanWithManagedIndexByB previous = null;
		int count = 0;
		for(TestDatabeanWithManagedIndexByB indexEntry : node.byB.scan(null, null)){
			if(previous != null){
				Assert.assertTrue(indexEntry.getKey().compareTo(previous.getKey()) >= 0);
			}
			previous = indexEntry;
			count++;
		}
		Assert.assertEquals(testDatabeans.size(), count);
	}

	private void testScanMultiIndex(TestDatabeanWithIndexNode node){
		TestDatabeanWithManagedIndexByC previous = null;
		int count = 0;
		for(TestDatabeanWithManagedIndexByC indexEntry : node.byC.scan(null, null)){
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
				Assert.assertTrue(databean.getB().compareTo(previous.getB()) >= 0);
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
				Assert.assertTrue(databean.getC().compareTo(previous.getC()) >= 0);
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
		TestDatabeanWithManagedIndexByBKey previous = null;
		int count = 0;
		for(TestDatabeanWithManagedIndexByBKey key : router.testDatabeanWithManagedIndex.byB.scanKeys(null, null)){
			if(previous != null){
				Assert.assertTrue(key.getB().compareTo(previous.getB()) > 0);
			}
			previous = key;
			count++;
		}
		Assert.assertEquals(testDatabeans.size(), count);
	}

}

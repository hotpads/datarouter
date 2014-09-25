package com.hotpads.datarouter.node.type.index;

import java.util.LinkedList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.hotpads.datarouter.node.type.index.databean.TestDatabeanWithManagedIndex;
import com.hotpads.datarouter.node.type.index.databean.TestDatabeanWithManagedIndexByB;
import com.hotpads.datarouter.node.type.index.databean.TestDatabeanWithManagedIndexByBKey;
import com.hotpads.datarouter.node.type.index.databean.TestDatabeanWithManagedIndexByCKey;
import com.hotpads.datarouter.node.type.index.databean.TestDatabeanWithManagedIndexKey;
import com.hotpads.datarouter.node.type.index.databean.TestDatabeanWithManagedIndexNode;
import com.hotpads.datarouter.storage.key.KeyTool;
import com.hotpads.datarouter.test.client.BasicClientTestRouter;
import com.hotpads.util.core.ListTool;

public class JdbcManagedIndexIntegrationTests{
	
	private static TestDatabeanWithManagedIndexNode node;

	@BeforeClass
	public static void setUp(){
		Injector injector = Guice.createInjector();
		node = injector.getInstance(BasicClientTestRouter.class).getTestDatabeanWithManagedIndex();
		
		Assert.assertNotNull(node);
		
		LinkedList<TestDatabeanWithManagedIndex> databeans = ListTool.createLinkedList(
				new TestDatabeanWithManagedIndex("un", "alarc'h", "un"),
				new TestDatabeanWithManagedIndex("alarc'h", "tra", "mor"),
				new TestDatabeanWithManagedIndex("war", "lein", "tour"),
				new TestDatabeanWithManagedIndex("moal", "kastell", "Arvor"),
				new TestDatabeanWithManagedIndex("Neventi vad", "d'ar Vreton", "ed"),
				new TestDatabeanWithManagedIndex("Ha malloz-ru", "d'ar C'hallaou", "ed"),
				new TestDatabeanWithManagedIndex("Erru eul lestr", "e pleg ar m", "or"),
				new TestDatabeanWithManagedIndex("He weliou gwenn", "gant han dig", "or"));
		node.mainNode.putMulti(databeans, null);
	}
	
	@AfterClass
	public static void tearDown(){
		node.mainNode.deleteAll(null);
	}
	
	@Test
	public void testLookupUnique(){
		TestDatabeanWithManagedIndex d = node.byB.lookupUnique(new TestDatabeanWithManagedIndexByBKey("martolod"), null);
		Assert.assertNull(d);
		d = node.byB.lookupUnique(new TestDatabeanWithManagedIndexByBKey("tra"), null);
		Assert.assertEquals(d.getA(), "alarc'h");
		Assert.assertEquals(d.getB(), "tra");
		Assert.assertEquals(d.getC(), "mor");
	}
	
	@Test
	public void testLookupMultiUnique(){
		LinkedList<TestDatabeanWithManagedIndexByBKey> keys = ListTool.createLinkedList(
				new TestDatabeanWithManagedIndexByBKey("martolod"),
				new TestDatabeanWithManagedIndexByBKey("kastell"),
				new TestDatabeanWithManagedIndexByBKey("lein"));
		List<TestDatabeanWithManagedIndex> databeans = node.byB.lookupMultiUnique(keys, null);
		Assert.assertEquals(2, databeans.size());
		for(TestDatabeanWithManagedIndex d : databeans){
			Assert.assertTrue(d.getA().equals("moal") || d.getA().equals("war"));
			if(d.getA().equals("moal")){
				Assert.assertEquals(d.getB(), "kastell");
				Assert.assertEquals(d.getC(), "Arvor");
			}
			if(d.getA().equals("war")){
				Assert.assertEquals(d.getB(), "lein");
				Assert.assertEquals(d.getC(), "tour");
			}
		}
	}
	
	@Test
	public void testLookupIndex(){
		TestDatabeanWithManagedIndexByB entry = node.byB.lookupUniqueIndex(new TestDatabeanWithManagedIndexByBKey("martolod"), null);
		Assert.assertNull(entry);
		entry = node.byB.lookupUniqueIndex(new TestDatabeanWithManagedIndexByBKey("tra"), null);
		Assert.assertNotNull(entry);
		Assert.assertEquals(entry.getA(), "alarc'h");
		Assert.assertEquals(entry.getB(), "tra");
	}
	
	@Test
	public void testLookupMultiIndex(){
		LinkedList<TestDatabeanWithManagedIndexByBKey> keys = ListTool.createLinkedList(
				new TestDatabeanWithManagedIndexByBKey("martolod"),
				new TestDatabeanWithManagedIndexByBKey("kastell"),
				new TestDatabeanWithManagedIndexByBKey("lein"));
		List<TestDatabeanWithManagedIndexByB> entries = node.byB.lookupMultiUniqueIndex(keys, null);
		Assert.assertEquals(2, entries.size());
		for(TestDatabeanWithManagedIndexByB entry : entries){
			Assert.assertTrue(entry.getA().equals("moal") || entry.getA().equals("war"));
			if(entry.getA().equals("moal")){
				Assert.assertEquals(entry.getB(), "kastell");
			}
			if(entry.getA().equals("war")){
				Assert.assertEquals(entry.getB(), "lein");
			}
		}
	}
	
	@Test
	public void testDeleteUnique(){
		TestDatabeanWithManagedIndex databean = new TestDatabeanWithManagedIndex("tri", "martolod", "yaouank");
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
		List<TestDatabeanWithManagedIndex> databeans = ListTool.createLinkedList(
				new TestDatabeanWithManagedIndex("tri", "martolod", "yaouank"),
				new TestDatabeanWithManagedIndex("i vonet", "da", "veajiñ"));
		List<TestDatabeanWithManagedIndexKey> keys = KeyTool.getKeys(databeans);
		List<TestDatabeanWithManagedIndexByBKey> entryKeys = new LinkedList<>();
		for(TestDatabeanWithManagedIndex databean : databeans){
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
		List<TestDatabeanWithManagedIndex> databeans = node.byC.lookupMulti(
				new TestDatabeanWithManagedIndexByCKey("lala"), true, null);
		Assert.assertEquals(databeans.size(), 0);
		databeans = node.byC.lookupMulti(
				new TestDatabeanWithManagedIndexByCKey("ed"), true, null);
		Assert.assertEquals(databeans.size(), 2);
		List<TestDatabeanWithManagedIndex> expected = ListTool.create(
				new TestDatabeanWithManagedIndex("Neventi vad", "d'ar Vreton", "ed"),
				new TestDatabeanWithManagedIndex("Ha malloz-ru", "d'ar C'hallaou", "ed"));
		for(TestDatabeanWithManagedIndex d : databeans){
			Assert.assertTrue(expected.remove(d));
		}
	}
	
	@Test
	public void testLookupMultiMulti(){
		List<TestDatabeanWithManagedIndexByCKey> keys = ListTool.create(
				new TestDatabeanWithManagedIndexByCKey("ed"),
				new TestDatabeanWithManagedIndexByCKey("or"));
		List<TestDatabeanWithManagedIndex> databeans = node.byC.lookupMultiMulti(keys, true, null);
		Assert.assertEquals(databeans.size(), 4);
		List<TestDatabeanWithManagedIndex> expected = ListTool.create(
				new TestDatabeanWithManagedIndex("Neventi vad", "d'ar Vreton", "ed"),
				new TestDatabeanWithManagedIndex("Ha malloz-ru", "d'ar C'hallaou", "ed"),
				new TestDatabeanWithManagedIndex("Erru eul lestr", "e pleg ar m", "or"),
				new TestDatabeanWithManagedIndex("He weliou gwenn", "gant han dig", "or"));
		for(TestDatabeanWithManagedIndex d : databeans){
			Assert.assertTrue(expected.remove(d));
		}
	}
	
	@Test
	public void testEquals(){
		TestDatabeanWithManagedIndex d1 = new TestDatabeanWithManagedIndex("tri", "martolod", "yaouank");
		TestDatabeanWithManagedIndex d2 = new TestDatabeanWithManagedIndex("lalala", "lalala", "la");
		TestDatabeanWithManagedIndex d3 = new TestDatabeanWithManagedIndex("tri", "martolod", "yaouank");
		
		Assert.assertEquals(d1, d3);
		Assert.assertNotSame(d2, d3);
		Assert.assertNotSame(d2, d1);
	}
	
}

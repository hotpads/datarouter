package com.hotpads.datarouter.storage.lazy;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.Injector;
import com.hotpads.datarouter.routing.DatarouterContext;
import com.hotpads.datarouter.storage.key.KeyTool;
import com.hotpads.datarouter.test.DatarouterTestInjectorProvider;
import com.hotpads.datarouter.test.TestDatabean;
import com.hotpads.datarouter.test.TestDatabeanKey;
import com.hotpads.datarouter.test.TestIndexedDatabeanFielder.TestDatabeanByBLookup;
import com.hotpads.datarouter.test.TestIndexedDatabeanFielder.TestDatabeanByCLookup;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;

public class LazyReaderIntegrationTests{

	private static DatarouterContext datarouterContext;
	private static LazyTestRouter router;
	private static Map<TestDatabeanKey, TestDatabean> keyToDatabean;
	private static List<TestDatabean> databeans;

	@BeforeClass
	public static void setUp(){
		Injector injector = new DatarouterTestInjectorProvider().get();
		datarouterContext = injector.getInstance(DatarouterContext.class);
		router = injector.getInstance(LazyTestRouter.class);
		
		databeans = ListTool.create(
				new TestDatabean("foo", "bar", "baz"),
				new TestDatabean("baz", "bar", "foo"),
				new TestDatabean("aaa", "bbb", "ccc"));
		router.testDatabean.putMulti(databeans, null);
		
		Collections.sort(databeans);
		keyToDatabean = KeyTool.getByKey(databeans);
	}
	
	@AfterClass
	public static void afterClass(){
		router.testDatabean.deleteMulti(keyToDatabean.keySet(), null);
		datarouterContext.shutdown();
	}
	
	@Test
	public void testGet(){
		TestDatabeanKey key = CollectionTool.getFirst(keyToDatabean.keySet());
		TestDatabean fromBlockingNode = router.testDatabean.get(key, null);
		TestDatabean fromLazyNode = router.lazyTestDatabean.get(key, null).get();
		Assert.assertEquals(fromBlockingNode, fromLazyNode);
		Assert.assertEquals(fromBlockingNode, CollectionTool.getFirst(keyToDatabean.values()));
	}
	
	@Test
	public void testGetMulti(){
		List<TestDatabean> fromBlockingNode = router.testDatabean.getMulti(keyToDatabean.keySet(), null);
		List<TestDatabean> fromLazyNode = router.lazyTestDatabean.getMulti(keyToDatabean.keySet(), null).get();
		Assert.assertEquals(fromBlockingNode, fromLazyNode);
		Assert.assertEquals(fromBlockingNode, databeans);
	}
	
	@Test
	public void testCount(){
		TestDatabeanByBLookup lookup = new TestDatabeanByBLookup("bar");
		Long fromBlockingNode = router.testDatabean.count(lookup, null);
		Long fromLazyNode = router.lazyTestDatabean.count(lookup, null).get();
		Assert.assertEquals(fromBlockingNode, fromLazyNode);
		Assert.assertEquals((Long)2L, fromBlockingNode);
	}
	
	@Test
	public void testLookupUnique(){
		TestDatabeanByCLookup lookup = new TestDatabeanByCLookup("baz");
		TestDatabean fromBlockingNode = router.testDatabean.lookupUnique(lookup, null);
		TestDatabean fromLazyNode = router.lazyTestDatabean.lookupUnique(lookup, null).get();
		Assert.assertEquals(fromBlockingNode, fromLazyNode);
		Assert.assertEquals(new TestDatabean("foo", "bar", "baz"), fromBlockingNode);
	}
	
	@Test
	public void testLookupMultiUnique(){
		List<TestDatabeanByCLookup> lookups = ListTool.create(
				new TestDatabeanByCLookup("baz"),
				new TestDatabeanByCLookup("foo"));
		List<TestDatabean> expectedDatabeans = ListTool.create(
				new TestDatabean("foo", "bar", "baz"),
				new TestDatabean("baz", "bar", "foo"));
		Collections.sort(expectedDatabeans);
		List<TestDatabean> fromBlockingNode = router.testDatabean.lookupMultiUnique(lookups, null);
		List<TestDatabean> fromLazyNode = router.lazyTestDatabean.lookupMultiUnique(lookups, null).get();
		Assert.assertEquals(fromBlockingNode, fromLazyNode);
		Assert.assertEquals(expectedDatabeans, fromBlockingNode);
	}
	
	@Test
	public void testLookup(){
		TestDatabeanByCLookup lookup = new TestDatabeanByCLookup("baz");
		List<TestDatabean> fromBlockingNode = router.testDatabean.lookup(lookup, false, null);
		List<TestDatabean> fromLazyNode = router.lazyTestDatabean.lookup(lookup, false, null).get();
		Assert.assertEquals(fromBlockingNode, fromLazyNode);
		Assert.assertEquals(Collections.singletonList(new TestDatabean("foo", "bar", "baz")), fromBlockingNode);
	}
	
	@Test
	public void testLookupMulti(){
		List<TestDatabeanByCLookup> lookups = ListTool.create(
				new TestDatabeanByCLookup("baz"),
				new TestDatabeanByCLookup("foo"));
		List<TestDatabean> expectedDatabeans = ListTool.create(
				new TestDatabean("foo", "bar", "baz"),
				new TestDatabean("baz", "bar", "foo"));
		Collections.sort(expectedDatabeans);
		List<TestDatabean> fromBlockingNode = router.testDatabean.lookup(lookups, null);
		List<TestDatabean> fromLazyNode = router.lazyTestDatabean.lookupMulti(lookups, null).get();
		Assert.assertEquals(fromBlockingNode, fromLazyNode);
		Assert.assertEquals(expectedDatabeans, fromBlockingNode);
	}
	
	@Test
	public void testGetFirstKey(){
		TestDatabeanKey fromBlockingNode = router.testDatabean.getFirstKey(null);
		TestDatabeanKey fromLazyNode = router.lazyTestDatabean.getFirstKey(null).get();
		Assert.assertEquals(fromBlockingNode, fromLazyNode);
		Assert.assertEquals(CollectionTool.getFirst(keyToDatabean.keySet()), fromBlockingNode);
	}
	
	@Test
	public void testGetFirst(){
		TestDatabean fromBlockingNode = router.testDatabean.getFirst(null);
		TestDatabean fromLazyNode = router.lazyTestDatabean.getFirst(null).get();
		Assert.assertEquals(fromBlockingNode, fromLazyNode);
		Assert.assertEquals(CollectionTool.getFirst(databeans), fromBlockingNode);
	}
	
	@Test
	public void testGetWithPrefix(){
		List<TestDatabean> expectedDatabeans = Collections.singletonList(new TestDatabean("baz", "bar", "foo"));
		TestDatabeanKey key = new TestDatabeanKey("ba");
		List<TestDatabean> fromBlockingNode = router.testDatabean.getWithPrefix(key, true, null);
		List<TestDatabean> fromLazyNode = router.testDatabean.getWithPrefix(key, true, null);
		Assert.assertEquals(fromBlockingNode, fromLazyNode);
		Assert.assertEquals(expectedDatabeans, fromBlockingNode);
	}
	
	@Test
	public void testGetWithPrefixes(){
		List<TestDatabean> expectedDatabeans = ListTool.create(
				new TestDatabean("baz", "bar", "foo"),
				new TestDatabean("foo", "bar", "baz"));
		List<TestDatabeanKey> keys = ListTool.create(
				new TestDatabeanKey("ba"),
				new TestDatabeanKey("f"));
		List<TestDatabean> fromBlockingNode = router.testDatabean.getWithPrefixes(keys, true, null);
		List<TestDatabean> fromLazyNode = router.testDatabean.getWithPrefixes(keys, true, null);
		Assert.assertEquals(fromBlockingNode, fromLazyNode);
		Assert.assertEquals(expectedDatabeans, fromBlockingNode);
	}
}

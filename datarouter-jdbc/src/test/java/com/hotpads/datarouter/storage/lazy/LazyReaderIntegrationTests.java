package com.hotpads.datarouter.storage.lazy;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
import com.hotpads.datarouter.test.TestIndexedDatabeanFielder.TestDatabeanByCLookup;
import com.hotpads.datarouter.util.core.DrCollectionTool;

@Guice(moduleFactory=TestDatarouterJdbcModuleFactory.class)
public class LazyReaderIntegrationTests{

	private static final String TEST_NAME = LazyReaderIntegrationTests.class.getSimpleName();
	private static final String
			foo = TEST_NAME + "foo",
			bar = TEST_NAME + "bar",
			baz = TEST_NAME + "baz",
			aaa = TEST_NAME + "aaa",
			bbb = TEST_NAME + "bbb",
			ccc = TEST_NAME + "ccc";

	@Inject
	private Datarouter datarouter;
	@Inject
	private LazyTestRouter router;

	private Map<TestDatabeanKey, TestDatabean> keyToDatabean;
	private List<TestDatabean> databeans;

	@BeforeClass
	public void setUp(){
		databeans = Arrays.asList(
				new TestDatabean(aaa, bbb, ccc),
				new TestDatabean(baz, bar, foo),
				new TestDatabean(foo, bar, baz));
		router.testDatabean.putMulti(databeans, null);
		keyToDatabean = DatabeanTool.getByKey(databeans);
	}

	@AfterClass
	public void afterClass(){
		router.testDatabean.deleteMulti(keyToDatabean.keySet(), null);
		datarouter.shutdown();
	}

	@Test
	public void testGet(){
		TestDatabeanKey key = DrCollectionTool.getFirst(keyToDatabean.keySet());
		TestDatabean fromBlockingNode = router.testDatabean.get(key, null);
		TestDatabean fromLazyNode = router.lazyTestDatabean.get(key, null).get();
		Assert.assertEquals(fromBlockingNode, fromLazyNode);
		Assert.assertEquals(fromBlockingNode, DrCollectionTool.getFirst(keyToDatabean.values()));
	}

	@Test
	public void testGetMulti(){
		List<TestDatabean> fromBlockingNode = router.testDatabean.getMulti(keyToDatabean.keySet(), null);
		List<TestDatabean> fromLazyNode = router.lazyTestDatabean.getMulti(keyToDatabean.keySet(), null).get();
		Assert.assertEquals(fromBlockingNode, fromLazyNode);
		Assert.assertEquals(fromBlockingNode, databeans);
	}

	@Test
	public void testLookupUnique(){
		TestDatabeanByCLookup lookup = new TestDatabeanByCLookup(baz);
		TestDatabean fromBlockingNode = router.testDatabean.lookupUnique(lookup, null);
		TestDatabean fromLazyNode = router.lazyTestDatabean.lookupUnique(lookup, null).get();
		Assert.assertEquals(fromBlockingNode, fromLazyNode);
		Assert.assertEquals(databeans.get(2), fromBlockingNode);
	}

	@Test
	public void testLookupMultiUnique(){
		List<TestDatabeanByCLookup> lookups = Arrays.asList(
				new TestDatabeanByCLookup(baz),
				new TestDatabeanByCLookup(foo));
		List<TestDatabean> expectedDatabeans = Arrays.asList(
				databeans.get(1),
				databeans.get(2));
		List<TestDatabean> fromBlockingNode = router.testDatabean.lookupMultiUnique(lookups, null);
		List<TestDatabean> fromLazyNode = router.lazyTestDatabean.lookupMultiUnique(lookups, null).get();
		Collections.sort(fromBlockingNode);
		Collections.sort(fromLazyNode);
		Assert.assertEquals(fromBlockingNode, fromLazyNode);
		Assert.assertEquals(expectedDatabeans, fromBlockingNode);
	}

	@Test
	public void testLookup(){
		TestDatabeanByCLookup lookup = new TestDatabeanByCLookup(baz);
		List<TestDatabean> fromBlockingNode = router.testDatabean.lookup(lookup, false, null);
		List<TestDatabean> fromLazyNode = router.lazyTestDatabean.lookup(lookup, false, null).get();
		Assert.assertEquals(fromBlockingNode, fromLazyNode);
		Assert.assertEquals(Collections.singletonList(databeans.get(2)), fromBlockingNode);
	}

	@Test
	public void testLookupMulti(){
		List<TestDatabeanByCLookup> lookups = Arrays.asList(
				new TestDatabeanByCLookup(baz),
				new TestDatabeanByCLookup(foo));
		List<TestDatabean> expectedDatabeans = Arrays.asList(
				databeans.get(1),
				databeans.get(2));
		List<TestDatabean> fromBlockingNode = router.testDatabean.lookupMulti(lookups, null);
		List<TestDatabean> fromLazyNode = router.lazyTestDatabean.lookupMulti(lookups, null).get();
		Collections.sort(fromBlockingNode);
		Collections.sort(fromLazyNode);
		Assert.assertEquals(fromBlockingNode, fromLazyNode);
		Assert.assertEquals(expectedDatabeans, fromBlockingNode);
	}

	@Test
	@SuppressWarnings("deprecation")
	public void testGetWithPrefix(){
		List<TestDatabean> expectedDatabeans = Collections.singletonList(databeans.get(1));
		TestDatabeanKey key = new TestDatabeanKey(TEST_NAME + "ba");
		List<TestDatabean> fromBlockingNode = router.testDatabean.getWithPrefix(key, true, null);
		List<TestDatabean> fromLazyNode = router.lazyTestDatabean.getWithPrefix(key, true, null).get();
		Assert.assertEquals(fromBlockingNode, fromLazyNode);
		Assert.assertEquals(expectedDatabeans, fromBlockingNode);
	}

	@Test
	@SuppressWarnings("deprecation")
	public void testGetWithPrefixes(){
		List<TestDatabean> expectedDatabeans = Arrays.asList(
				databeans.get(1),
				databeans.get(2));
		List<TestDatabeanKey> keys = Arrays.asList(
				new TestDatabeanKey(TEST_NAME + "f"),
				new TestDatabeanKey(TEST_NAME + "ba"));
		List<TestDatabean> fromBlockingNode = router.testDatabean.getWithPrefixes(keys, true, null);
		List<TestDatabean> fromLazyNode = router.lazyTestDatabean.getWithPrefixes(keys, true, null).get();
		Assert.assertEquals(fromBlockingNode, fromLazyNode);
		Assert.assertEquals(expectedDatabeans, fromBlockingNode);
	}
}

package com.hotpads.datarouter.client.imp.jdbc.test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import com.hotpads.datarouter.client.imp.jdbc.TestDatarouterJdbcModuleFactory;
import com.hotpads.datarouter.config.Config;
import com.hotpads.datarouter.test.DrTestConstants;
import com.hotpads.datarouter.test.node.basic.sorted.BaseIndexedNodeIntegrationTests;
import com.hotpads.datarouter.test.node.basic.sorted.SortedBean;
import com.hotpads.datarouter.test.node.basic.sorted.SortedBeanKey;
import com.hotpads.datarouter.util.core.DrIterableTool;
import com.hotpads.util.core.collections.Range;

@Guice(moduleFactory = TestDatarouterJdbcModuleFactory.class)
public class JdbcIndexedSortedNodeIntegrationTests extends BaseIndexedNodeIntegrationTests{

	@BeforeClass
	public void beforeClass(){
		setup(DrTestConstants.CLIENT_drTestJdbc0, true, false);
	}

	@AfterClass
	public void afterClass(){
		testIndexedDelete();
		datarouter.shutdown();
	}

	@Test
	public void testScanMulti(){
		SortedBeanKey start = new SortedBeanKey("aardvark", "aardvark", 1, "alpaca");
		SortedBeanKey end = new SortedBeanKey("aardvark", "aardvark", 1, "alpaca");
		Range<SortedBeanKey> range = new Range<>(start, true, end, true);
		SortedBeanKey start2 = new SortedBeanKey("aardvark", "aardvark", 6, "albatross");
		SortedBeanKey end2 = null;
		Range<SortedBeanKey> range2 = new Range<>(start2, true, end2, false);
		List<Range<SortedBeanKey>> ranges = Arrays.asList(range, range2);
		List<SortedBean> fromScanMulti = DrIterableTool.asList(router.indexedSortedBean().scanMulti(ranges,
				new Config().setIterateBatchSize(7)));
		System.out.println("===================================================");
		System.out.println(fromScanMulti);
		System.out.println("===================================================");
		List<SortedBean> fromScans = ranges.stream().flatMap(r -> router.indexedSortedBean().stream(r, null)).collect(
				Collectors.toList());
		Assert.assertEquals(fromScans, fromScanMulti);
	}

}

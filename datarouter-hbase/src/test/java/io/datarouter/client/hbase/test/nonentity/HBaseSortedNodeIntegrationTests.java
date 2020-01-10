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
package io.datarouter.client.hbase.test.nonentity;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import io.datarouter.client.hbase.config.DatarouterHBaseTestNgModuleFactory;
import io.datarouter.client.hbase.test.DatarouterHBaseTestClientIds;
import io.datarouter.storage.test.node.basic.sorted.BaseSortedNodeIntegrationTests;
import io.datarouter.storage.test.node.basic.sorted.SortedBean;
import io.datarouter.storage.test.node.basic.sorted.SortedBeanKey;

@Guice(moduleFactory = DatarouterHBaseTestNgModuleFactory.class)
public class HBaseSortedNodeIntegrationTests extends BaseSortedNodeIntegrationTests{

	@BeforeClass
	public void beforeClass(){
		setup(DatarouterHBaseTestClientIds.HBASE, false);
	}

	@AfterClass
	public void afterClass(){
		postTestTests();
		datarouter.shutdown();
	}

	@Test
	public void testEmptyTrailingStringInKey(){
		String firstField = "testEmptyTrailingStringInKey";
		String trailingString = "";
		SortedBeanKey pk = new SortedBeanKey(firstField, "bar", 3, trailingString);
		SortedBean input = new SortedBean(pk, "f1", 2L, "f3", 4D);
		dao.put(input);
		SortedBean getOutput = dao.get(pk);
		Assert.assertEquals(getOutput, input);
		dao.delete(pk);
	}

}

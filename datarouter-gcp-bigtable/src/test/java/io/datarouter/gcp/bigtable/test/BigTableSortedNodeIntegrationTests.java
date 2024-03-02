/*
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
package io.datarouter.gcp.bigtable.test;

import java.util.Optional;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import io.datarouter.gcp.bigtable.config.DatarouterBigTableTestNgModuleFactory;
import io.datarouter.storage.test.node.basic.sorted.BaseSortedNodeIntegrationTests;
import io.datarouter.storage.test.node.basic.sorted.SortedBean;
import io.datarouter.storage.test.node.basic.sorted.SortedBeanKey;

@Guice(moduleFactory = DatarouterBigTableTestNgModuleFactory.class)
public class BigTableSortedNodeIntegrationTests extends BaseSortedNodeIntegrationTests{

	@BeforeClass
	public void beforeClass(){
		setup(DatarouterBigTableTestClientIds.BIG_TABLE, Optional.of("SortedBeanBigtableNative"));
	}

	@Override
	@AfterClass
	public void afterClass(){
		postTestTests();
		datarouter.shutdown();
	}

	@Test
	public void testNonEndZero(){
		String firstField = "testspace";
		byte[] bytes = firstField.getBytes();
		byte[] endBytes = new byte[bytes.length + 1];
		for(int i = 0; i < bytes.length - 1; i++){
			endBytes[i] = bytes[i];
		}
		endBytes[bytes.length - 1] = 0;
		endBytes[bytes.length] = bytes[bytes.length - 1];
		String endField = new String(endBytes);
		SortedBeanKey pk = new SortedBeanKey(firstField, "bar", 3, endField);
		SortedBean input = new SortedBean(pk, "f1", 2L, "f3", 4D);
		dao.put(input);
		SortedBean getOutput = dao.get(pk);
		Assert.assertEquals(getOutput, input);
		dao.delete(pk);
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

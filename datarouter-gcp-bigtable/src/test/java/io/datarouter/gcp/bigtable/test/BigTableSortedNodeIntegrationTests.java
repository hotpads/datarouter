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

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import io.datarouter.gcp.bigtable.config.DatarouterBigtableTestNgModuleFactory;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.combo.SortedMapStorage;
import io.datarouter.storage.test.node.basic.sorted.BaseSortedNodeWriterIntegrationTests;
import io.datarouter.storage.test.node.basic.sorted.SortedBean;
import io.datarouter.storage.test.node.basic.sorted.SortedBean.SortedBeanFielder;
import io.datarouter.storage.test.node.basic.sorted.SortedBeanEntityKey;
import io.datarouter.storage.test.node.basic.sorted.SortedBeanKey;
import jakarta.inject.Inject;

@Guice(moduleFactory = DatarouterBigtableTestNgModuleFactory.class)
public class BigtableSortedNodeIntegrationTests extends BaseSortedNodeWriterIntegrationTests{

	@Inject
	public BigtableSortedNodeIntegrationTests(NodeFactory nodeFactory){
		super(makeNode(nodeFactory));
		resetTable();
	}

	private static SortedMapStorage<SortedBeanKey,SortedBean> makeNode(NodeFactory nodeFactory){
		return nodeFactory.create(
				DatarouterBigtableTestClientIds.BIG_TABLE,
				SortedBeanEntityKey::new,
				SortedBean::new,
				SortedBeanFielder::new)
				.withTableName("SortedBeanBigtableNative")
				.buildAndRegister();
	}

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
		node.put(input);
		SortedBean getOutput = readerNode.get(pk);
		Assert.assertEquals(getOutput, input);
		node.delete(pk);
	}

	@Test
	public void testEmptyTrailingStringInKey(){
		String firstField = "testEmptyTrailingStringInKey";
		String trailingString = "";
		SortedBeanKey pk = new SortedBeanKey(firstField, "bar", 3, trailingString);
		SortedBean input = new SortedBean(pk, "f1", 2L, "f3", 4D);
		node.put(input);
		SortedBean getOutput = readerNode.get(pk);
		Assert.assertEquals(getOutput, input);
		node.delete(pk);
	}

}

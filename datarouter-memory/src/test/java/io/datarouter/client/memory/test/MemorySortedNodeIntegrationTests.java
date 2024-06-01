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
package io.datarouter.client.memory.test;

import org.testng.annotations.AfterClass;
import org.testng.annotations.Guice;

import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.combo.SortedMapStorage;
import io.datarouter.storage.test.node.basic.sorted.BaseSortedNodeWriterIntegrationTests;
import io.datarouter.storage.test.node.basic.sorted.SortedBean;
import io.datarouter.storage.test.node.basic.sorted.SortedBean.SortedBeanFielder;
import io.datarouter.storage.test.node.basic.sorted.SortedBeanEntityKey;
import io.datarouter.storage.test.node.basic.sorted.SortedBeanKey;
import jakarta.inject.Inject;

@Guice(moduleFactory = DatarouterMemoryTestNgModuleFactory.class)
public class MemorySortedNodeIntegrationTests extends BaseSortedNodeWriterIntegrationTests{

	@Inject
	public MemorySortedNodeIntegrationTests(NodeFactory nodeFactory){
		super(makeNode(nodeFactory));
		resetTable();
	}

	private static SortedMapStorage<SortedBeanKey,SortedBean> makeNode(NodeFactory nodeFactory){
		return nodeFactory.create(
				DatarouterMemoryTestClientIds.MEMORY,
				SortedBeanEntityKey::new,
				SortedBean::new,
				SortedBeanFielder::new)
				.buildAndRegister();
	}

	@AfterClass
	public void afterClass(){
		postTestTests();
		datarouter.shutdown();
	}

}

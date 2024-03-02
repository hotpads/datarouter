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
package io.datarouter.client.mysql.test.test;

import org.testng.Assert;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import io.datarouter.client.mysql.DatarouterMysqlTestNgModuleFactory;
import io.datarouter.client.mysql.test.DatarouterMysqlTestClientids;
import io.datarouter.storage.Datarouter;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.type.physical.PhysicalNode;
import io.datarouter.storage.test.node.basic.map.databean.MapStorageBean;
import io.datarouter.storage.test.node.basic.map.databean.MapStorageBean.MapStorageBeanFielder;
import io.datarouter.storage.test.node.basic.map.databean.MapStorageBeanEntityKey;
import io.datarouter.storage.test.node.basic.map.databean.MapStorageBeanKey;
import jakarta.inject.Inject;

@Guice(moduleFactory = DatarouterMysqlTestNgModuleFactory.class)
public class PhysicalNodeNameTests{

	@SuppressWarnings("unused") // for tests
	@Inject
	private Datarouter datarouter;
	@Inject
	private NodeFactory nodeFactory;

	/**
	 * Datarouter's nodeName wasn't orignally meant to be persisted.
	 * But now many things use it for building links or even persist it in a database.
	 * This test is meant to catch a change in the nodeName logic for PhysicalNode.
	 * Other node types like virtual nodes have more complicated name-building logic, but they're not covered here.
	 * If an intentional change, the test can be updated, but a lot of other things probably need to be updated as well.
	 */
	@Test
	public void testPhysicalNodeName(){
		PhysicalNode<MapStorageBeanKey,MapStorageBean,MapStorageBeanFielder> node = nodeFactory.create(
				DatarouterMysqlTestClientids.MYSQL,
				MapStorageBeanEntityKey::new,
				MapStorageBean::new,
				MapStorageBeanFielder::new)
				.buildAndRegister();
		Assert.assertEquals(node.getName(), "drTestMysql0.MapStorageBean");
	}

}

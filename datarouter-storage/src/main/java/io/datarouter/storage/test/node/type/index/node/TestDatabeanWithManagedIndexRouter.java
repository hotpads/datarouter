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
package io.datarouter.storage.test.node.type.index.node;

import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.setting.DatarouterSettings;
import io.datarouter.storage.node.factory.IndexingNodeFactory;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.test.TestDatarouterProperties;
import io.datarouter.storage.test.node.type.index.databean.TestDatabeanWithManagedIndexByBar;
import io.datarouter.storage.test.node.type.index.databean.TestDatabeanWithManagedIndexByBar.TestDatabeanWithManagedIndexByBFielder;

public class TestDatabeanWithManagedIndexRouter extends TestDatabeanWithIndexRouter{

	public TestDatabeanWithManagedIndexRouter(Datarouter datarouter, TestDatarouterProperties datarouterProperties,
			NodeFactory nodeFactory, DatarouterSettings datarouterSettings, ClientId clientId){
		super(datarouter, datarouterProperties, "managedIndexTest", nodeFactory, datarouterSettings, clientId,
				"TestDatabeanWithManagedIndex");

		byB = backingMapNode.registerManaged(IndexingNodeFactory.newManagedUnique(backingMapNode,
				TestDatabeanWithManagedIndexByBFielder.class, TestDatabeanWithManagedIndexByBar.class, false));
	}

}
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
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.combo.IndexedMapStorage;
import io.datarouter.storage.node.op.raw.MapStorage;
import io.datarouter.storage.node.type.index.UniqueIndexNode;
import io.datarouter.storage.router.BaseRouter;
import io.datarouter.storage.router.TestRouter;
import io.datarouter.storage.test.TestDatabean;
import io.datarouter.storage.test.TestDatabeanFielder;
import io.datarouter.storage.test.TestDatabeanKey;
import io.datarouter.storage.test.node.type.index.databean.TestDatabeanWithManagedIndexByBar;
import io.datarouter.storage.test.node.type.index.databean.TestDatabeanWithManagedIndexByBarKey;

public abstract class DatarouterTestDatabeanWithIndexRouter extends BaseRouter implements TestRouter{

	protected IndexedMapStorage<TestDatabeanKey,TestDatabean> backingMapNode;

	public MapStorage<TestDatabeanKey,TestDatabean> mainNode;

	public UniqueIndexNode<TestDatabeanKey,TestDatabean,TestDatabeanWithManagedIndexByBarKey,
			TestDatabeanWithManagedIndexByBar> byB;

	public DatarouterTestDatabeanWithIndexRouter(Datarouter datarouter, NodeFactory nodeFactory, ClientId clientId,
			String tableName){
		super(datarouter);
		mainNode = backingMapNode = nodeFactory.create(clientId, TestDatabean::new, TestDatabeanFielder::new)
				.withTableName(tableName)
				.buildAndRegister();
	}

}

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
package io.datarouter.storage.test.node.type.index.router;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.setting.DatarouterSettings;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.test.TestDatarouterProperties;
import io.datarouter.storage.test.node.type.index.node.TestDatabeanWithManagedIndexRouter;
import io.datarouter.storage.test.node.type.index.node.TestDatabeanWithTxnManagedIndexRouter;

@Singleton
public class ManagedIndexTestRoutersFactory{

	@Inject
	private TestDatarouterProperties datarouterProperties;
	@Inject
	private Datarouter datarouter;
	@Inject
	private NodeFactory nodeFactory;
	@Inject
	private DatarouterSettings datarouterSettings;

	public class ManagedIndexTestRouters{

		public final TestDatabeanWithManagedIndexRouter testDatabeanWithManagedIndex;
		public final TestDatabeanWithTxnManagedIndexRouter testDatabeanWithTxnManagedIndex;

		public ManagedIndexTestRouters(ClientId clientId){
			testDatabeanWithManagedIndex = new TestDatabeanWithManagedIndexRouter(datarouter, datarouterProperties,
					nodeFactory, datarouterSettings, clientId);
			testDatabeanWithTxnManagedIndex = new TestDatabeanWithTxnManagedIndexRouter(datarouter,
					datarouterProperties, nodeFactory, datarouterSettings, clientId);
		}

	}

}

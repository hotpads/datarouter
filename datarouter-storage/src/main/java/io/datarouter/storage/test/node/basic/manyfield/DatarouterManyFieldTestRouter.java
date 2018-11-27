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
package io.datarouter.storage.test.node.basic.manyfield;

import java.util.Random;
import java.util.function.Supplier;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.config.setting.DatarouterSettings;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.raw.MapStorage.MapStorageNode;
import io.datarouter.storage.router.BaseRouter;
import io.datarouter.storage.router.TestRouter;
import io.datarouter.storage.test.TestDatarouterProperties;
import io.datarouter.storage.test.node.basic.manyfield.ManyFieldBean.ManyFieldTypeBeanFielder;

@Singleton
public class DatarouterManyFieldTestRouter extends BaseRouter implements TestRouter{

	private final MapStorageNode<ManyFieldBeanKey,ManyFieldBean,ManyFieldTypeBeanFielder> manyFieldTypeBeanNode;

	@Inject
	public DatarouterManyFieldTestRouter(TestDatarouterProperties datarouterProperties, Datarouter datarouter,
			DatarouterSettings datarouterSettings, NodeFactory nodeFactory, ClientId clientId,
			Supplier<ManyFieldTypeBeanFielder> fielderSupplier){
		super(datarouter, datarouterProperties, "datarouterManyFieldTestRouter", nodeFactory, datarouterSettings);

		manyFieldTypeBeanNode = create(clientId, ManyFieldBean::new, fielderSupplier)
				.withSchemaVersion(new Random().nextInt())
				.buildAndRegister();
	}

	public MapStorageNode<ManyFieldBeanKey,ManyFieldBean,ManyFieldTypeBeanFielder> manyFieldTypeBean(){
		return manyFieldTypeBeanNode;
	}

}
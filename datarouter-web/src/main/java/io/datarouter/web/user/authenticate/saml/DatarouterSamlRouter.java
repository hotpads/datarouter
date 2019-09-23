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
package io.datarouter.web.user.authenticate.saml;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.combo.IndexedSortedMapStorage.IndexedSortedMapStorageNode;
import io.datarouter.storage.router.BaseRouter;
import io.datarouter.storage.router.BaseRouterParams;
import io.datarouter.web.user.databean.SamlAuthnRequestRedirectUrl;
import io.datarouter.web.user.databean.SamlAuthnRequestRedirectUrl.SamlAuthnRequestRedirectUrlFielder;
import io.datarouter.web.user.databean.SamlAuthnRequestRedirectUrlKey;

@Singleton
public class DatarouterSamlRouter extends BaseRouter{

	public static class DatarouterSamlRouterParams extends BaseRouterParams{

		public DatarouterSamlRouterParams(ClientId clientId){
			super(clientId);
		}

	}

	public final IndexedSortedMapStorageNode<
			SamlAuthnRequestRedirectUrlKey,
			SamlAuthnRequestRedirectUrl,
			SamlAuthnRequestRedirectUrlFielder> samlAuthnRequestRedirectUrl;

	@Inject
	public DatarouterSamlRouter(Datarouter datarouter, NodeFactory nodeFactory, DatarouterSamlRouterParams params){
		super(datarouter);

		samlAuthnRequestRedirectUrl = nodeFactory.create(params.clientId, SamlAuthnRequestRedirectUrl::new,
				SamlAuthnRequestRedirectUrlFielder::new).buildAndRegister();
	}

}

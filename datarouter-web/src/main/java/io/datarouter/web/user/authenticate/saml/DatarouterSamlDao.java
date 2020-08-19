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

import java.time.Duration;
import java.time.Instant;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.BaseDao;
import io.datarouter.storage.dao.BaseDaoParams;
import io.datarouter.storage.node.factory.NodeFactory;
import io.datarouter.storage.node.op.combo.SortedMapStorage;
import io.datarouter.storage.util.DatabeanVacuum;
import io.datarouter.storage.util.DatabeanVacuum.DatabeanVacuumBuilder;
import io.datarouter.web.user.databean.SamlAuthnRequestRedirectUrl;
import io.datarouter.web.user.databean.SamlAuthnRequestRedirectUrl.SamlAuthnRequestRedirectUrlFielder;
import io.datarouter.web.user.databean.SamlAuthnRequestRedirectUrlKey;

@Singleton
public class DatarouterSamlDao extends BaseDao implements BaseDatarouterSamlDao{

	public static class DatarouterSamlDaoParams extends BaseDaoParams{

		public DatarouterSamlDaoParams(ClientId clientId){
			super(clientId);
		}

	}

	private final SortedMapStorage<SamlAuthnRequestRedirectUrlKey,SamlAuthnRequestRedirectUrl> node;

	@Inject
	public DatarouterSamlDao(Datarouter datarouter, NodeFactory nodeFactory, DatarouterSamlDaoParams params){
		super(datarouter);
		node = nodeFactory.create(params.clientId, SamlAuthnRequestRedirectUrl::new,
				SamlAuthnRequestRedirectUrlFielder::new)
				.disableNodewatch()
				.withIsSystemTable(true)
				.buildAndRegister();
	}

	@Override
	public void put(SamlAuthnRequestRedirectUrl databean){
		node.put(databean);
	}

	@Override
	public SamlAuthnRequestRedirectUrl get(SamlAuthnRequestRedirectUrlKey key){
		return node.get(key);
	}

	public DatabeanVacuum<SamlAuthnRequestRedirectUrlKey,SamlAuthnRequestRedirectUrl> makeVacuum(){
		Instant deleteBefore = Instant.now().minus(Duration.ofSeconds(5));
		return new DatabeanVacuumBuilder<>(
				node.scan(),
				databean -> databean.getCreated().toInstant().isBefore(deleteBefore),
				node::deleteMulti)
				.build();
	}

}

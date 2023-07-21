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
package io.datarouter.ratelimiter.storage;

import io.datarouter.storage.Datarouter;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.BaseDaoParams;
import io.datarouter.storage.node.factory.TallyNodeFactory;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterRateLimiterDao extends BaseTallyDao{

	public static class DatarouterRateLimiterDaoParams extends BaseDaoParams{

		public final String version;

		public DatarouterRateLimiterDaoParams(ClientId clientId, String version){
			super(clientId);
			this.version = version;
		}
	}

	@Inject
	public DatarouterRateLimiterDao(Datarouter datarouter, TallyNodeFactory nodeFactory,
			DatarouterRateLimiterDaoParams params){
		super(datarouter, nodeFactory, params.clientId, params.version);
	}

}

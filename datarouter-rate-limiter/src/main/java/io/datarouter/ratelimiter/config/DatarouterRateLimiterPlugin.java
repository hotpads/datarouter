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
package io.datarouter.ratelimiter.config;

import java.util.List;

import io.datarouter.job.BaseTriggerGroup;
import io.datarouter.ratelimiter.storage.BaseTallyDao;
import io.datarouter.ratelimiter.storage.DatarouterRateLimiterDao;
import io.datarouter.ratelimiter.storage.DatarouterRateLimiterDao.DatarouterRateLimiterDaoParams;
import io.datarouter.storage.client.ClientId;
import io.datarouter.storage.dao.Dao;
import io.datarouter.storage.dao.DaosModuleBuilder;
import io.datarouter.web.config.BaseWebPlugin;

public class DatarouterRateLimiterPlugin extends BaseWebPlugin{

	private DatarouterRateLimiterPlugin(DatarouterRateLimiterDaoModule daosModule){
		setDaosModule(daosModule);
		addDatarouterGithubDocLink("datarouter-rate-limiter");
		addSettingRoot(DatarouterRateLimiterSettings.class);
		addPluginEntry(BaseTriggerGroup.KEY, DatarouterRateLimiterTriggerGroup.class);
	}

	public static class DatarouterRateLimiterPluginBuilder{

		private final ClientId clientId;
		private String version = "1";

		public DatarouterRateLimiterPluginBuilder(ClientId clientId){
			this.clientId = clientId;
		}

		/*
		 * default version is 1
		 */
		public DatarouterRateLimiterPluginBuilder withVersion(String version){
			this.version = version;
			return this;
		}

		public DatarouterRateLimiterPlugin build(){
			return new DatarouterRateLimiterPlugin(new DatarouterRateLimiterDaoModule(clientId, version));
		}
	}

	public static class DatarouterRateLimiterDaoModule extends DaosModuleBuilder{

		private final ClientId clientId;
		private final String version;

		public DatarouterRateLimiterDaoModule(ClientId clientId, String version){
			this.clientId = clientId;
			this.version = version;
		}

		@Override
		public List<Class<? extends Dao>> getDaoClasses(){
			return List.of(DatarouterRateLimiterDao.class);
		}

		@Override
		public void configure(){
			bind(DatarouterRateLimiterDaoParams.class).toInstance(new DatarouterRateLimiterDaoParams(clientId,
					version));
			bind(BaseTallyDao.class).to(DatarouterRateLimiterDao.class);
		}

	}

}

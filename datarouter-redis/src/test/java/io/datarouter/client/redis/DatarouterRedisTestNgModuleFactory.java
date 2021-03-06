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
package io.datarouter.client.redis;

import java.util.List;

import io.datarouter.httpclient.client.DatarouterService;
import io.datarouter.httpclient.client.DatarouterService.NoOpDatarouterService;
import io.datarouter.inject.guice.BaseGuiceModule;
import io.datarouter.storage.TestDatarouterProperties;
import io.datarouter.storage.config.DatarouterProperties;
import io.datarouter.storage.servertype.ServerTypeDetector;
import io.datarouter.storage.servertype.ServerTypeDetector.NoOpServerTypeDetector;
import io.datarouter.testng.TestNgModuleFactory;
import io.datarouter.web.config.DatarouterWebGuiceModule;

public class DatarouterRedisTestNgModuleFactory extends TestNgModuleFactory{

	public DatarouterRedisTestNgModuleFactory(){
		super(List.of(
				new DatarouterWebGuiceModule(),
				new RedisGuiceModule()));
	}

	public static class RedisGuiceModule extends BaseGuiceModule{

		@Override
		protected void configure(){
			bind(DatarouterService.class).to(NoOpDatarouterService.class);
			bind(DatarouterProperties.class).to(RedisDatarouterProperties.class);
			bindDefault(ServerTypeDetector.class, NoOpServerTypeDetector.class);
		}

	}

	public static class RedisDatarouterProperties extends TestDatarouterProperties{

		@Override
		public String getDatarouterPropertiesFileLocation(){
			return getTestConfigDirectory() + "/redis.properties";
		}

	}

}

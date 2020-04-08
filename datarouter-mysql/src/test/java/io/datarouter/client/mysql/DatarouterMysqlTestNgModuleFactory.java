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
package io.datarouter.client.mysql;

import java.util.Arrays;
import java.util.Collections;

import io.datarouter.client.mysql.field.codec.factory.MysqlFieldCodecFactory;
import io.datarouter.client.mysql.field.codec.factory.StandardMysqlFieldCodecFactory;
import io.datarouter.inject.guice.BaseGuiceModule;
import io.datarouter.secret.config.DatarouterSecretGuiceModule;
import io.datarouter.storage.TestDatarouterProperties;
import io.datarouter.storage.config.DatarouterProperties;
import io.datarouter.storage.servertype.ServerTypeDetector;
import io.datarouter.storage.servertype.ServerTypeDetector.NoOpServerTypeDetector;
import io.datarouter.testng.TestNgModuleFactory;
import io.datarouter.web.config.DatarouterWebGuiceModule;
import io.datarouter.web.config.DatarouterWebTestGuiceModule;

public class DatarouterMysqlTestNgModuleFactory extends TestNgModuleFactory{

	public DatarouterMysqlTestNgModuleFactory(){
		super(Arrays.asList(
				new DatarouterWebTestGuiceModule(),
				new DatarouterWebGuiceModule(),
				new DatarouterSecretGuiceModule(),
				new DatarouterMysqlTestGuiceModule()));
	}


	public static class DatarouterMysqlTestGuiceModule extends BaseGuiceModule{

		@Override
		protected void configure(){
			bindActualInstance(MysqlFieldCodecFactory.class,
					new StandardMysqlFieldCodecFactory(Collections.emptyMap()));
			bind(DatarouterProperties.class).to(MysqlDatarouterProperties.class);
			bindDefault(ServerTypeDetector.class, NoOpServerTypeDetector.class);
		}

	}

	public static class MysqlDatarouterProperties extends TestDatarouterProperties{

		@Override
		public String getDatarouterPropertiesFileLocation(){
			return getTestConfigDirectory() + "/mysql.properties";
		}

	}

}

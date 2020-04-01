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
package io.datarouter.gcp.spanner;

import java.util.Arrays;

import io.datarouter.inject.guice.BaseGuiceModule;
import io.datarouter.storage.config.DatarouterProperties;
import io.datarouter.storage.servertype.BaseServerTypes;
import io.datarouter.storage.servertype.ServerType;
import io.datarouter.storage.servertype.ServerTypeDetector;
import io.datarouter.storage.servertype.ServerTypeDetector.NoOpServerTypeDetector;
import io.datarouter.testng.TestNgModuleFactory;
import io.datarouter.util.string.StringTool;
import io.datarouter.web.config.DatarouterWebGuiceModule;
import io.datarouter.web.config.DatarouterWebTestGuiceModule;

public class SpannerTestNgModuleFactory extends TestNgModuleFactory{

	public SpannerTestNgModuleFactory(){
		super(Arrays.asList(
				new DatarouterWebTestGuiceModule(),
				new DatarouterWebGuiceModule(),
				new SpannerTestGuiceModule()));
	}

	public static class SpannerTestGuiceModule extends BaseGuiceModule{

		@Override
		protected void configure(){
			bind(DatarouterProperties.class).to(SpannerTestDatarouterProperties.class);
			bindDefault(ServerTypeDetector.class, NoOpServerTypeDetector.class);
		}
	}


	public static class SpannerTestDatarouterProperties extends DatarouterProperties{

		private static final String SERVICE_NAME = "datarouter-test";
		private static final String DEFAULT_CONFIG_DIRECTORY = "/etc/datarouter/config";
		private static final String DEFAULT_TEST_CONFIG_DIRECTORY = "/etc/datarouter/test";
		private static final String BASE_CONFIG_DIRECTORY_ENV_VARIABLE = "BASE_CONFIG_DIRECTORY";
		private static final String SERVER_CONFIG_FILE_NAME = "server.properties";
		public static final String CONFIG_DIRECTORY;
		public static final String TEST_CONFIG_DIRECTORY;
		static{
			String baseConfigDirectoryPath = System.getenv(BASE_CONFIG_DIRECTORY_ENV_VARIABLE);
			if(StringTool.notEmpty(baseConfigDirectoryPath)){
				CONFIG_DIRECTORY = baseConfigDirectoryPath + "/config";
				TEST_CONFIG_DIRECTORY = baseConfigDirectoryPath + "/test";
			}else{
				CONFIG_DIRECTORY = DEFAULT_CONFIG_DIRECTORY;
				TEST_CONFIG_DIRECTORY = DEFAULT_TEST_CONFIG_DIRECTORY;
			}
		}


		public SpannerTestDatarouterProperties(){
			super(new BaseServerTypes(ServerType.DEV), SERVICE_NAME, CONFIG_DIRECTORY, SERVER_CONFIG_FILE_NAME);
		}

		@Override
		public String getDatarouterPropertiesFileLocation(){
			return TEST_CONFIG_DIRECTORY + "/spanner.properties";
		}

	}

}

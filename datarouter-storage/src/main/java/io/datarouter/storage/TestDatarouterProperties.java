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
package io.datarouter.storage;

import javax.inject.Singleton;

import io.datarouter.storage.config.DatarouterProperties;
import io.datarouter.storage.servertype.BaseServerTypes;
import io.datarouter.storage.servertype.ServerType;
import io.datarouter.util.string.StringTool;

@Singleton
public class TestDatarouterProperties extends DatarouterProperties{

	private static final String SERVICE_NAME = "datarouter-test";
	public static final String SERVER_CONFIG_FILE_NAME = "server.properties";
	public static final String DATAROUTER_TEST_FILE_NAME = "datarouter-test.properties";
	private static final String DEFAULT_CONFIG_DIRECTORY = "/etc/datarouter/config";
	private static final String BASE_CONFIG_DIRECTORY_ENV_VARIABLE = "BASE_CONFIG_DIRECTORY";
	public static final String CONFIG_DIRECTORY;
	static{
		String baseConfigDirectoryPath = System.getenv(BASE_CONFIG_DIRECTORY_ENV_VARIABLE);
		if(StringTool.notEmpty(baseConfigDirectoryPath)){
			CONFIG_DIRECTORY = baseConfigDirectoryPath + "/config";
		}else{
			CONFIG_DIRECTORY = DEFAULT_CONFIG_DIRECTORY;
		}
	}

	private final String datarouterTestFileLocation;

	public TestDatarouterProperties(){
		super(new BaseServerTypes(ServerType.DEV), SERVICE_NAME, CONFIG_DIRECTORY, SERVER_CONFIG_FILE_NAME);
		this.datarouterTestFileLocation = findConfigFile(DATAROUTER_TEST_FILE_NAME);
	}

	@Override
	public String getDatarouterPropertiesFileLocation(){
		return datarouterTestFileLocation;
	}

}

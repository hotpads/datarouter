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
package io.datarouter.storage.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.util.string.StringTool;

public class ConfigDirectoryConstants{
	private static final Logger logger = LoggerFactory.getLogger(ConfigDirectoryConstants.class);

	public static final String CONFIG_DIRECTORY_NAME = "config";
	public static final String TEST_CONFIG_DIRECTORY_NAME = "test";

	private static final String BASE_CONFIG_DIRECTORY_ENV_VARIABLE = "BASE_CONFIG_DIRECTORY";
	private static final String DEFAULT_BASE_CONFIG_DIRECTORY = "/etc/datarouter";
	private static final String CONFIG_DIRECTORY;
	private static final String TEST_CONFIG_DIRECTORY;
	private static final String SOURCE;

	static{
		String baseConfigDirectory = System.getenv(BASE_CONFIG_DIRECTORY_ENV_VARIABLE);
		if(StringTool.isEmpty(baseConfigDirectory)){
			baseConfigDirectory = DEFAULT_BASE_CONFIG_DIRECTORY;
			SOURCE = "default constant";
		}else{
			SOURCE = "environment variable";
		}
		CONFIG_DIRECTORY = baseConfigDirectory + "/" + CONFIG_DIRECTORY_NAME;
		TEST_CONFIG_DIRECTORY = baseConfigDirectory + "/" + TEST_CONFIG_DIRECTORY_NAME;
	}

	public static String getSource(){
		logger.info("source={}", SOURCE);
		return SOURCE;
	}

	public static String getConfigDirectory(){
		logger.info("configDirectory={}", CONFIG_DIRECTORY);
		return CONFIG_DIRECTORY;
	}

	public static String getTestConfigDirectory(){
		logger.info("testConfigDirectory={}", TEST_CONFIG_DIRECTORY);
		return TEST_CONFIG_DIRECTORY;
	}

}

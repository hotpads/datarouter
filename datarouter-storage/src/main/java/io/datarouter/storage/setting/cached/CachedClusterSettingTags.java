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
package io.datarouter.storage.setting.cached;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.ConfigDirectoryConstants;
import io.datarouter.storage.servertype.DatarouterServerTypeDetector;
import io.datarouter.storage.setting.DatarouterSettingTag;
import io.datarouter.util.cached.Cached;
import io.datarouter.util.properties.PropertiesTool;

@Singleton
public class CachedClusterSettingTags extends Cached<List<DatarouterSettingTag>>{
	private static final Logger logger = LoggerFactory.getLogger(CachedClusterSettingTags.class);

	public static final String CONFIG_FILENAME = "clusterSetting.properties";
	private static final String PROPERTY_TAGS_NAME = "tags";
	private final DatarouterServerTypeDetector datarouterServerTypeDetector;

	@Inject
	public CachedClusterSettingTags(DatarouterServerTypeDetector datarouterServerTypeDetector){
		super(15, TimeUnit.SECONDS);
		this.datarouterServerTypeDetector = datarouterServerTypeDetector;
	}

	@Override
	protected List<DatarouterSettingTag> reload(){
		if(!datarouterServerTypeDetector.mightBeDevelopment()){
			return List.of();
		}
		String configFileLocation = getConfigFilePath();
		Properties properties;
		try{
			properties = PropertiesTool.parse(configFileLocation);
			logger.info("Got clusterSetting properties from file {}", configFileLocation);
			if(properties.containsKey(PROPERTY_TAGS_NAME)){
				return Scanner.of(properties.getProperty(PROPERTY_TAGS_NAME).split(","))
						.map(DatarouterSettingTag::new)
						.list();
			}
		}catch(RuntimeException e1){
			logger.info("", e1);
			File file = new File(configFileLocation);
			try{
				file.createNewFile();
				logger.warn("Created clusterSetting properties file {}", configFileLocation);
			}catch(IOException e2){
				throw new RuntimeException("failed to create properties file " + configFileLocation);
			}
		}
		return List.of();
	}

	public void writeToFile(String value){
		Properties props = new Properties();
		props.setProperty(PROPERTY_TAGS_NAME, value);
		PropertiesTool.writeToFile(props, getConfigFilePath());
	}

	public static String getConfigFilePath(){
		return ConfigDirectoryConstants.getConfigDirectory() + '/' + CONFIG_FILENAME;
	}

}

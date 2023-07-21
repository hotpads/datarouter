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

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.TreeSet;

import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.ConfigDirectoryConstants;
import io.datarouter.storage.servertype.DatarouterServerTypeDetector;
import io.datarouter.storage.setting.DatarouterSettingTag;
import io.datarouter.util.cached.Cached;
import io.datarouter.util.properties.PropertiesTool;
import io.datarouter.util.string.StringTool;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class CachedClusterSettingTags extends Cached<List<DatarouterSettingTag>>{

	public static final String CONFIG_FILENAME = "clusterSetting.properties";
	private static final String PROPERTY_TAGS_NAME = "tags";
	private final DatarouterServerTypeDetector datarouterServerTypeDetector;

	@Inject
	public CachedClusterSettingTags(DatarouterServerTypeDetector datarouterServerTypeDetector){
		super(getCacheTtl());
		this.datarouterServerTypeDetector = datarouterServerTypeDetector;
	}

	/*-------- read ----------*/

	@Override
	protected List<DatarouterSettingTag> reload(){
		return datarouterServerTypeDetector.mightBeDevelopment()
				? readTags()
				: List.of();
	}

	public Optional<Properties> findProperties(){
		try{
			return Optional.of(PropertiesTool.parse(getConfigFilePath()));
		}catch(RuntimeException e){
			return Optional.empty();
		}
	}

	public Optional<String> getPropertiesTagsStringValue(){
		return findProperties()
				.map(properties -> properties.getProperty(PROPERTY_TAGS_NAME));
	}

	public List<String> readTagNames(){
		return getPropertiesTagsStringValue()
				.map(csv -> Scanner.of(csv.split(","))
						.exclude(StringTool::isEmpty)//TODO why is there an empty string?
						.list())
				.orElse(List.of());
	}

	public List<DatarouterSettingTag> readTags(){
		return Scanner.of(readTagNames())
				.map(DatarouterSettingTag::new)
				.list();
	}

	/*---------- write -----------*/

	public void updateTag(String name, boolean enabled){
		TreeSet<String> newTagNames = new TreeSet<>(readTagNames());
		if(enabled){
			newTagNames.add(name);
		}else{
			newTagNames.remove(name);
		}
		writeToFile(newTagNames);
	}

	public void writeToFile(Collection<String> tagNames){
		String csvValues = String.join(",", tagNames);
		writeToFile(csvValues);
	}

	public void writeToFile(String value){
		Properties props = new Properties();
		props.setProperty(PROPERTY_TAGS_NAME, value);
		PropertiesTool.writeToFile(props, getConfigFilePath());
	}

	/*-------- static ----------*/

	public static Duration getCacheTtl(){
		return Duration.ofSeconds(5);
	}

	public static String getConfigFilePath(){
		return ConfigDirectoryConstants.getConfigDirectory() + '/' + CONFIG_FILENAME;
	}

}

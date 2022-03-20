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
package io.datarouter.storage.config.properties;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.function.Supplier;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.storage.config.ComputedPropertiesFinder;
import io.datarouter.storage.config.ConfigDirectoryConstants;

@Singleton
public class InternalConfigDirectory implements Supplier<String>{

	public static final String INTERNAL_CONFIG_DIRECTORY = "internalConfigDirectory";

	private final String internalConfigDirectory;

	@Inject
	public InternalConfigDirectory(ComputedPropertiesFinder finder){
		this.internalConfigDirectory = finder.findProperty(INTERNAL_CONFIG_DIRECTORY);
	}

	@Override
	public String get(){
		return internalConfigDirectory;
	}

	// TODO move this out to a service class.
	public String findConfigFile(String filename){
		// call ConfigDirectory.get()
		String configDirectory = ConfigDirectoryConstants.getConfigDirectory();
		String externalLocation = configDirectory + "/" + filename;
		if(Files.exists(Paths.get(externalLocation))){
			return externalLocation;
		}
		Objects.requireNonNull(
				internalConfigDirectory,
				externalLocation + " doesn't exist and " + INTERNAL_CONFIG_DIRECTORY + " property is not set");
		externalLocation = configDirectory + "/" + internalConfigDirectory + "/" + filename;
		if(Files.exists(Paths.get(externalLocation))){
			return externalLocation;
		}
		return "/config/" + internalConfigDirectory + "/" + filename;
	}

}

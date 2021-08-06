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
package io.datarouter.secret.client.local;

import javax.inject.Singleton;

public interface LocalStorageConfig{

	static final String DEFAULT_CONFIG_DIRECTORY = "/etc/datarouter/config";
	static final String DEFAULT_CONFIG_FILENAME = "unencryptedSecretStorage.properties";

	default String getConfigDirectory(){
		return DEFAULT_CONFIG_DIRECTORY;
	}

	default String getConfigFilename(){
		return DEFAULT_CONFIG_FILENAME;
	}

	default String getConfigFilePath(){
		return getConfigDirectory() + '/' + getConfigFilename();
	}

	@Singleton
	public static class DefaultLocalStorageConfig implements LocalStorageConfig{
	}

}

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
package io.datarouter.auth.service.deprovisioning;

import java.util.List;

import io.datarouter.plugin.PluginConfigKey;
import io.datarouter.plugin.PluginConfigType;
import io.datarouter.plugin.PluginConfigValue;

/**
 * these methods are intended to be called by {@link UserDeprovisioningService} in conjunction with
 * {@link UserDeprovisioningStrategy}. See {@link UserDeprovisioningService} for exact order and configuration.
 */
public interface UserDeprovisioningListener extends PluginConfigValue<UserDeprovisioningListener>{

	PluginConfigKey<UserDeprovisioningListener> KEY = new PluginConfigKey<>(
			"userDeprovisioningListener",
			PluginConfigType.CLASS_LIST);

	/**
	 * @param usernames usernames that were deprovisioned
	 */
	default void onDeprovisionedUsers(List<String> usernames){
	}

	@Override
	default PluginConfigKey<UserDeprovisioningListener> getKey(){
		return KEY;
	}

}

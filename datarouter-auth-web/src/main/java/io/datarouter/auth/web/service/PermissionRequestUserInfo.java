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
package io.datarouter.auth.web.service;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import io.datarouter.auth.storage.user.datarouteruser.DatarouterUser;
import io.datarouter.plugin.PluginConfigKey;
import io.datarouter.plugin.PluginConfigType;
import io.datarouter.plugin.PluginConfigValue;
import io.datarouter.plugin.PluginInjector;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

public interface PermissionRequestUserInfo extends PluginConfigValue<PermissionRequestUserInfo>{

	PluginConfigKey<PermissionRequestUserInfo> KEY = new PluginConfigKey<>(
			"permissionRequestUserInfo",
			PluginConfigType.CLASS_SINGLE);

	List<UserInfo> getUserInformation(DatarouterUser user);

	@Override
	default PluginConfigKey<PermissionRequestUserInfo> getKey(){
		return KEY;
	}

	@Singleton
	class PermissionRequestUserInfoSupplier implements Supplier<PermissionRequestUserInfo>{

		@Inject
		private PluginInjector injector;

		@Override
		public PermissionRequestUserInfo get(){
			return injector.getInstance(KEY);
		}

	}

	public record UserInfo(
			String relation,
			String name,
			Optional<String> link,
			Optional<String> title){
	}

}

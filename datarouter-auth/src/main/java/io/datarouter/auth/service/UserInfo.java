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
package io.datarouter.auth.service;

import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import io.datarouter.auth.role.Role;
import io.datarouter.auth.session.SessionBasedUser;
import io.datarouter.plugin.PluginConfigKey;
import io.datarouter.plugin.PluginConfigType;
import io.datarouter.plugin.PluginConfigValue;
import io.datarouter.plugin.PluginInjector;
import io.datarouter.scanner.Scanner;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

// inject this class through UserInfoSupplier
public interface UserInfo extends PluginConfigValue<UserInfo>{

	PluginConfigKey<UserInfo> KEY = new PluginConfigKey<>(
			"userInfo",
			PluginConfigType.CLASS_SINGLE);

	//TODO DATAROUTER-2794
	Scanner<? extends SessionBasedUser> scanAllUsers(boolean enabledOnly, Set<Role> includedRoles);
	Optional<? extends SessionBasedUser> findUserByUsername(String username, boolean allowCached);
	Optional<? extends SessionBasedUser> findUserByToken(String token, boolean allowCached);
	Optional<? extends SessionBasedUser> findUserById(Long id, boolean allowCached);

	Set<Role> getRolesByUsername(String username, boolean allowCached);

	default Boolean hasRoleByUsername(String username, Role role, boolean allowCached){
		return getRolesByUsername(username, allowCached).contains(role);
	}

	@Override
	default PluginConfigKey<UserInfo> getKey(){
		return KEY;
	}

	@Singleton
	class UserInfoSupplier implements Supplier<UserInfo>{

		@Inject
		private PluginInjector injector;

		@Override
		public UserInfo get(){
			return injector.getInstance(UserInfo.KEY);
		}

	}

}

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
package io.datarouter.auth.service;

import java.util.Optional;
import java.util.Set;

import io.datarouter.scanner.Scanner;
import io.datarouter.web.user.session.service.Role;
import io.datarouter.web.user.session.service.SessionBasedUser;

public interface UserInfo{

	//TODO DATAROUTER-2794
	Scanner<? extends SessionBasedUser> scanAllUsers(boolean enabledOnly, Set<Role> includedRoles);

	Optional<? extends SessionBasedUser> getUserByUsername(String username);
	Optional<? extends SessionBasedUser> getUserByToken(String token);
	Optional<? extends SessionBasedUser> getUserById(Long id);

	default Set<Role> getRolesByUsername(String username){
		return getRolesByUsername(username, true);
	}

	Set<Role> getRolesByUsername(String username, boolean disallowCached);
	Set<Role> getRolesByToken(String token);
	Set<Role> getRolesById(Long id);

	default Boolean hasRoleByUsername(String username, Role role){
		return getRolesByUsername(username).contains(role);
	}

	default Boolean hasRoleByToken(String token, Role role){
		return getRolesByToken(token).contains(role);
	}

	default Boolean hasRoleById(Long id, Role role){
		return getRolesById(id).contains(role);
	}

}

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
package io.datarouter.web.user.session.service;

import java.util.Collection;
import java.util.Set;

public interface RoleManager{

	Role getRoleFromPersistentString(String persistentString);
	Boolean isAdmin(Role role);

	default Boolean isAdmin(Collection<Role> roles){
		return roles.stream()
				.anyMatch(this::isAdmin);
	}

	Set<Role> getAllRoles();
	Set<Role> getConferrableRoles(Collection<Role> userRoles);
	Set<Role> getRolesForGroup(String groupId);
	Set<Role> getRolesForSuperGroup();
	Set<Role> getRolesForDefaultGroup();

	//these are roles that do not present a security risk, although they may be more than just the default roles
	default Set<Role> getUnimportantRoles(){
		return Set.of();
	}

}

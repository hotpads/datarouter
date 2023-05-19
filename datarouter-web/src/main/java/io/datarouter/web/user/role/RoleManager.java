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
package io.datarouter.web.user.role;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import io.datarouter.scanner.Scanner;
import io.datarouter.web.user.databean.DatarouterUser;

public interface RoleManager{

	RoleEnum<? extends RoleEnum<?>> getRoleEnum();

	default Role getRoleFromPersistentString(String persistentString){
		return getRoleEnum().fromPersistentString(persistentString).getRole();
	}

	RoleApprovalTypeEnum<? extends RoleApprovalTypeEnum<?>> getRoleApprovalTypeEnum();

	default RoleApprovalType getRoleApprovalTypeFromPersistentString(String persistentString){
		return getRoleApprovalTypeEnum().fromPersistentString(persistentString).getRoleApprovalType();
	}

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

	default Map<RoleApprovalType, Integer> getRoleApprovalRequirements(Role role){
		return Map.of(DatarouterRoleApprovalType.ADMIN.getRoleApprovalType(), 1);
	}

	default Map<Role, Map<RoleApprovalType, Integer>> getAllRoleApprovalRequirements(){
		return Scanner.of(getAllRoles()).toMap(Function.identity(), role -> {
			Map<RoleApprovalType, Integer> roleApprovalRequirements = getRoleApprovalRequirements(role);
			// Each role should at a minimum require a single standard approval
			if(roleApprovalRequirements == null || roleApprovalRequirements.isEmpty()){
				return Map.of(DatarouterRoleApprovalType.ADMIN.getRoleApprovalType(), 1);
			}
			return roleApprovalRequirements;
		});
	}

	default List<RoleApprovalType> getPrioritizedRoleApprovalTypes(
			DatarouterUser editor,
			@SuppressWarnings("unused") DatarouterUser user,
			@SuppressWarnings("unused") Set<RoleApprovalType> relevantApprovalTypes){
		if(isAdmin(editor.getRoles())){
			return List.of(DatarouterRoleApprovalType.ADMIN.getRoleApprovalType());
		}
		return Collections.emptyList();
	}

	//these are roles that do not present a security risk, although they may be more than just the default roles
	default Set<Role> getUnimportantRoles(){
		return Set.of();
	}

	default Set<String> getAdditionalPermissionRequestEmailRecipients(
			DatarouterUser requestor,
			Set<Role> requestedRoles){
		return Set.of();
	}

}

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
package io.datarouter.auth.role;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import io.datarouter.auth.role.RoleApprovalType.RoleApprovalTypePriorityComparator;
import io.datarouter.auth.storage.user.datarouteruser.DatarouterUser;
import io.datarouter.scanner.Scanner;

public interface RoleManager{

	RoleRegistry getRoleRegistry();

	default Optional<Role> findRoleFromPersistentString(String persistentString){
		return getRoleRegistry().findRoleFromPersistentString(persistentString);
	}

	RoleApprovalTypeEnum<? extends RoleApprovalTypeEnum<?>> getRoleApprovalTypeEnum();

	default Optional<RoleApprovalType> findRoleApprovalTypeFromPersistentString(String persistentString){
		return Optional.ofNullable(getRoleApprovalTypeEnum().fromPersistentString(persistentString))
				.map(RoleApprovalTypeEnum::getRoleApprovalType);
	}

	default Set<Role> getAllRoles(){
		return getRoleRegistry().getAllRoles();
	}

	default Set<Role> getRequestableRoles(@SuppressWarnings("unused") DatarouterUser user){
		return getAllRoles();
	}

	Set<Role> getRolesForGroup(String groupId);
	default Map<String,Set<Role>> getRoleGroupMappings(){
		return Map.of();
	}
	Set<Role> getSuperAdminRoles();
	Set<Role> getDefaultRoles();

	default Map<RoleApprovalType,Integer> getRoleApprovalRequirements(@SuppressWarnings("unused") Role role){
		return Map.of(DatarouterRoleApprovalType.ADMIN.getRoleApprovalType(), 1);
	}

	default Map<Role,Map<RoleApprovalType,Integer>> getAllRoleApprovalRequirements(){
		return Scanner.of(getAllRoles()).toMap(Function.identity(),role -> {
			Map<RoleApprovalType,Integer> roleApprovalRequirements = getRoleApprovalRequirements(role);
			// Each role should at a minimum require a single standard approval
			if(roleApprovalRequirements == null || roleApprovalRequirements.isEmpty()){
				return Map.of(DatarouterRoleApprovalType.ADMIN.getRoleApprovalType(), 1);
			}
			return roleApprovalRequirements;
		});
	}

	Map<RoleApprovalType,BiFunction<DatarouterUser,DatarouterUser,Boolean>> getApprovalTypeAuthorityValidators();

	default List<RoleApprovalType> getPrioritizedRoleApprovalTypes(
			DatarouterUser editor,
			DatarouterUser user,
			Set<RoleApprovalType> relevantApprovalTypes){
		var approvalTypeAuthorityValidators = getApprovalTypeAuthorityValidators();
		return Scanner.of(relevantApprovalTypes)
				.include(approvalTypeAuthorityValidators::containsKey)
				.include(roleApprovalType -> {
					var validatorFunction = approvalTypeAuthorityValidators.get(roleApprovalType);
					return validatorFunction.apply(editor, user);
				})
				// For RoleManagers extending others and overriding the approval type's priority
				.map(RoleApprovalType::persistentString)
				.concatOpt(this::findRoleApprovalTypeFromPersistentString)
				.sort(new RoleApprovalTypePriorityComparator())
				.list();
	}

	//these are roles that do not present a security risk, although they may be more than just the default roles
	@Deprecated
	default Set<Role> getUnimportantRoles(){
		return Set.of();
	}

	default Set<String> getAdditionalPermissionRequestEmailRecipients(
			@SuppressWarnings("unused")
			DatarouterUser requestor,
			@SuppressWarnings("unused")
			Set<Role> requestedRoles){
		return Set.of();
	}

	default Set<Role> calculateRolesWithGroups(Collection<Role> roles, Collection<String> groups){
		return Scanner.of(groups)
				.concatIter(this::getRolesForGroup)
				.append(roles)
				.collect(HashSet::new);
	}

	default Map<Role,List<String>> getGroupsByRole(Collection<String> groups){
		Map<Role,List<String>> groupsByRoles = new HashMap<>();
		for(String group : groups){
			Set<Role> rolesForGroup = getRolesForGroup(group);
			for(Role role : rolesForGroup){
				groupsByRoles.putIfAbsent(role, new ArrayList<>());
				groupsByRoles.get(role).add(group);
			}
		}
		return groupsByRoles;
	}

}

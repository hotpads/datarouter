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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

import io.datarouter.auth.storage.user.datarouteruser.DatarouterUser;
import io.datarouter.scanner.Scanner;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterRoleManager extends BaseRoleManager{

	@Inject
	private DatarouterUserRoleRegistry roleRegistry;

	@Override
	public RoleRegistry getRoleRegistry(){
		return roleRegistry;
	}

	@Override
	public RoleApprovalTypeEnum<? extends RoleApprovalTypeEnum<?>> getRoleApprovalTypeEnum(){
		return DatarouterRoleApprovalType.ADMIN;
	}

	@Override
	public Map<RoleApprovalType,BiFunction<DatarouterUser,DatarouterUser,Boolean>> getApprovalTypeAuthorityValidators(){
		return Map.of(
				DatarouterRoleApprovalType.ADMIN.getRoleApprovalType(), this::editorIsDatarouterAdmin,
				DatarouterRoleApprovalType.PROHIBITED.getRoleApprovalType(),
				(_, _) -> false);
	}

	@Override
	public final Set<Role> getSuperAdminRoles(){
		// include all Datarouter roles by default
		return RoleRegistry.DEFAULT_ROLES;
	}

	@Override
	public final Set<Role> getDefaultRoles(){
		return Scanner.of(getAdditionalDefaultRoles())
				.append(DatarouterUserRoleRegistry.REQUESTOR)
				.collect(HashSet::new);
	}

	protected Set<Role> getAdditionalDefaultRoles(){
		return new HashSet<>();
	}

	protected Boolean editorIsDatarouterAdmin(DatarouterUser editor, DatarouterUser user){
		return isDatarouterAdmin(editor);
	}

	protected boolean isDatarouterAdmin(DatarouterUser user){
		return calculateRolesWithGroups(user.getRolesIgnoreSaml(), user.getSamlGroups())
				.contains(DatarouterUserRoleRegistry.DATAROUTER_ADMIN);
	}

}

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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

import io.datarouter.scanner.Scanner;
import io.datarouter.web.user.databean.DatarouterUser;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterRoleManager extends BaseRoleManager{

	@Override
	public RoleEnum<? extends RoleEnum<?>> getRoleEnum(){
		return DatarouterUserRole.ADMIN;
	}

	@Override
	public RoleApprovalTypeEnum<? extends RoleApprovalTypeEnum<?>> getRoleApprovalTypeEnum(){
		return DatarouterRoleApprovalType.ADMIN;
	}

	@Override
	public Set<Role> getAllRoles(){
		return Scanner.of(DatarouterUserRole.values())
				.map(RoleEnum::getRole)
				.collect(HashSet::new);
	}

	@Override
	public Map<RoleApprovalType,BiFunction<DatarouterUser,DatarouterUser,Boolean>> getApprovalTypeAuthorityValidators(){
		return Map.of(
				DatarouterRoleApprovalType.ADMIN.getRoleApprovalType(),
				(editor, $) -> editor.getRoles().contains(DatarouterUserRole.DATAROUTER_ADMIN.getRole()));
	}

	@Override
	public final Set<Role> getSuperAdminRoles(){
		return Scanner.of(DatarouterUserRole.values())
				.map(DatarouterUserRole::getRole)
				.append(getAdditionalSuperAdminRoles())
				.collect(HashSet::new);
	}

	protected Set<Role> getAdditionalSuperAdminRoles(){
		return new HashSet<>();
	}

	@Override
	public final Set<Role> getDefaultRoles(){
		return Scanner.of(getAdditionalDefaultRoles())
				.append(DatarouterUserRole.REQUESTOR.getRole())
				.collect(HashSet::new);
	}

	protected Set<Role> getAdditionalDefaultRoles(){
		return new HashSet<>();
	}

}

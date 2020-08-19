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
package io.datarouter.web.user.session.service;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Singleton;

import io.datarouter.scanner.Scanner;
import io.datarouter.web.user.role.DatarouterUserRole;

@Singleton
public class DatarouterRoleManager extends BaseRoleManager{

	protected DatarouterRoleManager(){
		super(DatarouterUserRole.ADMIN);
	}

	protected DatarouterRoleManager(RoleEnum<? extends RoleEnum<?>> roleEnum){
		super(roleEnum);
	}

	@Override
	public Set<Role> getAllRoles(){
		return Scanner.of(DatarouterUserRole.values())
				.map(RoleEnum::getRole)
				.collect(HashSet::new);
	}

	@Override
	public Set<Role> getConferrableRoles(Collection<Role> userRoles){
		if(userRoles.contains(DatarouterUserRole.DATAROUTER_ADMIN.getRole())){
			return getAllRoles();
		}
		if(userRoles.contains(DatarouterUserRole.ADMIN.getRole())){
			Set<Role> roles = getAllRoles();
			roles.remove(DatarouterUserRole.DATAROUTER_ADMIN.getRole());
			return roles;
		}
		return new HashSet<>(userRoles);
	}

	@Override
	protected Set<Role> getSuperRoles(){
		return getAllRoles();
	}

	@Override
	protected Set<Role> getDefaultRoles(){
		return Set.of(DatarouterUserRole.REQUESTOR.getRole());
	}

	@Override
	protected Set<Role> getAdminRoles(){
		return Set.of(
				DatarouterUserRole.ADMIN.getRole(),
				DatarouterUserRole.DATAROUTER_ACCOUNTS.getRole(),
				DatarouterUserRole.DATAROUTER_ADMIN.getRole(),
				DatarouterUserRole.DATAROUTER_JOB.getRole(),
				DatarouterUserRole.DATAROUTER_MONITORING.getRole(),
				DatarouterUserRole.DATAROUTER_SETTINGS.getRole(),
				DatarouterUserRole.DATAROUTER_TOOLS.getRole());
	}

}

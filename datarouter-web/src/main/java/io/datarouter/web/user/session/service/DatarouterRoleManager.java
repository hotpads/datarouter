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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Singleton;

import io.datarouter.util.array.ArrayTool;
import io.datarouter.web.user.role.DatarouterUserRole;

@Singleton
public class DatarouterRoleManager extends BaseRoleManager{

	@Override
	public Set<Role> getAllRoles(){
		return ArrayTool.mapToSet(RoleEnum::getRole, DatarouterUserRole.values());
	}

	@Override
	public Set<Role> getConferrableRoles(Collection<Role> userRoles){
		if(userRoles.contains(DatarouterUserRole.datarouterAdmin.getRole())){
			return getAllRoles();
		}
		if(userRoles.contains(DatarouterUserRole.admin.getRole())){
			Set<Role> roles = getAllRoles();
			roles.remove(DatarouterUserRole.datarouterAdmin.getRole());
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
		return Collections.singleton(DatarouterUserRole.requestor.getRole());
	}

	@Override
	protected Set<Role> getAdminRoles(){
		return new HashSet<>(Arrays.asList(DatarouterUserRole.admin.getRole(), DatarouterUserRole.datarouterAdmin
				.getRole()));
	}

}

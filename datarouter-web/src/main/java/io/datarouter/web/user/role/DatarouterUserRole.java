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
package io.datarouter.web.user.role;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import io.datarouter.util.enums.DatarouterEnumTool;
import io.datarouter.util.enums.StringEnum;
import io.datarouter.web.user.databean.DatarouterUser;
import io.datarouter.web.user.session.DatarouterSession;
import io.datarouter.web.user.session.service.Role;

public enum DatarouterUserRole
implements StringEnum<DatarouterUserRole>, Role{
	datarouterAdmin("datarouterAdmin"),
	docUser("docUser"),
	admin("admin"),
	user("user"),
	apiUser("apiUser"),
	requestor("requestor");

	private final String persistentString;

	private DatarouterUserRole(String persistentString){
		this.persistentString = persistentString;
	}

	public static boolean isUserAdmin(DatarouterUser user){
		return isAdmin(user.getRoles());
	}

	public static boolean isSessionAdmin(DatarouterSession session){
		return isAdmin(session.getRoles());
	}

	private static boolean isAdmin(Collection<DatarouterUserRole> roles){
		return roles.contains(datarouterAdmin) || roles.contains(admin);
	}

	public static DatarouterUserRole fromPersistentStringStatic(String persistentString){
		return DatarouterEnumTool.getEnumFromString(values(), persistentString, null);
	}

	public static Set<DatarouterUserRole> fromStringArray(String[] userRoles){
		Set<DatarouterUserRole> userRolesSet = new TreeSet<>();
		if(userRoles == null){
			return userRolesSet;
		}

		for(String roleString : userRoles){
			DatarouterUserRole role = DatarouterEnumTool.getEnumFromString(values(), roleString, null);
			if(role != null){
				userRolesSet.add(role);
			}
		}
		return userRolesSet;
	}

	public static Set<DatarouterUserRole> getPermissibleRolesForUser(DatarouterUser datarouterUser){
		return new HashSet<>(datarouterUser.getRoles());
	}

	@Override
	public String getPersistentString(){
		return persistentString;
	}

	@Override
	public DatarouterUserRole fromPersistentString(String str){
		return fromPersistentStringStatic(str);
	}

}

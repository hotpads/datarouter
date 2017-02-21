package com.hotpads.handler.user.role;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.hotpads.handler.user.DatarouterUser;
import com.hotpads.handler.user.session.DatarouterSession;
import com.hotpads.util.core.enums.DatarouterEnumTool;
import com.hotpads.util.core.enums.StringEnum;

public enum DatarouterUserRole
implements StringEnum<DatarouterUserRole>{

	datarouterAdmin("datarouterAdmin"),
	docUser("docUser"),
	admin("admin"),
	user("user"),
	apiUser("apiUser");

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

	public static Set<DatarouterUserRole> fromStringArray(String[] userRoles){
		Set<DatarouterUserRole> userRolesSet = new HashSet<>();
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

	public static Set<DatarouterUserRole> getPermissibleRolesForUser(DatarouterUser datarouterUser, boolean isSelf){
		Set<DatarouterUserRole> userRoles = new HashSet<>(datarouterUser.getRoles());
		if(isSelf && userRoles.contains(datarouterAdmin)){
			return userRoles;
		}

		userRoles.remove(datarouterAdmin);
		if(userRoles.contains(admin)){
			return userRoles;
		}

		userRoles.remove(admin);
		return userRoles;
	}

	@Override
	public String getPersistentString(){
		return persistentString;
	}

	@Override
	public DatarouterUserRole fromPersistentString(String str){
		return DatarouterEnumTool.getEnumFromString(values(), str, null);
	}

}

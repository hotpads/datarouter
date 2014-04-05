package com.hotpads.handler.user.role;

import java.util.List;
import java.util.Set;

import com.hotpads.datarouter.storage.field.enums.DataRouterEnumTool;
import com.hotpads.datarouter.storage.field.enums.StringEnum;
import com.hotpads.handler.user.DatarouterUser;
import com.hotpads.util.core.SetTool;

public enum DatarouterUserRole
implements StringEnum<DatarouterUserRole>{

	datarouterAdmin("datarouterAdmin"),
	admin("admin"),
	user("user");
	
	private String persistentString;

	private DatarouterUserRole(String persistentString){
		this.persistentString = persistentString;
	}
	
	
	/**************** StringEnum *******************/
	
	public static boolean isUserAdmin(DatarouterUser user) {
		List<DatarouterUserRole> roles = user.getRoles();
		return roles.contains(datarouterAdmin) || roles.contains(admin);
	}
	
	public static Set<DatarouterUserRole> fromStringArray(String[] userRoles) {
		Set<DatarouterUserRole> userRolesSet = SetTool.create();
		if (userRoles == null) { return userRolesSet; }
		
		for(String role : userRoles) {
			DatarouterUserRole r = DataRouterEnumTool.getEnumFromString(values(), role, null);
			if(r != null) {
				userRolesSet.add(r);
			}
		}
		return userRolesSet;
	}
	
	public static Set<DatarouterUserRole> getPermissibleRolesForUser(DatarouterUser datarouterUser, boolean isSelf) {
		Set<DatarouterUserRole> userRoles = SetTool.create(datarouterUser.getRoles());
		if(isSelf && userRoles.contains(datarouterAdmin)) { return userRoles; }
		
		userRoles.remove(datarouterAdmin);
		if(userRoles.contains(admin)) { return userRoles; }
		
		userRoles.remove(admin);
		return userRoles;
	}
	
	@Override
	public String getPersistentString(){
		return persistentString;
	}
	
	@Override
	public DatarouterUserRole fromPersistentString(String s){
		return DataRouterEnumTool.getEnumFromString(values(), s, null);
	}
}

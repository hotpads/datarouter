package com.hotpads.handler.user.role;

import java.util.Set;

import com.hotpads.datarouter.storage.field.enums.DataRouterEnumTool;
import com.hotpads.datarouter.storage.field.enums.StringEnum;
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
	
	public static Set<DatarouterUserRole> fromStringArray(String[] userRoles) {
		Set<DatarouterUserRole> userRolesSet = SetTool.wrap(DatarouterUserRole.user);
		for(String role : userRoles) {
			userRolesSet.add(DataRouterEnumTool.getEnumFromString(values(), role, DatarouterUserRole.user));
		}
		return userRolesSet;
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

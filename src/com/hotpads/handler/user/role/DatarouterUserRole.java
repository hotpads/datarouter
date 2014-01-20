package com.hotpads.handler.user.role;

import com.hotpads.datarouter.storage.field.enums.DataRouterEnumTool;
import com.hotpads.datarouter.storage.field.enums.StringEnum;

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
	
	@Override
	public String getPersistentString(){
		return persistentString;
	}
	
	@Override
	public DatarouterUserRole fromPersistentString(String s){
		return DataRouterEnumTool.getEnumFromString(values(), s, null);
	}
	
}

package com.hotpads.handler.user.authenticate;

import java.util.Collection;
import java.util.List;

import com.hotpads.datarouter.storage.field.enums.DataRouterEnumTool;
import com.hotpads.datarouter.storage.field.enums.StringEnum;
import com.hotpads.util.core.CollectionTool;
import com.hotpads.util.core.ListTool;

public enum DatarouterUserRole
implements StringEnum<DatarouterUserRole>{

	admin("admin"),
	user("user"),
	anonymous("anonymous");
	
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
		return DataRouterEnumTool.getEnumFromString(values(), s, anonymous);
	}
	
}

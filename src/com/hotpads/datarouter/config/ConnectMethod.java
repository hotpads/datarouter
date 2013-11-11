package com.hotpads.datarouter.config;

import com.hotpads.datarouter.storage.field.enums.DataRouterEnumTool;
import com.hotpads.datarouter.storage.field.enums.IntegerEnum;
import com.hotpads.datarouter.storage.field.enums.StringEnum;

/*
 * Method for obtaining a database connection from a client
 */
public enum ConnectMethod implements StringEnum<ConnectMethod>, IntegerEnum<ConnectMethod>{

	tryExisting(20, "tryExisting"),  //same as Participation.supports, usually the default
	requireExisting(21, "requireExisting"),
	tryExistingHandle(22, "tryExistingHandle"),
	requireExistingHandle(23, "requireExistingHandle"),
	
	requireNew(24, "requireNew"),
	;

	
	private int persistentInteger;
	private String persistentString;
	
	private ConnectMethod(int persistentInteger, String persistentString){
		this.persistentInteger = persistentInteger;
		this.persistentString = persistentString;
	}
	
	
	/***************************** IntegerEnum methods ******************************/
	
	@Override
	public Integer getPersistentInteger(){
		return persistentInteger;
	}
	
	@Override
	public ConnectMethod fromPersistentInteger(Integer i){
		return DataRouterEnumTool.getEnumFromInteger(values(), i, null);
	}
	
	
	/****************************** StringEnum methods *********************************/
	
	@Override
	public String getPersistentString(){
		return persistentString;
	}
	
	@Override
	public ConnectMethod fromPersistentString(String s){
		return DataRouterEnumTool.getEnumFromString(values(), s, null);
	}
	
}

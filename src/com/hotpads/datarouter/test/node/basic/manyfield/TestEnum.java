package com.hotpads.datarouter.test.node.basic.manyfield;


import com.hotpads.datarouter.storage.field.enums.DataRouterEnumTool;
import com.hotpads.datarouter.storage.field.enums.IntegerEnum;
import com.hotpads.datarouter.storage.field.enums.StringEnum;

public enum TestEnum implements IntegerEnum<TestEnum>, StringEnum<TestEnum>{

	dog(19, "dog"), 
	cat(20, "cat"), 
	beast(21, "beast"), 
	fish(22, "fish");
	
	int persistentInteger;
	String persistentString;
	
	private TestEnum(int persistentInteger, String persistentString){
		this.persistentInteger = persistentInteger;
		this.persistentString = persistentString;
	}
	
	
	/***************************** IntegerEnum methods ******************************/
	
	@Override
	public Integer getPersistentInteger(){
		return persistentInteger;
	}
	
	@Override
	public TestEnum fromPersistentInteger(Integer i){
		return DataRouterEnumTool.getEnumFromInteger(values(), i, null);
	}
	
	
	/****************************** StringEnum methods *********************************/
	
	@Override
	public String getPersistentString(){
		return persistentString;
	}
	
	@Override
	public TestEnum fromPersistentString(String s){
		return DataRouterEnumTool.getEnumFromString(values(), s, null);
	}
	
}

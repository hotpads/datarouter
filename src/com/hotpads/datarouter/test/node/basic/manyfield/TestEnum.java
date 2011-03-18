package com.hotpads.datarouter.test.node.basic.manyfield;


import com.hotpads.datarouter.storage.field.enums.DataRouterEnumTool;
import com.hotpads.datarouter.storage.field.enums.IntegerEnum;

public enum TestEnum implements IntegerEnum<TestEnum>{

	dog(19), cat(20), beast(21), fish(22);
	
	int persistentInteger;
	
	private TestEnum(int persistentInteger){
		this.persistentInteger = persistentInteger;
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
	
}

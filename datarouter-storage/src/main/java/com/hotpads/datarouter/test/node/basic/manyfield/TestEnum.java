package com.hotpads.datarouter.test.node.basic.manyfield;


import com.hotpads.util.core.enums.DatarouterEnumTool;
import com.hotpads.util.core.enums.IntegerEnum;
import com.hotpads.util.core.enums.StringEnum;

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
	public TestEnum fromPersistentInteger(Integer input){
		return DatarouterEnumTool.getEnumFromInteger(values(), input, null);
	}


	/****************************** StringEnum methods *********************************/

	@Override
	public String getPersistentString(){
		return persistentString;
	}

	@Override
	public TestEnum fromPersistentString(String input){
		return DatarouterEnumTool.getEnumFromString(values(), input, null);
	}

}

package com.hotpads.datarouter.storage.field.imp.comparable;

import com.hotpads.datarouter.storage.field.PrimitiveFieldKey;
import com.hotpads.datarouter.storage.field.encoding.FieldGeneratorType;
import com.hotpads.util.core.number.RandomTool;

public class IntegerFieldKey extends PrimitiveFieldKey<Integer>{

	public IntegerFieldKey(String name){
		super(name);
	}

	private IntegerFieldKey(String name, String columnName, boolean nullable, FieldGeneratorType fieldGeneratorType,
			Integer defaultValue){
		super(name, columnName, nullable, fieldGeneratorType, defaultValue);
	}

	public IntegerFieldKey withFieldGeneratorType(FieldGeneratorType fieldGeneratorTypeOverride){
		return new IntegerFieldKey(name, columnName, nullable, fieldGeneratorTypeOverride, defaultValue);
	}

	@Override
	public Integer generateRandomValue(){
		return RandomTool.nextPositiveInt();
	}

}

package com.hotpads.datarouter.storage.field.imp.comparable;

import com.hotpads.datarouter.storage.field.PrimitiveFieldKey;
import com.hotpads.datarouter.storage.field.encoding.FieldGeneratorType;

public class DoubleFieldKey extends PrimitiveFieldKey<Double>{

	public DoubleFieldKey(String name){
		super(name);
	}

	public DoubleFieldKey(String name, String columnName, boolean nullable, FieldGeneratorType fieldGeneratorType){
		super(name, columnName, nullable, fieldGeneratorType);
	}

}

package com.hotpads.datarouter.storage.field.imp.dumb;

import com.hotpads.datarouter.storage.field.PrimitiveFieldKey;
import com.hotpads.datarouter.storage.field.encoding.FieldGeneratorType;

public class DumbDoubleFieldKey extends PrimitiveFieldKey<Double>{

	public DumbDoubleFieldKey(String name){
		super(name, Double.class);
	}

	private DumbDoubleFieldKey(String name, String columnName, boolean nullable, FieldGeneratorType fieldGeneratorType,
			Double defaultValue){
		super(name, columnName, nullable, Double.class, fieldGeneratorType, defaultValue);
	}

	public DumbDoubleFieldKey withColumnName(String columnNameOverride){
		return new DumbDoubleFieldKey(name, columnNameOverride, nullable, fieldGeneratorType, defaultValue);
	}

}

package com.hotpads.datarouter.storage.field.imp.positive;

import com.hotpads.datarouter.storage.field.PrimitiveFieldKey;
import com.hotpads.datarouter.storage.field.encoding.FieldGeneratorType;

public class UInt31FieldKey extends PrimitiveFieldKey<Integer>{

	public UInt31FieldKey(String name){
		super(name);
	}

	private UInt31FieldKey(String name, String columnName, boolean nullable, FieldGeneratorType fieldGeneratorType,
			Integer defaultValue){
		super(name, columnName, nullable, fieldGeneratorType, defaultValue);
	}

	/*-------------------- with -------------------------*/

	public UInt31FieldKey withColumnName(String columnNameOverride){
		return new UInt31FieldKey(name, columnNameOverride, nullable, fieldGeneratorType, defaultValue);
	}

}

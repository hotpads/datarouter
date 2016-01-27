package com.hotpads.datarouter.storage.field.imp.comparable;

import com.hotpads.datarouter.storage.field.PrimitiveFieldKey;
import com.hotpads.datarouter.storage.field.encoding.FieldGeneratorType;

public class LongFieldKey extends PrimitiveFieldKey<Long>{

	public LongFieldKey(String name){
		super(name);
	}

	public LongFieldKey(String name, String columnName, boolean nullable,
			FieldGeneratorType fieldGeneratorType){
		super(name, columnName, nullable, fieldGeneratorType);
	}

}

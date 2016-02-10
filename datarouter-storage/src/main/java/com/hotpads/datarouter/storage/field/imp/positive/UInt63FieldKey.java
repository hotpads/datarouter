package com.hotpads.datarouter.storage.field.imp.positive;

import com.hotpads.datarouter.storage.field.PrimitiveFieldKey;
import com.hotpads.datarouter.storage.field.encoding.FieldGeneratorType;

public class UInt63FieldKey extends PrimitiveFieldKey<Long>{

	public UInt63FieldKey(String name){
		super(name);
	}

	public UInt63FieldKey(String name, FieldGeneratorType fieldGeneratorType){
		super(name, fieldGeneratorType);
	}

}

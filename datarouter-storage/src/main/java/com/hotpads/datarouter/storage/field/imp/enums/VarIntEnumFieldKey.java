package com.hotpads.datarouter.storage.field.imp.enums;

import com.hotpads.datarouter.storage.field.BaseFieldKey;
import com.hotpads.datarouter.storage.field.enums.IntegerEnum;

public class VarIntEnumFieldKey<E extends IntegerEnum<E>> extends BaseFieldKey<E>{
	
	public VarIntEnumFieldKey(String name){
		super(name);
	}
	
}
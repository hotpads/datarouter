package com.hotpads.datarouter.storage.field.imp;

import com.hotpads.datarouter.storage.field.PrimitiveField;
import com.hotpads.util.core.bytes.BooleanByteTool;

public class BooleanField extends PrimitiveField<Boolean>{

	public BooleanField(String name, Boolean value){
		super(name, value);
	}

	public BooleanField(String prefix, String name, Boolean value){
		super(prefix, name, value);
	}

	@Override
	public Boolean parseJdbcValueButDoNotSet(Object obj){
		return obj==null?null:(Boolean)obj;
	}

	@Override
	public byte[] getBytes(){
		return BooleanByteTool.getBytes(this.value);
	}
}

package com.hotpads.datarouter.storage.field.imp;

import com.hotpads.datarouter.storage.field.PrimitiveField;
import com.hotpads.util.core.bytes.IntegerByteTool;

public class IntegerField extends PrimitiveField<Integer>{

	public IntegerField(String name, Integer value){
		super(name, value);
	}

	public IntegerField(String prefix, String name, Integer value){
		super(prefix, name, value);
	}

	@Override
	public Integer parseJdbcValueButDoNotSet(Object obj){
		return obj==null?null:(Integer)obj;
	}

	@Override
	public byte[] getBytes(){
		return IntegerByteTool.getComparableByteArray(value);
	}
	
	
}

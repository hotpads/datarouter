package com.hotpads.datarouter.storage.field.imp;

import com.hotpads.datarouter.storage.field.PrimitiveField;
import com.hotpads.util.core.ByteTool;

public class ByteField extends PrimitiveField<Byte>{

	public ByteField(String name, Byte value){
		super(name, value);
	}

	public ByteField(String prefix, String name, Byte value){
		super(prefix, name, value);
	}

	@Override
	public Byte parseJdbcValueButDoNotSet(Object obj){
		return obj==null?null:(Byte)obj;
	}
	
	@Override
	public byte[] getBytes(){
		return ByteTool.getComparableByteArray(value);
	}
	

}

package com.hotpads.datarouter.storage.field.imp.comparable;

import com.hotpads.datarouter.storage.field.BasePrimitiveField;
import com.hotpads.datarouter.util.core.DrByteTool;
import com.hotpads.datarouter.util.core.DrStringTool;

//recognizes -128 to -1 using two's complement.  therefore max value is 127
public class SignedByteField extends BasePrimitiveField<Byte>{

	public SignedByteField(String name, Byte value){
		super(name, value);
	}

	public SignedByteField(String prefix, String name, Byte value){
		super(prefix, name, value);
	}
	
	
	/*********************** StringEncodedField ***********************/

	@Override
	public String getStringEncodedValue(){
		if(value==null){ return null; }
		return value.toString();
	}
	
	@Override
	public Byte parseStringEncodedValueButDoNotSet(String s){
		if(DrStringTool.isEmpty(s) || s.equals("null")){ return null; }
		return Byte.valueOf(s);
	}
	

	/*********************** ByteEncodedField ***********************/

	//recognizes -128 to -1 using two's complement.  therefore max value is 127
	@Override
	public byte[] getBytes(){
		return value==null?null:DrByteTool.getComparableBytes(value);
	}
	
	@Override
	public int numBytesWithSeparator(byte[] bytes, int offset){
		return 1;
	}
	
	@Override
	public Byte fromBytesButDoNotSet(byte[] bytes, int offset){
		return DrByteTool.getComparableByte(bytes[offset]);
	}

}

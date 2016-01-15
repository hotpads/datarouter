package com.hotpads.datarouter.storage.field.imp.positive;

import com.hotpads.datarouter.storage.field.BasePrimitiveField;
import com.hotpads.datarouter.util.core.DrByteTool;
import com.hotpads.datarouter.util.core.DrStringTool;

public class UInt8Field extends BasePrimitiveField<Byte>{

	public UInt8Field(String name, Integer intValue){
		super(name, DrByteTool.toUnsignedByte(intValue));
	}

	public UInt8Field(String prefix, String name, Integer intValue){
		super(prefix, name, DrByteTool.toUnsignedByte(intValue));
	}
	
	/************************ static *********************************/
	
//	protected static Integer checkRange(Integer value){
//		if(value==null){ return null; }
//		if(value < 0 || value > 255){ 
//			throw new IllegalArgumentException("UInt8 must be 0-255");
//		}
//		return value;
//	}

//	private static final Random random = new Random();
//
//	public static int nextPositiveRandom(){
//		
//	}
	
	
	/*********************** StringEncodedField ***********************/

	@Override
	public String getStringEncodedValue(){
		if(value==null){ return null; }
		return value.toString();
	}
	
	@Override
	public Byte parseStringEncodedValueButDoNotSet(String s){
		if(DrStringTool.isEmpty(s) || s.equals("null")){
			return null; 
		}
		return DrByteTool.toUnsignedByte(Integer.valueOf(s));
	}
	

	/*********************** ByteEncodedField ***********************/

	@Override
	public byte[] getBytes(){
		return value==null?null:new byte[]{value};
	}
	
	@Override
	public int numBytesWithSeparator(byte[] bytes, int offset){
		return 1;
	}
	
	@Override
	public Byte fromBytesButDoNotSet(byte[] bytes, int offset){
		return bytes[offset];
	}
	
}

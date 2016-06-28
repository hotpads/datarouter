package com.hotpads.datarouter.storage.field.imp.positive;

import com.hotpads.datarouter.storage.field.BasePrimitiveField;
import com.hotpads.datarouter.util.core.DrByteTool;
import com.hotpads.datarouter.util.core.DrStringTool;

public class UInt8Field extends BasePrimitiveField<Byte>{

	public UInt8Field(UInt8FieldKey key, Byte value){
		super(key, value);
	}

	@Deprecated
	public UInt8Field(String name, Integer intValue){
		this(new UInt8FieldKey(name), DrByteTool.toUnsignedByte(intValue));
	}

	/*********************** StringEncodedField ***********************/

	@Override
	public String getStringEncodedValue(){
		if(value == null){
			return null;
		}
		return value.toString();
	}

	@Override
	public Byte parseStringEncodedValueButDoNotSet(String str){
		if(DrStringTool.isEmpty(str) || "null".equals(str)){
			return null;
		}
		return DrByteTool.toUnsignedByte(Integer.valueOf(str));
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

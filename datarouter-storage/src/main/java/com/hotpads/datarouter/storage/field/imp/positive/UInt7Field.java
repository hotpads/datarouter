package com.hotpads.datarouter.storage.field.imp.positive;

import java.util.Random;

import com.hotpads.datarouter.storage.field.BasePrimitiveField;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.util.core.number.RandomTool;

public class UInt7Field extends BasePrimitiveField<Byte>{

	public UInt7Field(UInt7FieldKey key, Byte value){
		super(key, value);
	}

	@Deprecated
	public UInt7Field(String name, Byte value){
		this(new UInt7FieldKey(name), value);
	}

	/************************ static *********************************/

	private static final Random random = new Random();

	public static int nextPositiveRandom(){
		return RandomTool.nextPositiveByte(random);
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
		return Byte.valueOf(str);
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

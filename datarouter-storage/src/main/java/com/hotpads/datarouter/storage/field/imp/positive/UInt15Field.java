package com.hotpads.datarouter.storage.field.imp.positive;

import java.util.Random;

import com.hotpads.datarouter.storage.field.BasePrimitiveField;
import com.hotpads.datarouter.storage.field.PrimitiveFieldKey;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.util.core.bytes.ShortByteTool;
import com.hotpads.util.core.number.RandomTool;

public class UInt15Field extends BasePrimitiveField<Short>{

	public UInt15Field(PrimitiveFieldKey<Short> key, Short value){
		super(key, value);
	}

	@Deprecated
	public UInt15Field(String name, Short value){
		super(name, value);
	}

	@Deprecated
	public UInt15Field(String prefix, String name, Short value){
		super(prefix, name, value);
	}

	/************************ static *********************************/

	private static final Random random = new Random();

	public static int nextPositiveRandom(){
		return RandomTool.nextPositiveShort(random);
	}


	/*********************** StringEncodedField ***********************/

	@Override
	public String getStringEncodedValue(){
		if(value==null){ return null; }
		return value.toString();
	}

	@Override
	public Short parseStringEncodedValueButDoNotSet(String s){
		if(DrStringTool.isEmpty(s) || s.equals("null")){ return null; }
		return Short.valueOf(s);
	}


	/*********************** ByteEncodedField ***********************/

	@Override
	public byte[] getBytes(){
		return value==null?null:ShortByteTool.getUInt15Bytes(value);
	}

	@Override
	public int numBytesWithSeparator(byte[] bytes, int offset){
		return 2;
	}

	@Override
	public Short fromBytesButDoNotSet(byte[] bytes, int offset){
		return ShortByteTool.fromUInt15Bytes(bytes, offset);
	}

}

package com.hotpads.datarouter.storage.field.imp.positive;

import java.util.Random;

import com.hotpads.datarouter.storage.field.BasePrimitiveField;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.util.core.bytes.IntegerByteTool;
import com.hotpads.util.core.number.RandomTool;

public class UInt31Field extends BasePrimitiveField<Integer>{

	public UInt31Field(UInt31FieldKey key, Integer value){
		this(null, key, value);
	}

	public UInt31Field(String prefix, UInt31FieldKey key, Integer value){
		super(prefix, key, value);
	}

	@Deprecated
	public UInt31Field(String name, Integer value){
		this(null, name, value);
	}

	@Deprecated
	public UInt31Field(String prefix, String name, Integer value){
		this(prefix, new UInt31FieldKey(name), value);
	}

	/************************ static *********************************/

	private static final Random random = new Random();

	public static int nextPositiveRandom(){
		return RandomTool.nextPositiveInt(random);
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
	public Integer parseStringEncodedValueButDoNotSet(String str){
		if(DrStringTool.isEmpty(str) || "null".equals(str)){
			return null;
		}
		return Integer.valueOf(str);
	}

	/*********************** ByteEncodedField ***********************/

	@Override
	public byte[] getBytes(){
		return value == null ? null : IntegerByteTool.getUInt31Bytes(value);
	}

	@Override
	public int numBytesWithSeparator(byte[] bytes, int offset){
		return 4;
	}

	@Override
	public Integer fromBytesButDoNotSet(byte[] bytes, int offset){
		return IntegerByteTool.fromUInt31Bytes(bytes, offset);
	}

}

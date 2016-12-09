package com.hotpads.datarouter.storage.field.imp.positive;

import java.util.Random;

import com.hotpads.datarouter.storage.field.BasePrimitiveField;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.util.core.number.RandomTool;
import com.hotpads.util.core.number.VarInt;

public class VarIntField extends BasePrimitiveField<Integer>{

	public VarIntField(VarIntFieldKey key, Integer value){
		super(key, value);
	}

	@Deprecated
	public VarIntField(String name, Integer value){
		this(new VarIntFieldKey(name), assertInRange(value));
	}

	/************************ static *********************************/

	private static final Random random = new Random();

	public static int nextRandom(){
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
		return assertInRange(str == null ? null : Integer.valueOf(str));
	}

	/*********************** ByteEncodedField ***********************/

	@Override
	public byte[] getBytes(){
		return value == null ? null : new VarInt(value).getBytes();
	}

	@Override
	public int numBytesWithSeparator(byte[] bytes, int offset){
		return VarInt.fromByteArray(bytes, offset).getNumBytes();
	}

	@Override
	public Integer fromBytesButDoNotSet(byte[] bytes, int offset){
		return VarInt.fromByteArray(bytes, offset).getValue();
	}

	/***************************** validate *****************************************/

	public static Integer assertInRange(Integer value){
		if(value == null){
			return value;
		}
		if(value >= 0){
			return value;
		}
		throw new IllegalArgumentException("VarIntField must be null or positive integer");
	}

}

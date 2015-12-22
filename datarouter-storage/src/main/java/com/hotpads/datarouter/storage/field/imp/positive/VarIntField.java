package com.hotpads.datarouter.storage.field.imp.positive;

import java.util.Random;

import com.hotpads.datarouter.storage.field.BasePrimitiveField;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.util.core.number.RandomTool;
import com.hotpads.util.core.number.VarInt;

public class VarIntField extends BasePrimitiveField<Integer>{

	public VarIntField(String name, Integer value){
		super(name, assertInRange(value));
	}

	public VarIntField(String prefix, String name, Integer value){
		super(prefix, name, assertInRange(value));
	}
	
	/************************ static *********************************/

	private static final Random random = new Random();

	public static int nextRandom(){
		return RandomTool.nextPositiveInt(random);
	}
	
	
	/*********************** StringEncodedField ***********************/

	@Override
	public String getStringEncodedValue(){
		if(value==null){ return null; }
		return value.toString();
	}
	
	@Override
	public Integer parseStringEncodedValueButDoNotSet(String s){
		if(DrStringTool.isEmpty(s) || s.equals("null")){
			return null; 
		}
		return assertInRange(s==null?null:Integer.valueOf(s));
	}
	

	/*********************** ByteEncodedField ***********************/

	@Override
	public byte[] getBytes(){
		return value==null?null:new VarInt(value).getBytes();
	}
	
	@Override
	public int numBytesWithSeparator(byte[] bytes, int offset){
		return new VarInt(bytes, offset).getNumBytes();
	}
	
	@Override
	public Integer fromBytesButDoNotSet(byte[] bytes, int offset){
		return new VarInt(bytes, offset).getValue();
	}
	
	
	/***************************** validate *****************************************/
	
	public static Integer assertInRange(Integer i){
		if(i==null){ return i; }
		if(i >= 0){ return i; }
		throw new IllegalArgumentException("VarIntField must be null or positive integer");
	}

}

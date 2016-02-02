package com.hotpads.datarouter.storage.field.imp.comparable;

import com.hotpads.datarouter.storage.field.BasePrimitiveField;
import com.hotpads.datarouter.storage.field.PrimitiveFieldKey;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.util.core.bytes.IntegerByteTool;

public class IntegerField extends BasePrimitiveField<Integer>{

	public IntegerField(PrimitiveFieldKey<Integer> key, Integer value){
		super(key, value);
	}

	@Deprecated
	public IntegerField(String name, Integer value){
		super(name, value);
	}

	@Deprecated
	public IntegerField(String prefix, String name, Integer value){
		super(prefix, name, value);
	}


	/*********************** StringEncodedField ***********************/

	@Override
	public String getStringEncodedValue(){
		if(value==null){ return null; }
		return value.toString();
	}

	@Override
	public Integer parseStringEncodedValueButDoNotSet(String s){
		if(DrStringTool.isEmpty(s) || s.equals("null")){ return null; }
		return Integer.valueOf(s);
	}


	/*********************** ByteEncodedField ***********************/

	@Override
	public byte[] getBytes(){
		return value==null?null:IntegerByteTool.getComparableBytes(value);
	}

	@Override
	public int numBytesWithSeparator(byte[] bytes, int offset){
		return 4;
	}

	@Override
	public Integer fromBytesButDoNotSet(byte[] bytes, int offset){
		return IntegerByteTool.fromComparableBytes(bytes, offset);
	}

}

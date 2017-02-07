package com.hotpads.datarouter.storage.field.imp.comparable;

import com.hotpads.datarouter.storage.field.BasePrimitiveField;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.util.core.bytes.IntegerByteTool;

public class IntegerField extends BasePrimitiveField<Integer>{

	public IntegerField(IntegerFieldKey key, Integer value){
		this(null, key, value);
	}

	public IntegerField(String prefix, IntegerFieldKey key, Integer value){
		super(prefix, key, value);
	}

	@Deprecated
	public IntegerField(String name, Integer value){
		this(null, name, value);
	}

	@Deprecated
	public IntegerField(String prefix, String name, Integer value){
		this(prefix, new IntegerFieldKey(name), value);
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

package com.hotpads.datarouter.storage.field.imp.comparable;

import com.hotpads.datarouter.storage.field.BasePrimitiveField;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.util.core.bytes.ShortByteTool;

public class ShortField extends BasePrimitiveField<Short>{

	public ShortField(ShortFieldKey key, Short value){
		super(key, value);
	}

	@Deprecated
	public ShortField(String name, Short value){
		super(name, value);
	}

	@Deprecated
	public ShortField(String prefix, String name, Short value){
		super(prefix, name, value);
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
		return value==null?null:ShortByteTool.getComparableBytes(this.value);
	}

	@Override
	public int numBytesWithSeparator(byte[] bytes, int offset){
		return 2;
	}

	@Override
	public Short fromBytesButDoNotSet(byte[] bytes, int offset){
		return ShortByteTool.fromComparableBytes(bytes, offset);
	}

}

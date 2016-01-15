package com.hotpads.datarouter.storage.field.imp.comparable;

import com.hotpads.datarouter.storage.field.BasePrimitiveField;
import com.hotpads.datarouter.storage.field.PrimitiveFieldKey;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.util.core.bytes.BooleanByteTool;

public class BooleanField extends BasePrimitiveField<Boolean>{

	public BooleanField(PrimitiveFieldKey<Boolean> key, Boolean value){
		super(key, value);
	}

	public BooleanField(String name, Boolean value){
		this(name, value, null);
	}

	public BooleanField(String prefix, String name, Boolean value){
		super(prefix, name, value);
	}

	public BooleanField(String name, Boolean value, Boolean defaultValue){
		super(name, value, defaultValue);
	}

	/*********************** StringEncodedField ***********************/

	@Override
	public String getStringEncodedValue(){
		if(value==null){
			return null;
		}
		return value.toString();
	}

	@Override
	public Boolean parseStringEncodedValueButDoNotSet(String s){
		if(DrStringTool.isEmpty(s) || s.equals("null")){
			return null;
		}
		return Boolean.valueOf(s);
	}


	/*********************** ByteEncodedField ***********************/

	@Override
	public byte[] getBytes(){
		return value==null?null:BooleanByteTool.getBytes(value);
	}

	@Override
	public int numBytesWithSeparator(byte[] bytes, int offset){
		return 1;
	}

	@Override
	public Boolean fromBytesButDoNotSet(byte[] bytes, int offset){
		return BooleanByteTool.fromBytes(bytes, offset);
	}


}

package com.hotpads.datarouter.storage.field.imp.comparable;

import com.hotpads.datarouter.storage.field.BasePrimitiveField;
import com.hotpads.datarouter.util.core.DrBooleanTool;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.util.core.bytes.BooleanByteTool;

public class BooleanField extends BasePrimitiveField<Boolean>{

	public BooleanField(BooleanFieldKey key, Boolean value){
		super(key, value);
	}

	@Deprecated
	public BooleanField(String name, Boolean value){
		this(new BooleanFieldKey(name), value);
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
	public Boolean parseStringEncodedValueButDoNotSet(String str){
		if(DrStringTool.isEmpty(str) || "null".equals(str)){
			return null;
		}
		return DrBooleanTool.isTrue(str);
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

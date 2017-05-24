package com.hotpads.datarouter.storage.field.imp.array;

import java.util.List;

import com.google.common.reflect.TypeToken;
import com.hotpads.datarouter.storage.field.BaseListField;
import com.hotpads.util.core.bytes.BooleanByteTool;

public class BooleanArrayField extends BaseListField<Boolean,List<Boolean>>{

	public BooleanArrayField(BooleanArrayFieldKey key, List<Boolean> value){
		super(key, value);
	}

	@Deprecated
	public BooleanArrayField(String name, List<Boolean> value){
		super(name, value, new TypeToken<List<Boolean>>(){});
	}

	/*********************** StringEncodedField ******************************/

	@Override
	public List<Boolean> parseStringEncodedValueButDoNotSet(String value){
		return gson.fromJson(value, getKey().getValueType());
	}

	/*********************** ByteEncodedField ********************************/

	@Override
	public byte[] getBytes(){
		if(value == null){
			return null;
		}
		return BooleanByteTool.getBooleanByteArray(value);
	}

	@Override
	public List<Boolean> fromBytesButDoNotSet(byte[] bytes, int byteOffset){
		return BooleanByteTool.fromBooleanByteArray(bytes, byteOffset);
	}

	@Override
	public int numBytesWithSeparator(byte[] bytes, int byteOffset){
		// TODO Auto-generated method stub
		return 0;
	}
}

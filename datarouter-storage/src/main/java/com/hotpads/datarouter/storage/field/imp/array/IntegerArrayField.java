package com.hotpads.datarouter.storage.field.imp.array;

import java.util.List;

import com.google.common.reflect.TypeToken;
import com.hotpads.datarouter.storage.field.BaseListField;
import com.hotpads.util.core.bytes.IntegerByteTool;
import com.hotpads.util.core.exception.NotImplementedException;

public class IntegerArrayField extends BaseListField<Integer, List<Integer>>{

	public IntegerArrayField(IntegerArrayFieldKey key, List<Integer> value){
		super(key, value);
	}

	@Deprecated
	public IntegerArrayField(String name, List<Integer> value){
		super(name, value, new TypeToken<List<Integer>>(){});
	}

	/*********************** StringEncodedField ******************************/

	@Override
	public List<Integer> parseStringEncodedValueButDoNotSet(String value){
		return gson.fromJson(value, getKey().getValueType());
	}

	/*********************** ByteEncodedField ********************************/

	@Override
	public byte[] getBytes(){
		if(value == null){
			return null;
		}
		return IntegerByteTool.getIntegerByteArray(value);
	}

	@Override
	public List<Integer> fromBytesButDoNotSet(byte[] bytes, int byteOffset){
		return IntegerByteTool.fromIntegerByteArray(bytes, byteOffset);
	}

	@Override
	public int numBytesWithSeparator(byte[] bytes, int byteOffset){
		throw new NotImplementedException();//why isn't this implemented?
	}
}
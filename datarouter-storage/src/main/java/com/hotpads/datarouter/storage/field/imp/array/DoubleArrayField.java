package com.hotpads.datarouter.storage.field.imp.array;

import java.util.List;

import com.google.common.reflect.TypeToken;
import com.hotpads.datarouter.storage.field.BaseListField;
import com.hotpads.util.core.bytes.DoubleByteTool;

public class DoubleArrayField extends BaseListField<Double,List<Double>>{

	public DoubleArrayField(DoubleArrayFieldKey key, List<Double> value){
		super(key, value);
	}

	@Deprecated
	public DoubleArrayField(String name, List<Double> value){
		super(name, value, new TypeToken<List<Double>>(){});
	}

	/*********************** StringEncodedField ******************************/

	@Override
	public List<Double> parseStringEncodedValueButDoNotSet(String value){
		return gson.fromJson(value, getKey().getValueType());
	}

	/*********************** ByteEncodedField ********************************/

	@Override
	public byte[] getBytes(){
		if(value == null){
			return null;
		}
		return DoubleByteTool.getDoubleByteArray(value);
	}

	@Override
	public List<Double> fromBytesButDoNotSet(byte[] bytes, int byteOffset){
		return DoubleByteTool.fromDoubleByteArray(bytes, byteOffset);
	}

	@Override
	public int numBytesWithSeparator(byte[] bytes, int byteOffset){
		// TODO Auto-generated method stub
		return 0;
	}
}
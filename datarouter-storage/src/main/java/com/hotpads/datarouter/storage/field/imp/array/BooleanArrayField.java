package com.hotpads.datarouter.storage.field.imp.array;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hotpads.datarouter.storage.field.BaseListField;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.util.core.bytes.BooleanByteTool;

public class BooleanArrayField extends BaseListField<Boolean,List<Boolean>>{

	public BooleanArrayField(BooleanArrayFieldKey key, List<Boolean> value){
		super(key, value);
	}

	@Deprecated
	public BooleanArrayField(String name, List<Boolean> value){
		super(name, value);
	}

	@Deprecated
	public BooleanArrayField(String prefix, String name, List<Boolean> value){
		super(prefix, name, value);
	}


	/*********************** StringEncodedField ***********************/

	@Override
	public String getStringEncodedValue(){
		if(value == null){
			return null;
		}
		return new Gson().toJson(value);
	}

	@Override
	public List<Boolean> parseStringEncodedValueButDoNotSet(String value){
		return new Gson().fromJson(value, new TypeToken<List<Boolean>>(){}.getType());
	}


	/*********************** ByteEncodedField ***********************/

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


	/************************** main ***********************/

	public static void main(String[] args){
		BooleanArrayField testField = new BooleanArrayField("stuff", DrListTool.create(new Boolean(true), null, new Boolean(false)));
		for(Boolean num : testField.value){
			System.out.println(num);
		}
		byte[] bytes = testField.getBytes();
		List<Boolean> bools = testField.fromBytesButDoNotSet(bytes, 0);
		for(Boolean bool : bools){
			System.out.println(bool);
		}
	}

}

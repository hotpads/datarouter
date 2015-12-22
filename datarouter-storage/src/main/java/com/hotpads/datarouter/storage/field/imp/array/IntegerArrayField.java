package com.hotpads.datarouter.storage.field.imp.array;

import java.util.List;

import com.hotpads.datarouter.storage.field.BaseListField;
import com.hotpads.datarouter.storage.field.ListFieldKey;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.util.core.bytes.IntegerByteTool;
import com.hotpads.util.core.exception.NotImplementedException;

public class IntegerArrayField extends BaseListField<Integer, List<Integer>>{

	public IntegerArrayField(ListFieldKey<Integer, List<Integer>> key, List<Integer> value){
		super(key, value);
	}
	public IntegerArrayField(String name, List<Integer> value){
		super(name, value);
	}

	public IntegerArrayField(String prefix, String name, List<Integer> value){
		super(prefix, name, value);
	}


	/*********************** StringEncodedField ***********************/

	@Override
	public String getStringEncodedValue(){
		if(value==null){ return null; }
		//TODO to CSV format?
		throw new NotImplementedException();
	}

	@Override
	public List<Integer> parseStringEncodedValueButDoNotSet(String s){
		throw new NotImplementedException();
	}


	/*********************** ByteEncodedField ***********************/

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


	/********************* main ************************/

	public static void main(String[] args){
		IntegerArrayField testField = new IntegerArrayField("stuff", DrListTool.create(new Integer(-51), null, new Integer(2)));
		for(Integer num : testField.value){
			System.out.println(num);
		}
		byte[] bytes = testField.getBytes();
		List<Integer> integers = testField.fromBytesButDoNotSet(bytes, 0);
		for(Integer value : integers){
			System.out.println(value);
		}
	}


}

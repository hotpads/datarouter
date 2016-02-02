package com.hotpads.datarouter.storage.field.imp.array;

import java.util.List;

import com.hotpads.datarouter.storage.field.BaseListField;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.util.core.bytes.DoubleByteTool;
import com.hotpads.util.core.exception.NotImplementedException;


public class DoubleArrayField extends BaseListField<Double,List<Double>>{

	public DoubleArrayField(DoubleArrayFieldKey key, List<Double> value){
		super(key, value);
	}

	@Deprecated
	public DoubleArrayField(String name, List<Double> value){
		super(name, value);
	}

	@Deprecated
	public DoubleArrayField(String prefix, String name, List<Double> value){
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
	public List<Double> parseStringEncodedValueButDoNotSet(String s){
		throw new NotImplementedException();
	}


	/*********************** ByteEncodedField ***********************/

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


	public static void main(String[] args){
		DoubleArrayField testField = new DoubleArrayField("stuff", DrListTool.create(new Double(-5.00001), new Double(203920.555),  null));
		for(Double num : testField.value){
			System.out.println(num);
		}
		byte[] bytes = testField.getBytes();
		List<Double> doubles = testField.fromBytesButDoNotSet(bytes, 0);
		for(Double doub : doubles){
			System.out.println(doub);
		}
	}

}

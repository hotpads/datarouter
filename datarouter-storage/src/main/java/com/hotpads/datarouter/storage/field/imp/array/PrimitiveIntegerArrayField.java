package com.hotpads.datarouter.storage.field.imp.array;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.hotpads.datarouter.storage.field.BaseField;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.util.core.bytes.IntegerByteTool;
import com.hotpads.util.core.exception.NotImplementedException;

public class PrimitiveIntegerArrayField extends BaseField<int[]>{
	
	private PrimitiveIntegerArrayFieldKey key;

	public PrimitiveIntegerArrayField(String name, int[] value){
		this(null, name, value);
	}

	public PrimitiveIntegerArrayField(String prefix, String name, int[] value){
		super(prefix, value);
		this.key = new PrimitiveIntegerArrayFieldKey(name);
	}
	
	@Override
	public PrimitiveIntegerArrayFieldKey getKey(){
		return key;
	}

	@Override
	public String getValueString(){
		return String.valueOf(value);
	}

	@Override
	public int compareTo(Field<int[]> field){
		if(field == null){
			return 1;
		}
		return toString().compareTo(field.toString());
	}

	@Override
	public String getStringEncodedValue(){
		throw new NotImplementedException();
	}

	@Override
	public int[] parseStringEncodedValueButDoNotSet(String value){
		throw new NotImplementedException();
	}

	@Override
	public byte[] getBytes(){
		return IntegerByteTool.getComparableByteArray(value);
	}

	@Override
	public int[] fromBytesButDoNotSet(byte[] bytes, int byteOffset){
		return IntegerByteTool.fromComparableByteArray(bytes);
	}

	@Override
	public int numBytesWithSeparator(byte[] bytes, int byteOffset){
		throw new NotImplementedException();
	}
	
	
	/******************* test ************************/
	
	public static class Tests{
		@Test
		public void testByteSerialization(){
			int[] array = {1, 2, 100};
			PrimitiveIntegerArrayField field = new PrimitiveIntegerArrayField("test", array);
			Assert.assertEquals(field.fromBytesButDoNotSet(field.getBytes(), 0), array);
		}
	}

}

package com.hotpads.datarouter.storage.field.imp.array;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.base.Joiner;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.util.core.bytes.StringByteTool;
import com.hotpads.util.core.exception.NotImplementedException;

public class DelimitedStringArrayField extends KeyedListField<String,List<String>, DelimitedStringArrayFieldKey>{

	public DelimitedStringArrayField(DelimitedStringArrayFieldKey key, List<String> values){
		super(key, values);
	}

	public DelimitedStringArrayField(String name, String separator, List<String> values){
		super(new DelimitedStringArrayFieldKey(name, separator), values);
	}

	public DelimitedStringArrayField(String prefix, String name, String separator, List<String> values){
		super(prefix, new DelimitedStringArrayFieldKey(name, separator), values);
	}

	public String getSeparator(){
		return key.separator;
	}

	/*********************** StringEncodedField ***********************/

	@Override
	public String getStringEncodedValue(){
		return encode(value, key.separator);
	}

	@Override
	public List<String> parseStringEncodedValueButDoNotSet(String string){
		return decode(string, key.separator);
	}

	/*********************** ByteEncodedField ***********************/

	@Override
	public byte[] getBytes(){
		String encodedString = encode(value, key.separator);
		if(encodedString == null){
			return null;
		}
		return StringByteTool.getUtf8Bytes(encodedString);
	}

	@Override
	public List<String> fromBytesButDoNotSet(byte[] bytes, int byteOffset){
		if(bytes == null){
			return null;
		}
		String encodedString = StringByteTool.fromUtf8Bytes(bytes);
		return decode(encodedString, key.separator);
	}

	@Override
	public int numBytesWithSeparator(byte[] bytes, int byteOffset){
		throw new NotImplementedException();
	}

	/********************* methods ***********************/

	public static String encode(List<String> inputs, String separator){
		if(DrCollectionTool.isEmpty(inputs)){
			return null;
		}
		for(String input : inputs){
			if(input == null){
				throw new IllegalArgumentException("nulls not supported");
			}
			if(input.contains(separator)){
				throw new IllegalArgumentException("strings cannot contain separator");
			}
		}
		return Joiner.on(separator).join(inputs);
	}

	public static List<String> decode(String input, String separator){
		if(input == null){
			return null;
		}
		return DrListTool.create(input.split(separator));
	}

	/********************* tests ************************/

	public static class Tests{
		@Test
		public void testRoundTrip(){
			List<String> inputs = DrListTool.createArrayList("abc", "xyz", "def");
			String encoded = encode(inputs, ",");
			Assert.assertEquals("abc,xyz,def", encoded);
			List<String> decoded = decode(encoded, ",");
			Assert.assertEquals(inputs, decoded);
		}
	}

}

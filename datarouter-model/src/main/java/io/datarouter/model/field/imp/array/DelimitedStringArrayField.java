/**
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.model.field.imp.array;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.util.bytes.StringByteTool;
import io.datarouter.util.collection.CollectionTool;
import io.datarouter.util.collection.ListTool;
import io.datarouter.util.exception.NotImplementedException;

public class DelimitedStringArrayField extends KeyedListField<String,List<String>, DelimitedStringArrayFieldKey>{

	public DelimitedStringArrayField(DelimitedStringArrayFieldKey key, List<String> values){
		super(key, values);
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
		if(CollectionTool.isEmpty(inputs)){
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
		return String.join(separator, inputs);
	}

	public static List<String> decode(String input, String separator){
		if(input == null){
			return null;
		}
		return ListTool.create(input.split(separator));
	}

	/********************* tests ************************/

	public static class Tests{
		@Test
		public void testRoundTrip(){
			List<String> inputs = ListTool.createArrayList("abc", "xyz", "def");
			String encoded = encode(inputs, ",");
			Assert.assertEquals(encoded, "abc,xyz,def");
			List<String> decoded = decode(encoded, ",");
			Assert.assertEquals(decoded, inputs);
		}
	}

}

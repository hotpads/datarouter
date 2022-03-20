/*
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
package io.datarouter.model.field.imp.list;

import java.util.ArrayList;
import java.util.List;

import io.datarouter.bytes.codec.stringcodec.StringCodec;
import io.datarouter.scanner.Scanner;

public class DelimitedStringListField extends KeyedListField<String,List<String>, DelimitedStringListFieldKey>{

	public DelimitedStringListField(DelimitedStringListFieldKey key, List<String> values){
		super(key, values);
	}

	public String getSeparator(){
		return key.separator;
	}

	@Override
	public String getStringEncodedValue(){
		return encode(value, key.separator);
	}

	@Override
	public List<String> parseStringEncodedValueButDoNotSet(String string){
		return decode(string, key.separator);
	}

	@Override
	public byte[] getBytes(){
		String encodedString = encode(value, key.separator);
		if(encodedString == null){
			return null;
		}
		return StringCodec.UTF_8.encode(encodedString);
	}

	@Override
	public List<String> fromBytesButDoNotSet(byte[] bytes, int byteOffset){
		if(bytes == null){
			return null;
		}
		String encodedString = StringCodec.UTF_8.decode(bytes);
		return decode(encodedString, key.separator);
	}

	@Override
	public int numBytesWithSeparator(byte[] bytes, int byteOffset){
		throw new UnsupportedOperationException();
	}

	public static String encode(List<String> inputs, String separator){
		if(inputs == null){
			return null;
		}
		if(inputs.isEmpty()){
			return "";
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
		if("".equals(input)){
			return new ArrayList<>();
		}
		return Scanner.of(input.split(separator)).list();
	}

}

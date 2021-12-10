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
package io.datarouter.bytes;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class StringByteTool{

	private static final int NULL_LENGTH = 0;

	public static byte[] getUtf8Bytes(String str){
		if(str == null){
			return null;
		}
		return str.getBytes(StandardCharsets.UTF_8);
	}

	public static byte[] getNullableStringByteArray(List<String> valuesWithNulls){
		if(valuesWithNulls == null){
			throw new RuntimeException("String list cannot be null");
		}
		List<byte[]> byteArrays = new ArrayList<>(valuesWithNulls.size());
		// prepend the size of the string list at the very beginning so that when decoding, we can use it to set the
		// ArrayList capacity
		byteArrays.add(VarIntTool.encode(valuesWithNulls.size()));
		for(int i = 0; i < valuesWithNulls.size(); i++){
			String str = valuesWithNulls.get(i);
			byte[] stringByteValue = str == null ? new byte[0] : getUtf8Bytes(str);
			// byteLen=0 means null, byteLen=1 represents an empty string, byteLen=2 means the string is length 1, etc.
			// (byteLen equals 1+stringLength)
			byte[] byteLen = VarIntTool.encode(str == null ? NULL_LENGTH : stringByteValue.length + 1);
			byteArrays.add(ByteTool.concatenate(byteLen, stringByteValue));
		}
		return ByteTool.concatenate(byteArrays);
	}

	public static List<String> fromNullableStringByteArray(byte[] values){
		int position = 0;
		int listLength = VarIntTool.decodeInt(values, position);
		position += VarIntTool.length(listLength);
		List<String> strings = new ArrayList<>(listLength);

		while(position < values.length){
			int stringLength = VarIntTool.decodeInt(values, position);
			position += VarIntTool.length(stringLength);
			if(stringLength == NULL_LENGTH){
				strings.add(null);
				continue;
			}
			byte[] stringBytes = ByteTool.copyOfRange(values, position, stringLength - 1);
			position += stringBytes.length;
			String string = fromUtf8Bytes(stringBytes);
			strings.add(string);
		}
		return strings;
	}

	public static int toUtf8Bytes(String str, byte[] destination, int offset){
		byte[] bytes = getUtf8Bytes(str);
		System.arraycopy(bytes, 0, destination, offset, bytes.length);
		return bytes.length;
	}

	public static String fromUtf8Bytes(byte[] bytes, int offset, int length){
		return new String(bytes, offset, length, StandardCharsets.UTF_8);
	}

	public static String fromUtf8Bytes(byte[] bytes){
		return new String(bytes, StandardCharsets.UTF_8);
	}

}

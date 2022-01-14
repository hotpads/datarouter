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
package io.datarouter.bytes.codec.array.bytearray;

import io.datarouter.bytes.EmptyArray;
import io.datarouter.bytes.codec.bytecodec.ComparableByteCodec;

public class ComparableByteArrayCodec{

	public static final ComparableByteArrayCodec INSTANCE = new ComparableByteArrayCodec();

	private static final ComparableByteCodec COMPARABLE_BYTE_CODEC = ComparableByteCodec.INSTANCE;
	private static final int ITEM_LENGTH = COMPARABLE_BYTE_CODEC.length();

	public int itemLength(){
		return ITEM_LENGTH;
	}

	public byte[] encode(byte[] values){
		if(values.length == 0){
			return EmptyArray.BYTE;
		}
		byte[] bytes = new byte[values.length];
		encode(values, bytes, 0);
		return bytes;
	}

	public int encode(byte[] values, byte[] bytes, int offset){
		int cursor = offset;
		for(int i = 0; i < values.length; ++i){
			COMPARABLE_BYTE_CODEC.encode(values[i], bytes, cursor);
			cursor += ITEM_LENGTH;
		}
		return values.length;
	}

	public byte[] decode(byte[] bytes){
		return decode(bytes, 0, bytes.length);
	}

	public byte[] decode(byte[] bytes, int offset, int bytesLength){
		if(bytesLength == 0){
			return EmptyArray.BYTE;
		}
		int resultLength = bytesLength;
		byte[] result = new byte[resultLength];
		int cursor = offset;
		for(int i = 0; i < resultLength; ++i){
			result[i] = COMPARABLE_BYTE_CODEC.decode(bytes, cursor);
			++cursor;
		}
		return result;
	}

}

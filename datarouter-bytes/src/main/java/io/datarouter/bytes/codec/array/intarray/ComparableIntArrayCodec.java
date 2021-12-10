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
package io.datarouter.bytes.codec.array.intarray;

import io.datarouter.bytes.EmptyArray;
import io.datarouter.bytes.codec.intcodec.ComparableIntCodec;

public class ComparableIntArrayCodec{

	public static final ComparableIntArrayCodec INSTANCE = new ComparableIntArrayCodec();

	private static final ComparableIntCodec COMPARABLE_INT_CODEC = ComparableIntCodec.INSTANCE;
	private static final int LENGTH = COMPARABLE_INT_CODEC.length();

	public byte[] encode(int[] values){
		if(values.length == 0){
			return EmptyArray.BYTE;
		}
		byte[] bytes = new byte[LENGTH * values.length];
		encode(values, bytes, 0);
		return bytes;
	}

	public int encode(int[] values, byte[] bytes, int offset){
		int cursor = offset;
		for(int i = 0; i < values.length; ++i){
			COMPARABLE_INT_CODEC.encode(values[i], bytes, cursor);
			cursor += LENGTH;
		}
		return values.length * LENGTH;
	}

	public int[] decode(byte[] bytes){
		if(bytes == null){ //TODO remove null check
			return EmptyArray.INT;
		}
		return decode(bytes, 0, bytes.length);
	}

	public int[] decode(byte[] bytes, int offset, int bytesLength){
		if(bytesLength == 0){
			return EmptyArray.INT;
		}
		if(bytesLength % LENGTH != 0){
			throw new IllegalArgumentException("bytesLength must be multiple of " + LENGTH);
		}
		int resultLength = bytesLength / LENGTH;
		int[] result = new int[resultLength];
		int cursor = offset;
		for(int i = 0; i < resultLength; ++i){
			result[i] = COMPARABLE_INT_CODEC.decode(bytes, cursor);
			cursor += LENGTH;
		}
		return result;
	}

}

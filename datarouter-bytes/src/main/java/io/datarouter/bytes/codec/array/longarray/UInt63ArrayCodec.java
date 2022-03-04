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
package io.datarouter.bytes.codec.array.longarray;

import io.datarouter.bytes.EmptyArray;
import io.datarouter.bytes.codec.longcodec.UInt63Codec;

@Deprecated//use ComparableLongArrayCodec or custom codec
public class UInt63ArrayCodec{

	public static final UInt63ArrayCodec INSTANCE = new UInt63ArrayCodec();

	private static final UInt63Codec U_INT_63_CODEC = UInt63Codec.INSTANCE;
	private static final int LENGTH = U_INT_63_CODEC.length();

	public byte[] encode(long[] values){
		if(values.length == 0){
			return EmptyArray.BYTE;
		}
		var bytes = new byte[LENGTH * values.length];
		encode(values, bytes, 0);
		return bytes;
	}

	public int encode(long[] values, byte[] bytes, int offset){
		int cursor = offset;
		for(int i = 0; i < values.length; ++i){
			U_INT_63_CODEC.encode(values[i], bytes, cursor);
			cursor += LENGTH;
		}
		return values.length * LENGTH;
	}

	public long[] decode(byte[] bytes){
		if(bytes == null){ //TODO remove null check
			return EmptyArray.LONG;
		}
		return decode(bytes, 0, bytes.length);
	}

	public long[] decode(byte[] bytes, int offset, int bytesLength){
		if(bytesLength == 0){
			return EmptyArray.LONG;
		}
		if(bytesLength % LENGTH != 0){
			throw new IllegalArgumentException("bytesLength must be multiple of " + LENGTH);
		}
		int resultLength = bytesLength / LENGTH;
		var result = new long[resultLength];
		int cursor = offset;
		for(int i = 0; i < resultLength; ++i){
			result[i] = U_INT_63_CODEC.decode(bytes, cursor);
			cursor += LENGTH;
		}
		return result;
	}

}

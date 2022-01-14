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
package io.datarouter.bytes.codec.array.floatarray;

import io.datarouter.bytes.EmptyArray;
import io.datarouter.bytes.codec.floatcodec.ComparableFloatCodec;

public class ComparableFloatArrayCodec{

	public static final ComparableFloatArrayCodec INSTANCE = new ComparableFloatArrayCodec();

	private static final ComparableFloatCodec COMPARABLE_FLOAT_CODEC = ComparableFloatCodec.INSTANCE;
	private static final int ITEM_LENGTH = COMPARABLE_FLOAT_CODEC.length();

	public int itemLength(){
		return ITEM_LENGTH;
	}

	public byte[] encode(float[] values){
		if(values.length == 0){
			return EmptyArray.BYTE;
		}
		byte[] bytes = new byte[ITEM_LENGTH * values.length];
		encode(values, bytes, 0);
		return bytes;
	}

	public int encode(float[] values, byte[] bytes, int offset){
		int cursor = offset;
		for(int i = 0; i < values.length; ++i){
			COMPARABLE_FLOAT_CODEC.encode(values[i], bytes, cursor);
			cursor += ITEM_LENGTH;
		}
		return values.length * ITEM_LENGTH;
	}

	public float[] decode(byte[] bytes){
		return decode(bytes, 0, bytes.length);
	}

	public float[] decode(byte[] bytes, int offset, int bytesLength){
		if(bytesLength == 0){
			return EmptyArray.FLOAT;
		}
		if(bytesLength % ITEM_LENGTH != 0){
			throw new IllegalArgumentException("bytesLength must be multiple of " + ITEM_LENGTH);
		}
		int resultLength = bytesLength / ITEM_LENGTH;
		float[] result = new float[resultLength];
		int cursor = offset;
		for(int i = 0; i < resultLength; ++i){
			result[i] = COMPARABLE_FLOAT_CODEC.decode(bytes, cursor);
			cursor += ITEM_LENGTH;
		}
		return result;
	}

}

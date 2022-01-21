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
package io.datarouter.bytes.codec.floatcodec;

import io.datarouter.bytes.codec.intcodec.ComparableIntCodec;

public class ComparableFloatCodec{

	public static final ComparableFloatCodec INSTANCE = new ComparableFloatCodec();

	private static final ComparableIntCodec COMPARABLE_INT_CODEC = ComparableIntCodec.INSTANCE;
	private static final int LENGTH = COMPARABLE_INT_CODEC.length();

	public int length(){
		return LENGTH;
	}

	public byte[] encode(float value){
		var bytes = new byte[LENGTH];
		encode(value, bytes, 0);
		return bytes;
	}

	public int encode(float value, byte[] bytes, int offset){
		int intBits = Float.floatToRawIntBits(value);
		if(intBits < 0){
			intBits ^= Integer.MAX_VALUE;
		}
		return COMPARABLE_INT_CODEC.encode(intBits, bytes, offset);
	}

	public float decode(byte[] bytes){
		return decode(bytes, 0);
	}

	public float decode(byte[] bytes, int offset){
		int intBits = COMPARABLE_INT_CODEC.decode(bytes, offset);
		if(intBits < 0){
			intBits ^= Integer.MAX_VALUE;
		}
		return Float.intBitsToFloat(intBits);
	}

}

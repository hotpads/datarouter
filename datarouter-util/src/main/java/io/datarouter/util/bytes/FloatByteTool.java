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
package io.datarouter.util.bytes;

public class FloatByteTool{

	public static byte[] getBytes(float in){
		int bits = Float.floatToIntBits(in);
		byte[] out = new byte[4];
		out[0] = (byte) (bits >>> 24);
		out[1] = (byte) (bits >>> 16);
		out[2] = (byte) (bits >>> 8);
		out[3] = (byte) bits;
		return out;
	}

	public static float fromBytes(byte[] bytes, int startIdx){
		int bits = ((bytes[startIdx] & 0xff) << 24)
				| ((bytes[startIdx + 1] & 0xff) << 16)
				| ((bytes[startIdx + 2] & 0xff) << 8)
				| (bytes[startIdx + 3] & 0xff);
		return Float.intBitsToFloat(bits);
	}

	public static byte[] toComparableBytes(float value){
		int intBits = Float.floatToRawIntBits(value);
		if(intBits < 0){
			intBits ^= Integer.MAX_VALUE;
		}
		return IntegerByteTool.getComparableBytes(intBits);
	}

	public static float fromComparableBytes(byte[] comparableBytes, int offset){
		int intBits = IntegerByteTool.fromComparableBytes(comparableBytes, offset);
		if(intBits < 0){
			intBits ^= Integer.MAX_VALUE;
		}
		return Float.intBitsToFloat(intBits);
	}

}

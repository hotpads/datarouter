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

public class RawFloatCodec{

	public static final RawFloatCodec INSTANCE = new RawFloatCodec();

	private static final int LENGTH = 4;

	public int length(){
		return LENGTH;
	}

	public byte[] encode(float value){
		var bytes = new byte[LENGTH];
		encode(value, bytes, 0);
		return bytes;
	}

	public int encode(float value, byte[] bytes, int offset){
		int bits = Float.floatToIntBits(value);
		bytes[offset] = (byte) (bits >>> 24);
		bytes[offset + 1] = (byte) (bits >>> 16);
		bytes[offset + 2] = (byte) (bits >>> 8);
		bytes[offset + 3] = (byte) bits;
		return LENGTH;
	}

	public float decode(byte[] bytes){
		return decode(bytes, 0);
	}

	public float decode(byte[] bytes, int offset){
		int bits = ((bytes[offset] & 0xff) << 24)
				| ((bytes[offset + 1] & 0xff) << 16)
				| ((bytes[offset + 2] & 0xff) << 8)
				| (bytes[offset + 3] & 0xff);
		return Float.intBitsToFloat(bits);
	}

}

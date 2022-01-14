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
package io.datarouter.bytes.codec.charcodec;

public class ComparableCharCodec{

	public static final ComparableCharCodec INSTANCE = new ComparableCharCodec();

	private static final int LENGTH = Character.BYTES;

	public int length(){
		return LENGTH;
	}

	public byte[] encode(char value){
		byte[] bytes = new byte[LENGTH];
		encode(value, bytes, 0);
		return bytes;
	}

	public int encode(char value, byte[] bytes, int offset){
		bytes[offset] = (byte)(value >>> 8);
		bytes[offset + 1] = (byte)value;
		return LENGTH;
	}

	public char decode(byte[] bytes){
		return decode(bytes, 0);
	}

	public char decode(byte[] bytes, int offset){
		byte left = bytes[offset];
		byte right = bytes[offset + 1];
		int intValue = (left << 8) + (right & 0xff);
		return (char) intValue;
	}

}

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
package io.datarouter.bytes.codec.booleancodec;

public class RawBooleanCodec{

	public static final RawBooleanCodec INSTANCE = new RawBooleanCodec();

	private static final int LENGTH = 1;
	private static final byte TRUE = -1;
	private static final byte FALSE = 0;

	public final int length(){
		return LENGTH;
	}

	public byte[] encode(boolean value){
		byte[] bytes = new byte[LENGTH];
		encode(value, bytes, 0);
		return bytes;
	}

	public int encode(boolean value, byte[] bytes, int offset){
		bytes[offset] = value ? TRUE : FALSE;
		return LENGTH;
	}

	public boolean decode(byte[] bytes){
		return decode(bytes, 0);
	}

	public boolean decode(byte[] bytes, int offset){
		return bytes[offset] != FALSE;
	}

}

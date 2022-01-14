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
package io.datarouter.bytes.codec.bytecodec;

import io.datarouter.bytes.ByteTool;

public class ComparableByteCodec{

	public static final ComparableByteCodec INSTANCE = new ComparableByteCodec();

	private static final int LENGTH = Byte.BYTES;

	public int length(){
		return LENGTH;
	}

	public byte[] encode(byte value){
		byte encodedValue = ByteTool.getComparableByte(value);
		return new byte[]{encodedValue};
	}

	public int encode(byte value, byte[] bytes, int offset){
		bytes[offset] = ByteTool.getComparableByte(value);
		return LENGTH;
	}

	public byte decode(byte[] bytes, int offset){
		byte encodedValue = bytes[offset];
		return ByteTool.getComparableByte(encodedValue);
	}

}

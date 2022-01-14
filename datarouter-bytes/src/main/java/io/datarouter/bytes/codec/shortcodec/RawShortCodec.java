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
package io.datarouter.bytes.codec.shortcodec;

public class RawShortCodec{

	public static final RawShortCodec INSTANCE = new RawShortCodec();

	private static final int LENGTH = 2;

	public int length(short value){
		return LENGTH;
	}

	public byte[] encode(short value){
		byte[] bytes = new byte[LENGTH];
		encode(value, bytes, 0);
		return bytes;
	}

	public int encode(short value, byte[] bytes, int offset){
		bytes[offset] = (byte) (value >>> 8);
		bytes[offset + 1] = (byte) value;
		return LENGTH;
	}

	public short decode(byte[] bytes){
		return decode(bytes, 0);
	}

	public short decode(byte[] bytes, int offset){
		return (short)(
				(bytes[offset] & 0xff) << 8
						| bytes[offset + 1] & 0xff);
	}

}

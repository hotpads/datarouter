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
package io.datarouter.bytes.codec.intcodec;

public class RawIntCodec implements IntCodec{

	public static final RawIntCodec INSTANCE = new RawIntCodec();

	private static final int LENGTH = 4;

	@Override
	public int length(int value){
		return LENGTH;
	}

	@Override
	public byte[] encode(int value){
		byte[] bytes = new byte[LENGTH];
		encode(value, bytes, 0);
		return bytes;
	}

	@Override
	public int encode(int value, byte[] bytes, int offset){
		bytes[offset] = (byte) (value >>> 24);
		bytes[offset + 1] = (byte) (value >>> 16);
		bytes[offset + 2] = (byte) (value >>> 8);
		bytes[offset + 3] = (byte) value;
		return LENGTH;
	}

	@Override
	public int decode(byte[] bytes, int offset){
		return (bytes[offset] & 0xff) << 24
				| (bytes[offset + 1] & 0xff) << 16
				| (bytes[offset + 2] & 0xff) << 8
				| bytes[offset + 3] & 0xff;
	}

}

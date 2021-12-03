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
package io.datarouter.bytes.codec.longcodec;

public class RawLongCodec implements LongCodec{

	public static final RawLongCodec INSTANCE = new RawLongCodec();

	private static final int LENGTH = 8;

	@Override
	public int length(long value){
		return LENGTH;
	}

	@Override
	public byte[] encode(long value){
		byte[] bytes = new byte[LENGTH];
		encode(value, bytes, 0);
		return bytes;
	}

	@Override
	public int encode(long value, byte[] bytes, int offset){
		bytes[offset] = (byte) (value >>> 56);
		bytes[offset + 1] = (byte) (value >>> 48);
		bytes[offset + 2] = (byte) (value >>> 40);
		bytes[offset + 3] = (byte) (value >>> 32);
		bytes[offset + 4] = (byte) (value >>> 24);
		bytes[offset + 5] = (byte) (value >>> 16);
		bytes[offset + 6] = (byte) (value >>> 8);
		bytes[offset + 7] = (byte) value;
		return LENGTH;
	}

	@Override
	public long decode(byte[] bytes, int offset){
		return (bytes[offset] & (long)0xff) << 56
				| (bytes[offset + 1] & (long)0xff) << 48
				| (bytes[offset + 2] & (long)0xff) << 40
				| (bytes[offset + 3] & (long)0xff) << 32
				| (bytes[offset + 4] & (long)0xff) << 24
				| (bytes[offset + 5] & (long)0xff) << 16
				| (bytes[offset + 6] & (long)0xff) << 8
				| bytes[offset + 7] & (long)0xff;
	}

}

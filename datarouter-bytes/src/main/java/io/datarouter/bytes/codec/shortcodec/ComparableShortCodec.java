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

public class ComparableShortCodec implements ShortCodec{

	public static final ComparableShortCodec INSTANCE = new ComparableShortCodec();

	private static final int LENGTH = 2;
	private static final RawShortCodec RAW_CODEC = RawShortCodec.INSTANCE;

	@Override
	public int length(short value){
		return LENGTH;
	}

	@Override
	public byte[] encode(short value){
		byte[] bytes = new byte[LENGTH];
		encode(value, bytes, 0);
		return bytes;
	}

	@Override
	public int encode(short value, byte[] bytes, int offset){
		int shifted = value ^ Short.MIN_VALUE;
		return RAW_CODEC.encode((short)shifted, bytes, offset);
	}

	@Override
	public short decode(byte[] bytes, int offset){
		return (short)(Short.MIN_VALUE ^ RAW_CODEC.decode(bytes, offset));
	}

}

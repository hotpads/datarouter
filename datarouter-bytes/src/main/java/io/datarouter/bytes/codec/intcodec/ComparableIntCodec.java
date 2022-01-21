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

public class ComparableIntCodec{

	public static final ComparableIntCodec INSTANCE = new ComparableIntCodec();

	private static final int LENGTH = 4;
	private static final RawIntCodec RAW_CODEC = RawIntCodec.INSTANCE;

	public int length(){
		return LENGTH;
	}

	public byte[] encode(int value){
		var bytes = new byte[LENGTH];
		encode(value, bytes, 0);
		return bytes;
	}

	public int encode(int value, byte[] bytes, int offset){
		int shifted = value ^ Integer.MIN_VALUE;
		return RAW_CODEC.encode(shifted, bytes, offset);
	}

	public int decode(byte[] bytes){
		return decode(bytes, 0);
	}

	public int decode(byte[] bytes, int offset){
		return Integer.MIN_VALUE ^ RAW_CODEC.decode(bytes, offset);
	}

}

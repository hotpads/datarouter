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
package io.datarouter.bytes.codec.doublecodec;

import io.datarouter.bytes.codec.longcodec.RawLongCodec;

public class RawDoubleCodec{

	public static final RawDoubleCodec INSTANCE = new RawDoubleCodec();

	private static final int LENGTH = 8;
	private static final RawLongCodec RAW_LONG_CODEC = RawLongCodec.INSTANCE;

	public int length(){
		return LENGTH;
	}

	public byte[] encode(double value){
		var bytes = new byte[LENGTH];
		encode(value, bytes, 0);
		return bytes;
	}

	public int encode(double value, byte[] bytes, int offset){
		long longValue = Double.doubleToLongBits(value);
		RAW_LONG_CODEC.encode(longValue, bytes, offset);
		return LENGTH;
	}

	public double decode(byte[] bytes){
		return decode(bytes, 0);
	}

	public double decode(byte[] bytes, int offset){
		long longValue = RAW_LONG_CODEC.decode(bytes, offset);
		return Double.longBitsToDouble(longValue);
	}

}

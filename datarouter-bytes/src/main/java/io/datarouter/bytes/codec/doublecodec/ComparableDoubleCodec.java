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

import io.datarouter.bytes.codec.longcodec.ComparableLongCodec;

public class ComparableDoubleCodec implements DoubleCodec{

	public static final ComparableDoubleCodec INSTANCE = new ComparableDoubleCodec();

	private static final int LENGTH = 8;
	private static final ComparableLongCodec COMPARABLE_LONG_CODEC = ComparableLongCodec.INSTANCE;

	public int length(){
		return LENGTH;
	}

	@Override
	public byte[] encode(double value){
		byte[] bytes = new byte[LENGTH];
		encode(value, bytes, 0);
		return bytes;
	}

	@Override
	public int encode(double value, byte[] bytes, int offset){
		long longValue = Double.doubleToLongBits(value);
		if(longValue < 0){
			longValue ^= Long.MAX_VALUE;
		}
		return COMPARABLE_LONG_CODEC.encode(longValue, bytes, offset);
	}

	@Override
	public double decode(byte[] bytes, int offset){
		long longBits = COMPARABLE_LONG_CODEC.decode(bytes, offset);
		if(longBits < 0){
			longBits ^= Long.MAX_VALUE;
		}
		return Double.longBitsToDouble(longBits);
	}

}

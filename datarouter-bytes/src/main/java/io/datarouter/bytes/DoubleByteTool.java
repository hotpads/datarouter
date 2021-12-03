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
package io.datarouter.bytes;

import java.util.ArrayList;
import java.util.List;

import io.datarouter.bytes.codec.doublecodec.ComparableDoubleCodec;
import io.datarouter.bytes.codec.doublecodec.RawDoubleCodec;

public class DoubleByteTool{

	private static final long NaN = 0x0010000000000000L;
	private static final RawDoubleCodec RAW_CODEC = RawDoubleCodec.INSTANCE;
	private static final ComparableDoubleCodec COMPARABLE_CODEC = ComparableDoubleCodec.INSTANCE;

	public static byte[] toComparableBytes(double value){
		return COMPARABLE_CODEC.encode(value);
	}

	public static double fromComparableBytes(byte[] comparableBytes, int offset){
		return COMPARABLE_CODEC.decode(comparableBytes, offset);
	}

	private static Double fromBytesNullable(byte[] bytes, int offset){
		Long longValue = LongByteTool.fromRawBytes(bytes, offset);
		if(longValue.longValue() == NaN){
			return null;
		}
		return Double.longBitsToDouble(longValue);
	}

	private static byte[] getBytesNullable(Double in){
		if(in == null){
			return getBytes(Double.longBitsToDouble(NaN));
		}
		return getBytes(in);
	}

	public static byte[] getBytes(double in){
		return RAW_CODEC.encode(in);
	}

	public static int toBytes(double in, byte[] bytes, int offset){
		return RAW_CODEC.encode(in, bytes, offset);
	}

	public static double fromBytes(byte[] bytes, int offset){
		return RAW_CODEC.decode(bytes, offset);
	}

	public static List<Double> fromDoubleByteArray(byte[] bytes, int startIdx){
		int numDoubles = (bytes.length - startIdx) / 8;
		List<Double> doubles = new ArrayList<>();
		byte[] arrayToCopy = new byte[8];
		for(int i = 0; i < numDoubles; i++){
			System.arraycopy(bytes, i * 8 + startIdx, arrayToCopy, 0, 8);
			doubles.add(fromBytesNullable(arrayToCopy, 0));
		}
		return doubles;
	}

	public static byte[] getDoubleByteArray(List<Double> valuesWithNulls){
		if(valuesWithNulls == null){
			return null;
		}
		byte[] out = new byte[8 * valuesWithNulls.size()];
		for(int i = 0; i < valuesWithNulls.size(); ++i){
			System.arraycopy(getBytesNullable(valuesWithNulls.get(i)), 0, out, i * 8, 8);
		}
		return out;
	}

}

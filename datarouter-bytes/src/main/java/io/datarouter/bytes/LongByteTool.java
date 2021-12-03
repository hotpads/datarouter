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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.bytes.codec.longcodec.ComparableLongCodec;
import io.datarouter.bytes.codec.longcodec.RawLongCodec;

/*
 * methods for converting longs into bytes
 */
public class LongByteTool{
	private static final Logger logger = LoggerFactory.getLogger(LongByteTool.class);

	private static final RawLongCodec RAW_CODEC = RawLongCodec.INSTANCE;
	private static final ComparableLongCodec COMPARABLE_CODEC = ComparableLongCodec.INSTANCE;

	/*------------------------- serialize to bytes --------------------------*/

	public static byte[] getRawBytes(long in){
		return RAW_CODEC.encode(in);
	}

	public static int toRawBytes(long in, byte[] bytes, int offset){
		return RAW_CODEC.encode(in, bytes, offset);
	}

	public static long fromRawBytes(byte[] bytes, int byteOffset){
		return RAW_CODEC.decode(bytes, byteOffset);
	}


	// int64 -- flip first bit so bitwiseCompare is always correct

	/*------------------------- single values -------------------------------*/

	public static byte[] getComparableBytes(long value){
		return COMPARABLE_CODEC.encode(value);
	}

	public static int toComparableBytes(long value, byte[] bytes, int offset){
		return COMPARABLE_CODEC.encode(value, bytes, offset);
	}

	public static Long fromComparableBytes(byte[] bytes, int byteOffset){
		return COMPARABLE_CODEC.decode(bytes, byteOffset);
	}

	/*------------------------- arrays and collections ----------------------*/

	public static byte[] getComparableByteArray(List<Long> valuesWithNulls){
		if(valuesWithNulls == null){
			return EmptyArray.BYTE;
		}
		LongArray values;
		if(valuesWithNulls instanceof LongArray){
			values = (LongArray)valuesWithNulls;
		}else{
			values = new LongArray(valuesWithNulls);
		}
		byte[] out = new byte[8 * values.size()];
		for(int i = 0; i < values.size(); ++i){
			System.arraycopy(getComparableBytes(values.getPrimitive(i)), 0, out, i * 8, 8);
		}
		return out;
	}


	public static byte[] getComparableByteArray(long[] values){
		byte[] out = new byte[8 * values.length];
		for(int i = 0; i < values.length; ++i){
			System.arraycopy(getComparableBytes(values[i]), 0, out, i * 8, 8);
		}
		return out;
	}

	public static long[] fromComparableByteArray(byte[] bytes){
		if(bytes == null || bytes.length == 0){ //TODO remove null check
			return EmptyArray.LONG;
		}
		return fromComparableByteArray(bytes, 0, bytes.length);
	}

	private static long[] fromComparableByteArray(byte[] bytes, int startIdx, int length){
		long[] out = new long[length / 8];
		int byteOffset = startIdx;
		for(int i = 0; i < out.length; ++i){
			out[i] = Long.MIN_VALUE ^ fromRawBytes(bytes, byteOffset);
			byteOffset += 8;
		}
		return out;
	}

	// uInt63 -- first bit must be 0, reject others

	/*------------------------- single values -------------------------------*/

	public static byte[] getUInt63Bytes(long value){
		if(value < 0 && value != Long.MIN_VALUE){//need to allow Long.MIN_VALUE in for nulls
			logger.warn("", new IllegalArgumentException("no negatives: " + value));
		}
		return getRawBytes(value);
	}

	public static long fromUInt63Bytes(byte[] bytes, int byteOffset){
		long longValue = fromRawBytes(bytes, byteOffset);
		if(longValue < 0 && longValue != Long.MIN_VALUE){
			logger.warn("", new IllegalArgumentException("no negatives: " + longValue));
		}
		return longValue;
	}

	/*------------------------- arrays and collections ----------------------*/

	public static byte[] getUInt63ByteArray(List<Long> valuesWithNulls){
		if(valuesWithNulls == null){
			return EmptyArray.BYTE;
		}
		LongArray values;
		if(valuesWithNulls instanceof LongArray){
			values = (LongArray)valuesWithNulls;
		}else{
			values = new LongArray(valuesWithNulls);
		}
		byte[] out = new byte[8 * values.size()];
		for(int i = 0; i < values.size(); ++i){
			System.arraycopy(getUInt63Bytes(values.getPrimitive(i)), 0, out, i * 8, 8);
		}
		return out;
	}


	public static long[] fromUInt63ByteArray(byte[] bytes){
		if(bytes == null || bytes.length == 0){ //TODO remove null check
			return EmptyArray.LONG;
		}
		return fromUInt63ByteArray(bytes, 0, bytes.length);
	}

	public static long[] fromUInt63ByteArray(byte[] bytes, int startIdx, int length){
		long[] out = new long[length / 8];
		int byteOffset = startIdx;
		for(int i = 0; i < out.length; ++i){
			out[i] = fromUInt63Bytes(bytes, byteOffset);
			byteOffset += 8;
		}
		return out;
	}

}

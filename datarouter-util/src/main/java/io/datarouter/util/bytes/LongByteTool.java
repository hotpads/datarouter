/**
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
package io.datarouter.util.bytes;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.util.array.ArrayTool;
import io.datarouter.util.array.LongArray;

/*
 * methods for converting longs into bytes
 */
public class LongByteTool{
	private static final Logger logger = LoggerFactory.getLogger(LongByteTool.class);

	/*------------------------- serialize to bytes --------------------------*/

	public static byte[] getRawBytes(final long in){
		byte[] out = new byte[8];
		out[0] = (byte) (in >>> 56);
		out[1] = (byte) (in >>> 48);
		out[2] = (byte) (in >>> 40);
		out[3] = (byte) (in >>> 32);
		out[4] = (byte) (in >>> 24);
		out[5] = (byte) (in >>> 16);
		out[6] = (byte) (in >>> 8);
		out[7] = (byte) in;
		return out;
	}

	public static int toRawBytes(long in, byte[] bytes, int offset){
		bytes[offset] = (byte) (in >>> 56);
		bytes[offset + 1] = (byte) (in >>> 48);
		bytes[offset + 2] = (byte) (in >>> 40);
		bytes[offset + 3] = (byte) (in >>> 32);
		bytes[offset + 4] = (byte) (in >>> 24);
		bytes[offset + 5] = (byte) (in >>> 16);
		bytes[offset + 6] = (byte) (in >>> 8);
		bytes[offset + 7] = (byte) in;
		return 8;
	}

	public static long fromRawBytes(byte[] bytes, int byteOffset){
		return (bytes[byteOffset] & (long)0xff) << 56
		| (bytes[byteOffset + 1] & (long)0xff) << 48
		| (bytes[byteOffset + 2] & (long)0xff) << 40
		| (bytes[byteOffset + 3] & (long)0xff) << 32
		| (bytes[byteOffset + 4] & (long)0xff) << 24
		| (bytes[byteOffset + 5] & (long)0xff) << 16
		| (bytes[byteOffset + 6] & (long)0xff) << 8
		| bytes[byteOffset + 7] & (long)0xff;
	}


	// int64 -- flip first bit so bitwiseCompare is always correct

	/*------------------------- single values -------------------------------*/

	public static byte[] getComparableBytes(long value){
		long shifted = value ^ Long.MIN_VALUE;
		return getRawBytes(shifted);
	}

	public static int toComparableBytes(long value, byte[] bytes, int offset){
		long shifted = value ^ Long.MIN_VALUE;
		return toRawBytes(shifted, bytes, offset);
	}

	public static Long fromComparableBytes(byte[] bytes, int byteOffset){
		return Long.MIN_VALUE ^ fromRawBytes(bytes, byteOffset);
	}

	/*------------------------- arrays and collections ----------------------*/

	public static byte[] getComparableByteArray(List<Long> valuesWithNulls){
		if(valuesWithNulls == null){
			return new byte[0];
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

	public static long[] fromComparableByteArray(final byte[] bytes){
		if(ArrayTool.isEmpty(bytes)){
			return new long[0];
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
			return new byte[0];
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


	public static long[] fromUInt63ByteArray(final byte[] bytes){
		if(ArrayTool.isEmpty(bytes)){
			return new long[0];
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

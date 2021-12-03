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
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.bytes.codec.intcodec.ComparableIntCodec;
import io.datarouter.bytes.codec.intcodec.RawIntCodec;

/**
 * methods for converting ints into bytes
 */
public class IntegerByteTool{
	private static final Logger logger = LoggerFactory.getLogger(IntegerByteTool.class);

	private static final int NULL = Integer.MIN_VALUE;
	private static final RawIntCodec RAW_CODEC = RawIntCodec.INSTANCE;
	private static final ComparableIntCodec COMPARABLE_CODEC = ComparableIntCodec.INSTANCE;
	/*
	 * int32
	 *
	 * flip first bit so bitwiseCompare is always correct
	 */
	public static byte[] getRawBytes(int in){
		return RAW_CODEC.encode(in);
	}

	/**
	 * @return numBytes written
	 */
	public static int toRawBytes(int in, byte[] bytes, int offset){
		return RAW_CODEC.encode(in, bytes, offset);
	}

	public static int fromRawBytes(byte[] bytes, int offset){
		return RAW_CODEC.decode(bytes, offset);
	}

	private static byte[] getBytesNullable(Integer value){
		if(value == null){
			return getComparableBytes(NULL);
		}
		return getComparableBytes(value);
	}

	private static Integer fromBytesNullable(byte[] bytes, int offset){
		Integer fromBytes = fromComparableBytes(bytes, offset);
		if(fromBytes == NULL){
			return null;
		}
		return fromBytes;
	}

	public static byte[] getComparableBytes(int value){
		return COMPARABLE_CODEC.encode(value);
	}

	public static int toComparableBytes(int value, byte[] bytes, int offset){
		return COMPARABLE_CODEC.encode(value, bytes, offset);
	}

	public static int fromComparableBytes(byte[] bytes, int byteOffset){
		return COMPARABLE_CODEC.decode(bytes, byteOffset);
	}

	public static List<Integer> fromIntegerByteArray(byte[] bytes, int startIdx){
		int numIntegers = (bytes.length - startIdx) / 4;
		List<Integer> integers = new ArrayList<>();
		byte[] arrayToCopy = new byte[4];
		for(int i = 0; i < numIntegers; i++){
			System.arraycopy(bytes, i * 4 + startIdx, arrayToCopy, 0, 4);
			integers.add(fromBytesNullable(arrayToCopy, 0));
		}
		return integers;
	}

	public static byte[] getIntegerByteArray(List<Integer> valuesWithNulls){
		if(valuesWithNulls == null){
			return null;
		}
		byte[] out = new byte[4 * valuesWithNulls.size()];
		for(int i = 0; i < valuesWithNulls.size(); ++i){
			byte[] bytesNullable = getBytesNullable(valuesWithNulls.get(i));
			logger.debug(Arrays.toString(bytesNullable)); // fix java 9 jit
			System.arraycopy(bytesNullable, 0, out, i * 4, 4);
		}
		logger.info(Arrays.toString(out));
		return out;
	}

	public static byte[] getComparableByteArray(int[] values){
		byte[] out = new byte[4 * values.length];
		for(int i = 0; i < values.length; ++i){
			System.arraycopy(getComparableBytes(values[i]), 0, out, i * 4, 4);
		}
		return out;
	}

	public static int[] fromComparableByteArray(byte[] bytes){
		if(bytes == null || bytes.length == 0){ //TODO remove null check
			return EmptyArray.INT;
		}
		int[] out = new int[bytes.length / 4];
		for(int i = 0; i < out.length; ++i){
			int startIdx = i * 4;

			// i think the first bitwise operation causes the operand to be zero-padded
			// to an integer before the operation happens
			// parenthesis are extremely important here because of the automatic int upgrading

			//more compact
			out[i] = Integer.MIN_VALUE ^ (
					(bytes[startIdx] & 0xff) << 24
					| (bytes[startIdx + 1] & 0xff) << 16
					| (bytes[startIdx + 2] & 0xff) << 8
					| bytes[startIdx + 3] & 0xff);
		}
		return out;
	}

	/*
	 * uInt31
	 *
	 * first bit must be 0, reject others
	 */
	public static byte[] getUInt31Bytes(int value){
		byte[] out = new byte[4];
		out[0] = (byte) (value >>> 24);
		out[1] = (byte) (value >>> 16);
		out[2] = (byte) (value >>> 8);
		out[3] = (byte) value;
		return out;
	}

	public static int fromUInt31Bytes(byte[] bytes, int startIdx){
		return (bytes[startIdx] & 0xff) << 24
				| (bytes[startIdx + 1] & 0xff) << 16
				| (bytes[startIdx + 2] & 0xff) << 8
				| bytes[startIdx + 3] & 0xff;
	}

}

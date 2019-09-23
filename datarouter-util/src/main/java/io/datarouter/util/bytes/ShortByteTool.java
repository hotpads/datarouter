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

/*
 * methods for converting shorts into bytes
 */
public class ShortByteTool{

	/*
	 * int16
	 *
	 * flip first bit so bitwiseCompare is always correct
	 */
	private static byte[] getRawBytes(final short in){
		byte[] out = new byte[2];
		out[0] = (byte) (in >>> 8);
		out[1] = (byte) in;
		return out;
	}

	private static int toRawBytes(short in, byte[] bytes, int offset){
		bytes[offset] = (byte) (in >>> 8);
		bytes[offset + 1] = (byte) in;
		return 2;
	}

	private static short fromRawBytes(byte[] bytes, int startIdx){
		return (short)(
				 (bytes[startIdx] & 0xff) << 8
				| bytes[startIdx + 1] & 0xff);
	}

	public static byte[] getComparableBytes(final short value){
		int shifted = value ^ Short.MIN_VALUE;
		return getRawBytes((short)shifted);
	}

	public static int toComparableBytes(short value, byte[] bytes, int offset){
		int shifted = value ^ Short.MIN_VALUE;
		return toRawBytes((short)shifted, bytes, offset);
	}

	public static short fromComparableBytes(byte[] bytes, int byteOffset){
		return (short)(Short.MIN_VALUE ^ fromRawBytes(bytes, byteOffset));
	}

	/*
	 * uInt31
	 *
	 * first bit must be 0, reject others
	 */
	public static byte[] getUInt15Bytes(short value){
		byte[] out = new byte[2];
		out[0] = (byte) (value >>> 8);
		out[1] = (byte) value;
		return out;
	}

	public static short fromUInt15Bytes(byte[] bytes, int startIdx){
		return (short)(
			 (bytes[startIdx + 0] & 0xff) << 8
			| bytes[startIdx + 1] & 0xff);
	}

}

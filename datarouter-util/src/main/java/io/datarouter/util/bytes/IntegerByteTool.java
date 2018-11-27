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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.util.array.ArrayTool;

/*
 * methods for converting ints into bytes
 */
public class IntegerByteTool{
	private static final int NULL = Integer.MIN_VALUE;

	/*
	 * int32
	 *
	 * flip first bit so bitwiseCompare is always correct
	 */

	public static byte[] getRawBytes(final int in){
		byte[] out = new byte[4];
		out[0] = (byte) (in >>> 24);
		out[1] = (byte) (in >>> 16);
		out[2] = (byte) (in >>> 8);
		out[3] = (byte) in;
		return out;
	}

	/**
	 * @return numBytes written
	 */
	public static int toRawBytes(final int in, final byte[] bytes, final int offset){
		bytes[offset] = (byte) (in >>> 24);
		bytes[offset + 1] = (byte) (in >>> 16);
		bytes[offset + 2] = (byte) (in >>> 8);
		bytes[offset + 3] = (byte) in;
		return 4;
	}

	public static int fromRawBytes(final byte[] bytes, final int offset){
		return (bytes[offset] & 0xff) << 24
		| (bytes[offset + 1] & 0xff) << 16
		| (bytes[offset + 2] & 0xff) << 8
		| bytes[offset + 3] & 0xff;
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

	public static byte[] getComparableBytes(final int value){
		int shifted = value ^ Integer.MIN_VALUE;
		return getRawBytes(shifted);
	}

	public static int toComparableBytes(final int value, final byte[] bytes, final int offset){
		int shifted = value ^ Integer.MIN_VALUE;
		return toRawBytes(shifted, bytes, offset);
	}

	public static int fromComparableBytes(final byte[] bytes, int byteOffset){
		return Integer.MIN_VALUE ^ fromRawBytes(bytes, byteOffset);
	}

	public static List<Integer> fromIntegerByteArray(final byte[] bytes, final int startIdx){
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
			System.arraycopy(getBytesNullable(valuesWithNulls.get(i)), 0, out, i * 4, 4);
		}
		return out;
	}

	public static byte[] getComparableByteArray(int[] values){
		byte[] out = new byte[4 * values.length];
		for(int i = 0; i < values.length; ++i){
			System.arraycopy(getComparableBytes(values[i]), 0, out, i * 4, 4);
		}
		return out;
	}

	public static int[] fromComparableByteArray(final byte[] bytes){
		if(ArrayTool.isEmpty(bytes)){
			return new int[0];
		}
		int[] out = new int[bytes.length / 4];
		for(int i = 0; i < out.length; ++i){
			int startIdx = i * 4;

			/*
			 * i think the first bitwise operation causes the operand to be zero-padded
			 *     to an integer before the operation happens
			 *
			 * parenthesis are extremely important here because of the automatic int upgrading
			 */

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

	public static byte[] getUInt31Bytes(final int value){
//		if(value < 0){ throw new IllegalArgumentException("no negatives"); }
		byte[] out = new byte[4];
		out[0] = (byte) (value >>> 24);
		out[1] = (byte) (value >>> 16);
		out[2] = (byte) (value >>> 8);
		out[3] = (byte) value;
		return out;
	}

	public static int fromUInt31Bytes(final byte[] bytes, final int startIdx){
		return
		  (bytes[startIdx] & 0xff) << 24
		| (bytes[startIdx + 1] & 0xff) << 16
		| (bytes[startIdx + 2] & 0xff) << 8
		| bytes[startIdx + 3] & 0xff;
	}

	/*------------------------- tests ---------------------------------------*/

	public static class Tests{
		//verify that -128 in bytes gets converted to -128 long.  Bitwise cast would be +128
		@Test
		public void testCasting(){
			byte b0 = 0, b1 = 1, b127 = 127, bn128 = -128, bn1 = -1;
			Assert.assertEquals(b0, 0L);
			Assert.assertEquals(b1, 1L);
			Assert.assertEquals(b127, 127L);
			Assert.assertEquals(bn128, -128L);
			Assert.assertEquals(bn1, -1L);
		}

		@Test
		public void testGetOrderedBytes(){
			int intA = Integer.MIN_VALUE;
			byte[] ab = new byte[]{0,0,0,0};
			Assert.assertEquals(getComparableBytes(intA), ab);

			int intB = Integer.MAX_VALUE;
			byte[] bb = new byte[]{-1,-1,-1,-1};
			Assert.assertEquals(getComparableBytes(intB), bb);

			int intC = Integer.MIN_VALUE + 1;
			byte[] cb = new byte[]{0,0,0,1};
			byte[] cout = getComparableBytes(intC);
			Assert.assertEquals(cout, cb);

			int intD = Integer.MAX_VALUE - 3;
			byte[] db = new byte[]{-1,-1,-1,-4};
			Assert.assertEquals(getComparableBytes(intD), db);

			int intE = 0;
			byte[] eb = new byte[]{-128,0,0,0};
			Assert.assertEquals(getComparableBytes(intE), eb);
		}

		@Test
		public void testArrays(){
			byte[] p5 = getComparableBytes(5);
			byte[] n3 = getComparableBytes(-3);
			byte[] n7 = getComparableBytes(-7);
			Assert.assertTrue(ByteTool.bitwiseCompare(p5, n3) > 0);
			Assert.assertTrue(ByteTool.bitwiseCompare(p5, n7) > 0);
		}

		@Test
		public void testRoundTrip(){
			int[] subjects = new int[]{
					Integer.MIN_VALUE,Integer.MIN_VALUE + 1,
					0,1,127,128,
					Integer.MAX_VALUE - 1,Integer.MAX_VALUE};
			for(int subject : subjects){
				byte[] bytes = getComparableBytes(subject);
				int roundTripped = fromComparableByteArray(bytes)[0];
				Assert.assertEquals(roundTripped, subject);
			}
		}

		@Test
		public void testRoundTrips(){
			Random random = new Random();
			int intValue = Integer.MIN_VALUE;
			byte[] lastBytes = getComparableBytes(intValue);
			++intValue;
			int counter = 0;
			for(; intValue < Integer.MAX_VALUE / 2; intValue += 1 + Math.abs(random.nextInt() % 53 * 47 * 991)){
				byte[] bytes = getComparableBytes(intValue);
				int roundTripped = fromComparableByteArray(bytes)[0];
				try{
					Assert.assertTrue(ByteTool.bitwiseCompare(lastBytes, bytes) < 0);
					Assert.assertEquals(roundTripped, intValue);
				}catch(AssertionError e){
					throw e;
				}
				lastBytes = bytes;
				++counter;
			}
			Assert.assertTrue(counter > 1000);//make sure we did a lot of tests
		}

		@Test
		public void testToFromByteArray(){
			int one = -239483;
			int two = 583;

			List<Integer> integers = new ArrayList<>();
			integers.add(one);
			integers.add(null);
			integers.add(two);

			byte[] integerBytes = getIntegerByteArray(integers);
			List<Integer> result = fromIntegerByteArray(integerBytes, 0);
			Assert.assertEquals(result, integers);
		}
	}
}

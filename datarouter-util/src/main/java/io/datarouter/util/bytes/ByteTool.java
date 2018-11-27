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

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.util.array.ArrayTool;
import io.datarouter.util.collection.CollectionTool;

public class ByteTool{

	public static ArrayList<Byte> getArrayList(byte[] ins){
		ArrayList<Byte> outs = new ArrayList<>(ArrayTool.length(ins));
		for(byte in : ArrayTool.nullSafe(ins)){
			outs.add(in);
		}
		return outs;
	}

	public static byte toUnsignedByte(final int intValue){
		// Assert.assertTrue(i >=0 && i <=255);
		// if(i < 128){ return (byte)i; }
		int ib = intValue - 128;
		return (byte)ib;// subtract 256
	}

	// not really sure what this method means anymore
	public static byte fromUnsignedInt0To255(int unsignedIntValue){
		if(unsignedIntValue > 127){
			return (byte)(unsignedIntValue - 0x100);// subtract 256
		}
		return (byte)unsignedIntValue;
	}

	public static int bitwiseCompare(byte[] bytesA, byte[] bytesB){
		int lengthA = ArrayTool.length(bytesA);
		int lengthB = ArrayTool.length(bytesB);
		for(int i = 0, j = 0; i < lengthA && j < lengthB; ++i, ++j){
			// need to trick the built in byte comparator which treats 10000000 < 00000000 because it's negative
			int byteA = bytesA[i] & 0xff; // boost the "negative" numbers up to 128-255
			int byteB = bytesB[j] & 0xff;
			if(byteA != byteB){
				return byteA - byteB;
			}
		}
		return lengthA - lengthB;
	}

	public static int bitwiseCompare(byte[] bytesA, int offsetA, int lengthA, byte[] bytesB, int offsetB, int lengthB){
		for(int i = offsetA, j = offsetB; i < offsetA + lengthA && j < offsetB + lengthB; ++i, ++j){
			// need to trick the built in byte comparator which treats 10000000 < 00000000 because it's negative
			int byteA = bytesA[i] & 0xff; // boost the "negative" numbers up to 128-255
			int byteB = bytesB[j] & 0xff;
			if(byteA != byteB){
				return byteA - byteB;
			}
		}
		return lengthA - lengthB;
	}

	public static boolean equals(byte[] bytesA, int offsetA, int lengthA, byte[] bytesB, int offsetB, int lengthB){
		if(lengthA != lengthB){
			return false;
		}
		for(int i = offsetA + lengthA - 1, j = offsetB + lengthB - 1; i >= 0 && j >= 0; --i, --j){
			if(bytesA[i] != bytesB[j]){
				return false;
			}
		}
		return true;
	}

	public static byte[] getComparableBytes(byte value){
		if(value >= 0){
			return new byte[]{(byte)(value + Byte.MIN_VALUE)};
		}
		return new byte[]{(byte)(value - Byte.MIN_VALUE)};
	}

	public static byte getComparableByte(byte value){
		if(value >= 0){
			return (byte)(value + Byte.MIN_VALUE);
		}
		return (byte)(value - Byte.MIN_VALUE);
	}

	public static byte[] flipToAndFromComparableByteArray(byte[] ins){
		return flipToAndFromComparableByteArray(ins, 0, ins.length);
	}

	// basically a copyOfRange that also flips the bytes
	public static byte[] flipToAndFromComparableByteArray(byte[] ins, int offset, int length){
		byte[] outs = new byte[length];
		for(int i = 0; i < length; ++i){
			outs[i] = getComparableByte(ins[offset + i]);
		}
		return outs;
	}

	public static String getBinaryStringBigEndian(byte[] ba){
		StringBuilder sb = new StringBuilder();
		int len = ArrayTool.length(ba);
		for(int n = 0; n < len; ++n){
			for(int i = 7; i >= 0; --i){
				sb.append(ba[n] >> i & 1);
			}
		}
		return sb.toString();
	}

	public static String getHexString(byte[] bytes){
		StringBuilder sb = new StringBuilder(bytes.length * 2);
		for(byte word : bytes){
			String hex = Integer.toHexString(word & 0xff);
			if(hex.length() == 1){
				sb.append("0");
			}
			sb.append(hex);
		}
		return sb.toString();
	}

	public static byte[] copyOfRange(byte[] in, int offset, int length){
		byte[] out = new byte[length];
		System.arraycopy(in, offset, out, 0, length);
		return out;
	}

	public static byte[] unsignedIncrement(final byte[] in){
		byte[] copy = ArrayTool.clone(in);
		if(copy == null){
			throw new IllegalArgumentException("cannot increment null array");
		}
		for(int i = copy.length - 1; i >= 0; --i){
			if(copy[i] == -1){// -1 is all 1-bits, which is the unsigned maximum
				copy[i] = 0;
			}else{
				++copy[i];
				return copy;
			}
		}
		// we maxed out the array
		byte[] out = new byte[copy.length + 1];
		out[0] = 1;
		System.arraycopy(copy, 0, out, 1, copy.length);
		return out;
	}

	public static byte[] unsignedIncrementOverflowToNull(final byte[] in){
		byte[] out = ArrayTool.clone(in);
		for(int i = out.length - 1; i >= 0; --i){
			if(out[i] == -1){// -1 is all 1-bits, which is the unsigned maximum
				out[i] = 0;
			}else{
				++out[i];
				return out;
			}
		}
		return null;
	}

	/*------------------------- byte arrays ---------------------------------*/

	public static byte[] concatenate(byte[]... ins){
		if(ins == null){
			return new byte[0];
		}
		int totalLength = 0;
		for(int i = 0; i < ins.length; ++i){
			totalLength += ArrayTool.length(ins[i]);
		}
		byte[] out = new byte[totalLength];
		int startIndex = 0;
		for(int i = 0; i < ins.length; ++i){
			if(ins[i] == null){
				continue;
			}
			System.arraycopy(ins[i], 0, out, startIndex, ins[i].length);
			startIndex += ins[i].length;
		}
		return out;
	}

	public static byte[] padPrefix(final byte[] in, int finalWidth){
		byte[] out = new byte[finalWidth];
		int numPaddingBytes = finalWidth - in.length;
		System.arraycopy(in, 0, out, numPaddingBytes, in.length);
		return out;
	}

	/*------------------------- serialize -----------------------------------*/

	public static byte[] getUInt7Bytes(List<Byte> values){
		if(CollectionTool.isEmpty(values)){
			return new byte[0];
		}
		byte[] out = new byte[values.size()];
		int index = 0;
		for(Byte value : values){
			if(value < 0){
				throw new IllegalArgumentException("no negatives");
			}
			out[index] = value;
			++index;
		}
		return out;
	}

	public static byte[] fromUInt7ByteArray(byte[] bytes, int offset, int length){
		// validate?
		return copyOfRange(bytes, offset, length);
	}

	/*------------------------- tests ---------------------------------------*/

	public static class Tests{
		@Test
		public void testToUnsignedByte(){
			Assert.assertEquals(toUnsignedByte(0), -128);
			Assert.assertEquals(toUnsignedByte(127), -1);
			Assert.assertEquals(toUnsignedByte(128), 0);
			Assert.assertEquals(toUnsignedByte(129), 1);
			Assert.assertEquals(toUnsignedByte(255), 127);
		}

		@Test
		public void testFromUnsignedInt0To255(){
			Assert.assertEquals(fromUnsignedInt0To255(0), 0);
			Assert.assertEquals(fromUnsignedInt0To255(127), 127);
			Assert.assertEquals(fromUnsignedInt0To255(128), -128);
			Assert.assertEquals(fromUnsignedInt0To255(255), -1);
		}

		@Test
		public void testBitwiseCompare(){
			byte[] bytesA = new byte[]{1, -1};
			byte[] bytesB = new byte[]{-3};
			Assert.assertTrue(bitwiseCompare(bytesA, bytesB) < 0);// positive numbers come before negative when bitwise
			Assert.assertTrue(bitwiseCompare(bytesA, 1, 1, bytesB, 0, 1) > 0);// -1 is after -3
		}

		@Test
		public void testEquals(){
			byte[] a1 = new byte[]{1, -1};
			byte[] b1 = new byte[]{-3};
			Assert.assertFalse(ByteTool.equals(a1, 0, a1.length, b1, 0, b1.length));
			byte[] a2 = new byte[]{0, 1, 2, 3, 4, 5};
			byte[] b2 = new byte[]{2, 3, 4, 5, 6, 7};
			Assert.assertTrue(ByteTool.equals(a2, 2, 4, b2, 0, 4));

		}

		@Test
		public void testGetOrderedBytes(){
			byte min = Byte.MIN_VALUE;
			Assert.assertEquals(min, -128);
			byte max = Byte.MAX_VALUE;
			Assert.assertEquals(max, 127);
			Assert.assertTrue(min < max);

			byte[] minArray = getComparableBytes(min);
			byte[] maxArray = getComparableBytes(max);
			Assert.assertTrue(ByteTool.bitwiseCompare(maxArray, minArray) > 0);

			byte negative = -3;
			byte positive = 5;
			Assert.assertTrue(negative < positive);

			byte[] negativeArray = getComparableBytes(negative);
			byte[] positiveArray = getComparableBytes(positive);
			Assert.assertTrue(ByteTool.bitwiseCompare(positiveArray, negativeArray) > 0);
		}

		@Test
		public void testUnsignedIncrement(){
			byte[] bytesA = IntegerByteTool.getUInt31Bytes(0);
			int a2 = IntegerByteTool.fromUInt31Bytes(unsignedIncrement(bytesA), 0);
			Assert.assertTrue(a2 == 1);

			byte[] bytesB = IntegerByteTool.getUInt31Bytes(-1);
			byte[] actuals = unsignedIncrement(bytesB);
			byte[] expected = new byte[]{1, 0, 0, 0, 0};
			Assert.assertEquals(actuals, expected);

			byte[] bytesC = IntegerByteTool.getUInt31Bytes(255);// should wrap to the next significant byte
			int c2 = IntegerByteTool.fromUInt31Bytes(unsignedIncrement(bytesC), 0);
			Assert.assertTrue(c2 == 256);
		}

		@Test
		public void testUnsignedIncrementOverflowToNull(){
			byte[] bytesA = IntegerByteTool.getUInt31Bytes(0);
			int a2 = IntegerByteTool.fromUInt31Bytes(unsignedIncrementOverflowToNull(bytesA), 0);
			Assert.assertTrue(a2 == 1);

			byte[] bytesB = IntegerByteTool.getUInt31Bytes(-1);
			byte[] b2 = unsignedIncrementOverflowToNull(bytesB);
			Assert.assertTrue(b2 == null);

			byte[] bytesC = IntegerByteTool.getUInt31Bytes(255);// should wrap to the next significant byte
			int c2 = IntegerByteTool.fromUInt31Bytes(unsignedIncrementOverflowToNull(bytesC), 0);
			Assert.assertTrue(c2 == 256);
		}

		@Test
		public void testPadPrefix(){
			Assert.assertEquals(padPrefix(new byte[]{55, -21}, 7), new byte[]{0, 0, 0, 0, 0, 55, -21});
		}

		@Test
		public void testGetHexString(){
			byte[] textBytes = StringByteTool.getUtf8Bytes("hello world!");
			byte[] allBytes = concatenate(textBytes, new byte[]{0, 127, -128});
			String hexString = getHexString(allBytes);
			Assert.assertEquals(hexString, "68656c6c6f20776f726c6421007f80");
		}
	}
}

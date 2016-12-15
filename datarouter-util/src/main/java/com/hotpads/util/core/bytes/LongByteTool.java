package com.hotpads.util.core.bytes;

import java.util.List;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import com.hotpads.datarouter.util.core.DrArrayTool;
import com.hotpads.datarouter.util.core.DrByteTool;
import com.hotpads.util.core.collections.arrays.LongArray;

/*
 * methods for converting longs into bytes
 */
public class LongByteTool{

	/****************** serialize to bytes ****************************/

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

	public static int toRawBytes(final long in, final byte[] bytes, final int offset){
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

	public static long fromRawBytes(final byte[] bytes, final int byteOffset){
		return (bytes[byteOffset] & (long)0xff) << 56
		| (bytes[byteOffset + 1] & (long)0xff) << 48
		| (bytes[byteOffset + 2] & (long)0xff) << 40
		| (bytes[byteOffset + 3] & (long)0xff) << 32
		| (bytes[byteOffset + 4] & (long)0xff) << 24
		| (bytes[byteOffset + 5] & (long)0xff) << 16
		| (bytes[byteOffset + 6] & (long)0xff) << 8
		| bytes[byteOffset + 7] & (long)0xff;
	}

	/*
	 * int64
	 *
	 * flip first bit so bitwiseCompare is always correct
	 */

	//************ single values

	public static byte[] getComparableBytes(final long value){
		long shifted = value ^ Long.MIN_VALUE;
		return getRawBytes(shifted);
	}

	public static int toComparableBytes(final long value, final byte[] bytes, final int offset){
		long shifted = value ^ Long.MIN_VALUE;
		return toRawBytes(shifted, bytes, offset);
	}

	public static Long fromComparableBytes(final byte[] bytes, int byteOffset){
		return Long.MIN_VALUE ^ fromRawBytes(bytes, byteOffset);
	}

	//************ arrays and collections

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
		if(DrArrayTool.isEmpty(bytes)){
			return new long[0];
		}
		return fromComparableByteArray(bytes, 0, bytes.length);
	}

	private static long[] fromComparableByteArray(final byte[] bytes, final int startIdx, final int length){
		long[] out = new long[length / 8];
		int byteOffset = startIdx;
		for(int i = 0; i < out.length; ++i){
			out[i] = Long.MIN_VALUE ^ fromRawBytes(bytes, byteOffset);
			byteOffset += 8;
		}
		return out;
	}

	/*
	 * uInt63
	 *
	 * first bit must be 0, reject others
	 */

	//************ single values

	public static byte[] getUInt63Bytes(final long value){
		if(value < 0 && value != Long.MIN_VALUE){//need to allow Long.MIN_VALUE in for nulls
			throw new IllegalArgumentException("no negatives: " + value);
		}
		return getRawBytes(value);
	}

	public static long fromUInt63Bytes(final byte[] bytes, final int byteOffset){
		long longValue = fromRawBytes(bytes, byteOffset);
		if(longValue < 0 && longValue != Long.MIN_VALUE){
			throw new IllegalArgumentException("no negatives: " + longValue);
		}
		return longValue;
	}

	//************ arrays and collections

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
		if(DrArrayTool.isEmpty(bytes)){
			return new long[0];
		}
		return fromUInt63ByteArray(bytes, 0, bytes.length);
	}

	public static long[] fromUInt63ByteArray(final byte[] bytes, final int startIdx, int length){
		long[] out = new long[length / 8];
		int byteOffset = startIdx;
		for(int i = 0; i < out.length; ++i){
			out[i] = fromUInt63Bytes(bytes, byteOffset);
			byteOffset += 8;
		}
		return out;
	}

	/************************ tests ***************************************/

	public static class Tests{

		@Test
		public void testBuildingLong(){
			long longValue = 0;
			longValue |= (byte)-2;
			Assert.assertEquals(-2, longValue);
			// l = (l << 8) + (-2+128);
		}

		@Test
		public void testGetOrderedBytes(){
			long longA = Long.MIN_VALUE;
			byte[] ab = new byte[]{0,0,0,0,0,0,0,0};
			Assert.assertArrayEquals(ab, getComparableBytes(longA));

			long longB = Long.MAX_VALUE;
			byte[] bb = new byte[]{-1,-1,-1,-1,-1,-1,-1,-1};
			Assert.assertArrayEquals(bb, getComparableBytes(longB));

			long longC = Long.MIN_VALUE + 1;
			byte[] cb = new byte[]{0,0,0,0,0,0,0,1};
			Assert.assertArrayEquals(cb, getComparableBytes(longC));

			long longD = Long.MAX_VALUE - 3;
			byte[] db = new byte[]{-1,-1,-1,-1,-1,-1,-1,-4};
			Assert.assertArrayEquals(db, getComparableBytes(longD));

			long longE = 127;
			byte[] eb = new byte[]{-128,0,0,0,0,0,0,127};
			Assert.assertArrayEquals(eb, getComparableBytes(longE));

			long longF = 128;
			byte[] fb = new byte[]{-128,0,0,0,0,0,0,-128};
			Assert.assertArrayEquals(fb, getComparableBytes(longF));

			long longG = -128;
			byte[] gb = new byte[]{127,-1,-1,-1,-1,-1,-1,-128};
			Assert.assertArrayEquals(gb, getComparableBytes(longG));
		}

		@Test
		public void testFromOrderedBytes(){
			long longA = Long.MIN_VALUE;
			byte[] ab = new byte[]{0,0,0,0,0,0,0,0};
			Assert.assertEquals(longA, fromComparableByteArray(ab)[0]);

			long longB = Long.MAX_VALUE;
			byte[] bb = new byte[]{-1,-1,-1,-1,-1,-1,-1,-1};
			Assert.assertEquals(longB, fromComparableByteArray(bb)[0]);

			long longC = Long.MIN_VALUE + 1;
			byte[] cb = new byte[]{0,0,0,0,0,0,0,1};
			Assert.assertEquals(longC, fromComparableByteArray(cb)[0]);

			long longD = Long.MAX_VALUE - 3;
			byte[] db = new byte[]{-1,-1,-1,-1,-1,-1,-1,-4};
			Assert.assertEquals(longD, fromComparableByteArray(db)[0]);

			long longE = 3;
			byte[] eb = new byte[]{-128,0,0,0,0,0,0,3};
			Assert.assertEquals(longE, fromComparableByteArray(eb)[0]);
		}

		@Test
		public void testRoundTrip(){
			long[] subjects = new long[]{
					Long.MIN_VALUE,Long.MIN_VALUE + 1,
					0,1,127,128,
					Long.MAX_VALUE - 1,Long.MAX_VALUE,
					-9223372036845049055L};
			for(long subject : subjects){
				byte[] bytes = getComparableBytes(subject);
				long roundTripped = fromComparableByteArray(bytes)[0];
				Assert.assertEquals(subject, roundTripped);
			}
		}

		@Test
		public void testRoundTrips(){
			Random random = new Random();
			long value = Long.MIN_VALUE;
			byte[] lastBytes = getComparableBytes(value);
			long lastValue = value;
			++value;
			int counter = 0;
			long stopAt = Long.MAX_VALUE - 2 * (long)Integer.MAX_VALUE;
			Assert.assertTrue(stopAt > Integer.MAX_VALUE);
			do{
				if(counter < 1000){
					Assert.assertTrue(value < 0);
				}
				byte[] bytes = getComparableBytes(value);
				long roundTripped = fromComparableByteArray(bytes)[0];
				try{
					Assert.assertTrue(value > lastValue);
					Assert.assertTrue(DrByteTool.bitwiseCompare(lastBytes, bytes) < 0);
					Assert.assertEquals(value, roundTripped);
				}catch(AssertionError e){
					throw e;
				}
				lastBytes = bytes;
				++counter;
				lastValue = value;
				long incrementor = random.nextLong() >>> 18;
				value = value + incrementor;
			}while(value < stopAt && value > lastValue);// watch out for overflowing and going back negative
			Assert.assertTrue(counter > 1000);//make sure we did a lot of tests
		}
	}
}

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
public class LongByteTool {
	
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
		return (
		      ((bytes[byteOffset    ] & (long)0xff) << 56)
			| ((bytes[byteOffset + 1] & (long)0xff) << 48)
			| ((bytes[byteOffset + 2] & (long)0xff) << 40)
			| ((bytes[byteOffset + 3] & (long)0xff) << 32)
			| ((bytes[byteOffset + 4] & (long)0xff) << 24)
			| ((bytes[byteOffset + 5] & (long)0xff) << 16)
			| ((bytes[byteOffset + 6] & (long)0xff) <<  8)
			|  (bytes[byteOffset + 7] & (long)0xff)      
		);
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
		if(valuesWithNulls==null){ return new byte[0]; }
		LongArray values;
		if(valuesWithNulls instanceof LongArray){
			values = (LongArray)valuesWithNulls;
		}else{
			values = new LongArray(valuesWithNulls);
		}
		byte[] out = new byte[8*values.size()];
		for(int i=0; i < values.size(); ++i){
			System.arraycopy(getComparableBytes(values.getPrimitive(i)), 0, out, i*8, 8);
		}
		return out;
	}
	

	public static byte[] getComparableByteArray(long[] values){
		byte[] out = new byte[8*values.length];
		for(int i=0; i < values.length; ++i){
			System.arraycopy(getComparableBytes(values[i]), 0, out, i*8, 8);
		}
		return out;
	}

	public static long[] fromComparableByteArray(final byte[] bytes){
		if(DrArrayTool.isEmpty(bytes)){ return new long[0]; }
		return fromComparableByteArray(bytes, 0, bytes.length);
	}

	public static long[] fromComparableByteArray(final byte[] bytes, final int startIdx, final int length){
		long[] out = new long[length / 8];
		int byteOffset = startIdx;
		for(int i=0; i < out.length; ++i){
			/*
			 * i think the first bitwise operation causes the operand to be zero-padded 
			 *     to an integer before the operation happens
			 *     
			 * parenthesis are extremely important here because of the automatic int upgrading
			 */
			
			//more compact
			out[i] = Long.MIN_VALUE ^ (
						  ((bytes[byteOffset    ] & (long)0xff) << 56)
						| ((bytes[byteOffset + 1] & (long)0xff) << 48)
						| ((bytes[byteOffset + 2] & (long)0xff) << 40)
						| ((bytes[byteOffset + 3] & (long)0xff) << 32)
						| ((bytes[byteOffset + 4] & (long)0xff) << 24)
						| ((bytes[byteOffset + 5] & (long)0xff) << 16)
						| ((bytes[byteOffset + 6] & (long)0xff) <<  8)
						|  (bytes[byteOffset + 7] & (long)0xff)      
					);
			
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
//		if(value < 0){ throw new IllegalArgumentException("no negatives"); }//need to allow Long.MIN_VALUE in for nulls
		byte[] out = new byte[8];
		out[0] = (byte) (value >>> 56);
		out[1] = (byte) (value >>> 48);
		out[2] = (byte) (value >>> 40);
		out[3] = (byte) (value >>> 32);
		out[4] = (byte) (value >>> 24);
		out[5] = (byte) (value >>> 16);
		out[6] = (byte) (value >>> 8);
		out[7] = (byte) value;
		return out;
	}
	
	public static Long fromUInt63Bytes(final byte[] bytes, final int byteOffset){
		return 
		  ((bytes[byteOffset    ] & (long)0xff) << 56)
		| ((bytes[byteOffset + 1] & (long)0xff) << 48)
		| ((bytes[byteOffset + 2] & (long)0xff) << 40)
		| ((bytes[byteOffset + 3] & (long)0xff) << 32)
		| ((bytes[byteOffset + 4] & (long)0xff) << 24)
		| ((bytes[byteOffset + 5] & (long)0xff) << 16)
		| ((bytes[byteOffset + 6] & (long)0xff) <<  8)
		|  (bytes[byteOffset + 7] & (long)0xff);
	}

	//************ arrays and collections

	public static byte[] getUInt63ByteArray(List<Long> valuesWithNulls){
		if(valuesWithNulls==null){ return new byte[0]; }
		LongArray values;
		if(valuesWithNulls instanceof LongArray){
			values = (LongArray)valuesWithNulls;
		}else{
			values = new LongArray(valuesWithNulls);
		}
		byte[] out = new byte[8*values.size()];
		for(int i=0; i < values.size(); ++i){
			System.arraycopy(getUInt63Bytes(values.getPrimitive(i)), 0, out, i*8, 8);
		}
		return out;
	}
	

	public static byte[] getUInt63ByteArray(long[] values){
		byte[] out = new byte[8*values.length];
		for(int i=0; i < values.length; ++i){
			System.arraycopy(getUInt63Bytes(values[i]), 0, out, i*8, 8);
		}
		return out;
	}
	
	public static long[] fromUInt63ByteArray(final byte[] bytes){
		if(DrArrayTool.isEmpty(bytes)){ return new long[0]; }
		return fromUInt63ByteArray(bytes, 0, bytes.length);
	}
		
	public static long[] fromUInt63ByteArray(final byte[] bytes, final int startIdx, int length){
		long[] out = new long[length / 8];
		int byteOffset = startIdx;
		for(int i=0; i < out.length; ++i){
			
			/*
			 * i think the first bitwise operation causes the operand to be zero-padded 
			 *     to an integer before the operation happens
			 *     
			 * parenthesis are extremely important here because of the automatic int upgrading
			 */
			
			//more compact
			out[i] =      ((bytes[byteOffset    ] & (long)0xff) << 56)
						| ((bytes[byteOffset + 1] & (long)0xff) << 48)
						| ((bytes[byteOffset + 2] & (long)0xff) << 40)
						| ((bytes[byteOffset + 3] & (long)0xff) << 32)
						| ((bytes[byteOffset + 4] & (long)0xff) << 24)
						| ((bytes[byteOffset + 5] & (long)0xff) << 16)
						| ((bytes[byteOffset + 6] & (long)0xff) <<  8)
						|  (bytes[byteOffset + 7] & (long)0xff);
			
			byteOffset += 8;
		}
		return out;
	}
	
	
	
	/************************ tests ***************************************/
	
	public static class Tests{

		@Test public void testBuildingLong(){
			long l = 0;
			l |= (byte)-2;
//			System.out.println(LongTool.toBitString(l));
			Assert.assertEquals(-2, l);
			l = (l << 8) + (int)(-2+128);
//			System.out.println(LongTool.toBitString(l));
		}
		
		@Test public void testGetOrderedBytes(){
			long a = Long.MIN_VALUE;
			byte[] ab = new byte[]{0,0,0,0,0,0,0,0};
			Assert.assertArrayEquals(ab, getComparableBytes(a));

			long b = Long.MAX_VALUE;
			byte[] bb = new byte[]{-1,-1,-1,-1,-1,-1,-1,-1};
			Assert.assertArrayEquals(bb, getComparableBytes(b));

			long c = Long.MIN_VALUE + 1;
			byte[] cb = new byte[]{0,0,0,0,0,0,0,1};
			Assert.assertArrayEquals(cb, getComparableBytes(c));

			long d = Long.MAX_VALUE - 3;
			byte[] db = new byte[]{-1,-1,-1,-1,-1,-1,-1,-4};
			Assert.assertArrayEquals(db, getComparableBytes(d));

			long e = 127;
			byte[] eb = new byte[]{-128,0,0,0,0,0,0,127};
			Assert.assertArrayEquals(eb, getComparableBytes(e));

			long f = 128;
			byte[] fb = new byte[]{-128,0,0,0,0,0,0,-128};
			Assert.assertArrayEquals(fb, getComparableBytes(f));

			long g = -128;
			byte[] gb = new byte[]{127,-1,-1,-1,-1,-1,-1,-128};
			Assert.assertArrayEquals(gb, getComparableBytes(g));
		}
		@Test public void testFromOrderedBytes(){
			long a = Long.MIN_VALUE;
			byte[] ab = new byte[]{0,0,0,0,0,0,0,0};
			Assert.assertEquals(a, fromComparableByteArray(ab)[0]);

			long b = Long.MAX_VALUE;
			byte[] bb = new byte[]{-1,-1,-1,-1,-1,-1,-1,-1};
			Assert.assertEquals(b, fromComparableByteArray(bb)[0]);

			long c = Long.MIN_VALUE + 1;
			byte[] cb = new byte[]{0,0,0,0,0,0,0,1};
			Assert.assertEquals(c, fromComparableByteArray(cb)[0]);

			long d = Long.MAX_VALUE - 3;
			byte[] db = new byte[]{-1,-1,-1,-1,-1,-1,-1,-4};
			Assert.assertEquals(d, fromComparableByteArray(db)[0]);
			
			long e = 3;
			byte[] eb = new byte[]{-128,0,0,0,0,0,0,3};
			Assert.assertEquals(e, fromComparableByteArray(eb)[0]);
		}
		@Test public void testRoundTrip(){
			long[] subjects = new long[]{
					Long.MIN_VALUE,Long.MIN_VALUE+1,
					0,1,127,128,
					Long.MAX_VALUE-1,Long.MAX_VALUE,
					-9223372036845049055L};
			for(int i=0; i < subjects.length; ++i){
//				System.out.println("roundTrip "+Integer.toHexString(subjects[i]));
//				System.out.println("origi "+toBitString(subjects[i]));
				byte[] bytes = getComparableBytes(subjects[i]);
//				System.out.println("bytes "+ByteTool.getBinaryStringBigEndian(bytes));
				long roundTripped = fromComparableByteArray(bytes)[0];
//				System.out.println("round "+toBitString(roundTripped));
				Assert.assertEquals(subjects[i], roundTripped);
			}
		}
		
		@Test public void testRoundTrips(){
			Random r = new Random();
			long value=Long.MIN_VALUE;
			byte[] lastBytes = getComparableBytes(value);
			long lastValue = value;
			++value;
			int counter = 0;
			long stopAt = Long.MAX_VALUE-2*(long)Integer.MAX_VALUE;
			Assert.assertTrue(stopAt > (long)Integer.MAX_VALUE);
			do{
				if(counter < 1000){
					Assert.assertTrue(value < 0);
				}
				byte[] bytes = getComparableBytes(value);
				long roundTripped = fromComparableByteArray(bytes)[0];
				try{
					Assert.assertTrue(value > lastValue);
//					System.out.println("#"+counter);
//					System.out.println("hex   "+Long.toHexString(value));
//					System.out.println("bin   "+Long.toBinaryString(value));
//					System.out.println("bytes "+ByteTool.getBinaryStringBigEndian(bytes));
					Assert.assertTrue(DrByteTool.bitwiseCompare(lastBytes, bytes) < 0);
					Assert.assertEquals(value, roundTripped);
				}catch(AssertionError e){
					System.out.println("counter: "+counter);
					System.out.println(value+" -> "+roundTripped);
					System.out.println("lastBytes:"+DrByteTool.getBinaryStringBigEndian(lastBytes));
					System.out.println("thisBytes:"+DrByteTool.getBinaryStringBigEndian(bytes));
					throw e;
				}
				lastBytes = bytes;
				++counter;
				lastValue = value;
				long incrementor = r.nextLong() >>> 18;
				value = value + incrementor;
			}while(value < stopAt && value > lastValue);//watch out for overflowing and going back negative
			Assert.assertTrue(counter > 1000);//make sure we did a lot of tests
//			System.out.println(counter);
		}
	}
}

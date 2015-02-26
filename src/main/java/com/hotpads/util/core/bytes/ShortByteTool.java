package com.hotpads.util.core.bytes;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.hotpads.util.core.ArrayTool;
import com.hotpads.util.core.ByteTool;
import com.hotpads.util.core.CollectionTool;

/*
 * methods for converting shorts into bytes
 */
public class ShortByteTool {
	
	/*
	 * int16
	 * 
	 * flip first bit so bitwiseCompare is always correct
	 */
	
	public static byte[] getRawBytes(final short in){
		byte[] out = new byte[2];
		out[0] = (byte) (in >>> 8);
		out[1] = (byte) in;
		return out;
	}

	public static int toRawBytes(final short in, final byte[] bytes, final int offset){
		bytes[offset] = (byte) (in >>> 8);
		bytes[offset + 1] = (byte) in;
		return 2;
	}
	
	public static short fromRawBytes(final byte[] bytes, final int startIdx){
		return (short)(
				  ((bytes[startIdx    ] & (int)0xff) << 8)
				|  (bytes[startIdx + 1] & (int)0xff)      
			);
	}	
		
	public static byte[] getComparableBytes(final short value){
		int shifted = value ^ Short.MIN_VALUE;
		return getRawBytes((short)shifted);
	}

	public static int toComparableBytes(final short value, final byte[] bytes, final int offset){
		int shifted = value ^ Short.MIN_VALUE;
		return toRawBytes((short)shifted, bytes, offset);
	}
	
	public static short fromComparableBytes(final byte[] bytes, int byteOffset){
		return (short)(Short.MIN_VALUE ^ fromRawBytes(bytes, byteOffset));
	}

//	public static byte[] getComparableBytes(final short value){
//		int shifted = value ^ Short.MIN_VALUE;
//		byte[] out = new byte[2];
//		out[0] = (byte) (shifted >>> 8);
//		out[1] = (byte) shifted;
//		return out;
//	}
//	
//	public static short fromComparableBytes(final byte[] bytes, final int startIdx){
//		return (short)(Short.MIN_VALUE ^ (
//				  ((bytes[startIdx    ] & (int)0xff) << 8)
//				|  (bytes[startIdx + 1] & (int)0xff)      
//			));
//	}

	public static byte[] getComparableByteArray(List<Short> values){
		if(CollectionTool.isEmpty(values)){ return new byte[0]; }
		byte[] out = new byte[2*values.size()];
		int index = 0;
		for(Short value : values){
			System.arraycopy(getComparableBytes(value), 0, out, index*2, 2);
			++index;
		}
		return out;
	}
	

	public static byte[] getComparableByteArray(short[] values){
		byte[] out = new byte[2*values.length];
		for(int i=0; i < values.length; ++i){
			System.arraycopy(getComparableBytes(values[i]), 0, out, i*2, 2);
		}
		return out;
	}

	public static short[] fromComparableByteArray(final byte[] bytes){
		if(ArrayTool.isEmpty(bytes)){ return new short[0]; }
		short[] out = new short[bytes.length / 2];
		for(int i=0; i < out.length; ++i){
			int startIdx = i*2;
			
			/*
			 * i think the first bitwise operation causes the operand to be zero-padded 
			 *     to an integer before the operation happens
			 *     
			 * parenthesis are extremely important here because of the automatic int upgrading
			 */
			
			//more compact
			out[i] = (short)(Short.MIN_VALUE ^ (
						  ((bytes[startIdx    ] & (int)0xff) << 8)
						|  (bytes[startIdx + 1] & (int)0xff)      
					));
			
		}
		return out;
	}
	
	

	/*
	 * uInt31
	 * 
	 * first bit must be 0, reject others
	 */
	
	public static byte[] getUInt15Bytes(final short value){
//		if(value < 0){ throw new IllegalArgumentException("no negatives"); }
		byte[] out = new byte[2];
		out[0] = (byte) (value >>> 8);
		out[1] = (byte) value;
		return out;
	}
	
	public static short fromUInt15Bytes(final byte[] bytes, final int startIdx){
		return (short)(
			  ((bytes[startIdx + 0] & (int)0xff) <<  8)
			|  (bytes[startIdx + 1] & (int)0xff));
	}
	
	//TODO copy array methods from IntegerByteTool
	
	
	/********************************* tests ***********************************************/	
	
	public static class Tests{
		@Test public void testGetOrderedBytes(){
			short a = Short.MIN_VALUE;
			byte[] ab = new byte[]{0,0};
			Assert.assertArrayEquals(ab, getComparableBytes(a));
			byte[] ac = new byte[]{5,5};//5's are just filler
			toComparableBytes(a, ac, 0);
			Assert.assertArrayEquals(ab, ac);

			short b = Short.MAX_VALUE;
			byte[] bb = new byte[]{-1,-1};
			Assert.assertArrayEquals(bb, getComparableBytes(b));
			byte[] bc = new byte[]{5,5};
			toComparableBytes(b, bc, 0);
			Assert.assertArrayEquals(bb, bc);

			short c = Short.MIN_VALUE + 1;
			byte[] cb = new byte[]{0,1};
			Assert.assertArrayEquals(cb, getComparableBytes(c));
			byte[] cc = new byte[]{5,5};
			toComparableBytes(c, cc, 0);
			Assert.assertArrayEquals(cb, cc);

			short d = Short.MAX_VALUE - 3;
			byte[] db = new byte[]{-1,-4};
			Assert.assertArrayEquals(db, getComparableBytes(d));
			byte[] dc = new byte[]{5,5};
			toComparableBytes(d, dc, 0);
			Assert.assertArrayEquals(db, dc);

			short z = 0;
			byte[] zb = new byte[]{Byte.MIN_VALUE,0};
			Assert.assertArrayEquals(zb, getComparableBytes(z));
			byte[] zc = new byte[]{5,5};
			toComparableBytes(z, zc, 0);
			Assert.assertArrayEquals(zb, zc);
		}

		@Test public void testArrays(){
			byte[] p5 = getComparableBytes((short)5);
			byte[] n3 = getComparableBytes((short)-3);
			byte[] n7 = getComparableBytes((short)-7);
			Assert.assertTrue(ByteTool.bitwiseCompare(p5, n3) > 0);
			Assert.assertTrue(ByteTool.bitwiseCompare(p5, n7) > 0);
		}
		
		@Test public void testRoundTrip(){
			short[] subjects = new short[]{
					Short.MIN_VALUE,Short.MIN_VALUE+1,
					0,1,127,128,
					Short.MAX_VALUE-1,Short.MAX_VALUE};
			for(int i=0; i < subjects.length; ++i){
//				System.out.println("roundTrip "+Integer.toHexString(subjects[i]));
//				System.out.println("origi "+toBitString(subjects[i]));
				byte[] bytes = getComparableBytes(subjects[i]);
//				System.out.println("bytes "+ByteTool.getBinaryStringBigEndian(bytes));
				int roundTripped = fromComparableByteArray(bytes)[0];
//				System.out.println("round "+toBitString(roundTripped));
				Assert.assertEquals(subjects[i], roundTripped);
			}
		}
		
		@Test public void testRoundTrips(){
			short i = Short.MIN_VALUE;
			byte[] lastBytes = getComparableBytes(i);
			++i;
			int counter = 0;
			for(; i<Short.MAX_VALUE; i+=1){
//				System.out.println("#"+counter++);
//				System.out.println("hex   "+Integer.toHexString(i));
//				System.out.println("bin   "+Integer.toBinaryString(i));
				byte[] bytes = getComparableBytes(i);
//				System.out.println("bytes "+ByteTool.getBinaryStringBigEndian(bytes));
				short roundTripped = fromComparableByteArray(bytes)[0];
				try{
					Assert.assertTrue(ByteTool.bitwiseCompare(lastBytes, bytes) < 0);
					Assert.assertEquals(i, roundTripped);
				}catch(AssertionError e){
//					System.out.println(i+" -> "+roundTripped);
//					System.out.println("lastBytes:"+ByteTool.getBinaryStringBigEndian(lastBytes));
//					System.out.println("thisBytes:"+ByteTool.getBinaryStringBigEndian(bytes));
					throw e;
				}
				lastBytes = bytes;
				++counter;
			}
			Assert.assertTrue(counter > 1000);//make sure we did a lot of tests
		}
		
		@Test public void testUnsignedRoundTrips(){
//			for(short i=0; i <= Short.MAX_VALUE; ++i){//infinite loop
			short i=0;
			while(true){
				byte[] bytes = getUInt15Bytes(i);
				short roundTripped = fromUInt15Bytes(bytes, 0);
				Assert.assertEquals(i, roundTripped);
				if(i==Short.MAX_VALUE){ break; }
				++i;
			}
		}
	}

}

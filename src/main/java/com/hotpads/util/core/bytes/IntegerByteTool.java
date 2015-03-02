package com.hotpads.util.core.bytes;

import java.util.List;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import com.hotpads.datarouter.util.core.DrArrayTool;
import com.hotpads.datarouter.util.core.DrByteTool;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrListTool;

/*
 * methods for converting ints into bytes
 */
public class IntegerByteTool {
	public static final int NULL = Integer.MIN_VALUE;
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
		return (
				  ((bytes[offset    ] & (int)0xff) << 24)
				| ((bytes[offset + 1] & (int)0xff) << 16)
				| ((bytes[offset + 2] & (int)0xff) <<  8)
				|  (bytes[offset + 3] & (int)0xff)      
			);
	}	
	
	public static byte[] getBytesNullable(Integer value){
		if(value == null){
			return getComparableBytes(NULL);
		}
		return getComparableBytes(value);
	}
	
	public static Integer fromBytesNullable(byte[] bytes, int offset){
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
	
//	public static byte[] getComparableBytes(final int value){
//		int shifted = value ^ Integer.MIN_VALUE;
//		byte[] out = new byte[4];
//		out[0] = (byte) (shifted >>> 24);
//		out[1] = (byte) (shifted >>> 16);
//		out[2] = (byte) (shifted >>> 8);
//		out[3] = (byte) shifted;
//		return out;
//	}
//	
//	public static int fromComparableBytes(final byte[] bytes, final int startIdx){
//		return Integer.MIN_VALUE ^ (
//				  ((bytes[startIdx    ] & (int)0xff) << 24)
//				| ((bytes[startIdx + 1] & (int)0xff) << 16)
//				| ((bytes[startIdx + 2] & (int)0xff) <<  8)
//				|  (bytes[startIdx + 3] & (int)0xff)      
//			);
//	}

	public static byte[] getComparableByteArray(List<Integer> values){
		if(DrCollectionTool.isEmpty(values)){ return new byte[0]; }
		byte[] out = new byte[4*values.size()];
		int index = 0;
		for(Integer value : values){
			System.arraycopy(getComparableBytes(value), 0, out, index*4, 4);
			++index;
		}
		return out;
	}
	
	public static List<Integer> fromIntegerByteArray(final byte[] bytes, final int startIdx){
		int numIntegers = (bytes.length - startIdx)/4;
		List<Integer> integers = DrListTool.createArrayList();
		byte[] arrayToCopy = new byte[4];
		for(int i = 0; i < numIntegers; i++){
			System.arraycopy(bytes, i*4+startIdx, arrayToCopy, 0, 4);
			integers.add(fromBytesNullable(arrayToCopy, 0));
		}
		return integers;
	}
	
	public static byte[] getIntegerByteArray(List<Integer> valuesWithNulls){
		if(valuesWithNulls==null){return null;}
		byte[] out = new byte[4*valuesWithNulls.size()];
		for(int i = 0; i < valuesWithNulls.size(); ++i){
			System.arraycopy(getBytesNullable(valuesWithNulls.get(i)), 0, out, i*4, 4);
		}
		return out;
	}

	public static byte[] getComparableByteArray(int[] values){
		byte[] out = new byte[4*values.length];
		for(int i=0; i < values.length; ++i){
			System.arraycopy(getComparableBytes(values[i]), 0, out, i*4, 4);
		}
		return out;
	}

	public static int[] fromComparableByteArray(final byte[] bytes){
		if(DrArrayTool.isEmpty(bytes)){ return new int[0]; }
		int[] out = new int[bytes.length / 4];
		for(int i=0; i < out.length; ++i){
			int startIdx = i*4;
			
			/*
			 * i think the first bitwise operation causes the operand to be zero-padded 
			 *     to an integer before the operation happens
			 *     
			 * parenthesis are extremely important here because of the automatic int upgrading
			 */
			
			//more compact
			out[i] = Integer.MIN_VALUE ^ (
						  ((bytes[startIdx    ] & (int)0xff) << 24)
						| ((bytes[startIdx + 1] & (int)0xff) << 16)
						| ((bytes[startIdx + 2] & (int)0xff) <<  8)
						|  (bytes[startIdx + 3] & (int)0xff));
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
		  ((bytes[startIdx    ] & (int)0xff) << 24)
		| ((bytes[startIdx + 1] & (int)0xff) << 16)
		| ((bytes[startIdx + 2] & (int)0xff) <<  8)
		|  (bytes[startIdx + 3] & (int)0xff);
	}

	public static byte[] getUInt31ByteArray(List<Integer> values){
		if(DrCollectionTool.isEmpty(values)){ return new byte[0]; }
		byte[] out = new byte[4*values.size()];
		int i = 0;
		for(Integer value : values){
			System.arraycopy(getUInt31Bytes(value), 0, out, i*4, 4);
			++i;
		}
		return out;
	}

	public static byte[] getUInt31ByteArray(int[] values){
		byte[] out = new byte[4*values.length];
		for(int i=0; i < values.length; ++i){
			System.arraycopy(getUInt31Bytes(values[i]), 0, out, i*4, 4);
		}
		return out;
	}

	public static int[] fromUInt31ByteArray(final byte[] bytes){
		if(DrArrayTool.isEmpty(bytes)){ return new int[0]; }
		int[] out = new int[bytes.length / 4];
		for(int i=0; i < out.length; ++i){
			int startIdx = i*4;
			
			/*
			 * i think the first bitwise operation causes the operand to be zero-padded 
			 *     to an integer before the operation happens
			 *     
			 * parenthesis are extremely important here because of the automatic int upgrading
			 */
			
			//more compact
			out[i] =      ((bytes[startIdx    ] & (int)0xff) << 24)
						| ((bytes[startIdx + 1] & (int)0xff) << 16)
						| ((bytes[startIdx + 2] & (int)0xff) <<  8)
						|  (bytes[startIdx + 3] & (int)0xff);
			
		}
		return out;
	}
	
	/************************ tests ***************************************/

	public static class Tests{
		//verify that -128 in bytes gets converted to -128 long.  Bitwise cast would be +128
		@Test public void testCasting(){
			byte b0=0,b1=1,b127=127,bn128=-128,bn1=-1;
			Assert.assertEquals(0L, (long)b0);
			Assert.assertEquals(1L, (long)b1);
			Assert.assertEquals(127L, (long)b127);
			Assert.assertEquals(-128L, (long)bn128);
			Assert.assertEquals(-1L, (long)bn1);
		}
		@Test public void testGetOrderedBytes(){
			int a = Integer.MIN_VALUE;
			byte[] ab = new byte[]{0,0,0,0};
			Assert.assertArrayEquals(ab, getComparableBytes(a));

			int b = Integer.MAX_VALUE;
			byte[] bb = new byte[]{-1,-1,-1,-1};
			Assert.assertArrayEquals(bb, getComparableBytes(b));

			int c = Integer.MIN_VALUE + 1;
			byte[] cb = new byte[]{0,0,0,1};
//			System.out.println(ByteTool.getBinaryStringBigEndian(cb));
			byte[] cout = getComparableBytes(c);
			Assert.assertArrayEquals(cb, cout);

			int d = Integer.MAX_VALUE - 3;
			byte[] db = new byte[]{-1,-1,-1,-4};
			Assert.assertArrayEquals(db, getComparableBytes(d));

			int e = 0;
			byte[] eb = new byte[]{-128,0,0,0};
			Assert.assertArrayEquals(eb, getComparableBytes(e));
		}

		@Test public void testArrays(){
			byte[] p5 = getComparableBytes(5);
			byte[] n3 = getComparableBytes(-3);
			byte[] n7 = getComparableBytes(-7);
			Assert.assertTrue(DrByteTool.bitwiseCompare(p5, n3) > 0);
			Assert.assertTrue(DrByteTool.bitwiseCompare(p5, n7) > 0);
		}
		
		@Test public void testRoundTrip(){
			int[] subjects = new int[]{
					Integer.MIN_VALUE,Integer.MIN_VALUE+1,
					0,1,127,128,
					Integer.MAX_VALUE-1,Integer.MAX_VALUE};
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
			Random r = new Random();
			int i=Integer.MIN_VALUE;
			byte[] lastBytes = getComparableBytes(i);
			++i;
			int counter = 0;
			for(; i<Integer.MAX_VALUE/2; i+=(1+Math.abs(r.nextInt()%53*47*991))){
//				System.out.println("#"+counter++);
//				System.out.println("hex   "+Integer.toHexString(i));
//				System.out.println("bin   "+Integer.toBinaryString(i));
				byte[] bytes = getComparableBytes(i);
//				System.out.println("bytes "+ByteTool.getBinaryStringBigEndian(bytes));
				int roundTripped = fromComparableByteArray(bytes)[0];
				try{
					Assert.assertTrue(DrByteTool.bitwiseCompare(lastBytes, bytes) < 0);
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
		
		@Test
		public void testToFromByteArray(){
			int one = -239483;
			int two = 583;
			
			List<Integer> integers = DrListTool.create();
			integers.add(one);
			integers.add(null);
			integers.add(two);

			byte[] integerBytes = getIntegerByteArray(integers);
			List<Integer> result = fromIntegerByteArray(integerBytes, 0);
			Assert.assertEquals(integers, result);
		}
	}
}

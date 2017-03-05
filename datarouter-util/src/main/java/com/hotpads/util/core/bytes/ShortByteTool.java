package com.hotpads.util.core.bytes;

import org.junit.Assert;
import org.junit.Test;

import com.hotpads.datarouter.util.core.DrArrayTool;
import com.hotpads.datarouter.util.core.DrByteTool;

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

	private static int toRawBytes(final short in, final byte[] bytes, final int offset){
		bytes[offset] = (byte) (in >>> 8);
		bytes[offset + 1] = (byte) in;
		return 2;
	}

	private static short fromRawBytes(final byte[] bytes, final int startIdx){
		return (short)(
				 (bytes[startIdx] & 0xff) << 8
				| bytes[startIdx + 1] & 0xff);
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

	private static short[] fromComparableByteArray(final byte[] bytes){
		if(DrArrayTool.isEmpty(bytes)){
			return new short[0];
		}
		short[] out = new short[bytes.length / 2];
		for(int i = 0; i < out.length; ++i){
			int startIdx = i * 2;

			/*
			 * i think the first bitwise operation causes the operand to be zero-padded
			 *     to an integer before the operation happens
			 *
			 * parenthesis are extremely important here because of the automatic int upgrading
			 */

			//more compact
			out[i] = (short)(Short.MIN_VALUE ^ (
						 (bytes[startIdx] & 0xff) << 8
						| bytes[startIdx + 1] & 0xff));

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
			 (bytes[startIdx + 0] & 0xff) << 8
			| bytes[startIdx + 1] & 0xff);
	}

	//TODO copy array methods from IntegerByteTool


	/********************************* tests ***********************************************/

	public static class Tests{
		@Test
		public void testGetOrderedBytes(){
			short shortA = Short.MIN_VALUE;
			byte[] ab = new byte[]{0,0};
			Assert.assertArrayEquals(ab, getComparableBytes(shortA));
			byte[] ac = new byte[]{5,5};//5's are just filler
			toComparableBytes(shortA, ac, 0);
			Assert.assertArrayEquals(ab, ac);

			short shortB = Short.MAX_VALUE;
			byte[] bb = new byte[]{-1,-1};
			Assert.assertArrayEquals(bb, getComparableBytes(shortB));
			byte[] bc = new byte[]{5,5};
			toComparableBytes(shortB, bc, 0);
			Assert.assertArrayEquals(bb, bc);

			short shortC = Short.MIN_VALUE + 1;
			byte[] cb = new byte[]{0,1};
			Assert.assertArrayEquals(cb, getComparableBytes(shortC));
			byte[] cc = new byte[]{5,5};
			toComparableBytes(shortC, cc, 0);
			Assert.assertArrayEquals(cb, cc);

			short shortD = Short.MAX_VALUE - 3;
			byte[] db = new byte[]{-1,-4};
			Assert.assertArrayEquals(db, getComparableBytes(shortD));
			byte[] dc = new byte[]{5,5};
			toComparableBytes(shortD, dc, 0);
			Assert.assertArrayEquals(db, dc);

			short shortZ = 0;
			byte[] zb = new byte[]{Byte.MIN_VALUE,0};
			Assert.assertArrayEquals(zb, getComparableBytes(shortZ));
			byte[] zc = new byte[]{5,5};
			toComparableBytes(shortZ, zc, 0);
			Assert.assertArrayEquals(zb, zc);
		}

		@Test
		public void testArrays(){
			byte[] p5 = getComparableBytes((short)5);
			byte[] n3 = getComparableBytes((short)-3);
			byte[] n7 = getComparableBytes((short)-7);
			Assert.assertTrue(DrByteTool.bitwiseCompare(p5, n3) > 0);
			Assert.assertTrue(DrByteTool.bitwiseCompare(p5, n7) > 0);
		}

		@Test
		public void testRoundTrip(){
			short[] subjects = new short[]{
					Short.MIN_VALUE,Short.MIN_VALUE + 1,
					0,1,127,128,
					Short.MAX_VALUE - 1,Short.MAX_VALUE};
			for(short subject : subjects){
				byte[] bytes = getComparableBytes(subject);
				int roundTripped = fromComparableByteArray(bytes)[0];
				Assert.assertEquals(subject, roundTripped);
			}
		}

		@Test
		public void testRoundTrips(){
			short shortValue = Short.MIN_VALUE;
			byte[] lastBytes = getComparableBytes(shortValue);
			++shortValue;
			int counter = 0;
			for(; shortValue < Short.MAX_VALUE; shortValue += 1){
				byte[] bytes = getComparableBytes(shortValue);
				short roundTripped = fromComparableByteArray(bytes)[0];
				try{
					Assert.assertTrue(DrByteTool.bitwiseCompare(lastBytes, bytes) < 0);
					Assert.assertEquals(shortValue, roundTripped);
				}catch(AssertionError e){
					throw e;
				}
				lastBytes = bytes;
				++counter;
			}
			Assert.assertTrue(counter > 1000);//make sure we did a lot of tests
		}

		@Test
		public void testUnsignedRoundTrips(){
			short shortValue = 0;
			while(true){
				byte[] bytes = getUInt15Bytes(shortValue);
				short roundTripped = fromUInt15Bytes(bytes, 0);
				Assert.assertEquals(shortValue, roundTripped);
				if(shortValue == Short.MAX_VALUE){
					break;
				}
				++shortValue;
			}
		}
	}

}

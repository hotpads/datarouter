package com.hotpads.datarouter.util.core;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.hotpads.util.core.bytes.IntegerByteTool;

public class DrByteTool {
	
	public static final Integer
		BYTES_PER_POINTER = (int)DrRuntimeTool.getBytesPerPointer();//do we need to add bytes (4?) for class id?
	
	public static final Integer
		BYTES_PER_BOOLEAN = 1,
		BYTES_PER_CHAR = 2,
		BYTES_PER_BYTE = 1,
		BYTES_PER_SHORT = 2,
		BYTES_PER_INTEGER = 4,
		BYTES_PER_LONG = 8,
		BYTES_PER_FLOAT = 4,
		BYTES_PER_DOUBLE = 8,

		BYTES_PER_BOOLEAN_WITH_POINTER = BYTES_PER_BOOLEAN + BYTES_PER_POINTER,
		BYTES_PER_CHAR_WITH_POINTER = BYTES_PER_CHAR + BYTES_PER_POINTER,
		BYTES_PER_BYTE_WITH_POINTER = BYTES_PER_BYTE + BYTES_PER_POINTER,
		BYTES_PER_SHORT_WITH_POINTER = BYTES_PER_SHORT + BYTES_PER_POINTER,
		BYTES_PER_INTEGER_WITH_POINTER = BYTES_PER_INTEGER + BYTES_PER_POINTER,
		BYTES_PER_LONG_WITH_POINTER = BYTES_PER_LONG + BYTES_PER_POINTER,
		BYTES_PER_FLOAT_WITH_POINTER = BYTES_PER_FLOAT + BYTES_PER_POINTER,
		BYTES_PER_DOUBLE_WITH_POINTER = BYTES_PER_DOUBLE + BYTES_PER_POINTER,
		
		BYTES_PER_HASH_MAP_ENTRY = 3 * BYTES_PER_POINTER + BYTES_PER_INTEGER;
				
	
	public static ArrayList<Byte> getArrayList(byte[] ins){
		ArrayList<Byte> outs = new ArrayList<>(DrArrayTool.length(ins));
		for(byte in : DrArrayTool.nullSafe(ins)){
			outs.add(in);
		}
		return outs;
	}
	
	public static byte toUnsignedByte(final int i){
		//Assert.assertTrue(i >=0 && i <=255);
//		if(i < 128){ return (byte)i; } 
		int ib = i - 128;
		return (byte)(ib);//subtract 256
	}
	
	//not really sure what this method means anymore
	public static byte fromUnsignedInt0To255(int u){
		if(u > 127){
			return (byte)(u - 0x100);//subtract 256
		}
		return (byte)u;
	}
	
	public static int bitwiseCompare(byte[] a, byte[] b){
		int aLength = DrArrayTool.length(a);
		int bLength = DrArrayTool.length(b);
		for(int i = 0, j = 0; i < aLength && j < bLength; ++i, ++j){
			//need to trick the built in byte comparator which treats 10000000 < 00000000 because it's negative
			int aByte = a[i] & 0xff;  //boost the "negative" numbers up to 128-255
			int bByte = b[j] & 0xff;
			if(aByte != bByte){ return aByte - bByte; }
		}
		return aLength - bLength;
	}
	
	public static int bitwiseCompare(byte[] a, int aOffset, int aLength,
			byte[] b, int bOffset, int bLength){
		for(int i = aOffset, j = bOffset; i < aOffset + aLength && j < bOffset + bLength; ++i, ++j){
			//need to trick the built in byte comparator which treats 10000000 < 00000000 because it's negative
			int aByte = a[i] & 0xff;  //boost the "negative" numbers up to 128-255
			int bByte = b[j] & 0xff;
			if(aByte != bByte){ return aByte - bByte; }
		}
		return aLength - bLength;
	}
	
	public static boolean equals(byte[] a, int aOffset, int aLength,
			byte[] b, int bOffset, int bLength){
		if(aLength != bLength){ return false; }
		for(int i = aOffset + aLength - 1, j = bOffset + bLength - 1; i >= 0 && j >= 0; --i, --j){
			if(a[i] != b[j]){ return false; }
		}
		return true;
	}

	public static byte[] getComparableBytes(byte value){
		if(value >= 0){
			return new byte[]{(byte) (value + Byte.MIN_VALUE) };
		}
		return new byte[]{(byte) (value - Byte.MIN_VALUE) };
	}

	public static byte getComparableByte(byte value){
		if(value >= 0){
			return (byte) (value + Byte.MIN_VALUE);
		}
		return (byte) (value - Byte.MIN_VALUE);
	}
	
	public static byte[] flipToAndFromComparableByteArray(byte[] ins){
		return flipToAndFromComparableByteArray(ins, 0, ins.length);
	}
	
	//basically a copyOfRange that also flips the bytes
	public static byte[] flipToAndFromComparableByteArray(byte[] ins, int offset, int length){
		byte[] outs = new byte[length];
		for(int i=0; i < length; ++i){
			outs[i] = getComparableByte(ins[offset+i]);
		}
		return outs;
	}

	public static String getBinaryStringBigEndian(byte b){
		StringBuilder sb = new StringBuilder();
		for(int i=7; i >=0; --i){
			sb.append(((b>>i) & 1)); 
		}
		return sb.toString();
	}
	
	public static String getBinaryStringBigEndian(byte[] ba){
		StringBuilder sb = new StringBuilder();
		int len = DrArrayTool.length(ba);
		for(int n=0; n < len; ++n){
			for(int i=7; i >=0; --i){
				sb.append(((ba[n]>>i) & 1)); 
			}
		}
		return sb.toString();
	}
	
	public static byte[] copyOfRange(byte[] in, int offset, int length){
		byte[] out = new byte[length];
		System.arraycopy(in, offset, out, 0, length);
		return out;
	}
	
	public static byte[] unsignedIncrement(final byte[] in){
		byte[] copy = DrArrayTool.clone(in);
		if(copy==null){ throw new IllegalArgumentException("cannot increment null array"); }
		for(int i=copy.length-1; i >=0; --i){
			if(copy[i]==-1){//-1 is all 1-bits, which is the unsigned maximum
				copy[i] = 0;
			}else{
				++copy[i];
				return copy;
			}
		}
		//we maxed out the array
		byte[] out = new byte[copy.length+1];
		out[0] = 1;
		System.arraycopy(copy, 0, out, 1, copy.length);
		return out;
	}
	
	public static byte[] unsignedIncrementOverflowToNull(final byte[] in){
		byte[] out = DrArrayTool.clone(in);
		for(int i=out.length-1; i >=0; --i){
			if(out[i]==-1){//-1 is all 1-bits, which is the unsigned maximum
				out[i] = 0;
			}else{
				++out[i];
				return out;
			}
		}
		return null;
	}
	
	
	/************************* byte arrays ************************************/
	
	public static byte[] concatenate(byte[]... ins){
		if(ins==null){ return new byte[0]; }
		int totalLength = 0;
		for(int i=0; i < ins.length; ++i){
			totalLength+=DrArrayTool.length(ins[i]);
		}
		byte[] out = new byte[totalLength];
		int startIndex=0;
		for(int i=0; i < ins.length; ++i){
			if(ins[i]==null){ continue; }
			System.arraycopy(ins[i], 0, out, startIndex, ins[i].length);
			startIndex+=ins[i].length;
		}
		return out;
	}
	
	public static byte[] padPrefix(final byte[] in, int finalWidth){
		byte[] out = new byte[finalWidth];
		int numPaddingBytes = finalWidth - in.length;
		System.arraycopy(in, 0, out, numPaddingBytes, in.length);
		return out;
	}
	
	
	/************************* serialize ****************************************/

	public static byte[] getUInt7Bytes(List<Byte> values){
		if(DrCollectionTool.isEmpty(values)){ return new byte[0]; }
		byte[] out = new byte[values.size()];
		int i = 0;
		for(Byte value : values){
			if(value < 0){ throw new IllegalArgumentException("no negatives"); }
			out[i] = value;
			++i;
		}
		return out;
	}
	
	public static byte[] fromUInt7ByteArray(byte[] bytes, int offset, int length){
		//validate?
		return copyOfRange(bytes, offset, length);
	}
	
	
	
	/************************* tests ***********************************************/	

	public static class Tests{
		@Test public void testToUnsignedByte(){
			Assert.assertEquals(-128, toUnsignedByte(0));
			Assert.assertEquals(-1, toUnsignedByte(127));
			Assert.assertEquals(0, toUnsignedByte(128));
			Assert.assertEquals(1, toUnsignedByte(129));
			Assert.assertEquals(127, toUnsignedByte(255));
		}
		@Test public void testFromUnsignedInt0To255(){
			Assert.assertEquals(0, fromUnsignedInt0To255(0));
			Assert.assertEquals(127, fromUnsignedInt0To255(127));
			Assert.assertEquals(-128, fromUnsignedInt0To255(128));
			Assert.assertEquals(-1, fromUnsignedInt0To255(255));
		}	
		@Test public void testBitwiseCompare(){
			byte[] a = new byte[]{1,-1};
			byte[] b = new byte[]{-3};
			Assert.assertTrue(bitwiseCompare(a, b) < 0);//positive numbers come before negative when bitwise
			Assert.assertTrue(bitwiseCompare(a, 1, 1, b, 0, 1) > 0);// -1 is after -3
		}
		@Test public void testEquals(){
			byte[] a1 = new byte[]{1,-1};
			byte[] b1 = new byte[]{-3};
			Assert.assertFalse(DrByteTool.equals(a1, 0, a1.length, b1, 0, b1.length));
			byte[] a2 = new byte[]{0,1,2,3,4,5};
			byte[] b2 = new byte[]{2,3,4,5,6,7};
			Assert.assertTrue(DrByteTool.equals(a2, 2, 4, b2, 0, 4));
			
		}
		@Test public void testGetOrderedBytes(){
			byte min = Byte.MIN_VALUE;
			Assert.assertEquals(-128, min);
			byte max = Byte.MAX_VALUE;
			Assert.assertEquals(127, max);
			Assert.assertTrue(min < max);
			
			byte[] minArray = getComparableBytes(min);
			byte[] maxArray = getComparableBytes(max);
			Assert.assertTrue(DrByteTool.bitwiseCompare(maxArray, minArray) > 0);
			
			System.out.println(DrByteTool.getBinaryStringBigEndian(min)+" "
					+DrByteTool.getBinaryStringBigEndian(max)+" "
					+DrByteTool.getBinaryStringBigEndian(minArray)+" "
					+DrByteTool.getBinaryStringBigEndian(maxArray));

			byte negative = -3;
			byte positive = 5;
			Assert.assertTrue(negative < positive);

			byte[] negativeArray = getComparableBytes(negative);
			byte[] positiveArray = getComparableBytes(positive);
			Assert.assertTrue(DrByteTool.bitwiseCompare(positiveArray, negativeArray) > 0);
			
			System.out.println(DrByteTool.getBinaryStringBigEndian(negative)+" "
					+DrByteTool.getBinaryStringBigEndian(positive)+" "
					+DrByteTool.getBinaryStringBigEndian(negativeArray)+" "
					+DrByteTool.getBinaryStringBigEndian(positiveArray));
		}
		
		@Test public void testGetBinaryString(){
			Assert.assertEquals("00000000", getBinaryStringBigEndian((byte)0));
			Assert.assertEquals("00000001", getBinaryStringBigEndian((byte)1));
			Assert.assertEquals("00000010", getBinaryStringBigEndian((byte)2));
			Assert.assertEquals("01111111", getBinaryStringBigEndian((byte)127));
			Assert.assertEquals("10000000", getBinaryStringBigEndian((byte)128));
			Assert.assertEquals("11111110", getBinaryStringBigEndian((byte)-2));
			Assert.assertEquals("11111000", getBinaryStringBigEndian((byte)-8));
			Assert.assertEquals("11111111", getBinaryStringBigEndian((byte)-1));
		}
		
		@Test public void testUnsignedIncrement(){
			byte[] a = IntegerByteTool.getUInt31Bytes(0);
			int a2 = IntegerByteTool.fromUInt31Bytes(unsignedIncrement(a), 0);
			Assert.assertTrue(a2==1);

			byte[] b = IntegerByteTool.getUInt31Bytes(-1);
			byte[] actuals = unsignedIncrement(b);
			byte[] expected = new byte[]{1,0,0,0,0};
			Assert.assertArrayEquals(expected, actuals);
			
			byte[] c = IntegerByteTool.getUInt31Bytes(255);//should wrap to the next significant byte
			int c2 = IntegerByteTool.fromUInt31Bytes(unsignedIncrement(c), 0);
			Assert.assertTrue(c2==256);
		}
		
		@Test public void testUnsignedIncrementOverflowToNull(){
			byte[] a = IntegerByteTool.getUInt31Bytes(0);
			int a2 = IntegerByteTool.fromUInt31Bytes(unsignedIncrementOverflowToNull(a), 0);
			Assert.assertTrue(a2==1);

			byte[] b = IntegerByteTool.getUInt31Bytes(-1);
			byte[] b2 = unsignedIncrementOverflowToNull(b);
			Assert.assertTrue(b2==null);
			
			byte[] c = IntegerByteTool.getUInt31Bytes(255);//should wrap to the next significant byte
			int c2 = IntegerByteTool.fromUInt31Bytes(unsignedIncrementOverflowToNull(c), 0);
			Assert.assertTrue(c2==256);
		}
		
		@Test public void testPadPrefix(){
			Assert.assertArrayEquals(new byte[]{0, 0, 0, 0, 0, 55, -21}, padPrefix(new byte[]{55, -21}, 7));
		}
	}
}





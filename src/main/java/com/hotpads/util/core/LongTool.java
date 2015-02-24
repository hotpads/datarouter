package com.hotpads.util.core;

import org.junit.Assert;
import org.junit.Test;


public class LongTool {
	
	public static final long LONG_NO_BITS = 0;
	public static final long LONG_ALL_BITS = -1;
	public static final long LONG_RIGHT_BIT = 1;
	public static final long LONG_LEFT_BIT = Long.MAX_VALUE + 1;
	public static final long LONG_RIGHT_63_BITS = Long.MAX_VALUE;
	public static final long LONG_LEFT_63_BITS = -2;
	
	public static final int INT_ALL_BITS = -1;
	public static final int INT_RIGHT_6_BITS = INT_ALL_BITS >>> 26;
	public static final int INT_LEFT_26_BITS = INT_ALL_BITS << 6;
	
	
	/*********************** bitwise ops **********************************/
	
	public static long numBitsSet(long a){
		return Long.bitCount(a);
	}
	
	public static long numBitsSetBefore(long a, int ignoreThisIndexAndHigher){
		return Long.bitCount(a << (64 - ignoreThisIndexAndHigher));
	}
	
	public static int numBitsSetBefore(long[] a, int ignoreThisBitIndexAndHigher){
		int numFullLongs = ignoreThisBitIndexAndHigher >>> 6;
//		int remainder = ignoreThisBitIndexAndHigher & INT_RIGHT_6_BITS;
		int remainder = ignoreThisBitIndexAndHigher % 64;
		int count = 0;
		for(int i=0; i < numFullLongs; ++i){
			count += Long.bitCount(a[i]);
		}
		if(remainder > 0){
			count += numBitsSetBefore(a[numFullLongs], remainder);
		}
		return count;
	}
	
	public static long numBitsClear(long a){
		return 64 - numBitsSet(a);
	}
	
	public static long setRangeOfBits(int leftMost, int rightMostExclusive){
		long builder = 0;
		for(int i=leftMost; i < rightMostExclusive; ++i){
			builder |= (1L << i);
		}
		return builder;
	}
	
	public static long setRightBits(int numSetBitsOnRight){
		long builder = LONG_ALL_BITS;
		return builder >>> (64 - numSetBitsOnRight);
	}
		
	public static long setLeftBits(int numSetBitsOnLeft){
		long builder = LONG_ALL_BITS;
		return builder  << (64 - numSetBitsOnLeft);
	}
	
	public static long clearAllButRightmostSetBit(long a){
		return Long.lowestOneBit(a);
	}
	
	public static long clearAllButLeftmostSetBit(long a){
		return Long.highestOneBit(a);
	}
	
	public static long rotateBitsRight(long a, int numPositions){
		return Long.rotateRight(a, numPositions);
	}
	
	public static long rotateBitsLeft(long a, int numPositions){
		return Long.rotateLeft(a, numPositions);
	}
	
	public static long setOneBit(int bit){
		return 1L << bit; //CAREFUL - must shift "1L", not just "1"
	}
	
	public static long or(long a, long b){
		return a | b;
	}
	
	public static long and(long a, long b){
		return a & b;
	}
	
	public static long xor(long a, long b){
		return a ^ b;
	}
	
	public static long not(long a){
		return a ^ LONG_ALL_BITS;
	}
	
	public static long flipAllBits(long a){
		return a ^ LONG_ALL_BITS;  //same as "not"
	}
	
	public static String toBitString(long a){
		return StringTool.pad(Long.toBinaryString(a), '0', 64);
	}
	
	public static Long fromString(String longString){
		try{
			return Long.parseLong(longString);
		}catch(NumberFormatException e){
			return null;
		}
	}
	
	public static class Tests{
		
		@Test public void testCount(){
			Assert.assertEquals(0, numBitsSet(LONG_NO_BITS));
			Assert.assertEquals(63, numBitsSet(LONG_RIGHT_63_BITS));
			Assert.assertEquals(63, numBitsSet(LONG_LEFT_63_BITS));
			Assert.assertEquals(1, numBitsSet(LONG_LEFT_BIT));
			Assert.assertEquals(64, numBitsSet(LONG_ALL_BITS));
			Assert.assertEquals(5, numBitsSetBefore(LONG_ALL_BITS, 5));
		}
		@Test public void testSet(){
			Assert.assertEquals(1, setOneBit(0));
			Assert.assertEquals(256, setOneBit(8));
			Assert.assertEquals(LONG_LEFT_BIT, setOneBit(63));
			long a = setRangeOfBits(3,25);
			Assert.assertEquals(22, numBitsSet(a));
			Assert.assertEquals(setOneBit(24), clearAllButLeftmostSetBit(a));
			Assert.assertEquals(setOneBit(3), clearAllButRightmostSetBit(a));
			Assert.assertEquals(22, numBitsSet(a));
			Assert.assertEquals(12, numBitsSet(setRightBits(12)));
		}
		@Test public void testFlip(){
			Assert.assertEquals(LONG_ALL_BITS, flipAllBits(LONG_NO_BITS));
			Assert.assertEquals(LONG_NO_BITS, flipAllBits(LONG_ALL_BITS));
			Assert.assertEquals(LONG_RIGHT_63_BITS, flipAllBits(LONG_LEFT_BIT));
			long a = setOneBit(33);
			Assert.assertEquals(63, Long.bitCount(flipAllBits(a)));
		}
		@Test public void testBitwiseOps(){
			Assert.assertEquals(LONG_ALL_BITS, or(LONG_LEFT_63_BITS, LONG_RIGHT_BIT));
			Assert.assertEquals(17, or(setOneBit(4), LONG_RIGHT_BIT));
			Assert.assertEquals(setOneBit(63), and(LONG_ALL_BITS, LONG_LEFT_BIT));
		}
		@Test public void testMultiCount(){
			long[] a = new long[2];
			a[0] = LONG_RIGHT_63_BITS;
			a[1] = 8;
			Assert.assertEquals(64, numBitsSetBefore(a,128));
			Assert.assertEquals(63, numBitsSetBefore(a,66));
			a[1] = LONG_ALL_BITS;
			Assert.assertEquals(127, numBitsSetBefore(a,128));
			Assert.assertEquals(123, numBitsSetBefore(a,124));
		}
		@Test public void testIntBits(){
			Assert.assertEquals(32, Integer.bitCount(INT_ALL_BITS));
//			Assert.assertEquals(6, Integer.bitCount(INT_RIGHT_6_BITS));//???????????????????????
		}
		
		@Test public void testFromString(){
			Assert.assertEquals(new Long(1), fromString("1"));
			Assert.assertEquals(new Long(111111111111111l), 
					fromString("111111111111111"));
			Assert.assertEquals(null, fromString("h"));
		}
	}
	
	
}

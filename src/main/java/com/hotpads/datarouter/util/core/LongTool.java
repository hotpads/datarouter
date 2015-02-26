package com.hotpads.datarouter.util.core;

import org.junit.Assert;
import org.junit.Test;


public class LongTool {
	
	public static final long LONG_ALL_BITS = -1;
	public static final int INT_ALL_BITS = -1;
	
	
	/*********************** bitwise ops **********************************/
	
	public static long setRightBits(int numSetBitsOnRight){
		long builder = LONG_ALL_BITS;
		return builder >>> (64 - numSetBitsOnRight);
	}
		
	public static long setLeftBits(int numSetBitsOnLeft){
		long builder = LONG_ALL_BITS;
		return builder  << (64 - numSetBitsOnLeft);
	}
	
	public static String toBitString(long a){
		return StringTool.pad(Long.toBinaryString(a), '0', 64);
	}
	
	
	public static class Tests{
		@Test public void testIntBits(){
			Assert.assertEquals(32, Integer.bitCount(INT_ALL_BITS));
//			Assert.assertEquals(6, Integer.bitCount(INT_RIGHT_6_BITS));//???????????????????????
		}
	}
	
	
}

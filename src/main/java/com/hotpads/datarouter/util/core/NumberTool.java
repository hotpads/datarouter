package com.hotpads.datarouter.util.core;

import org.junit.Assert;
import org.junit.Test;



public class NumberTool {
	
	/************************* is this or that methods ************************/
	
	public static boolean isNullOrZero(Number n){
		//careful, this method is fragile and not even sure if works with BigInteger stuff now
		return n==null || n.equals(0L) || n.equals(0F) || n.equals(0D) || n.intValue()==0;
	}

	public static boolean isMax(Long v){
		if(v==null){ return false; }
		return v==Long.MAX_VALUE;
	}
	
	
	public static Long max(Long n1, Long n2){
		if(n1 == null){ return n2; }
		if(n2 == null){ return n1; }
		return Math.max(n1, n2);
	}

	
	
	/************************ numeric null safe *******************************/
	
	public static Integer nullSafe(Integer in){
		if(in==null){ return 0; }
		return in;
	}
	
	public static Long nullSafeLong(Long in, Long defaultValue){
		if(in==null){ return defaultValue; }
		return in;
	}
	
	
	/*************************** parsing **************************************/
	
	public static Double getDoubleNullSafe(String toDouble, Double alternate){
		return getDoubleNullSafe(toDouble, alternate, false);
	}
	
	public static Double getDoubleNullSafe(
			String toDouble, Double alternate, boolean filterInput){
		if(toDouble==null) return alternate;
		if(filterInput){
			toDouble = StringTool.enforceNumeric(toDouble);
			if(toDouble==null) return alternate;
		}
		try{
			return Double.valueOf(toDouble);
		}catch(NumberFormatException e){
			return alternate;
		}
	}
	
	//e.g. For "5.3", it will return 5
	public static Integer parseIntegerFromNumberString(String toInteger, Integer alternate){
		return parseIntegerFromNumberString(toInteger, alternate, false);
	}
	
	//e.g. For "5.3", it will return 5
	public static Integer parseIntegerFromNumberString(
			String toInteger, Integer alternate, boolean filterInput){
		Double d = getDoubleNullSafe(toInteger,null,filterInput);
		if(d==null) return alternate;
		return d.intValue();
	}
	
	public static Long getLongNullSafe(String toLong, Long alternate){
		if(toLong==null) return alternate;
		try{
			return Long.valueOf(toLong);
		}catch(NumberFormatException e){
			return alternate;
		}
	}
	
	
	/****************************** tests *************************************/
	
	public static class Tests {
		@Test public void testIsNullOrZero(){
			Byte b = 0;
			Short s = 0;
			Integer i = 0;
			Long l = 0L;
			Float f = 0.0f;
			Double d = 0.0;
			Assert.assertTrue(isNullOrZero(b));
			Assert.assertTrue(isNullOrZero(s));
			Assert.assertTrue(isNullOrZero(i));
			Assert.assertTrue(isNullOrZero(l));
			Assert.assertTrue(isNullOrZero(f));
			Assert.assertTrue(isNullOrZero(d));
		}
		@Test public void testParseIntegerFromNumberString(){
			Assert.assertEquals(new Integer(5), parseIntegerFromNumberString("5.5", null));
			Assert.assertEquals(new Integer(5), parseIntegerFromNumberString("5.0", null));
			Assert.assertEquals(new Integer(5), parseIntegerFromNumberString("5.9", null));
			Assert.assertEquals(new Integer(-9), parseIntegerFromNumberString("-9", null));
			Assert.assertEquals(new Integer(-9), parseIntegerFromNumberString("-9", -9));
			Assert.assertEquals(null, parseIntegerFromNumberString("5-9", null));
			Assert.assertEquals(null, parseIntegerFromNumberString(null, null));
			Assert.assertEquals(null, parseIntegerFromNumberString("banana", null));
			Assert.assertEquals(new Integer(2), parseIntegerFromNumberString("banana", 2));
			Assert.assertEquals(new Integer(2), parseIntegerFromNumberString("banana", 2));
			Assert.assertEquals(new Integer(2), parseIntegerFromNumberString("2", 3));
			Assert.assertEquals(new Integer(Integer.MAX_VALUE), 
					parseIntegerFromNumberString(Integer.MAX_VALUE+"", null));
			Assert.assertEquals(new Integer(Integer.MIN_VALUE), 
					parseIntegerFromNumberString(Integer.MIN_VALUE+"", null));

			Assert.assertEquals(new Integer(400000),
					parseIntegerFromNumberString("$400,000", null, true));
			Assert.assertNull(parseIntegerFromNumberString("$400,000", null, false));
		}
		@Test public void testCachedIntegers(){
			Integer foo0 = 1000;
			Integer bar0 = 1000;
			Assert.assertTrue(foo0 <= bar0);
			Assert.assertTrue(foo0 >= bar0);
			Assert.assertFalse(foo0 == bar0);
			
			Integer foo1 = 42;
			Integer bar1 = 42;
			Assert.assertTrue(foo1 <= bar1);
			Assert.assertTrue(foo1 >= bar1);
			Assert.assertTrue(foo1 == bar1);
			
			Integer foo2 = 1000;
			int bar2 = 1000;
			Assert.assertTrue(foo2 <= bar2);
			Assert.assertTrue(foo2 >= bar2);
			Assert.assertTrue(foo2 == bar2);
		}
	}

}

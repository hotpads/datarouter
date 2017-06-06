package com.hotpads.datarouter.util.core;

import org.junit.Assert;
import org.junit.Test;

public class DrNumberTool{

	/************************* is this or that methods ************************/

	public static boolean isEmpty(Number number){
		return isNullOrZero(number);
	}

	public static boolean notEmpty(Number number){
		return !isEmpty(number);
	}

	// careful, this method is fragile and not even sure if works with BigInteger stuff now
	public static boolean isNullOrZero(Number number){
		return number == null || number.equals(0L) || number.equals(0F) || number.equals(0D) || number.intValue() == 0;
	}

	public static boolean isMax(Long number){
		return number != null && number == Long.MAX_VALUE;
	}

	public static Long max(Long n1, Long n2){
		if(n1 == null){
			return n2;
		}
		if(n2 == null){
			return n1;
		}
		return Math.max(n1, n2);
	}

	/************************ numeric null safe *******************************/

	public static Integer nullSafe(Integer in){
		if(in == null){
			return 0;
		}
		return in;
	}

	public static Long nullSafeLong(Long in, Long defaultValue){
		if(in == null){
			return defaultValue;
		}
		return in;
	}

	public static Long longValue(Number number){
		if(number == null){
			return null;
		}
		return number.longValue();
	}

	/*************************** parsing **************************************/

	public static Double getDoubleNullSafe(String toDouble){
		return getDoubleNullSafe(toDouble, null);
	}

	public static Double getDoubleNullSafe(String toDouble, Double alternate){
		return getDoubleNullSafe(toDouble, alternate, false);
	}

	public static Double getDoubleNullSafe(String toDouble, Double alternate, boolean filterInput){
		if(toDouble == null){
			return alternate;
		}
		if(filterInput){
			toDouble = DrStringTool.enforceNumeric(toDouble);
			if(toDouble == null){
				return alternate;
			}
		}
		try{
			return Double.valueOf(toDouble);
		}catch(NumberFormatException e){
			return alternate;
		}
	}

	public static Integer parseIntegerFromNumberString(String toInteger){
		return parseIntegerFromNumberString(toInteger, null);
	}

	// e.g. For "5.3", it will return 5
	public static Integer parseIntegerFromNumberString(String toInteger, Integer alternate){
		return parseIntegerFromNumberString(toInteger, alternate, false);
	}

	// e.g. For "5.3", it will return 5
	public static Integer parseIntegerFromNumberString(String toInteger, Integer alternate, boolean filterInput){
		Double dub = getDoubleNullSafe(toInteger, null, filterInput);
		if(dub == null){
			return alternate;
		}
		return dub.intValue();
	}

	public static Long getLongNullSafe(String toLong, Long alternate){
		if(toLong == null){
			return alternate;
		}
		try{
			return Long.valueOf(toLong);
		}catch(NumberFormatException e){
			return alternate;
		}
	}

	/****************************** tests *************************************/

	public static class Tests{
		@Test
		public void testIsNullOrZero(){
			Byte byt = 0;
			Short shrt = 0;
			Integer intgr = 0;
			Long lng = 0L;
			Float flt = 0.0f;
			Double dub = 0.0;
			Assert.assertTrue(isNullOrZero(byt));
			Assert.assertTrue(isNullOrZero(shrt));
			Assert.assertTrue(isNullOrZero(intgr));
			Assert.assertTrue(isNullOrZero(lng));
			Assert.assertTrue(isNullOrZero(flt));
			Assert.assertTrue(isNullOrZero(dub));
		}

		@Test
		public void testParseIntegerFromNumberString(){
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
			Assert.assertEquals(new Integer(Integer.MAX_VALUE), parseIntegerFromNumberString(Integer.MAX_VALUE + "",
					null));
			Assert.assertEquals(new Integer(Integer.MIN_VALUE), parseIntegerFromNumberString(Integer.MIN_VALUE + "",
					null));

			Assert.assertEquals(new Integer(400000), parseIntegerFromNumberString("$400,000", null, true));
			Assert.assertNull(parseIntegerFromNumberString("$400,000", null, false));
		}

		@Test
		public void testCachedIntegers(){
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

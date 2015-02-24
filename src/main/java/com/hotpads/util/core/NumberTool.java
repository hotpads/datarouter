package com.hotpads.util.core;

import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import com.hotpads.util.core.number.DoubleComparator;



public class NumberTool {
	
	/*************************** floating point corrections *******************/
		
	public static boolean equals(Double d1, Double d2, double error){
		if(d1==null || d2==null){ 
			return false;
		}
		return new DoubleComparator(error).compare(d1, d2) == 0;
	}
	
	public static final double roundDown(double value, double granularity){
		double remainder = value % granularity;
		return value - remainder;
	}
	
	public static final double roundUp(double value, double granularity){
		double remainder = value % granularity;
		return value + (granularity - remainder);
	}

	public static Float zeroForStrangeValues(Float in){
		if(in==null){ return 0f; }
		if(Float.isInfinite(in)){ return 0f; }
		if(Float.isNaN(in)){ return 0f; }
		return in;
	}

	public static Double zeroForStrangeValues(Double in){
		if(in==null){ return 0d; }
		if(Double.isInfinite(in)){ return 0d; }
		if(Double.isNaN(in)){ return 0d; }
		return in;
	}
	
	
	/************************* is this or that methods ************************/
	
	public static boolean isEmpty(Number n){
		return isNullOrZero(n);
	}
	
	public static boolean notEmpty(Number n){
		return !isEmpty(n);
	}
	
	public static boolean isNullOrZero(Number n){
		//careful, this method is fragile and not even sure if works with BigInteger stuff now
		return n==null || n.equals(0L) || n.equals(0F) || n.equals(0D) || n.intValue()==0;
	}

	public static boolean infiniteOrNaN(Double n){
		return n!=null && (n.isInfinite() || n.isNaN());
	}

	public static final boolean isGreaterThanOrEqualToZero(Double n){
		if(n == null){ 
			return false; 
		}
		if(n < 0){ 
			return false; 
		}
		return true;
	}
	
	public static final boolean isGreaterThanOrEqualToZero(Integer n){
		return isGreaterThanOrEqualTo(n,0);
	}
	
	public static final boolean isGreaterThanOrEqualTo(Integer n, int reference){
		if(n == null){ return false; }
		if(n < reference){ return false; }
		return true;
	}
	
	public static final boolean isPositive(Double n){
		if(n == null){ return false; }
		if(n <= 0){ return false; }
		return true;
	}
	
	public static final boolean isPositive(Integer n){
		if(n == null){ return false; }
		if(n <= 0){ return false; }
		return true;
	}
	
	public static boolean isMax(Long v){
		if(v==null){ return false; }
		return v==Long.MAX_VALUE;
	}
	
	public static Integer max(Integer n1, Integer n2){
		if(n1==null) return n2;
		if(n2==null) return n1;
		return Math.max(n1,n2);
	}
	
	public static Long max(Long n1, Long n2){
		if(n1 == null){ return n2; }
		if(n2 == null){ return n1; }
		return Math.max(n1, n2);
	}

	//consider using ComparableTool.first/last instead of permutations of these
	
	public static Double max(Double... doubles){
		Double max = null;
		for(Double d : doubles){
			if(d == null){
				continue;
			}
			if(max == null){
				max = d;
				continue;
			}
			max = Math.max(max,d);
		}
		return max;
	}
	
	public static Double min(Double... doubles){
		Double min = null;
		for(Double d : doubles){
			if(d == null){
				continue;
			}
			if(min == null){
				min = d;
				continue;
			}
			min = Math.min(min,d);
		}
		return min;
	}
	
	
	/************************ numeric null safe *******************************/
	
	public static Integer nullSafe(Integer in){
		if(in==null){ return 0; }
		return in;
	}
	
	public static Long nullSafe(Long in){
		if(in==null){ return 0L; }
		return in;
	}
	
	public static Double nullSafe(Double in){
		if(in==null) return 0d;
		return in;
	}
		
	public static Integer nullSafeInteger(Byte in, Integer defaultValue){
		if(in==null){ return defaultValue; }
		return in.intValue();
	}
		
	public static Integer nullSafeInteger(Integer in, Integer defaultValue){
		if(in==null){ return defaultValue; }
		return in;
	}
	
	public static Long nullSafeLong(Long in, Long defaultValue){
		if(in==null){ return defaultValue; }
		return in;
	}
	
	public static Float nullSafeFloat(Float in, Float defaultValue){
		if(in==null){ return defaultValue; }
		return in;
	}
	
	public static Double nullSafeDouble(Double in, Double defaultValue){
		if(in==null){ return defaultValue; }
		return in;
	}
	
	public static Integer nullForZero(Integer n){
		if(n==null || n==0){ return null; }
		return n;
	}
	
	public static String nullSafeToString(Number n, String alt){
		if(n == null) return alt;
		return n.toString();
	}
	
	public static String nullAndZeroSafeToString(Number n, String alt){
		if(isNullOrZero(n)){ return alt; }
		return n.toString();
	}
	
	public static <N extends Number> N nullSafe(N n, N alternate){
		if(n==null) return alternate;
		return n;
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
	
	public static Integer getIntegerNullSafe(String toInteger, Integer alternate){
		try{
			if(toInteger == null){
				return null;
			}
			String numeric = toInteger.trim();
			numeric = numeric.indexOf(".") < 0 ? numeric : numeric.replaceAll("0*$", "").replaceAll("\\.$", "");			
			return Integer.valueOf(numeric);
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
	
	
	public static String getSuffix(int x){
		int d = x%100;
		if(d%10>3 || d%10<1) return "th"; //70% return here
		if(d>10 && d<14) return "th"; //3%
		String[] suffix = {"th","st","nd","rd"};
		return suffix[d%10]; //27%
	}
	
	public static <N extends Number> N getLowestValue(Collection<N> in){
		if(CollectionTool.isEmpty(in)){ return null; }
		N lowest = null;
		for(N n : in){
			if(n != null){
				lowest = n;
				break;
			}
		}
		for(N n : in){
			if(n != null && n.doubleValue() < lowest.doubleValue()){
				lowest = n;
			}
		}
		return lowest;
	}
	
	public static <N extends Number> N getHighestValue(Collection<N> in){
		if(CollectionTool.isEmpty(in)){ return null; }
		N highest = null;
		for(N n : in){
			if(n != null){
				highest = n;
				break;
			}
		}
		for(N n : in){
			if(n != null && n.doubleValue() > highest.doubleValue()){
				highest = n;
			}
		}
		return highest;
	}
	
	public static int sum(int[] in){
		if(ArrayTool.isEmpty(in)){ return 0; }
		int sum = 0;
		for(int i=0; i < in.length; ++i){
			sum+=in[i];
		}
		return sum;
	}
	
	public static Integer sum(Iterable<Integer> ins){
		int sum = 0;
		for(Integer in : IterableTool.nullSafe(ins)){
			sum += in;
		}
		return sum;
	}
	
	public static Integer getFirstInteger(String textWithNumbers){
    	String[] numbers = textWithNumbers.split("[^0-9,]+");
    	String firstNumber=null;
    	for(String number : numbers){
    		while(number.startsWith(",")) number = number.substring(1);
    		if(number.length()==0) continue;
    		firstNumber=number;
    		break;
    	}
    	if(firstNumber==null) return null;
    	if(firstNumber.matches("[0-9]{1,3}(,[0-9]{3})*")){
    		firstNumber = firstNumber.replaceAll(",","");
    	}else{
    		firstNumber = firstNumber.substring(0,
    				firstNumber.indexOf(","));
    	}    	
    	return NumberTool.parseIntegerFromNumberString(firstNumber, null);
    }
	
	
	/****************************** random ************************************/
	
	public static int randInt(int length){
		int min = new Double(Math.pow(10d,length-1)).intValue();
		int max = new Double(Math.pow(10d,length)-1).intValue();
		Double r = new Random().nextDouble();
		r = r * new Double(max-min);
		return r.intValue() + min;
	}
	
	
	/************************* parse ****************************************/
	
	//Zillow code test stringToLong
	public static long stringToLong(String s){
		if(s==null || s.length()==0){ return 0; }
		long result = 0;
		for(char c : s.toCharArray()){
			if(Character.isDigit(c)){
				result *= 10;
				result += c - '0';
			}
		}
		return s.charAt(0)=='-' ? -result : result;
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
		
		@Test public void testNullSafeNumber(){
			Assert.assertEquals(Short.class,nullSafe((short)1,(short)0).getClass());
			Assert.assertEquals(Short.class,nullSafe(null,(short)0).getClass());
			Assert.assertEquals(Short.class,nullSafe((short)1,null).getClass());
			
			Assert.assertEquals(new Short((short)1),nullSafe((short)1,(short)0));
			Assert.assertEquals(new Short((short)0),nullSafe(null,(short)0));
			Assert.assertEquals(new Short((short)1),nullSafe((short)1,null));
			
			Assert.assertEquals(new Integer(1),nullSafe(1,0));
			Assert.assertEquals(new Integer(0),nullSafe(null,0));
			Assert.assertEquals(new Integer(1),nullSafe(1,null));

			Assert.assertEquals(new Long(1L),nullSafe(1L,0L));
			Assert.assertEquals(new Long(0L),nullSafe(null,0L));
			Assert.assertEquals(new Long(1L),nullSafe(1L,null));
			
			Assert.assertEquals(new Double(1d),nullSafe(1d,0d));
			Assert.assertEquals(new Double(0d),nullSafe(null,0d));
			Assert.assertEquals(new Double(1d),nullSafe(1d,null));
			
			Assert.assertEquals(new Float(1f),nullSafe(1f,0f));
			Assert.assertEquals(new Float(0f),nullSafe(null,0f));
			Assert.assertEquals(new Float(1f),nullSafe(1f,null));
		}
		@Test public void testEqualsDouble(){
			Assert.assertTrue(NumberTool.equals(61.137932522000085,61.1379325220001,1e-10d));
			Assert.assertTrue(NumberTool.equals(61.137932522000085,61.1379325220001,1e-13d));
			Assert.assertFalse(NumberTool.equals(61.137932522000085,61.1379325220001,1e-20d));
			Assert.assertFalse(NumberTool.equals(61.137932522000085,61.1379325220001,1e-14d));
		}
		@Test public void testGTE(){
			Assert.assertTrue(isGreaterThanOrEqualTo(5, 4));
			Assert.assertTrue(isGreaterThanOrEqualTo(5, 5));
			Assert.assertTrue(isGreaterThanOrEqualTo(5, 0));
			Assert.assertTrue(isGreaterThanOrEqualTo(-1, -5));
			Assert.assertTrue(isGreaterThanOrEqualTo(5, -1));
			Assert.assertTrue( ! isGreaterThanOrEqualTo(5, 6));
			Assert.assertTrue( ! isGreaterThanOrEqualTo(-1, 2));
			Assert.assertTrue( ! isGreaterThanOrEqualTo(null, 0));
		}
		@Test public void testSuffix(){
			Assert.assertEquals("th", getSuffix(0));
			Assert.assertEquals("st", getSuffix(1));
			Assert.assertEquals("nd", getSuffix(2));
			Assert.assertEquals("rd", getSuffix(3));
			Assert.assertEquals("th", getSuffix(4));
			
			Assert.assertEquals("th", getSuffix(30));
			Assert.assertEquals("st", getSuffix(31));
			Assert.assertEquals("nd", getSuffix(32));
			Assert.assertEquals("rd", getSuffix(33));
			Assert.assertEquals("th", getSuffix(34));
			
			Assert.assertEquals("th", getSuffix(100));
			Assert.assertEquals("st", getSuffix(101));
			Assert.assertEquals("nd", getSuffix(102));
			Assert.assertEquals("rd", getSuffix(103));
			Assert.assertEquals("th", getSuffix(104));

			Assert.assertEquals("th", getSuffix(11));
			Assert.assertEquals("th", getSuffix(12));
			Assert.assertEquals("th", getSuffix(13));
			Assert.assertEquals("th", getSuffix(14));

			Assert.assertEquals("th", getSuffix(111));
			Assert.assertEquals("th", getSuffix(112));
			Assert.assertEquals("th", getSuffix(113));
			Assert.assertEquals("th", getSuffix(114));
		}
		@Test public void testGetLowestValue(){
			Assert.assertEquals(new Double(-1.2), NumberTool.getLowestValue(Arrays.asList(new Double[]{5.0,2.1,-.7,-1.2,55.0})));
			Assert.assertEquals(null, NumberTool.getLowestValue(Arrays.asList(new Double[]{null,null})));
			Assert.assertEquals(new Double(3.4), NumberTool.getLowestValue(Arrays.asList(new Double[]{3.4})));
			Assert.assertEquals(new Double(3.4), NumberTool.getLowestValue(Arrays.asList(new Double[]{null,3.4,null})));
		}
		@Test public void testGetHighestValue(){
			Assert.assertEquals(new Double(55.0), NumberTool.getHighestValue(Arrays.asList(new Double[]{5.0,2.1,-.7,-1.2,55.0})));
			Assert.assertEquals(null, NumberTool.getHighestValue(Arrays.asList(new Double[]{null,null})));
			Assert.assertEquals(new Double(3.4), NumberTool.getHighestValue(Arrays.asList(new Double[]{3.4})));
			Assert.assertEquals(new Double(3.4), NumberTool.getHighestValue(Arrays.asList(new Double[]{null,3.4,null})));
		}
		@Test public void testRandInt(){
			for(int i=1;i<11;i++){
				Assert.assertEquals(i,(randInt(i)+"").length());
			}
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
		@Test public void testGetIntegerNullSafe(){
			Assert.assertEquals(null, getIntegerNullSafe("5.5", null));
			Assert.assertEquals(new Integer(5), getIntegerNullSafe("5.", null));
			Assert.assertEquals(new Integer(5), getIntegerNullSafe("  5.00", null));
			Assert.assertEquals(new Integer(5), getIntegerNullSafe("5  ", null));
			Assert.assertEquals(new Integer(5), getIntegerNullSafe("   5  ", null));
			Assert.assertEquals(new Integer(5), getIntegerNullSafe("5.0", null));
			Assert.assertEquals(null, getIntegerNullSafe("5.9", null));
			Assert.assertEquals(new Integer(-9), getIntegerNullSafe("-9", null));
			Assert.assertEquals(new Integer(-9), getIntegerNullSafe("-9", -9));
			Assert.assertEquals(null, getIntegerNullSafe("5-9", null));
			Assert.assertEquals(null, getIntegerNullSafe(null, null));
			Assert.assertEquals(null, getIntegerNullSafe("banana", null));
			Assert.assertEquals(new Integer(2), getIntegerNullSafe("banana", 2));
			Assert.assertEquals(new Integer(2), getIntegerNullSafe("banana", 2));
			Assert.assertEquals(new Integer(2), getIntegerNullSafe("2", 3));
			Assert.assertEquals(new Integer(Integer.MAX_VALUE), 
					getIntegerNullSafe(Integer.MAX_VALUE+"", null));
			Assert.assertEquals(new Integer(Integer.MIN_VALUE), 
					getIntegerNullSafe(Integer.MIN_VALUE+"", null));

			Assert.assertNull(getIntegerNullSafe("$400,000", null));
			Assert.assertNull(getIntegerNullSafe("DEF400,000ABC", null));
		}
		
		@Test public void testNullSafeToString(){
			Assert.assertEquals("1",nullSafeToString(1, ""));
			Assert.assertEquals("1.0",nullSafeToString(1d, ""));
			Assert.assertEquals("1",nullSafeToString(1L, ""));
			Assert.assertEquals("",nullSafeToString(null, ""));
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
		@Test public void testGetFirstInteger(){
			Assert.assertNull(getFirstInteger(""));
			Assert.assertNull(getFirstInteger(" "));
			Assert.assertNull(getFirstInteger(" "));
			Assert.assertEquals(new Integer(1),getFirstInteger("1"));
			Assert.assertEquals(new Integer(1),getFirstInteger("1,2"));
			Assert.assertEquals(new Integer(2),getFirstInteger("2,1"));
			Assert.assertEquals(new Integer(2),getFirstInteger("2,10"));
			Assert.assertEquals(new Integer(20),getFirstInteger("20,10"));
			Assert.assertEquals(new Integer(20100),getFirstInteger("20,100"));
			Assert.assertEquals(new Integer(20),getFirstInteger("20,100,0"));
			Assert.assertEquals(new Integer(20100),getFirstInteger("20,100.0"));
		}

		@Test public void testMax(){
			Assert.assertNull(max((Integer)null,null));
			Assert.assertEquals(new Integer(0),max(null,0));
			Assert.assertEquals(new Integer(0),max(0,null));
			Assert.assertEquals(new Integer(0),max(0,0));
			Assert.assertEquals(new Integer(3),max(0,3));
			Assert.assertEquals(new Integer(3),max(3,0));

			Assert.assertEquals(null, max());
			Assert.assertEquals(null, max((Double)null));
			Assert.assertEquals(null, max((Double)null, null));
			Assert.assertEquals(null, max((Double)null, null, null));
			Assert.assertEquals(new Double(3), max(null,null,3d));
			Assert.assertEquals(new Double(3), max(3d,null));
			Assert.assertEquals(new Double(3), max(3d,1d));
			Assert.assertEquals(new Double(3), max(1d,3d));
			Assert.assertEquals(new Double(3), max(1d,2d,3d));
			Assert.assertEquals(new Double(3), max(1d,3d,2d));
			Assert.assertEquals(new Double(3), max(1d,3d,null,2d));
			Assert.assertEquals(new Double(2), max(1d,null,2d));
		}
		@Test public void testMin(){
			Assert.assertEquals(null, min());
			Assert.assertEquals(null, min((Double)null));
			Assert.assertEquals(null, min((Double)null, null));
			Assert.assertEquals(null, min((Double)null, null, null));
			Assert.assertEquals(new Double(3), min(null,null,3d));
			Assert.assertEquals(new Double(3), min(3d,null));
			Assert.assertEquals(new Double(1), min(3d,1d));
			Assert.assertEquals(new Double(1), min(1d,3d));
			Assert.assertEquals(new Double(1), min(1d,2d,3d));
			Assert.assertEquals(new Double(1), min(1d,3d,2d));
			Assert.assertEquals(new Double(1), min(1d,3d,null,2d));
			Assert.assertEquals(new Double(2), min(3d,null,2d));
		}
		@Test public void testIsPositive(){
			Assert.assertEquals(true,isPositive(0.00001d));
			Assert.assertEquals(true,isPositive(1d));
			Assert.assertEquals(true,isPositive(Double.MAX_VALUE));
			Assert.assertEquals(true,isPositive(Double.MIN_VALUE));
			Assert.assertEquals(true,isPositive(Double.POSITIVE_INFINITY));
			Assert.assertEquals(false,isPositive(0d));
			Assert.assertEquals(false,isPositive(-1d));
			Assert.assertEquals(false,isPositive(Double.NEGATIVE_INFINITY));
			Assert.assertEquals(false,isPositive((Double)null));
		}
		@Test public void testStringToLong(){
			Assert.assertEquals(0L, stringToLong("0"));
			Assert.assertEquals(1234560L, stringToLong("1234560"));
			Assert.assertEquals(-89L, stringToLong("-89"));
			Assert.assertEquals(-1234567L, stringToLong("-1,234,567"));
		}
	}

}

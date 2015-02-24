package com.hotpads.util.core;

import java.math.RoundingMode;
import java.text.DecimalFormat;

import org.junit.Assert;
import org.junit.Test;

public class NumberFormatter {

	public static final DecimalFormat PERCENT_0_DECIMALS = new DecimalFormat("0%");
	public static final DecimalFormat PERCENT_2_DECIMALS = new DecimalFormat("0.00%");
	public static final DecimalFormat TWO_DIGIT = new DecimalFormat("##");
	
	public static final String getPaddedPositiveDecimal(double input, int preDecimalDigits, int postDecimalDigits){
		if(input > Math.pow(10, preDecimalDigits)){
			throw new IllegalArgumentException("input too big to be represented with preDecimalDigits" + preDecimalDigits);
		}
		
		StringBuilder sb = new StringBuilder();

		int preDecimalDivisor = (int)Math.pow(10, preDecimalDigits);
		for(int i=preDecimalDigits; i > 0; --i){
			preDecimalDivisor /= 10;
			int singleDigit = (int)((input % (preDecimalDivisor * 10)) / preDecimalDivisor);
			sb.append(singleDigit);
		}
		
		if(postDecimalDigits>0){
			sb.append(".");
		}
		
		for(int i=0; i < postDecimalDigits; ++i){
			int postDecimalMultiplier = (int)Math.pow(10, i + 1);
			int singleDigit = (int)((input * postDecimalMultiplier) % 10);
			sb.append(singleDigit);
		}
		
		return sb.toString();
	}
	
	public static String format(Number n, int numFractionDigits){
		return format(n, "", "", numFractionDigits);
	}
	
	public static String format(Number n, String prefix, String suffix, int numFractionDigits){
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(numFractionDigits);
		df.setMinimumFractionDigits(numFractionDigits);
		df.setRoundingMode(RoundingMode.HALF_UP);
		df.setGroupingUsed(true);
		df.setPositivePrefix(prefix);
		df.setNegativePrefix(prefix + "-");
		df.setPositiveSuffix(suffix);
		df.setNegativeSuffix(suffix);
		return df.format(n);
	}
	
	public static String addCommas(final Number pValue){
		if(pValue==null){ return null; }
		return new DecimalFormat("###,###,###,###,###,###,###,###.#####################").format(pValue);//biggest is 19 digits
	}
	
	public static class Tests{
		@Test public void testFormat(){
			double input = 1234567890.1234567890;
			Assert.assertEquals("$1,234,567,890.12", format(input,"$","",2));
		}
		@Test public void testAddCommas(){
			Short nullShort = null;
			Assert.assertEquals(null, addCommas(nullShort));
			Assert.assertEquals("0", addCommas(0));
			Assert.assertEquals("-32,768", addCommas(Short.MIN_VALUE));
			Assert.assertEquals("32,767", addCommas(Short.MAX_VALUE));
			
			Integer nullInteger = null;
			Assert.assertEquals(null, addCommas(nullInteger));
			Assert.assertEquals("0", addCommas(0));
			Assert.assertEquals("-2,147,483,648", addCommas(Integer.MIN_VALUE));
			Assert.assertEquals("2,147,483,647", addCommas(Integer.MAX_VALUE));
			
			Long nullLong = null;
			Assert.assertEquals(null, addCommas(nullLong));
			Assert.assertEquals("0", addCommas(0L));
			Assert.assertEquals("-9,223,372,036,854,775,808", addCommas(Long.MIN_VALUE));
			Assert.assertEquals("9,223,372,036,854,775,807", addCommas(Long.MAX_VALUE));

//			System.out.println(addCommas(Double.MAX_VALUE));
//			System.out.println(addCommas(1234567890.1234567890));
			Assert.assertEquals("1,234,567,890.1234567", addCommas(1234567890.1234567890));//precision overflow
			Assert.assertEquals("1,234.123456789", addCommas(1234.1234567890));
//			System.out.println(addCommas(1234.1234567890123456789));
			Assert.assertEquals("1,234.123456789012", addCommas(1234.123456789012));
		}
	}
}

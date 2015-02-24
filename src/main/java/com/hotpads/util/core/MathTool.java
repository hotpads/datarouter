package com.hotpads.util.core;

import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.hotpads.util.core.collections.Pair;
import com.hotpads.util.core.collections.accumulator.DoubleAccumulator;


public class MathTool {

	public static int getLogBase2Truncated(Long input){
		Long zeroedOut = Long.highestOneBit(input);
		return Long.numberOfTrailingZeros(zeroedOut);
	}
	
	public static int getLogBase2Truncated(Integer input){
		Long zeroedOut = Long.highestOneBit(input);
		return Long.numberOfTrailingZeros(zeroedOut);
	}
	
	public static int digitsAfterDecimal(Number input){
		if(input==null || input.toString().length()<=0) return 0;
		String[] s = input.toString().split("\\.");
		if(s.length!=2) return 0;
		return s[1].length();
	}
	
	public static int positiveMod(int input, int mod){
		int javaResult = input % mod;
		if(javaResult < 0){
			return javaResult + mod;
		}else{
			return javaResult;
		}
	}

	public static Pair<Double, Double> averageAndStddev(Iterable<Double> prices) {
		double total = 0;
		int count = 0;
		for (Double p : prices) {
			total += p;
			++count;
		}
		
		double average = total / count;
		
		//calculate std dev
		//add up squares of the differences between each number and the mean
		//find the mean of that number and take the square root for the standard deviation
		List<Double> deviations = GenericsFactory.makeArrayList();
		for (Double p : prices) {
			deviations.add(p - average);
		}
		double stddevTotal = 0;
		for (Double deviation : deviations) {
			stddevTotal += deviation*deviation;
		}		
		double stddev = Math.sqrt(stddevTotal / deviations.size());
		
		return Pair.create(average, stddev);
	}
	
	public static Double median(List<Double> prices) {
		Collections.sort(prices);
		return (prices.get(prices.size()/2));
	}
	
	public static Double median(DoubleAccumulator prices) {
		prices.sort();
		return (prices.get(prices.size()/2));
	}
	
	public static class Tests {
		@Test public void testAverageAndStddev() {
			List<Double> nums = GenericsFactory.makeArrayList();
			nums.add(3.0);
			nums.add(7.0);
			nums.add(7.0);
			nums.add(19.0);
			Pair<Double,Double> pair = averageAndStddev(nums);
			Assert.assertEquals(pair.getLeft().doubleValue(), 9.0, 0.00001);
			Assert.assertEquals(pair.getRight().doubleValue(), 6.0, 0.00001);
		}
		
		@Test public void testMedian() {
			List<Double> nums = GenericsFactory.makeArrayList();
			nums.add(7.0);
			nums.add(7.5);
			nums.add(3.0);
			nums.add(7.0);
			nums.add(19.0);
			Double median = median(nums);
			Assert.assertEquals(median, 7.0, 0.00001);
		}
	}

	public static int min(int a, int b) {
		if(a<=b) return a;
		else	return b;
	}
	
	public static int max(int a, int b) {
		if(a<=b) return b;
		else	return a;
	}
	
	public static double max(List<Double> list){
		double max = Double.MIN_VALUE;
		if(CollectionTool.isEmpty(list)) return max;
		
		for(double d: list){
			if(d > max){
				max = d;
			}
		}
		return max;
	}
	
	public static double min(List<Double> list){
		double min = Double.MAX_VALUE;
		if(CollectionTool.isEmpty(list)) return min;

		for(double d: list){
			if(d < min){
				min = d;
			}
		}
		return min;
	}
	
	public static int sum(int[] numbers){
		int sum = 0;
		for(int i=0; i<numbers.length; i++){
			sum+=numbers[i];
		}
		return sum;
	}
	
	
	
/* The numerical versions are much (5x) faster, but inaccurate because of types/floats... don't use them. */	
//	private static int digitsAfterDecimal2(Float input, boolean print){
//		int c = 0;
//		long multiplier = 1;
//		while((input*multiplier)%1!=0){
//			c++;
//			if(print) System.err.println(input+"   "+multiplier+"   "+c+"   "+input*multiplier+"   "+(input*multiplier)%1);
//			multiplier*=10;
//		}
//		return c;
//	}
//
//	private static int digitsAfterDecimalR(Float input){
//		if(input%1f==0f) return 0;
//		return 1+digitsAfterDecimalR(input*10f);
//		
//	}
//	
//	private static float[] makeTestNumbers(){
//		java.util.Random r = new java.util.Random();
//		float[] ns = new float[100];
//		for(int i=0;i<ns.length;i++){
//			ns[i] = r.nextFloat();
//		}
//		return ns;
//	}
//	
//	public static void main(String[] args){
//		float[] testnums = makeTestNumbers();
//		long a = System.currentTimeMillis();
//		long totDigits = 0L;
//		for(int i=0;i<testnums.length;i++){
//			totDigits+= digitsAfterDecimal(testnums[i]);
//		}
//		long b = System.currentTimeMillis();
//		//System.err.println("string("+totDigits+") took "+(b-a)+"ms for an average of "+((b-a)/testnums.length));
//		for(int j=0;j<10;j++){
//			totDigits=0L;
//			a = System.currentTimeMillis();
//			for(int i=0;i<testnums.length;i++){
//				totDigits+= digitsAfterDecimal(testnums[i]);
//			}
//			b = System.currentTimeMillis();
//			System.err.println("string("+totDigits+") took "+(b-a)+"ms");
//			totDigits=0L;
//			a = System.currentTimeMillis();
//			for(int i=0;i<testnums.length;i++){
//				totDigits+= digitsAfterDecimal2(testnums[i],false);
//			}
//			b = System.currentTimeMillis();
//			System.err.println("number("+totDigits+") took "+(b-a)+"ms");
//			totDigits=0L;
//			a = System.currentTimeMillis();
//			for(int i=0;i<testnums.length;i++){
//				totDigits+= digitsAfterDecimalR(testnums[i]);
//			}
//			b = System.currentTimeMillis();
//			System.err.println("recursive("+totDigits+") took "+(b-a)+"ms");
//		}
//		for(int i=0;i<testnums.length;i++){
//			if(digitsAfterDecimal(testnums[i])!=digitsAfterDecimal2(testnums[i],false)){
//				digitsAfterDecimal2(testnums[i],true);
//				System.err.println("str: "+digitsAfterDecimal(testnums[i])+" num:"+digitsAfterDecimal2(testnums[i],false)+" rec:"+digitsAfterDecimalR(testnums[i]));
//			}
//		}
//	}
	
}

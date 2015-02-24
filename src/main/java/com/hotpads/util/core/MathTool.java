package com.hotpads.util.core;

import com.hotpads.util.core.collections.accumulator.DoubleAccumulator;


public class MathTool {

	public static int min(int a, int b) {
		if(a<=b) return a;
		else	return b;
	}
	
	public static Double median(DoubleAccumulator prices) {
		prices.sort();
		return (prices.get(prices.size()/2));
	}
}

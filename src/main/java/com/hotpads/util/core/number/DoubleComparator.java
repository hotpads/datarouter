package com.hotpads.util.core.number;

import java.util.Comparator;

import org.junit.Assert;
import org.junit.Test;

public class DoubleComparator implements Comparator<Double>{

	public static final double ACCEPTABLE_ERROR = 0.000000000001;
	private double acceptableError = ACCEPTABLE_ERROR;
	
	public DoubleComparator(double acceptableError){
		this.acceptableError = acceptableError;
	}
	public DoubleComparator(){}
	
	@Override
	public int compare(Double d1, Double d2) {
		if(Math.abs(d1-d2) < acceptableError) return 0;
		return d1.compareTo(d2);
	}
	
	/** Tests *****************************************************************/
	public static class Tests {
		@Test public void testCompare(){
			DoubleComparator c = new DoubleComparator();

			Assert.assertEquals(0,c.compare(1d,1d));
			Assert.assertEquals(0,c.compare(.1d,.1d));
			Assert.assertEquals(0,c.compare(.001d,.001d));
			Assert.assertEquals(0,c.compare(.00001d,.00001d));
			
			Assert.assertEquals(0,c.compare(150d+0d-149.48d, .52d));
			Assert.assertEquals(0,c.compare(0.5200000000000102d,0.52d));
			Assert.assertTrue(0 < c.compare(0.5200001000000102d,0.52d));
			Assert.assertTrue(0 > c.compare(0.52d,0.5200001000000102d));
		}
		
		@Test public void testCompareWithCustomError(){
			DoubleComparator c = new DoubleComparator(.1);
			Assert.assertEquals(0, c.compare(1.09d, 1.0d));
			Assert.assertEquals(0, c.compare(1.0d, 1.09d));
			Assert.assertEquals(0, c.compare(1.0d, 1.0d));
			Assert.assertEquals(-1, c.compare(1.0d, 1.1d));
			Assert.assertEquals(1, c.compare(1.1d, 1.0d));
		}
		
		@Test public void testCompareNanInf(){
			DoubleComparator c = new DoubleComparator();
			Assert.assertEquals(0, c.compare(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY));
			Assert.assertEquals(0, c.compare(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY));
			Assert.assertTrue(0 < c.compare(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY));
			Assert.assertTrue(0 > c.compare(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY));

			Assert.assertEquals(0, c.compare(Double.NaN, Double.NaN));
		}
	}

}

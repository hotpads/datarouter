package com.hotpads.datarouter.batch.math;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.testng.Assert;
import org.testng.annotations.Test;

// http://en.wikipedia.org/wiki/Polynomial_regression
// notation sjk to mean the sum of x_i^j*y_i^k.
public class PolynomialRegressionOptimumFinder{

	private boolean optimumIsMaximum;
	private double optimumAbscissa;

	public PolynomialRegressionOptimumFinder(List<PolynomialRegressionOptimumFinderPoint> points){
		double s00 = points.size();
		double s40 = 0;
		double s30 = 0;
		double s20 = 0;
		double s10 = 0;
		double s21 = 0;
		double s11 = 0;
		double s01 = 0;
		for (PolynomialRegressionOptimumFinderPoint point : points){
			s40 += Math.pow(point.getAbscissa(), 4);
			s30 += Math.pow(point.getAbscissa(), 3);
			s20 += Math.pow(point.getAbscissa(), 2);
			s10 += point.getAbscissa();
			s21 += Math.pow(point.getAbscissa(), 2) * point.getOrdinate();
			s11 += point.getAbscissa() * point.getOrdinate();
			s01 += point.getOrdinate();
		}
		double deno = s40 * (s20 * s00 - s10 * s10) - s30 * (s30 * s00 - s10 * s20) + s20 * (s30 * s10 - s20 * s20);
		double beta2 = (s40 * (s11 * s00 - s01 * s10) - s30 * (s21 * s00 - s01 * s20) + s20 * (s21 * s10 - s11 * s20))
				/ deno;
		double beta1 = (s21 * (s20 * s00 - s10 * s10) - s11 * (s30 * s00 - s10 * s20) + s01 * (s30 * s10 - s20 * s20))
				/ deno;
		this.optimumAbscissa = -beta2 / (2 * beta1);
		this.optimumIsMaximum = beta1 < 0;
	}

	public double getOptimumAbscissa(){
		return optimumAbscissa;
	}

	public boolean optimumIsMaximum(){
		return optimumIsMaximum;
	}

	public static class PolynomialRegressionOptimumFinderTests{
		@Test
		public void testMaximum(){
			List<PolynomialRegressionOptimumFinderPoint> points = createDataPoints(new int[][]{{-1,0}, {0,1}, {1,0}});
			PolynomialRegressionOptimumFinder optimumFinder = new PolynomialRegressionOptimumFinder(points);
			Assert.assertTrue(optimumFinder.optimumIsMaximum());
			Assert.assertEquals(optimumFinder.getOptimumAbscissa(), 0.0);
		}

		@Test
		public void testMimimum(){
			List<PolynomialRegressionOptimumFinderPoint> points = createDataPoints(new int[][]{{0,1}, {1,-5}, {2,1}});
			PolynomialRegressionOptimumFinder optimumFinder = new PolynomialRegressionOptimumFinder(points);
			Assert.assertFalse(optimumFinder.optimumIsMaximum());
			Assert.assertEquals(optimumFinder.getOptimumAbscissa(), 1.0);
		}

		private static List<PolynomialRegressionOptimumFinderPoint> createDataPoints(int[][] points){
			return Arrays.stream(points)
					.map(point -> new PolynomialRegressionOptimumFinderPoint(point[0], point[1]))
					.collect(Collectors.toList());
		}
	}
}

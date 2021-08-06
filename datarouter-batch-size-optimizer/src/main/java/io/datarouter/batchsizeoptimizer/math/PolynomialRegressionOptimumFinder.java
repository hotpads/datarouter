/*
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.batchsizeoptimizer.math;

import java.util.List;

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
		for(PolynomialRegressionOptimumFinderPoint point : points){
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

}

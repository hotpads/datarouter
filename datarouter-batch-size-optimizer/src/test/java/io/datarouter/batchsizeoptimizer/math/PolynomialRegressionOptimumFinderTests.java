/**
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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.testng.Assert;
import org.testng.annotations.Test;

public class PolynomialRegressionOptimumFinderTests{

	@Test
	public void testMaximum(){
		List<PolynomialRegressionOptimumFinderPoint> points = createDataPoints(new int[][]{{-1, 0}, {0, 1}, {1, 0}});
		PolynomialRegressionOptimumFinder optimumFinder = new PolynomialRegressionOptimumFinder(points);
		Assert.assertTrue(optimumFinder.optimumIsMaximum());
		Assert.assertEquals(optimumFinder.getOptimumAbscissa(), 0.0);
	}

	@Test
	public void testMimimum(){
		List<PolynomialRegressionOptimumFinderPoint> points = createDataPoints(new int[][]{{0, 1}, {1, -5}, {2, 1}});
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

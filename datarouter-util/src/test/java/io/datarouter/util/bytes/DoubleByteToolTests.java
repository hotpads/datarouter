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
package io.datarouter.util.bytes;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.scanner.Scanner;
import io.datarouter.util.Java9;

public class DoubleByteToolTests{

	@Test
	public void testComparableBytes(){
		List<Double> interestingDoubles = Scanner.of(
				Double.NEGATIVE_INFINITY,
				-Double.MAX_VALUE,
				-Double.MIN_NORMAL,
				-Double.MIN_VALUE,
				-0D,
				+0D,
				Double.MIN_VALUE,
				Double.MIN_NORMAL,
				Double.MAX_VALUE,
				Double.POSITIVE_INFINITY,
				Double.NaN)
				.sort()
				.list();
		List<Double> roundTripped = interestingDoubles.stream()
				.map(DoubleByteTool::toComparableBytes)
				.sorted(Java9::compareUnsigned)
				.map(bytes -> DoubleByteTool.fromComparableBytes(bytes, 0))
				.collect(Collectors.toList());
		Assert.assertEquals(roundTripped, interestingDoubles);
	}

	@Test
	public void testBytes1(){
		double valueA = 12354234.456D;
		byte[] abytes = DoubleByteTool.getBytes(valueA);
		double aback = DoubleByteTool.fromBytes(abytes, 0);
		Assert.assertTrue(valueA == aback);

		double valueB = -1234568.456D;
		byte[] bbytes = DoubleByteTool.getBytes(valueB);
		double bback = DoubleByteTool.fromBytes(bbytes, 0);
		Assert.assertTrue(valueB == bback);

		Assert.assertTrue(Java9.compareUnsigned(abytes, bbytes) < 0);//positives and negatives are reversed
	}

	@Test
	public void testToFromByteArray(){
		double one = 2.39483;
		double two = -583.2039;
		double three = 5;
		double four = -.0000001;

		List<Double> doubles = new ArrayList<>();
		doubles.add(one);
		doubles.add(two);
		doubles.add(null);
		doubles.add(three);
		doubles.add(four);

		byte[] doubleBytes = DoubleByteTool.getDoubleByteArray(doubles);
		List<Double> result = DoubleByteTool.fromDoubleByteArray(doubleBytes, 0);
		Assert.assertEquals(result, doubles);
	}

}

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
package io.datarouter.util.bytes;

import java.util.List;
import java.util.stream.Collectors;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.scanner.Scanner;
import io.datarouter.util.Java9;

public class FloatByteToolTests{

	@Test
	public void testComparableBytes(){
		List<Float> interestingFloats = Scanner.of(
				Float.NEGATIVE_INFINITY,
				-Float.MAX_VALUE,
				-Float.MIN_NORMAL,
				-Float.MIN_VALUE,
				-0F,
				+0F,
				Float.MIN_VALUE,
				Float.MIN_NORMAL,
				Float.MAX_VALUE,
				Float.POSITIVE_INFINITY,
				Float.NaN)
				.sort()
				.list();
		List<Float> roundTripped = interestingFloats.stream()
				.map(FloatByteTool::toComparableBytes)
				.sorted(Java9::compareUnsigned)
				.map(bytes -> FloatByteTool.fromComparableBytes(bytes, 0))
				.collect(Collectors.toList());
		Assert.assertEquals(roundTripped, interestingFloats);
	}

	@Test
	public void testBytes1(){
		float floatA = 123.456f;
		byte[] bytesA = FloatByteTool.getBytes(floatA);
		float backA = FloatByteTool.fromBytes(bytesA, 0);
		Assert.assertTrue(floatA == backA);

		float floatB = -123.456f;
		byte[] bytesB = FloatByteTool.getBytes(floatB);
		float backB = FloatByteTool.fromBytes(bytesB, 0);
		Assert.assertTrue(floatB == backB);

		Assert.assertTrue(Java9.compareUnsigned(bytesA, bytesB) < 0); //positives and negatives are reversed
	}

}

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
package io.datarouter.bytes.codec.doublecodec;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.bytes.Java9;

public class ComparableDoubleCodecTests{

	private static final ComparableDoubleCodec CODEC = ComparableDoubleCodec.INSTANCE;

	@Test
	public void testPositiveAndNegative(){
		Assert.assertEquals(roundTrip(5.5D), 5.5D);
		Assert.assertEquals(roundTrip(-5.5D), -5.5D);
	}

	@Test
	public void testCompare(){
		Assert.assertTrue(Java9.compareUnsigned(CODEC.encode(-3D), CODEC.encode(3D)) < 0);
	}

	@Test
	public void testComparableBytes(){
		List<Double> interestingDoubles = Stream.of(
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
				.sorted()
				.collect(Collectors.toList());
		List<Double> roundTripped = interestingDoubles.stream()
				.map(CODEC::encode)
				.sorted(Java9::compareUnsigned)
				.map(CODEC::decode)
				.collect(Collectors.toList());
		Assert.assertEquals(roundTripped, interestingDoubles);
	}

	private static double roundTrip(double value){
		byte[] bytes = CODEC.encode(value);
		return CODEC.decode(bytes);
	}

}

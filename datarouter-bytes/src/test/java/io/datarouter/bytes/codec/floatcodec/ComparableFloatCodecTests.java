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
package io.datarouter.bytes.codec.floatcodec;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.bytes.Java9;

public class ComparableFloatCodecTests{

	private static final ComparableFloatCodec CODEC = ComparableFloatCodec.INSTANCE;

	@Test
	public void testPositiveAndNegative(){
		Assert.assertEquals(roundTrip(5.5F), 5.5F);
		Assert.assertEquals(roundTrip(-5.5F), -5.5F);
	}

	@Test
	public void testCompare(){
		Assert.assertTrue(Java9.compareUnsigned(CODEC.encode(-3F), CODEC.encode(3F)) < 0);
	}

	@Test
	public void testSorting(){
		List<Float> interestingFloats = Stream.of(
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
				.sorted()
				.collect(Collectors.toList());
		List<Float> roundTripped = interestingFloats.stream()
				.map(CODEC::encode)
				.sorted(Java9::compareUnsigned)
				.map(CODEC::decode)
				.collect(Collectors.toList());
		Assert.assertEquals(roundTripped, interestingFloats);
	}

	private static float roundTrip(float value){
		byte[] bytes = CODEC.encode(value);
		return CODEC.decode(bytes);
	}

}

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

import java.util.Arrays;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.scanner.Scanner;

public class ComparableFloatCodecTests{

	private static final ComparableFloatCodec CODEC = ComparableFloatCodec.INSTANCE;

	@Test
	public void testPositiveAndNegative(){
		Assert.assertEquals(roundTrip(5.5F), 5.5F);
		Assert.assertEquals(roundTrip(-5.5F), -5.5F);
	}

	@Test
	public void testCompare(){
		Assert.assertTrue(Arrays.compareUnsigned(CODEC.encode(-3F), CODEC.encode(3F)) < 0);
	}

	@Test
	public void testSorting(){
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
		List<Float> roundTripped = Scanner.of(interestingFloats)
				.map(CODEC::encode)
				.sort(Arrays::compareUnsigned)
				.map(CODEC::decode)
				.list();
		Assert.assertEquals(roundTripped, interestingFloats);
	}

	private static float roundTrip(float value){
		byte[] bytes = CODEC.encode(value);
		return CODEC.decode(bytes);
	}

}

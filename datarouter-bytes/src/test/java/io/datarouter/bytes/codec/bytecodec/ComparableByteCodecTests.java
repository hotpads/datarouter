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
package io.datarouter.bytes.codec.bytecodec;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.bytes.Java9;

public class ComparableByteCodecTests{

	private static final ComparableByteCodec COMPARABLE_BYTE_CODEC = ComparableByteCodec.INSTANCE;

	@Test
	public void testGetComparableBytes(){
		byte min = Byte.MIN_VALUE;
		Assert.assertEquals(min, -128);
		byte max = Byte.MAX_VALUE;
		Assert.assertEquals(max, 127);
		Assert.assertTrue(min < max);

		byte[] minArray = COMPARABLE_BYTE_CODEC.encode(min);
		byte[] maxArray = COMPARABLE_BYTE_CODEC.encode(max);
		Assert.assertTrue(Java9.compareUnsigned(maxArray, minArray) > 0);

		byte negative = -3;
		byte positive = 5;
		Assert.assertTrue(negative < positive);

		byte[] negativeArray = COMPARABLE_BYTE_CODEC.encode(negative);
		byte[] positiveArray = COMPARABLE_BYTE_CODEC.encode(positive);
		Assert.assertTrue(Java9.compareUnsigned(positiveArray, negativeArray) > 0);
	}

}

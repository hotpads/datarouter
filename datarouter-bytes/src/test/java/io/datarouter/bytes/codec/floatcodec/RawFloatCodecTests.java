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

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.bytes.Java9;

public class RawFloatCodecTests{

	private static final RawFloatCodec CODEC = RawFloatCodec.INSTANCE;

	@Test
	public void testRoundTrip(){
		float floatA = 123.456f;
		byte[] bytesA = CODEC.encode(floatA);
		float backA = CODEC.decode(bytesA, 0);
		Assert.assertTrue(floatA == backA);

		float floatB = -123.456f;
		byte[] bytesB = CODEC.encode(floatB);
		float backB = CODEC.decode(bytesB, 0);
		Assert.assertTrue(floatB == backB);

		Assert.assertTrue(Java9.compareUnsigned(bytesA, bytesB) < 0); //positives and negatives are reversed
	}

	@Test
	public void testPositiveAndNegative(){
		Assert.assertEquals(roundTrip(5.5F), 5.5F);
		Assert.assertEquals(roundTrip(-5.5F), -5.5F);
	}

	private static float roundTrip(float value){
		byte[] bytes = CODEC.encode(value);
		return CODEC.decode(bytes);
	}

}

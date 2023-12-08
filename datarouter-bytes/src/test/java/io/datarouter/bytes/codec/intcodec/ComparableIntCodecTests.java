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
package io.datarouter.bytes.codec.intcodec;

import java.util.Arrays;
import java.util.Random;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ComparableIntCodecTests{

	private static final ComparableIntCodec CODEC = new ComparableIntCodec();

	@Test
	public void testRoundTrip(){
		int[] subjects = {
				Integer.MIN_VALUE, Integer.MIN_VALUE + 1,
				0, 1, 127, 128,
				Integer.MAX_VALUE - 1, Integer.MAX_VALUE};
		for(int subject : subjects){
			byte[] bytes = CODEC.encode(subject);
			int roundTripped = CODEC.decode(bytes);
			Assert.assertEquals(roundTripped, subject);
		}
	}

	@Test
	public void testRandomRoundTrips(){
		var random = new Random();
		int intValue = Integer.MIN_VALUE;
		byte[] lastBytes = CODEC.encode(intValue);
		++intValue;
		int counter = 0;
		for(; intValue < Integer.MAX_VALUE / 2; intValue += 1 + Math.abs(random.nextInt() % 53 * 47 * 991)){
			byte[] bytes = CODEC.encode(intValue);
			int roundTripped = CODEC.decode(bytes);
			Assert.assertTrue(Arrays.compareUnsigned(lastBytes, bytes) < 0);
			Assert.assertEquals(roundTripped, intValue);
			lastBytes = bytes;
			++counter;
		}
		Assert.assertTrue(counter > 1000);//make sure we did a lot of tests
	}

	@Test
	public void testExplicitValues(){
		int intA = Integer.MIN_VALUE;
		byte[] ab = {0, 0, 0, 0};
		Assert.assertEquals(CODEC.encode(intA), ab);

		int intB = Integer.MAX_VALUE;
		byte[] bb = {-1, -1, -1, -1};
		Assert.assertEquals(CODEC.encode(intB), bb);

		int intC = Integer.MIN_VALUE + 1;
		byte[] cb = {0, 0, 0, 1};
		byte[] cout = CODEC.encode(intC);
		Assert.assertEquals(cout, cb);

		int intD = Integer.MAX_VALUE - 3;
		byte[] db = {-1, -1, -1, -4};
		Assert.assertEquals(CODEC.encode(intD), db);

		int intE = 0;
		byte[] eb = {-128, 0, 0, 0};
		Assert.assertEquals(CODEC.encode(intE), eb);
	}

	@Test
	public void testCompare(){
		byte[] p5 = CODEC.encode(5);
		byte[] n3 = CODEC.encode(-3);
		byte[] n7 = CODEC.encode(-7);
		Assert.assertTrue(Arrays.compareUnsigned(p5, n3) > 0);
		Assert.assertTrue(Arrays.compareUnsigned(p5, n7) > 0);
	}

}

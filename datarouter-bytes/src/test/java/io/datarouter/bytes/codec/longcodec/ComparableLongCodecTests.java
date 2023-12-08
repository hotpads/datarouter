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
package io.datarouter.bytes.codec.longcodec;

import java.util.Arrays;
import java.util.Random;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ComparableLongCodecTests{

	private static final ComparableLongCodec CODEC = ComparableLongCodec.INSTANCE;

	@Test
	public void testGetOrderedBytes(){
		long longA = Long.MIN_VALUE;
		byte[] ab = {0, 0, 0, 0, 0, 0, 0, 0};
		Assert.assertEquals(CODEC.encode(longA), ab);

		long longB = Long.MAX_VALUE;
		byte[] bb = {-1, -1, -1, -1, -1, -1, -1, -1};
		Assert.assertEquals(CODEC.encode(longB), bb);

		long longC = Long.MIN_VALUE + 1;
		byte[] cb = {0, 0, 0, 0, 0, 0, 0, 1};
		Assert.assertEquals(CODEC.encode(longC), cb);

		long longD = Long.MAX_VALUE - 3;
		byte[] db = {-1, -1, -1, -1, -1, -1, -1, -4};
		Assert.assertEquals(CODEC.encode(longD), db);

		long longE = 127;
		byte[] eb = {-128, 0, 0, 0, 0, 0, 0, 127};
		Assert.assertEquals(CODEC.encode(longE), eb);

		long longF = 128;
		byte[] fb = {-128, 0, 0, 0, 0, 0, 0, -128};
		Assert.assertEquals(CODEC.encode(longF), fb);

		long longG = -128;
		byte[] gb = {127, -1, -1, -1, -1, -1, -1, -128};
		Assert.assertEquals(CODEC.encode(longG), gb);
	}

	@Test
	public void testFromOrderedBytes(){
		long longA = Long.MIN_VALUE;
		byte[] ab = {0, 0, 0, 0, 0, 0, 0, 0};
		Assert.assertEquals(CODEC.decode(ab), longA);

		long longB = Long.MAX_VALUE;
		byte[] bb = {-1, -1, -1, -1, -1, -1, -1, -1};
		Assert.assertEquals(CODEC.decode(bb), longB);

		long longC = Long.MIN_VALUE + 1;
		byte[] cb = {0, 0, 0, 0, 0, 0, 0, 1};
		Assert.assertEquals(CODEC.decode(cb), longC);

		long longD = Long.MAX_VALUE - 3;
		byte[] db = {-1, -1, -1, -1, -1, -1, -1, -4};
		Assert.assertEquals(CODEC.decode(db), longD);

		long longE = 3;
		byte[] eb = {-128, 0, 0, 0, 0, 0, 0, 3};
		Assert.assertEquals(CODEC.decode(eb), longE);
	}

	@Test
	public void testRoundTrip(){
		long[] subjects = {
				Long.MIN_VALUE, Long.MIN_VALUE + 1,
				0, 1, 127, 128,
				Long.MAX_VALUE - 1, Long.MAX_VALUE,
				-9223372036845049055L};
		for(long subject : subjects){
			byte[] bytes = CODEC.encode(subject);
			long roundTripped = CODEC.decode(bytes);
			Assert.assertEquals(roundTripped, subject);
		}
	}

	@Test
	public void testRoundTrips(){
		var random = new Random();
		long value = Long.MIN_VALUE;
		byte[] lastBytes = CODEC.encode(value);
		long lastValue = value;
		++value;
		int counter = 0;
		long stopAt = Long.MAX_VALUE - 2 * (long)Integer.MAX_VALUE;
		Assert.assertTrue(stopAt > Integer.MAX_VALUE);
		do{
			if(counter < 1000){
				Assert.assertTrue(value < 0);
			}
			byte[] bytes = CODEC.encode(value);
			long roundTripped = CODEC.decode(bytes);
			Assert.assertTrue(value > lastValue);
			Assert.assertTrue(Arrays.compareUnsigned(lastBytes, bytes) < 0);
			Assert.assertEquals(roundTripped, value);
			lastBytes = bytes;
			++counter;
			lastValue = value;
			long incrementor = random.nextLong() >>> 18;
			value = value + incrementor;
		}while(value < stopAt && value > lastValue);// watch out for overflowing and going back negative
		Assert.assertTrue(counter > 1000);//make sure we did a lot of tests
	}

}

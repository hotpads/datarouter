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
package io.datarouter.bytes.codec.shortcodec;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.bytes.Java9;

public class ComparableShortCodecTests{

	private static final ComparableShortCodec CODEC = new ComparableShortCodec();

	@Test
	public void testGetOrderedBytes(){
		short shortA = Short.MIN_VALUE;
		byte[] ab = new byte[]{0, 0};
		Assert.assertEquals(CODEC.encode(shortA), ab);
		byte[] ac = new byte[]{5, 5};//5's are just filler
		CODEC.encode(shortA, ac, 0);
		Assert.assertEquals(ac, ab);

		short shortB = Short.MAX_VALUE;
		byte[] bb = new byte[]{-1, -1};
		Assert.assertEquals(CODEC.encode(shortB), bb);
		byte[] bc = new byte[]{5, 5};
		CODEC.encode(shortB, bc, 0);
		Assert.assertEquals(bc, bb);

		short shortC = Short.MIN_VALUE + 1;
		byte[] cb = new byte[]{0, 1};
		Assert.assertEquals(CODEC.encode(shortC), cb);
		byte[] cc = new byte[]{5, 5};
		CODEC.encode(shortC, cc, 0);
		Assert.assertEquals(cc, cb);

		short shortD = Short.MAX_VALUE - 3;
		byte[] db = new byte[]{-1, -4};
		Assert.assertEquals(CODEC.encode(shortD), db);
		byte[] dc = new byte[]{5, 5};
		CODEC.encode(shortD, dc, 0);
		Assert.assertEquals(dc, db);

		short shortZ = 0;
		byte[] zb = new byte[]{Byte.MIN_VALUE,0};
		Assert.assertEquals(CODEC.encode(shortZ), zb);
		byte[] zc = new byte[]{5, 5};
		CODEC.encode(shortZ, zc, 0);
		Assert.assertEquals(zc, zb);
	}

	@Test
	public void testCompare(){
		byte[] p5 = CODEC.encode((short)5);
		byte[] n3 = CODEC.encode((short)-3);
		byte[] n7 = CODEC.encode((short)-7);
		Assert.assertTrue(Java9.compareUnsigned(p5, n3) > 0);
		Assert.assertTrue(Java9.compareUnsigned(p5, n7) > 0);
	}

	@Test
	public void testRoundTrip(){
		short[] subjects = new short[]{
				Short.MIN_VALUE, Short.MIN_VALUE + 1,
				0, 1, 127, 128,
				Short.MAX_VALUE - 1, Short.MAX_VALUE};
		for(short subject : subjects){
			byte[] bytes = CODEC.encode(subject);
			int roundTripped = CODEC.decode(bytes);
			Assert.assertEquals(roundTripped, subject);
		}
	}

	@Test
	public void testRoundTrips(){
		short shortValue = Short.MIN_VALUE;
		byte[] lastBytes = CODEC.encode(shortValue);
		++shortValue;
		int counter = 0;
		for(; shortValue < Short.MAX_VALUE; shortValue += 1){
			byte[] bytes = CODEC.encode(shortValue);
			short roundTripped = CODEC.decode(bytes);
			try{
				Assert.assertTrue(Java9.compareUnsigned(lastBytes, bytes) < 0);
				Assert.assertEquals(roundTripped, shortValue);
			}catch(AssertionError e){
				throw e;
			}
			lastBytes = bytes;
			++counter;
		}
		Assert.assertTrue(counter > 1000);//make sure we did a lot of tests
	}

}

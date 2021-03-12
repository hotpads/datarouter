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

import java.util.Random;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.util.Java9;

public class LongByteToolTests{

	@Test
	public void testBuildingLong(){
		long longValue = 0;
		longValue |= (byte)-2;
		Assert.assertEquals(longValue, -2);
		// l = (l << 8) + (-2+128);
	}

	@Test
	public void testGetOrderedBytes(){
		long longA = Long.MIN_VALUE;
		byte[] ab = new byte[]{0,0,0,0,0,0,0,0};
		Assert.assertEquals(LongByteTool.getComparableBytes(longA), ab);

		long longB = Long.MAX_VALUE;
		byte[] bb = new byte[]{-1,-1,-1,-1,-1,-1,-1,-1};
		Assert.assertEquals(LongByteTool.getComparableBytes(longB), bb);

		long longC = Long.MIN_VALUE + 1;
		byte[] cb = new byte[]{0,0,0,0,0,0,0,1};
		Assert.assertEquals(LongByteTool.getComparableBytes(longC), cb);

		long longD = Long.MAX_VALUE - 3;
		byte[] db = new byte[]{-1,-1,-1,-1,-1,-1,-1,-4};
		Assert.assertEquals(LongByteTool.getComparableBytes(longD), db);

		long longE = 127;
		byte[] eb = new byte[]{-128,0,0,0,0,0,0,127};
		Assert.assertEquals(LongByteTool.getComparableBytes(longE), eb);

		long longF = 128;
		byte[] fb = new byte[]{-128,0,0,0,0,0,0,-128};
		Assert.assertEquals(LongByteTool.getComparableBytes(longF), fb);

		long longG = -128;
		byte[] gb = new byte[]{127,-1,-1,-1,-1,-1,-1,-128};
		Assert.assertEquals(LongByteTool.getComparableBytes(longG), gb);
	}

	@Test
	public void testFromOrderedBytes(){
		long longA = Long.MIN_VALUE;
		byte[] ab = new byte[]{0,0,0,0,0,0,0,0};
		Assert.assertEquals(LongByteTool.fromComparableByteArray(ab)[0], longA);

		long longB = Long.MAX_VALUE;
		byte[] bb = new byte[]{-1,-1,-1,-1,-1,-1,-1,-1};
		Assert.assertEquals(LongByteTool.fromComparableByteArray(bb)[0], longB);

		long longC = Long.MIN_VALUE + 1;
		byte[] cb = new byte[]{0,0,0,0,0,0,0,1};
		Assert.assertEquals(LongByteTool.fromComparableByteArray(cb)[0], longC);

		long longD = Long.MAX_VALUE - 3;
		byte[] db = new byte[]{-1,-1,-1,-1,-1,-1,-1,-4};
		Assert.assertEquals(LongByteTool.fromComparableByteArray(db)[0], longD);

		long longE = 3;
		byte[] eb = new byte[]{-128,0,0,0,0,0,0,3};
		Assert.assertEquals(LongByteTool.fromComparableByteArray(eb)[0], longE);
	}

	@Test
	public void testRoundTrip(){
		long[] subjects = new long[]{
				Long.MIN_VALUE,Long.MIN_VALUE + 1,
				0,1,127,128,
				Long.MAX_VALUE - 1,Long.MAX_VALUE,
				-9223372036845049055L};
		for(long subject : subjects){
			byte[] bytes = LongByteTool.getComparableBytes(subject);
			long roundTripped = LongByteTool.fromComparableByteArray(bytes)[0];
			Assert.assertEquals(roundTripped, subject);
		}
	}

	@Test
	public void testRoundTrips(){
		Random random = new Random();
		long value = Long.MIN_VALUE;
		byte[] lastBytes = LongByteTool.getComparableBytes(value);
		long lastValue = value;
		++value;
		int counter = 0;
		long stopAt = Long.MAX_VALUE - 2 * (long)Integer.MAX_VALUE;
		Assert.assertTrue(stopAt > Integer.MAX_VALUE);
		do{
			if(counter < 1000){
				Assert.assertTrue(value < 0);
			}
			byte[] bytes = LongByteTool.getComparableBytes(value);
			long roundTripped = LongByteTool.fromComparableByteArray(bytes)[0];
			try{
				Assert.assertTrue(value > lastValue);
				Assert.assertTrue(Java9.compareUnsigned(lastBytes, bytes) < 0);
				Assert.assertEquals(roundTripped, value);
			}catch(AssertionError e){
				throw e;
			}
			lastBytes = bytes;
			++counter;
			lastValue = value;
			long incrementor = random.nextLong() >>> 18;
			value = value + incrementor;
		}while(value < stopAt && value > lastValue);// watch out for overflowing and going back negative
		Assert.assertTrue(counter > 1000);//make sure we did a lot of tests
	}

}

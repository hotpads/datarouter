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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.util.Java9;

public class IntegerByteToolTests{

	//verify that -128 in bytes gets converted to -128 long.  Bitwise cast would be +128
	@Test
	public void testCasting(){
		byte b0 = 0, b1 = 1, b127 = 127, bn128 = -128, bn1 = -1;
		Assert.assertEquals(b0, 0L);
		Assert.assertEquals(b1, 1L);
		Assert.assertEquals(b127, 127L);
		Assert.assertEquals(bn128, -128L);
		Assert.assertEquals(bn1, -1L);
	}

	@Test
	public void testGetOrderedBytes(){
		int intA = Integer.MIN_VALUE;
		byte[] ab = new byte[]{0,0,0,0};
		Assert.assertEquals(IntegerByteTool.getComparableBytes(intA), ab);

		int intB = Integer.MAX_VALUE;
		byte[] bb = new byte[]{-1,-1,-1,-1};
		Assert.assertEquals(IntegerByteTool.getComparableBytes(intB), bb);

		int intC = Integer.MIN_VALUE + 1;
		byte[] cb = new byte[]{0,0,0,1};
		byte[] cout = IntegerByteTool.getComparableBytes(intC);
		Assert.assertEquals(cout, cb);

		int intD = Integer.MAX_VALUE - 3;
		byte[] db = new byte[]{-1,-1,-1,-4};
		Assert.assertEquals(IntegerByteTool.getComparableBytes(intD), db);

		int intE = 0;
		byte[] eb = new byte[]{-128,0,0,0};
		Assert.assertEquals(IntegerByteTool.getComparableBytes(intE), eb);
	}

	@Test
	public void testArrays(){
		byte[] p5 = IntegerByteTool.getComparableBytes(5);
		byte[] n3 = IntegerByteTool.getComparableBytes(-3);
		byte[] n7 = IntegerByteTool.getComparableBytes(-7);
		Assert.assertTrue(Java9.compareUnsigned(p5, n3) > 0);
		Assert.assertTrue(Java9.compareUnsigned(p5, n7) > 0);
	}

	@Test
	public void testRoundTrip(){
		int[] subjects = new int[]{
				Integer.MIN_VALUE,Integer.MIN_VALUE + 1,
				0,1,127,128,
				Integer.MAX_VALUE - 1,Integer.MAX_VALUE};
		for(int subject : subjects){
			byte[] bytes = IntegerByteTool.getComparableBytes(subject);
			int roundTripped = IntegerByteTool.fromComparableByteArray(bytes)[0];
			Assert.assertEquals(roundTripped, subject);
		}
	}

	@Test
	public void testRoundTrips(){
		Random random = new Random();
		int intValue = Integer.MIN_VALUE;
		byte[] lastBytes = IntegerByteTool.getComparableBytes(intValue);
		++intValue;
		int counter = 0;
		for(; intValue < Integer.MAX_VALUE / 2; intValue += 1 + Math.abs(random.nextInt() % 53 * 47 * 991)){
			byte[] bytes = IntegerByteTool.getComparableBytes(intValue);
			int roundTripped = IntegerByteTool.fromComparableByteArray(bytes)[0];
			try{
				Assert.assertTrue(Java9.compareUnsigned(lastBytes, bytes) < 0);
				Assert.assertEquals(roundTripped, intValue);
			}catch(AssertionError e){
				throw e;
			}
			lastBytes = bytes;
			++counter;
		}
		Assert.assertTrue(counter > 1000);//make sure we did a lot of tests
	}

	@Test
	public void testToFromByteArray(){
		int one = -239483;
		int two = 583;

		List<Integer> integers = new ArrayList<>();
		integers.add(one);
		integers.add(null);
		integers.add(two);

		byte[] integerBytes = IntegerByteTool.getIntegerByteArray(integers);
		List<Integer> result = IntegerByteTool.fromIntegerByteArray(integerBytes, 0);
		Assert.assertEquals(result, integers);
	}

}

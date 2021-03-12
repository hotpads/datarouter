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

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.util.Java9;

public class ByteToolTests{

	@Test
	public void testGetComparableBytes(){
		byte min = Byte.MIN_VALUE;
		Assert.assertEquals(min, -128);
		byte max = Byte.MAX_VALUE;
		Assert.assertEquals(max, 127);
		Assert.assertTrue(min < max);

		byte[] minArray = ByteTool.getComparableBytes(min);
		byte[] maxArray = ByteTool.getComparableBytes(max);
		Assert.assertTrue(Java9.compareUnsigned(maxArray, minArray) > 0);

		byte negative = -3;
		byte positive = 5;
		Assert.assertTrue(negative < positive);

		byte[] negativeArray = ByteTool.getComparableBytes(negative);
		byte[] positiveArray = ByteTool.getComparableBytes(positive);
		Assert.assertTrue(Java9.compareUnsigned(positiveArray, negativeArray) > 0);
	}

	@Test
	public void testUnsignedIncrement(){
		byte[] bytesA = IntegerByteTool.getUInt31Bytes(0);
		int a2 = IntegerByteTool.fromUInt31Bytes(ByteTool.unsignedIncrement(bytesA), 0);
		Assert.assertTrue(a2 == 1);

		byte[] bytesB = IntegerByteTool.getUInt31Bytes(-1);
		byte[] actuals = ByteTool.unsignedIncrement(bytesB);
		byte[] expected = new byte[]{1, 0, 0, 0, 0};
		Assert.assertEquals(actuals, expected);

		byte[] bytesC = IntegerByteTool.getUInt31Bytes(255);// should wrap to the next significant byte
		int c2 = IntegerByteTool.fromUInt31Bytes(ByteTool.unsignedIncrement(bytesC), 0);
		Assert.assertTrue(c2 == 256);
	}

	@Test
	public void testUnsignedIncrementOverflowToNull(){
		byte[] bytesA = IntegerByteTool.getUInt31Bytes(0);
		int a2 = IntegerByteTool.fromUInt31Bytes(ByteTool.unsignedIncrementOverflowToNull(bytesA), 0);
		Assert.assertTrue(a2 == 1);

		byte[] bytesB = IntegerByteTool.getUInt31Bytes(-1);
		byte[] b2 = ByteTool.unsignedIncrementOverflowToNull(bytesB);
		Assert.assertTrue(b2 == null);

		byte[] bytesC = IntegerByteTool.getUInt31Bytes(255);// should wrap to the next significant byte
		int c2 = IntegerByteTool.fromUInt31Bytes(ByteTool.unsignedIncrementOverflowToNull(bytesC), 0);
		Assert.assertTrue(c2 == 256);
	}

	@Test
	public void testPadPrefix(){
		Assert.assertEquals(ByteTool.padPrefix(new byte[]{55, -21}, 7), new byte[]{0, 0, 0, 0, 0, 55, -21});
	}

	@Test
	public void testGetHexString(){
		byte[] textBytes = StringByteTool.getUtf8Bytes("hello world!");
		byte[] allBytes = ByteTool.concatenate(textBytes, new byte[]{0, 127, -128});
		String hexString = ByteTool.getHexString(allBytes);
		Assert.assertEquals(hexString, "68656c6c6f20776f726c6421007f80");
	}

}
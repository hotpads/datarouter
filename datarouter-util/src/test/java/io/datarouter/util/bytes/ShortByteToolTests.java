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

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.util.Java9;
import io.datarouter.util.array.ArrayTool;

public class ShortByteToolTests{

	@Test
	public void testGetOrderedBytes(){
		short shortA = Short.MIN_VALUE;
		byte[] ab = new byte[]{0,0};
		Assert.assertEquals(ShortByteTool.getComparableBytes(shortA), ab);
		byte[] ac = new byte[]{5,5};//5's are just filler
		ShortByteTool.toComparableBytes(shortA, ac, 0);
		Assert.assertEquals(ac, ab);

		short shortB = Short.MAX_VALUE;
		byte[] bb = new byte[]{-1,-1};
		Assert.assertEquals(ShortByteTool.getComparableBytes(shortB), bb);
		byte[] bc = new byte[]{5,5};
		ShortByteTool.toComparableBytes(shortB, bc, 0);
		Assert.assertEquals(bc, bb);

		short shortC = Short.MIN_VALUE + 1;
		byte[] cb = new byte[]{0,1};
		Assert.assertEquals(ShortByteTool.getComparableBytes(shortC), cb);
		byte[] cc = new byte[]{5,5};
		ShortByteTool.toComparableBytes(shortC, cc, 0);
		Assert.assertEquals(cc, cb);

		short shortD = Short.MAX_VALUE - 3;
		byte[] db = new byte[]{-1,-4};
		Assert.assertEquals(ShortByteTool.getComparableBytes(shortD), db);
		byte[] dc = new byte[]{5,5};
		ShortByteTool.toComparableBytes(shortD, dc, 0);
		Assert.assertEquals(dc, db);

		short shortZ = 0;
		byte[] zb = new byte[]{Byte.MIN_VALUE,0};
		Assert.assertEquals(ShortByteTool.getComparableBytes(shortZ), zb);
		byte[] zc = new byte[]{5,5};
		ShortByteTool.toComparableBytes(shortZ, zc, 0);
		Assert.assertEquals(zc, zb);
	}

	@Test
	public void testArrays(){
		byte[] p5 = ShortByteTool.getComparableBytes((short)5);
		byte[] n3 = ShortByteTool.getComparableBytes((short)-3);
		byte[] n7 = ShortByteTool.getComparableBytes((short)-7);
		Assert.assertTrue(Java9.compareUnsigned(p5, n3) > 0);
		Assert.assertTrue(Java9.compareUnsigned(p5, n7) > 0);
	}

	@Test
	public void testRoundTrip(){
		short[] subjects = new short[]{
				Short.MIN_VALUE,Short.MIN_VALUE + 1,
				0,1,127,128,
				Short.MAX_VALUE - 1,Short.MAX_VALUE};
		for(short subject : subjects){
			byte[] bytes = ShortByteTool.getComparableBytes(subject);
			int roundTripped = fromComparableByteArray(bytes)[0];
			Assert.assertEquals(roundTripped, subject);
		}
	}

	@Test
	public void testRoundTrips(){
		short shortValue = Short.MIN_VALUE;
		byte[] lastBytes = ShortByteTool.getComparableBytes(shortValue);
		++shortValue;
		int counter = 0;
		for(; shortValue < Short.MAX_VALUE; shortValue += 1){
			byte[] bytes = ShortByteTool.getComparableBytes(shortValue);
			short roundTripped = fromComparableByteArray(bytes)[0];
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

	@Test
	public void testUnsignedRoundTrips(){
		short shortValue = 0;
		while(true){
			byte[] bytes = ShortByteTool.getUInt15Bytes(shortValue);
			short roundTripped = ShortByteTool.fromUInt15Bytes(bytes, 0);
			Assert.assertEquals(roundTripped, shortValue);
			if(shortValue == Short.MAX_VALUE){
				break;
			}
			++shortValue;
		}
	}

	private static short[] fromComparableByteArray(byte[] bytes){
		if(ArrayTool.isEmpty(bytes)){
			return new short[0];
		}
		short[] out = new short[bytes.length / 2];
		for(int i = 0; i < out.length; ++i){
			int startIdx = i * 2;

			/*
			 * i think the first bitwise operation causes the operand to be zero-padded
			 *     to an integer before the operation happens
			 *
			 * parenthesis are extremely important here because of the automatic int upgrading
			 */

			// more compact
			out[i] = (short)(Short.MIN_VALUE ^ ((bytes[startIdx] & 0xff) << 8 | bytes[startIdx + 1] & 0xff));
		}
		return out;
	}
}

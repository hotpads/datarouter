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

import org.testng.annotations.Test;
import org.testng.Assert;

public class FloatByteTool{

	public static byte[] getBytes(float in){
		int bits = Float.floatToIntBits(in);
		byte[] out = new byte[4];
		out[0] = (byte) (bits >>> 24);
		out[1] = (byte) (bits >>> 16);
		out[2] = (byte) (bits >>> 8);
		out[3] = (byte) bits;
		return out;
	}

	public static float fromBytes(final byte[] bytes, final int startIdx){
		int bits =
		  ((bytes[startIdx] & 0xff) << 24)
		| ((bytes[startIdx + 1] & 0xff) << 16)
		| ((bytes[startIdx + 2] & 0xff) << 8)
		| (bytes[startIdx + 3] & 0xff);
		return Float.intBitsToFloat(bits);
	}


	public static class Tests{
		@Test
		public void testBytes1(){
			float floatA = 123.456f;
			byte[] bytesA = getBytes(floatA);
			float backA = fromBytes(bytesA, 0);
			Assert.assertTrue(floatA == backA);

			float floatB = -123.456f;
			byte[] bytesB = getBytes(floatB);
			float backB = fromBytes(bytesB, 0);
			Assert.assertTrue(floatB == backB);

			Assert.assertTrue(ByteTool.bitwiseCompare(bytesA, bytesB) < 0);//positives and negatives are reversed
		}
	}
}

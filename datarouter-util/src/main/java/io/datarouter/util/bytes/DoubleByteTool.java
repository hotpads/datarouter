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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.testng.Assert;
import org.testng.annotations.Test;

public class DoubleByteTool{

	private static final long NaN = 0x0010000000000000L;

	public static byte[] toComparableBytes(double value){
		long longBits = Double.doubleToRawLongBits(value);
		if(longBits < 0){
			longBits ^= Long.MAX_VALUE;
		}
		return LongByteTool.getComparableBytes(longBits);
	}

	public static double fromComparableBytes(byte[] comparableBytes, int offset){
		long longBits = LongByteTool.fromComparableBytes(comparableBytes, offset);
		if(longBits < 0){
			longBits ^= Long.MAX_VALUE;
		}
		return Double.longBitsToDouble(longBits);
	}

	private static Double fromBytesNullable(final byte[] bytes, final int offset){
		Long longValue = LongByteTool.fromRawBytes(bytes, offset);
		if(longValue.longValue() == NaN){
			return null;
		}
		return Double.longBitsToDouble(longValue);
	}

	private static byte[] getBytesNullable(Double in){
		if(in == null){
			return getBytes(Double.longBitsToDouble(NaN));
		}
		return getBytes(in);
	}

	public static byte[] getBytes(final double in){
		long bits = Double.doubleToLongBits(in);
		return LongByteTool.getRawBytes(bits);
	}

	public static int toBytes(final double in, final byte[] bytes, final int offset){
		long bits = Double.doubleToLongBits(in);
		LongByteTool.toRawBytes(bits, bytes, offset);
		return 8;
	}

	public static double fromBytes(final byte[] bytes, final int offset){
		return Double.longBitsToDouble(LongByteTool.fromRawBytes(bytes, offset));
	}

	public static List<Double> fromDoubleByteArray(final byte[] bytes, final int startIdx){
		int numDoubles = (bytes.length - startIdx) / 8;
		List<Double> doubles = new ArrayList<>();
		byte[] arrayToCopy = new byte[8];
		for(int i = 0; i < numDoubles; i++){
			System.arraycopy(bytes, i * 8 + startIdx, arrayToCopy, 0, 8);
			doubles.add(fromBytesNullable(arrayToCopy, 0));
		}
		return doubles;
	}

	public static byte[] getDoubleByteArray(List<Double> valuesWithNulls){
		if(valuesWithNulls == null){
			return null;
		}
		byte[] out = new byte[8 * valuesWithNulls.size()];
		for(int i = 0; i < valuesWithNulls.size(); ++i){
			System.arraycopy(getBytesNullable(valuesWithNulls.get(i)), 0, out, i * 8, 8);
		}
		return out;
	}

	public static class DoubleByteToolTests{

		@Test
		public void testComparableBytes(){
			List<Double> interestingDoubles = Arrays.asList(Double.NEGATIVE_INFINITY, -Double.MAX_VALUE,
					-Double.MIN_NORMAL, -Double.MIN_VALUE, -0D, +0D, Double.MIN_VALUE, Double.MIN_NORMAL,
					Double.MAX_VALUE, Double.POSITIVE_INFINITY, Double.NaN);
			Collections.sort(interestingDoubles);
			List<Double> roundTripped = interestingDoubles.stream()
					.map(DoubleByteTool::toComparableBytes)
					.sorted(ByteTool::bitwiseCompare)
					.map(bytes -> fromComparableBytes(bytes, 0))
					.collect(Collectors.toList());
			Assert.assertEquals(roundTripped, interestingDoubles);
		}

		@Test
		public void testBytes1(){
			double valueA = 12354234.456D;
			byte[] abytes = getBytes(valueA);
			double aback = fromBytes(abytes, 0);
			Assert.assertTrue(valueA == aback);

			double valueB = -1234568.456D;
			byte[] bbytes = getBytes(valueB);
			double bback = fromBytes(bbytes, 0);
			Assert.assertTrue(valueB == bback);

			Assert.assertTrue(ByteTool.bitwiseCompare(abytes, bbytes) < 0);//positives and negatives are reversed
		}

		@Test
		public void testToFromByteArray(){
			double one = 2.39483;
			double two = -583.2039;
			double three = 5;
			double four = -.0000001;

			List<Double> doubles = new ArrayList<>();
			doubles.add(one);
			doubles.add(two);
			doubles.add(null);
			doubles.add(three);
			doubles.add(four);

			byte[] doubleBytes = getDoubleByteArray(doubles);
			List<Double> result = fromDoubleByteArray(doubleBytes, 0);
			Assert.assertEquals(result, doubles);

		}
	}
}

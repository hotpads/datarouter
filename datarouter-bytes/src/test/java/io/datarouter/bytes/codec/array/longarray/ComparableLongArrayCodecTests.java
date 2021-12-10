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
package io.datarouter.bytes.codec.array.longarray;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.bytes.EmptyArray;

public class ComparableLongArrayCodecTests{

	private static final ComparableLongArrayCodec CODEC = ComparableLongArrayCodec.INSTANCE;

	@Test
	public void testEmpty(){
		Assert.assertEquals(CODEC.encode(EmptyArray.LONG), EmptyArray.BYTE);
		Assert.assertEquals(CODEC.decode(EmptyArray.BYTE), EmptyArray.LONG);
	}

	@Test
	public void testSingleValues(){
		Assert.assertEquals(CODEC.encode(new long[]{0}), new byte[]{-128, 0, 0, 0, 0, 0, 0, 0});
		Assert.assertEquals(CODEC.encode(new long[]{3}), new byte[]{-128, 0, 0, 0, 0, 0, 0, 3});
	}

	@Test
	public void testMultipleValues(){
		long[] input = new long[]{-3, 0, 3};
		byte[] encoded = CODEC.encode(input);
		long[] decoded = CODEC.decode(encoded);
		Assert.assertEquals(decoded, input);
	}

	@Test
	public void testMultipleValuesWithOffsets(){
		long[] input = new long[]{-3, 0, 3};
		int offset = 5;
		int length = input.length * 8;
		byte[] encoded = new byte[100];
		int encodedLength = CODEC.encode(input, encoded, offset);
		Assert.assertEquals(encodedLength, length);
		long[] decoded = CODEC.decode(encoded, offset, length);
		Assert.assertEquals(decoded, input);
	}

}

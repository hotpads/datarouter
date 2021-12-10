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
package io.datarouter.bytes.codec.array.intarray;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.bytes.EmptyArray;

public class ComparableIntArrayCodecTests{

	private static final ComparableIntArrayCodec CODEC = ComparableIntArrayCodec.INSTANCE;

	@Test
	public void testEmpty(){
		Assert.assertEquals(CODEC.encode(EmptyArray.INT), EmptyArray.BYTE);
		Assert.assertEquals(CODEC.decode(EmptyArray.BYTE), EmptyArray.INT);
	}

	@Test
	public void testSingleValues(){
		Assert.assertEquals(CODEC.encode(new int[]{0}), new byte[]{-128, 0, 0, 0});
		Assert.assertEquals(CODEC.encode(new int[]{3}), new byte[]{-128, 0, 0, 3});
	}

	@Test
	public void testMultipleValues(){
		int[] input = new int[]{-3, 0, 3};
		byte[] encoded = CODEC.encode(input);
		int[] decoded = CODEC.decode(encoded);
		Assert.assertEquals(decoded, input);
	}

	@Test
	public void testMultipleValuesWithOffsets(){
		int[] input = new int[]{-3, 0, 3};
		int offset = 5;
		int length = input.length * 4;
		byte[] encoded = new byte[100];
		int encodedLength = CODEC.encode(input, encoded, offset);
		Assert.assertEquals(encodedLength, length);
		int[] decoded = CODEC.decode(encoded, offset, length);
		Assert.assertEquals(decoded, input);
	}

}

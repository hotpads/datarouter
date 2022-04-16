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
package io.datarouter.bytes;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.bytes.codec.intcodec.ComparableIntCodec;
import io.datarouter.scanner.Scanner;

public class SortedByteArraysTests{

	private static final byte[] EMPTY = EmptyArray.BYTE;

	@Test
	public void testEmpty(){
		var ba1 = SortedByteArrays.ofIndividualArrays(List.of());
		Assert.assertEquals(ba1.size(), 0);

		var ba2 = new ByteArrays(new byte[]{0, 0, 0, 0}, 2);
		Assert.assertEquals(ba2.size(), 0);
	}

	@Test
	public void testValidationFromArrays(){
		List<byte[]> input = List.of(
				EMPTY,
				new byte[0],
				new byte[2],
				new byte[1],
				new byte[3]);
		Assert.assertThrows(IllegalArgumentException.class, () -> SortedByteArrays.ofIndividualArrays(input));
	}

	@Test
	public void testValidationFromFlatArrays(){
		List<byte[]> input = List.of(
				EMPTY,
				new byte[0],
				new byte[2],
				new byte[1],
				new byte[3]);
		var arrays = new ByteArrays(input);
		Assert.assertThrows(IllegalArgumentException.class, () -> SortedByteArrays.ofByteArrays(arrays));
	}

	@Test
	public void testFromArrays(){
		List<byte[]> arrays = List.of(
				EMPTY,
				EMPTY,
				new byte[]{0},
				new byte[]{1, 2});
		var sba = SortedByteArrays.ofIndividualArrays(arrays);
		Assert.assertEquals(sba.size(), 4);
		Assert.assertEquals(sba.get(0), EMPTY);
		Assert.assertEquals(sba.get(1), EMPTY);
		Assert.assertEquals(sba.get(2), new byte[]{0});
		Assert.assertEquals(sba.get(3), new byte[]{1, 2});
	}

	@Test
	public void testFromBackingArray(){
		int offset = 1;
		byte[] backingArray = new byte[]{
				-1, //before offset
				3,//size
				0, 1, 2, //lengths
				0, 1, 2, //concatenated values
				-1}; //after end
		var byteArrays = new ByteArrays(backingArray, offset);
		var sba = SortedByteArrays.ofByteArrays(byteArrays);
		Assert.assertEquals(sba.size(), 3);
		Assert.assertEquals(sba.get(0), EMPTY);
		Assert.assertEquals(sba.get(1), new byte[]{0});
		Assert.assertEquals(sba.get(2), new byte[]{1, 2});
	}

	@Test
	public void testBinarySearchContains(){
		ComparableIntCodec codec = ComparableIntCodec.INSTANCE;
		List<byte[]> arrays = Scanner.iterate(0, i -> i + 1)
				.limit(10)
				.map(codec::encode)
				.listTo(SortedByteArrays::ofIndividualArrays);
		Scanner.iterate(0, i -> i + 1)
				.limit(10)
				.map(codec::encode)
				.forEach(item -> Assert.assertTrue(arrays.contains(item)));
		Assert.assertFalse(arrays.contains(null));
		Assert.assertFalse(arrays.contains(codec.encode(-1)));
		Assert.assertFalse(arrays.contains(codec.encode(11)));
	}

}

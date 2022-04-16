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
import io.datarouter.bytes.codec.list.bytearraylist.ByteArraysCodec;
import io.datarouter.scanner.Scanner;

public class ByteArraysTests{

	private static final byte[] EMPTY = EmptyArray.BYTE;

	@Test
	public void testEmpty(){
		var ba1 = ByteArrays.of(List.of());
		Assert.assertEquals(ba1.size(), 0);

		var ba2 = ByteArrays.of(new byte[]{0, 0, 0, 0}, 2);
		Assert.assertEquals(ba2.size(), 0);
	}

	@Test
	public void testFromArrays(){
		List<byte[]> arrays = List.of(
				EMPTY,
				new byte[]{0},
				EMPTY,
				new byte[]{1, 2},
				EMPTY);
		var ba = ByteArrays.of(arrays);
		Assert.assertEquals(ba.size(), 5);
		Assert.assertEquals(ba.get(0), EMPTY);
		Assert.assertEquals(ba.get(1), new byte[]{0});
		Assert.assertEquals(ba.get(2), EMPTY);
		Assert.assertEquals(ba.get(3), new byte[]{1, 2});
		Assert.assertEquals(ba.get(4), EMPTY);
	}

	@Test
	public void testFromBackingArray(){
		int offset = 1;
		byte[] backingArray = new byte[]{
				-1, //before offset
				5,//size
				0, 1, 0, 2, 0, //lengths
				0, 1, 2, //concatenated values
				-1}; //after end
		var ba = ByteArrays.of(backingArray, offset);
		Assert.assertEquals(ba.size(), 5);
		Assert.assertEquals(ba.get(0), EMPTY);
		Assert.assertEquals(ba.get(1), new byte[]{0});
		Assert.assertEquals(ba.get(2), EMPTY);
		Assert.assertEquals(ba.get(3), new byte[]{1, 2});
		Assert.assertEquals(ba.get(4), EMPTY);
	}

	@Test
	public void testContains(){
		ComparableIntCodec codec = ComparableIntCodec.INSTANCE;
		List<byte[]> arrays = Scanner.iterate(0, i -> i + 1)
				.limit(10)
				.shuffle()
				.map(codec::encode)
				.listTo(ByteArrays::of);
		Scanner.iterate(0, i -> i + 1)
				.limit(10)
				.map(codec::encode)
				.forEach(item -> Assert.assertTrue(arrays.contains(item)));
		Assert.assertFalse(arrays.contains(null));
		Assert.assertFalse(arrays.contains(codec.encode(-1)));
		Assert.assertFalse(arrays.contains(codec.encode(11)));
	}

	@Test
	public void testCodec(){
		List<byte[]> items = Scanner.iterate(0, i -> i + 1)
				.limit(10)
				.map(ComparableIntCodec.INSTANCE::encode)
				.list();
		var input = ByteArrays.of(items);
		var output = ByteArraysCodec.INSTANCE.encodeAndDecode(input);
		for(int i = 0; i < items.size(); ++i){
			Assert.assertEquals(output.get(i), items.get(i));
		}
	}

}

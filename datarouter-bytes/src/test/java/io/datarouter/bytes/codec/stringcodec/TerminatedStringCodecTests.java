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
package io.datarouter.bytes.codec.stringcodec;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.scanner.Scanner;

public class TerminatedStringCodecTests{

	private static final TerminatedStringCodec CODEC = TerminatedStringCodec.UTF_8;

	@Test
	public void testEncode(){
		byte[] inputBytes = {'a', 0, 1, 'b'};
		String inputString = new String(inputBytes);
		byte[] actualBytes = CODEC.encode(inputString);
		byte[] expectedBytes = {'a', 1, 2, 1, 3, 'b', 0};
		Assert.assertEquals(actualBytes, expectedBytes);
	}

	@Test
	public void testDecode(){
		byte[] expectedBytes = {'a', 1, 2, 1, 3, 'b', 0};
		String actualString = CODEC.decode(expectedBytes).value;
		String expectedString = new String(new byte[]{'a', 0, 1, 'b'});
		Assert.assertEquals(actualString, expectedString);
	}

	@Test
	public void testSorting(){
		String first = "ab";
		String second = "abc";
		byte[] firstBytes = CODEC.encode(first);
		byte[] secondBytes = CODEC.encode(second);
		Assert.assertTrue(Arrays.compareUnsigned(firstBytes, secondBytes) < 0);
	}

	@Test
	public void testSortingWithZeros(){
		byte[] bytes0 = {'a', 0, 'b'};
		byte[] bytes1 = {'a', 1, 0, 'b'};
		byte[] bytes2 = {'a', 1, 1, 'b'};
		byte[] bytes3 = {'a', 1, 'b'};
		String string0 = new String(bytes0, StandardCharsets.UTF_8);
		String string1 = new String(bytes1, StandardCharsets.UTF_8);
		String string2 = new String(bytes2, StandardCharsets.UTF_8);
		String string3 = new String(bytes3, StandardCharsets.UTF_8);
		//out of order with duplicates
		List<byte[]> inputBytes = List.of(bytes1, bytes3, bytes2, bytes0, bytes2, bytes1);
		List<String> inputStrings = Scanner.of(inputBytes)
				.map(String::new)
				.list();
		List<byte[]> outputBytes = Scanner.of(inputStrings)
				.map(CODEC::encode)
				.distinctBy(Arrays::hashCode)
				.sort(Arrays::compareUnsigned)
				.list();
		List<String> outputStrings = Scanner.of(outputBytes)
				.map(CODEC::decode)
				.map(lengthAndValue -> lengthAndValue.value)
				.list();
		List<String> expectedOutputStrings = List.of(string0, string1, string2, string3);
		Assert.assertEquals(outputStrings, expectedOutputStrings);
	}

}
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

import io.datarouter.bytes.codec.stringcodec.StringCodec;
import io.datarouter.bytes.varint.VarIntByteArraysTool;
import io.datarouter.scanner.Scanner;

public class VarIntByteArraysToolTests{

	@Test
	public void testEncodeOne(){
		byte[] input = new byte[129];// 2 byte header
		byte[] output = VarIntByteArraysTool.encodeOne(input);
		Assert.assertEquals(output.length, 131);
		Assert.assertEquals(output[0], -127);// value: 1, lastByte: false
		Assert.assertEquals(output[1], 1);// value: 128 * 1, lastBytes: true
	}

	@Test
	public void testDecodeOne(){
		byte[] encoded = {99, 3, 0, 1, 2};// skip the 99, length=3
		byte[] expected = {0, 1, 2};
		byte[] actual = VarIntByteArraysTool.decodeOne(encoded, 1);
		Assert.assertEquals(actual, expected);
	}

	@Test
	public void testRoundTrip(){
		List<String> expected = List.of("hello", "a", "", "list");
		byte[] encoded = Scanner.of(expected)
				.map(StringCodec.UTF_8::encode)
				.apply(VarIntByteArraysTool::encodeMulti);
		List<String> actual = VarIntByteArraysTool.decodeMulti(encoded)
				.map(StringCodec.UTF_8::decode)
				.list();
		Assert.assertEquals(actual, expected);
	}

	@Test
	public void testEncodedLength(){
		List<byte[]> arrays = List.of(new byte[0], new byte[1], new byte[2]);
		int expected = 6;// 1 + 2 + 3
		int actual = VarIntByteArraysTool.encodedLength(arrays);
		Assert.assertEquals(actual, expected);
	}

}

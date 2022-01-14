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
package io.datarouter.bytes.binarydto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.bytes.ByteTool;
import io.datarouter.bytes.Java9;
import io.datarouter.bytes.binarydto.codec.BinaryDtoCodec;
import io.datarouter.bytes.binarydto.codec.bytearray.BinaryDtoByteArrayScanner;
import io.datarouter.bytes.binarydto.codec.bytearray.MultiBinaryDtoEncoder;
import io.datarouter.bytes.binarydto.codec.iostream.BinaryDtoInputStreamScanner;
import io.datarouter.bytes.binarydto.codec.iostream.BinaryDtoOutputStreamWriter;
import io.datarouter.bytes.binarydto.dto.BaseBinaryDto;
import io.datarouter.bytes.binarydto.dto.BinaryDtoField;
import io.datarouter.scanner.Scanner;

public class BinaryDtoEncodeMultiTests{

	public static class TestDto extends BaseBinaryDto{
		@BinaryDtoField(index = 2)
		public final float cherry;
		@BinaryDtoField(index = 0)
		public final Integer banana;
		@BinaryDtoField(index = 1)
		public final String apple;//first alphabetically

		public TestDto(float cherry, Integer banana, String apple){
			this.cherry = cherry;
			this.banana = banana;
			this.apple = apple;
		}
	}

	@Test
	public void testSingle(){
		BinaryDtoCodec<TestDto> codec = new BinaryDtoCodec<>(TestDto.class);
		TestDto expected = new TestDto(1.1f, 3, "AAA");
		int expectedLengthBanana = 1 + 4;//nullable int
		int expectedLengthApple = 1 + 3 + 1;//nullable string, 3 chars without escaping, terminal byte
		int expectedLengthCherry = 4;//primitive float
		int expectedLength = expectedLengthBanana + expectedLengthApple + expectedLengthCherry;
		byte[] bytes = codec.encode(expected);
		Assert.assertEquals(bytes.length, expectedLength);
		TestDto actual = codec.decode(bytes);
		Assert.assertEquals(actual, expected);
	}

	@Test
	public void testMultiByteArray(){
		List<TestDto> expected = Java9.listOf(
				new TestDto(1.1f, 3, "A"),
				new TestDto(2.1f, 4, "BB"),
				new TestDto(3.1f, 5, "CCC"));
		byte[] bytes = new MultiBinaryDtoEncoder<>(TestDto.class)
				.encode(expected)
				.listTo(ByteTool::concatenate);
		List<TestDto> actual = BinaryDtoByteArrayScanner.of(TestDto.class, bytes).list();
		Assert.assertEquals(actual, expected);
	}

	@Test
	public void testMultiOutputIntputStreams(){
		List<TestDto> expected = Java9.listOf(
				new TestDto(1.1f, 3, "A"),
				new TestDto(2.1f, 4, "BB"),
				new TestDto(-3.5f, 5, "CCC"));
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try(BinaryDtoOutputStreamWriter<TestDto> dtoWriter = new BinaryDtoOutputStreamWriter<>(
				TestDto.class,
				outputStream)){
			expected.forEach(dtoWriter::write);
		}
		try(Scanner<TestDto> dtoScanner = new BinaryDtoInputStreamScanner<>(
				TestDto.class,
				new ByteArrayInputStream(outputStream.toByteArray()))){
			List<TestDto> actual = dtoScanner.list();
			Assert.assertEquals(actual, expected);
		}
	}

}

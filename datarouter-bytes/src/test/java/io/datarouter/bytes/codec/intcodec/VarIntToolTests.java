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
package io.datarouter.bytes.codec.intcodec;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.bytes.EmptyArray;
import io.datarouter.bytes.VarIntTool;

public class VarIntToolTests{

	private record TestItem(
			long value,
			byte[] bytes){
	}

	private static final List<TestItem> ITEMS = List.of(
			//1 byte
			new TestItem(0, new byte[]{0}),
			new TestItem(1, new byte[]{1}),
			new TestItem(63, new byte[]{63}),
			new TestItem(127, new byte[]{127}),
			//2 bytes
			new TestItem(128, new byte[]{-128, 1}),
			new TestItem(129, new byte[]{-127, 1}),
			new TestItem(155, new byte[]{-101, 1}),
			new TestItem(255, new byte[]{-1, 1}),
			new TestItem(256, new byte[]{-128, 2}),
			new TestItem(16_380, new byte[]{-4, 127}),
			new TestItem(16_383, new byte[]{-1, 127}),
			//3 bytes
			new TestItem(16_384, new byte[]{-128, -128, 1}),
			new TestItem(2_097_151, new byte[]{-1, -1, 127}),
			//4 bytes
			new TestItem(2_097_152, new byte[]{-128, -128, -128, 1}),
			new TestItem(268_435_455, new byte[]{-1, -1, -1, 127}),
			//5 bytes
			new TestItem(268_435_456, new byte[]{-128, -128, -128, -128, 1}),
			new TestItem(2_147_483_647, new byte[]{-1, -1, -1, -1, 7}),//max int
			new TestItem(Integer.MAX_VALUE, VarIntTool.INTEGER_MAX_ENCODED_VALUE),
			new TestItem(34_359_738_367L, new byte[]{-1, -1, -1, -1, 127}),
			//6 bytes
			new TestItem(34_359_738_368L, new byte[]{-128, -128, -128, -128, -128, 1}),
			new TestItem(4_398_046_511_103L, new byte[]{-1, -1, -1, -1, -1, 127}),
			//7 bytes
			new TestItem(4_398_046_511_104L, new byte[]{-128, -128, -128, -128, -128, -128, 1}),
			new TestItem(562_949_953_421_311L, new byte[]{-1, -1, -1, -1, -1, -1, 127}),
			//8 bytes
			new TestItem(562_949_953_421_312L, new byte[]{-128, -128, -128, -128, -128, -128, -128, 1}),
			new TestItem(72_057_594_037_927_935L, new byte[]{-1, -1, -1, -1, -1, -1, -1, 127}),
			//9 bytes
			new TestItem(72_057_594_037_927_936L, new byte[]{-128, -128, -128, -128, -128, -128, -128, -128, 1}),
			new TestItem(9_223_372_036_854_775_807L, new byte[]{-1, -1, -1, -1, -1, -1, -1, -1, 127}),//max long
			new TestItem(Long.MAX_VALUE, VarIntTool.LONG_MAX_ENCODED_VALUE));

	/*--------- length --------*/

	@Test
	public void testKnownValueLengths(){
		ITEMS.forEach(item -> Assert.assertEquals(
				VarIntTool.length(item.value),
				item.bytes.length));
	}

	@Test
	public void testLengthRandomValues(){
		var random = new Random();
		for(int i = 0; i < 10000; ++i){
			long value = Math.abs(random.nextLong());
			int expectedLength = VarIntTool.encode(value).length;
			int actualLength = VarIntTool.length(value);
			Assert.assertEquals(actualLength, expectedLength);
		}
	}

	@Test
	public void testLengthNegativeError(){
		var exception = Assert.expectThrows(IllegalArgumentException.class, () -> VarIntTool.length(-3));
		Assert.assertEquals(exception.getMessage(), VarIntTool.ERROR_MESSAGE_NEGATIVE_VALUE);
	}

	/*--------- byte arrays --------*/

	@Test
	public void testArrayEncoding(){
		ITEMS.forEach(item -> Assert.assertEquals(
				VarIntTool.encode(item.value),
				item.bytes));
	}

	@Test
	public void testArrayDecoding(){
		ITEMS.forEach(item -> Assert.assertEquals(
				VarIntTool.decodeLong(item.bytes),
				item.value));
	}

	@Test
	public void testArrayRandomValues(){
		var random = new Random();
		for(int i = 0; i < 10000; ++i){
			long value = Math.abs(random.nextLong());
			byte[] bytes = VarIntTool.encode(value);
			long roundTripped = VarIntTool.decodeLong(bytes);
			Assert.assertEquals(roundTripped, value);
		}
	}

	@Test
	public void testArrayOffset(){
		Assert.assertEquals(VarIntTool.decodeLong(new byte[]{-1, -1, 28}, 2), 28);
	}

	@Test
	public void testInvalidOffsetError(){
		var exception = Assert.expectThrows(
				IllegalArgumentException.class,
				() -> VarIntTool.decodeLong(new byte[]{0, 0, 0}, 4));
		Assert.assertEquals(exception.getMessage(), VarIntTool.ERROR_MESSAGE_INVALID_OFFSET);
	}

	@Test
	public void testNegativeValueError(){
		var exception = Assert.expectThrows(IllegalArgumentException.class, () -> VarIntTool.encode(-1));
		Assert.assertEquals(exception.getMessage(), VarIntTool.ERROR_MESSAGE_NEGATIVE_VALUE);
	}

	@Test
	public void testArrayMaxLengthError(){
		byte[] invalidBytes = {-1, -1, -1, -1, -1, -1, -1, -1, -1};//max 8 negatives
		var exception = Assert.expectThrows(IllegalArgumentException.class, () -> VarIntTool.decodeLong(invalidBytes));
		Assert.assertEquals(exception.getMessage(), VarIntTool.ERROR_MESSAGE_MAX_SIZE_EXCEEDED);
	}

	/*--------- IO streams --------*/

	@Test
	public void testStreamEncoding(){
		ITEMS.forEach(item -> {
			var outputStream = new ByteArrayOutputStream();
			VarIntTool.encode(outputStream, item.value);
			Assert.assertEquals(outputStream.toByteArray(), item.bytes);
		});
	}

	@Test
	public void testStreamDecoding(){
		ITEMS.forEach(item -> {
			var inputStream = new ByteArrayInputStream(item.bytes);
			Assert.assertEquals(VarIntTool.decodeLong(inputStream), item.value);
		});
	}

	@Test
	public void testStreamRandomValues(){
		var random = new Random();
		for(int i = 0; i < 10000; ++i){
			long value = Math.abs(random.nextLong());
			var outputStream = new ByteArrayOutputStream();
			VarIntTool.encode(outputStream, value);
			var inputStream = new ByteArrayInputStream(outputStream.toByteArray());
			long roundTripped = VarIntTool.decodeLong(inputStream);
			Assert.assertEquals(roundTripped, value);
		}
	}

	@Test
	public void testStreamEmptyInputStream(){
		var is = new ByteArrayInputStream(EmptyArray.BYTE);
		var exception = Assert.expectThrows(IllegalArgumentException.class, () -> VarIntTool.decodeLong(is));
		Assert.assertEquals(exception.getMessage(), VarIntTool.ERROR_MESSAGE_UNEXPECTED_END_OF_INPUT_STREAM);
	}

	@Test
	public void testStreamMaxLengthError(){
		byte[] invalidBytes = {-1, -1, -1, -1, -1, -1, -1, -1, -1};//max 8 negatives
		var inputStream = new ByteArrayInputStream(invalidBytes);
		var exception = Assert.expectThrows(IllegalArgumentException.class, () -> VarIntTool.decodeLong(inputStream));
		Assert.assertEquals(exception.getMessage(), VarIntTool.ERROR_MESSAGE_MAX_SIZE_EXCEEDED);
	}

	/*--------- optional IO streams --------*/

	@Test
	public void testOptionalDecoding(){
		ITEMS.forEach(item -> {
			var inputStream = new ByteArrayInputStream(item.bytes);
			Assert.assertEquals(VarIntTool.fromInputStream(inputStream).orElseThrow(), item.value);
		});
	}

	@Test
	public void testOptionalRandomValues(){
		var random = new Random();
		for(int i = 0; i < 10000; ++i){
			long value = Math.abs(random.nextLong());
			var outputStream = new ByteArrayOutputStream();
			VarIntTool.encode(outputStream, value);
			var inputStream = new ByteArrayInputStream(outputStream.toByteArray());
			long roundTripped = VarIntTool.fromInputStream(inputStream).orElseThrow();
			Assert.assertEquals(roundTripped, value);
		}
	}

	@Test
	public void testOptionalEmptyInputStream(){
		var is = new ByteArrayInputStream(EmptyArray.BYTE);
		Assert.assertEquals(VarIntTool.fromInputStream(is), Optional.empty());
	}

	@Test
	public void testOptionalIncompleteError(){
		byte[] invalidValue = new byte[]{-128};//there should be more bytes after a negative value
		var inputStream = new ByteArrayInputStream(invalidValue);
		var exception = Assert.expectThrows(RuntimeException.class, () -> VarIntTool.fromInputStream(inputStream));
		Assert.assertEquals(exception.getMessage(), VarIntTool.ERROR_MESSAGE_INCOMPLETE_VALUE);
	}

	@Test
	public void testOptionalMaxLengthError(){
		byte[] invalidBytes = {-1, -1, -1, -1, -1, -1, -1, -1, -1};//max 8 negatives
		var inputStream = new ByteArrayInputStream(invalidBytes);
		var exception = Assert.expectThrows(
				IllegalArgumentException.class,
				() -> VarIntTool.fromInputStream(inputStream));
		Assert.assertEquals(exception.getMessage(), VarIntTool.ERROR_MESSAGE_MAX_SIZE_EXCEEDED);
	}

	/*--------- ints -----------*/

	@Test
	public void testIntDowncasting(){
		byte[] int130 = {-126, 1};
		Assert.assertEquals(VarIntTool.decodeInt(int130), 130);
		Assert.assertEquals(VarIntTool.decodeInt(new ByteArrayInputStream(int130)), 130);
		Assert.assertEquals(VarIntTool.fromInputStreamInt(new ByteArrayInputStream(int130)).orElseThrow(), 130);
	}

	@Test
	public void testDecodeIntOverflowError(){
		long overflowValue = (long)Integer.MAX_VALUE + 1;
		byte[] bytes = VarIntTool.encode(overflowValue);
		var exception = Assert.expectThrows(
				IllegalArgumentException.class,
				() -> VarIntTool.decodeInt(bytes));
		Assert.assertEquals(exception.getMessage(), VarIntTool.ERROR_MESSAGE_INTEGER_OVERFLOW);
	}

}

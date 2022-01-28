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

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.bytes.ByteTool;
import io.datarouter.bytes.binarydto.codec.BinaryDtoCodec;
import io.datarouter.bytes.binarydto.dto.BinaryDto;

public class BinaryDtoDecodePrefixTests{

	public static class TestDto extends BinaryDto<TestDto>{
		public final int f1;
		public final short[] f2;
		public final String f3;
		public final Short f4;

		public TestDto(int f1, short[] f2, String f3, Short f4){
			this.f1 = f1;
			this.f2 = f2;
			this.f3 = f3;
			this.f4 = f4;
		}
	}

	private static final TestDto DTO = new TestDto(
			9,
			new short[]{1, 2},
			"hello",
			(short)7);

	private static final byte[] BYTES_F1 = {
			Byte.MIN_VALUE, 0, 0, 9};//f1 value
	private static final byte[] BYTES_F2 = {
			1,//f2 present
			2,//f2 size,
			Byte.MIN_VALUE, 1,//f2 value 0
			Byte.MIN_VALUE, 2};//f2 value 1
	private static final byte[] BYTES_F3 = {
			1,//f3 present
			'h', 'e', 'l', 'l', 'o',//f3 value
			0};//f3 terminator
	private static final byte[] BYTES_F4 = {
			1,//f4 present
			Byte.MIN_VALUE, 7};

	private static final BinaryDtoCodec<TestDto> CODEC = BinaryDtoCodec.of(TestDto.class);

	@Test
	public void testEncoding(){
		byte[] expectedBytes = ByteTool.concat(BYTES_F1, BYTES_F2, BYTES_F3, BYTES_F4);
		byte[] actualBytes = CODEC.encode(DTO);
		Assert.assertEquals(actualBytes, expectedBytes);

		TestDto actual = CODEC.decode(actualBytes);
		Assert.assertEquals(actual, DTO);
	}

	@Test
	public void testPrefixLength(){
		byte[] bytes = CODEC.encode(DTO);

		int length1 = CODEC.decodePrefixLength(bytes, 0, 1);
		Assert.assertEquals(length1, BYTES_F1.length);
		int length2 = CODEC.decodePrefixLength(bytes, 0, 2);
		Assert.assertEquals(length2, length1 + BYTES_F2.length);
		int length3 = CODEC.decodePrefixLength(bytes, 0, 3);
		Assert.assertEquals(length3, length2 + BYTES_F3.length);
		int length4 = CODEC.decodePrefixLength(bytes, 0, 4);
		Assert.assertEquals(length4, length3 + BYTES_F4.length);
	}

	@Test
	public void testPrefix(){
		byte[] expectedBytes = ByteTool.concat(BYTES_F1, BYTES_F2);
		byte[] actualBytes = CODEC.encodePrefix(DTO, 2);
		Assert.assertEquals(actualBytes, expectedBytes);

		TestDto actual = CODEC.decodePrefix(actualBytes, 0, 2);
		Assert.assertEquals(actual.f1, 9);
		Assert.assertEquals(actual.f2, new short[]{1, 2});
		Assert.assertNull(actual.f3);
		Assert.assertNull(actual.f4);
	}

}

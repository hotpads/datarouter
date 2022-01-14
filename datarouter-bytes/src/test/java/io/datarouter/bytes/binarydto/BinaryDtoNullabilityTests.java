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

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.bytes.ByteTool;
import io.datarouter.bytes.Java9;
import io.datarouter.bytes.binarydto.codec.BinaryDtoCodec;
import io.datarouter.bytes.binarydto.dto.BaseBinaryDto;
import io.datarouter.bytes.binarydto.dto.BinaryDtoField;

public class BinaryDtoNullabilityTests{

	public static class TestDto extends BaseBinaryDto{
		public final int f1;
		@BinaryDtoField(nullable = true)
		public final int f2;
		public final Integer f3;
		@BinaryDtoField(nullable = false)
		public final Integer f4;
		public final Integer[] f5;
		@BinaryDtoField(nullable = false, nullableItems = false)
		public final Integer[] f6;

		public TestDto(int f1, int f2, Integer f3, Integer f4, Integer[] f5, Integer[] f6){
			this.f1 = f1;
			this.f2 = f2;
			this.f3 = f3;
			this.f4 = f4;
			this.f5 = f5;
			this.f6 = f6;
		}
	}

	@Test
	public void testEncoding(){
		BinaryDtoCodec<TestDto> codec = new BinaryDtoCodec<>(TestDto.class);
		TestDto dto = new TestDto(1, 2, 3, 4, new Integer[]{5, 6}, new Integer[]{7, 8});
		List<byte[]> expectedByteSegments = Java9.listOf(
				//f1
				new byte[]{Byte.MIN_VALUE, 0, 0, 1},//value
				//f2
				new byte[]{Byte.MIN_VALUE, 0, 0, 2},//value
				//f3
				new byte[]{1},//present
				new byte[]{Byte.MIN_VALUE, 0, 0, 3},//value
				//f4
				//no nullable byte
				new byte[]{Byte.MIN_VALUE, 0, 0, 4},//value
				//f5
				new byte[]{1},//present
				new byte[]{2},//length
				new byte[]{1},//present
				new byte[]{Byte.MIN_VALUE, 0, 0, 5},//item0
				new byte[]{1},//present
				new byte[]{Byte.MIN_VALUE, 0, 0, 6},//item1
				//f6
				//no nullable byte
				new byte[]{2},//f6 length
				//no nullable byte
				new byte[]{Byte.MIN_VALUE, 0, 0, 7},//item0
				//no nullable byte
				new byte[]{Byte.MIN_VALUE, 0, 0, 8});//item1
		byte[] expectedFullBytes = ByteTool.concatenate(expectedByteSegments);
		byte[] actualFullBytes = codec.encode(dto);
		Assert.assertEquals(actualFullBytes, expectedFullBytes);

		TestDto actual = codec.decode(actualFullBytes);
		Assert.assertEquals(actual, dto);
	}

	@Test(expectedExceptions = { IllegalArgumentException.class })
	public void testRejectNullAndExceptionType(){
		BinaryDtoCodec<TestDto> codec = new BinaryDtoCodec<>(TestDto.class);
		TestDto dto = new TestDto(1, 2, 3, null, null, null);
		codec.encode(dto);
	}

}

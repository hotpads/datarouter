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

import io.datarouter.bytes.binarydto.codec.BinaryDtoCodec;
import io.datarouter.bytes.binarydto.dto.BinaryDto;
import io.datarouter.bytes.binarydto.dto.BinaryDtoField;

public class BinaryDtoEncodePrefixTests{

	public static class TestDto extends BinaryDto<TestDto>{

		public final int f1;
		@BinaryDtoField(nullable = false)
		public final String f2;
		@BinaryDtoField(nullable = false)//can be null in the prefix
		public final String f3;
		@BinaryDtoField(nullable = false)
		public final Integer f4;

		public TestDto(int f1, String f2, String f3, Integer f4){
			this.f1 = f1;
			this.f2 = f2;
			this.f3 = f3;
			this.f4 = f4;
		}

		public static TestDto prefix2(int s1, String s2){
			return new TestDto(s1, s2, null, null);
		}

	}

	@Test
	public void testEncodeFull(){
		var codec = BinaryDtoCodec.of(TestDto.class);
		var dto = new TestDto(1, "a", "b", 2);
		byte[] expectedBytes = {
				Byte.MIN_VALUE, 0, 0, 1,
				'a', 0,//present, value, terminator
				'b', 0,//present, value, terminator
				Byte.MIN_VALUE, 0, 0, 2};//present, value, terminator
		byte[] actualBytes = codec.encode(dto);
		Assert.assertEquals(actualBytes, expectedBytes);
		TestDto actualDto = codec.decode(actualBytes);
		Assert.assertEquals(actualDto, dto);
	}

	@Test
	public void testEncodePrefix(){
		var codec = BinaryDtoCodec.of(TestDto.class);
		var dto = TestDto.prefix2(1, "a");
		int numPrefixFields = 2;
		byte[] expectedBytes = {
				Byte.MIN_VALUE, 0, 0, 1,
				'a', 0};//present, value, terminator
		byte[] actualBytes = codec.encodePrefix(dto, numPrefixFields);
		Assert.assertEquals(actualBytes, expectedBytes);
		TestDto actualDto = codec.decodePrefix(actualBytes, 0, numPrefixFields);
		Assert.assertEquals(actualDto, dto);
	}

}

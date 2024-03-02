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
package io.datarouter.binarydto.encoding;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.binarydto.codec.BinaryDtoComparableCodec;
import io.datarouter.binarydto.dto.BinaryDtoField;
import io.datarouter.binarydto.dto.ComparableBinaryDto;

public class BinaryDtoEncodePrefixTests{

	private static class TestDto extends ComparableBinaryDto<TestDto>{

		@SuppressWarnings("unused")
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
		var dto = new TestDto(1, "a", "b", 2);
		Assert.assertEquals(dto.cloneIndexed(), dto);
		Assert.assertEquals(dto.cloneComparable(), dto);
	}

	@Test
	public void testEncodePrefix(){
		var codec = BinaryDtoComparableCodec.of(TestDto.class);
		var dto = TestDto.prefix2(1, "a");
		int numPrefixFields = 2;
		byte[] actualBytes = codec.encodePrefix(dto, numPrefixFields);
		TestDto actualDto = codec.decodePrefix(actualBytes, numPrefixFields);
		Assert.assertEquals(actualDto, dto);
	}

}

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

import java.util.Arrays;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.bytes.binarydto.codec.BinaryDtoComparableCodec;
import io.datarouter.bytes.binarydto.dto.ComparableBinaryDto;
import io.datarouter.scanner.Scanner;

public class BinaryDtoEnumTests{

	public static enum TestEnum{
		AA, BB, CC
	}

	public static class TestDto extends ComparableBinaryDto<TestDto>{
		public final TestEnum f1;
		public final TestEnum f2;
		public final TestEnum f3;

		public TestDto(TestEnum f1, TestEnum f2, TestEnum f3){
			this.f1 = f1;
			this.f2 = f2;
			this.f3 = f3;
		}
	}

	@Test
	public void testEncoding(){
		var dto = new TestDto(TestEnum.AA, null, TestEnum.CC);
		Assert.assertEquals(dto.cloneIndexed(), dto);
		Assert.assertEquals(dto.cloneComparable(), dto);
	}

	@Test
	public void testSorting(){
		var a1 = new TestDto(TestEnum.AA, TestEnum.AA, TestEnum.AA);
		var a2 = new TestDto(TestEnum.AA, TestEnum.AA, TestEnum.CC);
		var a3 = new TestDto(TestEnum.BB, TestEnum.AA, TestEnum.AA);
		var a4 = new TestDto(TestEnum.AA, null, TestEnum.AA);
		List<TestDto> input = List.of(a4, a1, a2, a3);

		//test dto sorting
		List<TestDto> expected = List.of(a4, a1, a2, a3);
		List<TestDto> actual = Scanner.of(input)
				.sort()
				.list();
		Assert.assertEquals(actual, expected);

		//test binary sorting matches dto sorting
		BinaryDtoComparableCodec<TestDto> comparableCodec = BinaryDtoComparableCodec.of(TestDto.class);
		List<TestDto> actualBinarySorted = Scanner.of(input)
				.map(comparableCodec::encode)
				.sort(Arrays::compareUnsigned)
				.map(comparableCodec::decode)
				.list();
		Assert.assertEquals(actualBinarySorted, expected);

	}


}

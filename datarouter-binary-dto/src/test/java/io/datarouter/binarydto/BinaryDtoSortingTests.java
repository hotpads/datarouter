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
package io.datarouter.binarydto;

import java.util.Arrays;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.binarydto.codec.BinaryDtoComparableCodec;
import io.datarouter.binarydto.dto.ComparableBinaryDto;
import io.datarouter.scanner.Scanner;

public class BinaryDtoSortingTests{

	private static class TestDto extends ComparableBinaryDto<TestDto>{
		@SuppressWarnings("unused")
		public final int f1;

		public TestDto(int f1){
			this.f1 = f1;
		}
	}

	@Test
	public void testCompareInt(){
		var a1 = new TestDto(1);
		var a2 = new TestDto(2);
		var a3 = new TestDto(-1);
		List<TestDto> input = List.of(a1, a2, a3);
		List<TestDto> expected = List.of(a3, a1, a2);

		//test dto sorting
		List<TestDto> actual = Scanner.of(input)
				.sort()
				.list();
		Assert.assertEquals(actual, expected);

		//test binary sorting matches dto sorting
		BinaryDtoComparableCodec<TestDto> codec = BinaryDtoComparableCodec.of(TestDto.class);
		List<TestDto> actualBinarySorted = Scanner.of(input)
				.map(codec::encode)
				.sort(Arrays::compareUnsigned)
				.map(codec::decode)
				.list();
		Assert.assertEquals(actualBinarySorted, expected);
	}

	private static class TestArrayDto extends ComparableBinaryDto<TestArrayDto>{
		@SuppressWarnings("unused")
		public final int[] f1;

		public TestArrayDto(int[] f1){
			this.f1 = f1;
		}
	}

	@Test
	public void testCompareIntArray(){
		var a1 = new TestArrayDto(new int[]{0, 1, 2});
		var a2 = new TestArrayDto(new int[]{1});
		var a3 = new TestArrayDto(new int[]{0, 1});
		var a4 = new TestArrayDto(new int[]{-2});
		var a5 = new TestArrayDto(new int[]{-2, 3});
		List<TestArrayDto> input = List.of(a1, a2, a3, a4, a5);
		List<TestArrayDto> expected = List.of(a4, a5, a3, a1, a2);

		//test dto sorting
		List<TestArrayDto> actual = Scanner.of(input)
				.sort()
				.list();
		Assert.assertEquals(actual, expected);

		//test binary sorting matches dto sorting
		BinaryDtoComparableCodec<TestArrayDto> codec = BinaryDtoComparableCodec.of(TestArrayDto.class);
		List<TestArrayDto> actualBinarySorted = Scanner.of(input)
				.map(codec::encode)
				.sort(Arrays::compareUnsigned)
				.map(codec::decode)
				.list();
		Assert.assertEquals(actualBinarySorted, expected);

	}

}

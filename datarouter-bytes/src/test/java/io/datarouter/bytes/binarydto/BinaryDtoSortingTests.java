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

public class BinaryDtoSortingTests{

	public static class TestIntDto extends ComparableBinaryDto<TestIntDto>{
		public final int f1;

		public TestIntDto(int f1){
			this.f1 = f1;
		}
	}

	@Test
	public void testCompareInt(){
		var a1 = new TestIntDto(1);
		var a2 = new TestIntDto(2);
		var a3 = new TestIntDto(-1);
		List<TestIntDto> input = List.of(a1, a2, a3);
		List<TestIntDto> expected = List.of(a3, a1, a2);

		//test dto sorting
		List<TestIntDto> actual = Scanner.of(input)
				.sort()
				.list();
		Assert.assertEquals(actual, expected);

		//test binary sorting matches dto sorting
		BinaryDtoComparableCodec<TestIntDto> codec = BinaryDtoComparableCodec.of(TestIntDto.class);
		List<TestIntDto> actualBinarySorted = Scanner.of(input)
				.map(codec::encode)
				.sort(Arrays::compareUnsigned)
				.map(codec::decode)
				.list();
		Assert.assertEquals(actualBinarySorted, expected);
	}

	public static class TestIntArrayDto extends ComparableBinaryDto<TestIntArrayDto>{
		public final int[] f1;

		public TestIntArrayDto(int[] f1){
			this.f1 = f1;
		}
	}

	@Test
	public void testCompareIntArray(){
		var a1 = new TestIntArrayDto(new int[]{0, 1, 2});
		var a2 = new TestIntArrayDto(new int[]{1});
		var a3 = new TestIntArrayDto(new int[]{0, 1});
		var a4 = new TestIntArrayDto(new int[]{-2});
		var a5 = new TestIntArrayDto(new int[]{-2, 3});
		List<TestIntArrayDto> input = List.of(a1, a2, a3, a4, a5);
		List<TestIntArrayDto> expected = List.of(a4, a5, a3, a1, a2);

		//test dto sorting
		List<TestIntArrayDto> actual = Scanner.of(input)
				.sort()
				.list();
		Assert.assertEquals(actual, expected);

		//test binary sorting matches dto sorting
		BinaryDtoComparableCodec<TestIntArrayDto> codec = BinaryDtoComparableCodec.of(TestIntArrayDto.class);
		List<TestIntArrayDto> actualBinarySorted = Scanner.of(input)
				.map(codec::encode)
				.sort(Arrays::compareUnsigned)
				.map(codec::decode)
				.list();
		Assert.assertEquals(actualBinarySorted, expected);

	}

}

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

import java.util.Arrays;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.binarydto.codec.BinaryDtoComparableCodec;
import io.datarouter.binarydto.codec.BinaryDtoIndexedCodec;
import io.datarouter.binarydto.dto.ComparableBinaryDto;
import io.datarouter.bytes.HexBlockTool;
import io.datarouter.scanner.Scanner;
import io.datarouter.types.MilliTime;

public class BinaryDtoMilliTimeTests{

	@SuppressWarnings("unused")
	private static class TestDto extends ComparableBinaryDto<TestDto>{
		public final MilliTime t1;

		public TestDto(MilliTime t1){
			this.t1 = t1;
		}
	}

	private static final TestDto DTO_0 = new TestDto(MilliTime.ofEpochMilli(0));
	private static final TestDto DTO_1 = new TestDto(MilliTime.ofEpochMilli(1));
	private static final TestDto DTO_2 = new TestDto(MilliTime.ofEpochMilli(2));

	@Test
	public void testIndexedEncoding(){
//		HexBlockTool.print(DTO_0.encodeIndexed());
		String hex = "00088000000000000000";
		byte[] bytes = HexBlockTool.fromHexBlock(hex);
		Assert.assertEquals(DTO_0.encodeIndexed(), bytes);
		Assert.assertEquals(BinaryDtoIndexedCodec.of(TestDto.class).decode(bytes), DTO_0);
	}

	@Test
	public void testComparableEncoding(){
//		HexBlockTool.print(DTO_0.encodeComparable());
		String hex = "018000000000000000";
		byte[] bytes = HexBlockTool.fromHexBlock(hex);
		Assert.assertEquals(DTO_0.encodeComparable(), bytes);
		Assert.assertEquals(BinaryDtoComparableCodec.of(TestDto.class).decode(bytes), DTO_0);
	}

	@Test
	public void testSorting(){
		List<TestDto> unsorted = List.of(DTO_2, DTO_0, DTO_1);
		List<TestDto> expected = List.of(DTO_0, DTO_1, DTO_2);
		List<TestDto> actual = Scanner.of(unsorted)
				.map(TestDto::encodeComparable)
				.sort(Arrays::compareUnsigned)
				.map(BinaryDtoComparableCodec.of(TestDto.class)::decode)
				.list();
		Assert.assertEquals(actual, expected);
	}

}

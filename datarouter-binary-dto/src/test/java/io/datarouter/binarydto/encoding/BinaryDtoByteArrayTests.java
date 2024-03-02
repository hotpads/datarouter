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

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.binarydto.codec.BinaryDtoComparableCodec;
import io.datarouter.binarydto.codec.BinaryDtoIndexedCodec;
import io.datarouter.binarydto.dto.ComparableBinaryDto;
import io.datarouter.bytes.HexBlockTool;
import io.datarouter.scanner.Scanner;

public class BinaryDtoByteArrayTests{

	@SuppressWarnings("unused")
	private static class TestDto extends ComparableBinaryDto<TestDto>{
		public final byte[] f1;
		public final byte[] f2;
		public final byte[] f3;

		public TestDto(byte[] f1, byte[] f2, byte[] f3){
			this.f1 = f1;
			this.f2 = f2;
			this.f3 = f3;
		}
	}

	private static final TestDto DTO = new TestDto(
			new byte[]{'M', 'a', 't', 't'},
			null,
			new byte[]{});

	@Test
	public void testIndexedEncoding(){
//		HexBlockTool.print(DTO.encodeIndexed());
//		String hex = "00044d6174740200";
		String hex = String.join("",
				"00",// index 0
				"04",// length 4
				"4d617474",// "Matt"
				// there's no index 1 because the value is null
				"02",// index 2
				"00");// length 0
		byte[] bytes = HexBlockTool.fromHexBlock(hex);
		Assert.assertEquals(DTO.encodeIndexed(), bytes);
		Assert.assertEquals(BinaryDtoIndexedCodec.of(TestDto.class).decode(bytes), DTO);
	}

	@Test
	public void testComparableEncoding(){
//		HexBlockTool.print(DTO.encodeComparable());
//		String hex = "014d61747400000100";
		String hex = String.join("",
				"01",// f0 is present
				"4d617474",// "Matt"
				"00",// byte array terminator
				"00",// f1 is null
				"01",// f2 is present
				"00");// byte array terminator
		byte[] bytes = HexBlockTool.fromHexBlock(hex);
		Assert.assertEquals(DTO.encodeComparable(), bytes);
		Assert.assertEquals(BinaryDtoComparableCodec.of(TestDto.class).decode(bytes), DTO);
	}


	private static class CompareTestDto extends ComparableBinaryDto<TestDto>{
		@SuppressWarnings("unused")
		public final byte[] f1;

		public CompareTestDto(byte[] f1){
			this.f1 = f1;
		}
	}

	@Test
	public void testSorting(){
		var dto1 = new CompareTestDto(new byte[]{-1, 0});
		var dto2 = new CompareTestDto(new byte[]{-2});
		var dto3 = new CompareTestDto(new byte[]{1});
		var dto4 = new CompareTestDto(new byte[]{1, 0});
		List<CompareTestDto> unsorted = List.of(dto1, dto2, dto3, dto4);
		List<CompareTestDto> expected = List.of(dto3, dto4, dto2, dto1);
		List<CompareTestDto> actual = Scanner.of(unsorted).sort().list();
		Assert.assertEquals(actual, expected);
	}

}

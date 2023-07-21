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
import io.datarouter.binarydto.codec.BinaryDtoIndexedCodec;
import io.datarouter.binarydto.dto.ComparableBinaryDto;
import io.datarouter.bytes.HexBlockTool;

public class BinaryDtoLongArrayTests{

	private static class TestDto extends ComparableBinaryDto<TestDto>{
		public final long[] f1;
		public final long[] f2;
		public final long[] f3;

		public TestDto(long[] f1, long[] f2, long[] f3){
			this.f1 = f1;
			this.f2 = f2;
			this.f3 = f3;
		}
	}

	private static final TestDto DTO = new TestDto(
			new long[]{1, 2},
			null,
			new long[]{});

	@Test
	public void testIndexedEncoding(){
//		HexBlockTool.print(DTO.encodeIndexed());
		String hex = "0010800000000000000180000000000000020200";
		byte[] bytes = HexBlockTool.fromHexBlock(hex);
		Assert.assertEquals(DTO.encodeIndexed(), bytes);
		Assert.assertEquals(BinaryDtoIndexedCodec.of(TestDto.class).decode(bytes), DTO);
	}

	@Test
	public void testComparableEncoding(){
//		HexBlockTool.print(DTO.encodeComparable());
		String hex = "01800102010201020102010201020103800102010201020102010201020200000100";
		byte[] bytes = HexBlockTool.fromHexBlock(hex);
		Assert.assertEquals(DTO.encodeComparable(), bytes);
		Assert.assertEquals(BinaryDtoComparableCodec.of(TestDto.class).decode(bytes), DTO);
	}

}

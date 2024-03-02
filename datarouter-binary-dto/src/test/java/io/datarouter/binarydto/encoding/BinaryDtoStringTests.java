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

public class BinaryDtoStringTests{

	@SuppressWarnings("unused")
	private static class TestDto extends ComparableBinaryDto<TestDto>{

		public final String s1;
		public final String s2;
		public final String s3;

		public TestDto(String s1, String s2, String s3){
			this.s1 = s1;
			this.s2 = s2;
			this.s3 = s3;
		}

	}

	private static final TestDto DTO = new TestDto(
			"Aa", // hex 4161
			"Bb", // hex 4262
			"Cc");// hex 4363

	@Test
	public void testIndexedEncoding(){
//		HexBlockTool.print(dto.encodeIndexed());
		String hex = "000241610102426202024363";
		byte[] bytes = HexBlockTool.fromHexBlock(hex);
		Assert.assertEquals(DTO.encodeIndexed(), bytes);
		Assert.assertEquals(BinaryDtoIndexedCodec.of(TestDto.class).decode(bytes), DTO);
	}

	@Test
	public void testComparableEncoding(){
//		HexBlockTool.print(DTO.encodeComparable());
		String hex = "014161000142620001436300";
		byte[] bytes = HexBlockTool.fromHexBlock(hex);
		Assert.assertEquals(DTO.encodeComparable(), bytes);
		Assert.assertEquals(BinaryDtoComparableCodec.of(TestDto.class).decode(bytes), DTO);
	}

}

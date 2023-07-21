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

import io.datarouter.binarydto.codec.BinaryDtoIndexedCodec;
import io.datarouter.binarydto.dto.BinaryDto;
import io.datarouter.bytes.HexBlockTool;

public class BinaryDtoCharArrayTests{

	private static class TestDto extends BinaryDto<TestDto>{
		public final char[] f1;
		public final char[] f2;
		public final char[] f3;

		public TestDto(char[] f1, char[] f2, char[] f3){
			this.f1 = f1;
			this.f2 = f2;
			this.f3 = f3;
		}
	}

	private static final TestDto DTO = new TestDto(
			new char[]{'M', 'a', 't', 't'},
			null,
			new char[]{});

	@Test
	public void testIndexedEncoding(){
//		HexBlockTool.print(DTO.encodeIndexed());
		String hex = "0008004d0061007400740200";
		byte[] bytes = HexBlockTool.fromHexBlock(hex);
		Assert.assertEquals(DTO.encodeIndexed(), bytes);
		Assert.assertEquals(BinaryDtoIndexedCodec.of(TestDto.class).decode(bytes), DTO);
	}

}

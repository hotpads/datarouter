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

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.bytes.ByteTool;
import io.datarouter.bytes.Java9;
import io.datarouter.bytes.binarydto.codec.BinaryDtoCodec;
import io.datarouter.bytes.binarydto.dto.BaseBinaryDto;

public class BinaryDtoCharArrayTests{

	public static class TestDto extends BaseBinaryDto{
		public final char[] f1;
		public final char[] f2;
		public final char[] f3;

		public TestDto(char[] f1, char[] f2, char[] f3){
			this.f1 = f1;
			this.f2 = f2;
			this.f3 = f3;
		}
	}

	@Test
	public void testEncoding(){
		BinaryDtoCodec<TestDto> codec = new BinaryDtoCodec<>(TestDto.class);
		TestDto dto = new TestDto(
				new char[]{'M', 'a', 't', 't'},
				null,
				new char[]{});
		List<byte[]> expectedByteSegments = Java9.listOf(
				new byte[]{1},//f1 present
				new byte[]{4},//f1 length 4
				new byte[]{0, 77},//M
				new byte[]{0, 97, },//a
				new byte[]{0, 116},//t
				new byte[]{0, 116},//t
				new byte[]{0},//f2 null
				new byte[]{1},//f3 present
				new byte[]{0});//f3 length 0
		byte[] expectedFullBytes = ByteTool.concatenate(expectedByteSegments);
		byte[] actualFullBytes = codec.encode(dto);
		Assert.assertEquals(actualFullBytes, expectedFullBytes);

		TestDto actual = codec.decode(actualFullBytes);
		Assert.assertEquals(actual, dto);
	}


}

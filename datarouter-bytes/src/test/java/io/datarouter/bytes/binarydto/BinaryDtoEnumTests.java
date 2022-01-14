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

public class BinaryDtoEnumTests{

	public static enum TestEnum{
		AA, BB, CC
	}

	public static class TestDto extends BaseBinaryDto{
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
		BinaryDtoCodec<TestDto> codec = new BinaryDtoCodec<>(TestDto.class);
		TestDto dto = new TestDto(TestEnum.AA, null, TestEnum.CC);
		List<byte[]> expectedByteSegments = Java9.listOf(
				new byte[]{1},//f1 present
				new byte[]{'A', 'A', 0},//f1 value with terminator
				new byte[]{0},//f2 null
				new byte[]{1},//f3 present
				new byte[]{'C', 'C', 0});//f3 value with terminator
		byte[] expectedFullBytes = ByteTool.concatenate(expectedByteSegments);
		byte[] actualFullBytes = codec.encode(dto);
		Assert.assertEquals(actualFullBytes, expectedFullBytes);

		TestDto actual = codec.decode(actualFullBytes);
		Assert.assertEquals(actual, dto);
	}


}

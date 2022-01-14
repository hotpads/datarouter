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

public class BinaryDtoStringArrayTests{

	public static class TestDto extends BaseBinaryDto{
		public final String[] f1;
		public final String[] f2;
		public final String[] f3;

		public TestDto(String[] f1, String[] f2, String[] f3){
			this.f1 = f1;
			this.f2 = f2;
			this.f3 = f3;
		}
	}


	@Test
	public void testCreateCodec(){
		new BinaryDtoCodec<>(TestDto.class);
	}

	@Test
	public void testEncoding(){
		BinaryDtoCodec<TestDto> codec = new BinaryDtoCodec<>(TestDto.class);
		TestDto dto = new TestDto(
				new String[]{"a", null, "b"},
				null,
				new String[]{});
		List<byte[]> expectedByteSegments = Java9.listOf(
				//f1
				new byte[]{1},//present
				new byte[]{3},//size
				new byte[]{1},//item0 present
				new byte[]{'a', 0},//item0
				new byte[]{0},//item1 null
				new byte[]{1},//item2 present
				new byte[]{'b', 0},//item2
				//f2
				new byte[]{0},//null
				//f3
				new byte[]{1},//present
				new byte[]{0});//size 0
		byte[] expectedFullBytes = ByteTool.concatenate(expectedByteSegments);
		byte[] actualFullBytes = codec.encode(dto);
		Assert.assertEquals(actualFullBytes, expectedFullBytes);

		TestDto actual = codec.decode(actualFullBytes);
		Assert.assertEquals(actual, dto);
	}


}

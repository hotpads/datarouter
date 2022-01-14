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

import io.datarouter.bytes.ByteTool;
import io.datarouter.bytes.Java9;
import io.datarouter.bytes.binarydto.codec.BinaryDtoCodec;
import io.datarouter.bytes.binarydto.dto.BaseBinaryDto;

public class BinaryDtoIntListTests{

	public static class TestDto extends BaseBinaryDto{
		public final List<Integer> f1;
		public final List<Integer> f2;
		public final List<Integer> f3;

		public TestDto(List<Integer> f1, List<Integer> f2, List<Integer> f3){
			this.f1 = f1;
			this.f2 = f2;
			this.f3 = f3;
		}
	}

	@Test
	public void testEncoding(){
		BinaryDtoCodec<TestDto> codec = new BinaryDtoCodec<>(TestDto.class);
		TestDto dto = new TestDto(
				Arrays.asList(1, null, 2),
				null,
				Java9.listOf());
		List<byte[]> expectedByteSegments = Java9.listOf(
				new byte[]{1},//f1 present
				new byte[]{3},//f1 length 3
				new byte[]{1},//f1 item0 present
				new byte[]{Byte.MIN_VALUE, 0, 0, 1},//f1 item 0
				new byte[]{0},//f1 item1 null
				new byte[]{1},//f1 item2 present
				new byte[]{Byte.MIN_VALUE, 0, 0, 2},//f1 item 2
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

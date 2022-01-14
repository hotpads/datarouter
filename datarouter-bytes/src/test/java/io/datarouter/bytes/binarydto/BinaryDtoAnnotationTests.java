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

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.bytes.binarydto.codec.BinaryDtoCodec;
import io.datarouter.bytes.binarydto.dto.BaseBinaryDto;
import io.datarouter.bytes.binarydto.dto.BinaryDtoField;
import io.datarouter.bytes.binarydto.fieldcodec.string.PrefixedUtf8BinaryDtoFieldCodec;
import io.datarouter.bytes.binarydto.fieldcodec.string.TerminatedUtf8BinaryDtoFieldCodec;

public class BinaryDtoAnnotationTests{

	public static class TestDto extends BaseBinaryDto{

		@BinaryDtoField(codec = TerminatedUtf8BinaryDtoFieldCodec.class)
		public final String s1;
		@BinaryDtoField(codec = PrefixedUtf8BinaryDtoFieldCodec.class)
		public final String s2;
		//should use default TerminatedUtf8BinaryDtoFieldCodec
		public final String s3;

		public TestDto(String s1, String s2, String s3){
			this.s1 = s1;
			this.s2 = s2;
			this.s3 = s3;
		}

	}

	@Test
	public void testCustomCodecs(){
		BinaryDtoCodec<TestDto> codec = new BinaryDtoCodec<>(TestDto.class);
		TestDto dto = new TestDto("Aa", "Bb", "Cc");
		byte[] expectedBytes = new byte[]{
				1, 'A', 'a', 0,//present, value, terminator
				1, 2, 'B', 'b',//present, length, value
				1, 'C', 'c', 0};//present, value, terminator
		byte[] actualBytes = codec.encode(dto);
		Assert.assertEquals(actualBytes, expectedBytes);
		TestDto actualDto = codec.decode(actualBytes);
		Assert.assertEquals(actualDto, dto);
	}

}

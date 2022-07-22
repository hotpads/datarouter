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
package io.datarouter.binarydto;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.binarydto.dto.BinaryDtoField;
import io.datarouter.binarydto.dto.ComparableBinaryDto;
import io.datarouter.binarydto.fieldcodec.string.Utf8BinaryDtoFieldCodec;

public class BinaryDtoAnnotationTests{

	public static class TestDto extends ComparableBinaryDto<TestDto>{

		@BinaryDtoField(codec = Utf8BinaryDtoFieldCodec.class)
		public final String s1;
		@BinaryDtoField(codec = Utf8BinaryDtoFieldCodec.class)
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
		var dto = new TestDto("Aa", "Bb", "Cc");
		Assert.assertEquals(dto.cloneIndexed(), dto);
		Assert.assertEquals(dto.cloneComparable(), dto);
	}

}

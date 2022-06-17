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

import io.datarouter.bytes.binarydto.codec.BinaryDtoIndexedCodec;
import io.datarouter.bytes.binarydto.dto.BinaryDto;

public class BinaryDtoStringArrayTests{

	public static class TestDto extends BinaryDto<TestDto>{
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
		BinaryDtoIndexedCodec.of(TestDto.class);
	}

	@Test
	public void testEncoding(){
		var dto = new TestDto(
				new String[]{"a", null, "b"},
				null,
				new String[]{});
		Assert.assertEquals(dto.cloneIndexed(), dto);
	}

	public static class Test2Dto extends BinaryDto<Test2Dto>{
		public final String[] f1;

		public Test2Dto(String[] f1){
			this.f1 = f1;
		}
	}

	@Test
	public void testEncoding2(){
		var dto = new Test2Dto(new String[]{"a", "bc"});
		Assert.assertEquals(dto.cloneIndexed(), dto);
	}

}

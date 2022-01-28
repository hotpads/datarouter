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

import io.datarouter.bytes.binarydto.codec.BinaryDtoCodec;
import io.datarouter.bytes.binarydto.dto.BinaryDto;

public class BinaryDtoInheritanceTests{

	public static class InnerTestDto<T extends InnerTestDto<T>> extends BinaryDto<T>{
		public final String inner1;

		public InnerTestDto(String inner1){
			this.inner1 = inner1;
		}
	}

	public static class OuterTestDto extends InnerTestDto<OuterTestDto>{
		public final String outer1;

		public OuterTestDto(String inner1, String outer1){
			super(inner1);
			this.outer1 = outer1;
		}
	}

	@Test
	public void testFieldsDetected(){
		List<String> expected = List.of("inner1", "outer1");
		List<String> actual = new OuterTestDto("in", "out").scanFieldNames().list();
		Assert.assertEquals(actual, expected);
	}

	@Test
	public void testFieldsSerialized(){
		var codec = BinaryDtoCodec.of(OuterTestDto.class);
		var dto = new OuterTestDto("in", "out");
		byte[] bytes = codec.encode(dto);
		OuterTestDto actual = codec.decode(bytes);
		Assert.assertEquals(actual, dto);
	}

}

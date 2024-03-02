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

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.binarydto.dto.ComparableBinaryDto;

public class BinaryDtoInheritanceTests{

	private static class InnerTestDto<T extends InnerTestDto<T>> extends ComparableBinaryDto<T>{
		@SuppressWarnings("unused")
		public final String inner1;

		public InnerTestDto(String inner1){
			this.inner1 = inner1;
		}
	}

	private static class OuterTestDto extends InnerTestDto<OuterTestDto>{
		@SuppressWarnings("unused")
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
		var dto = new OuterTestDto("in", "out");
		Assert.assertEquals(dto.cloneIndexed(), dto);
		Assert.assertEquals(dto.cloneComparable(), dto);
	}

}

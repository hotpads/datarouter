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
import io.datarouter.binarydto.dto.BinaryDtoField;

public class BinaryDtoNullabilityTests{

	private static class TestDto extends BinaryDto<TestDto>{
		public final int f1;
		@BinaryDtoField(nullable = true)
		public final int f2;
		public final Integer f3;
		@BinaryDtoField(nullable = false)
		public final Integer f4;
		public final Integer[] f5;
		@BinaryDtoField(nullable = false, nullableItems = false)
		public final Integer[] f6;

		public TestDto(int f1, int f2, Integer f3, Integer f4, Integer[] f5, Integer[] f6){
			this.f1 = f1;
			this.f2 = f2;
			this.f3 = f3;
			this.f4 = f4;
			this.f5 = f5;
			this.f6 = f6;
		}
	}

	@Test
	public void testEncoding(){
		var dto = new TestDto(1, 2, 3, 4, new Integer[]{5, 6}, new Integer[]{7, 8});
		Assert.assertEquals(dto.cloneIndexed(), dto);
	}

	@Test(expectedExceptions = { IllegalArgumentException.class })
	public void testRejectNullAndExceptionType(){
		var codec = BinaryDtoIndexedCodec.of(TestDto.class);
		var dto = new TestDto(1, 2, 3, null, null, null);
		codec.encode(dto);
	}

}

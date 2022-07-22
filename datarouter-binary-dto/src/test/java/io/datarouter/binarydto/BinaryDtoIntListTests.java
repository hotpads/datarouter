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

import java.util.Arrays;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.binarydto.dto.BinaryDto;

public class BinaryDtoIntListTests{

	public static class TestDto extends BinaryDto<TestDto>{
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
		var dto = new TestDto(
				Arrays.asList(1, null, 2),
				null,
				List.of());
		Assert.assertEquals(dto.cloneIndexed(), dto);
	}


}

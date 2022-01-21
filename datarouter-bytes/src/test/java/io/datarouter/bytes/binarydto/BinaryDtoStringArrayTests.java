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
		BinaryDtoCodec.of(TestDto.class);
	}

	@Test
	public void testEncoding(){
		var codec = BinaryDtoCodec.of(TestDto.class);
		var dto = new TestDto(
				new String[]{"a", null, "b"},
				null,
				new String[]{});
		byte[] expectedBytes = {
				//f1
				1,//present
				3,//size
				1,//item0 present
				'a', 0,//item0
				0,//item1 null
				1,//item2 present
				'b', 0,//item2
				//f2
				0,//null
				//f3
				1,//present
				0};//size 0
		byte[] actualBytes = codec.encode(dto);
		Assert.assertEquals(actualBytes, expectedBytes);

		TestDto actual = codec.decode(actualBytes);
		Assert.assertEquals(actual, dto);
	}


}

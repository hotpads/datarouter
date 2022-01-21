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

public class BinaryDtoFieldVisibilityTests{

	public static class TestDto extends BinaryDto<TestDto>{
		final Integer fPackage;
		@SuppressWarnings("unused")
		private final Integer fPrivate;
		protected final Integer fProtected;
		public final Integer fPublic;

		public TestDto(Integer fPackage, Integer fPrivate, Integer fProtected, Integer fPublic){
			this.fPackage = fPackage;
			this.fPrivate = fPrivate;
			this.fProtected = fProtected;
			this.fPublic = fPublic;
		}
	}

	@Test
	public void testFieldsDetected(){
		List<String> expected = List.of("fPackage", "fPrivate", "fProtected", "fPublic");
		List<String> actual = new TestDto(1, 2, 3, 4).scanFieldNames().list();
		Assert.assertEquals(actual, expected);
	}

	@Test
	public void testFieldsSerialized(){
		var codec = BinaryDtoCodec.of(TestDto.class);
		var dto = new TestDto(1, 2, 3, 4);
		byte[] bytes = codec.encode(dto);
		TestDto actual = codec.decode(bytes);
		Assert.assertEquals(actual, dto);
	}

}

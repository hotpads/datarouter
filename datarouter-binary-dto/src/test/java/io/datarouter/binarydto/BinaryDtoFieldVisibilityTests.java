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

public class BinaryDtoFieldVisibilityTests{

	@SuppressWarnings("unused")
	private static class TestDto extends ComparableBinaryDto<TestDto>{

		//Ensure the static field is ignored
		public static final int STATIC_FIELD = 0;

		final Integer fPackage;
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
		List<String> expected = List.of("fPackage", "fPrivate", "fProtected", "fPublic");// excludes STATIC_FIELD
		List<String> actual = new TestDto(1, 2, 3, 4).scanFieldNames().list();
		Assert.assertEquals(actual, expected);
	}

	@Test
	public void testFieldsSerialized(){
		var dto = new TestDto(1, 2, 3, 4);
		Assert.assertEquals(dto.cloneIndexed(), dto);
		Assert.assertEquals(dto.cloneComparable(), dto);
	}

}

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
package io.datarouter.instrumentation.doc;

import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ApiDocTests{

	public static class TestClassParametrizedField{
		@ApiDoc(
				isOptional = true,
				description = "A primitive field",
				max = 100,
				min = 0,
				isDeprecated = true,
				maxLength = 255)
		public int primitiveField;
		@ApiDoc(
				description = "A list of strings")
		public List<String> stringList;
		public List<List<String>> nestedStringList;
		Map<String, Integer> testMap;
	}

	@Test
	public void testPrimitiveFieldAnnotation() throws NoSuchFieldException{
		ApiDoc annotation = TestClassParametrizedField.class.getField("primitiveField").getAnnotation(ApiDoc.class);
		Assert.assertTrue(annotation.isOptional());
		Assert.assertTrue(annotation.isDeprecated());
		Assert.assertEquals(annotation.description(), "A primitive field");
		Assert.assertEquals(annotation.max(), 100);
		Assert.assertEquals(annotation.min(), 0);
		Assert.assertEquals(annotation.maxLength(), 255);
	}

	@Test
	public void testStringListAnnotation() throws NoSuchFieldException{
		ApiDoc annotation = TestClassParametrizedField.class.getField("stringList").getAnnotation(ApiDoc.class);
		Assert.assertFalse(annotation.isOptional()); // default value
		Assert.assertEquals(annotation.description(), "A list of strings");
		Assert.assertEquals(annotation.max(), Long.MAX_VALUE); // default value
		Assert.assertEquals(annotation.min(), Long.MIN_VALUE); // default value
		Assert.assertEquals(annotation.maxLength(), Long.MAX_VALUE); // default value
		Assert.assertFalse(annotation.isDeprecated()); // default value
	}

	@Test
	public void testNestedStringListAnnotation() throws NoSuchFieldException{
		ApiDoc annotation = TestClassParametrizedField.class.getField("nestedStringList").getAnnotation(ApiDoc.class);
		Assert.assertNull(annotation); // no annotation present
	}
}

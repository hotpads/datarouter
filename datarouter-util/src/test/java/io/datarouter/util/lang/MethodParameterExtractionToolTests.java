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
package io.datarouter.util.lang;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Optional;

import org.testng.Assert;
import org.testng.annotations.Test;

public class MethodParameterExtractionToolTests{

	private static final String METHOD_NAME = "fooBar";
	private static final Method METHOD = ReflectionTool.getDeclaredMethodIncludingAncestors(
			MethodParameterExtractionToolTests.class,
			METHOD_NAME,
			Optional.class,
			Optional.class,
			Optional.class,
			String.class);

	@SuppressWarnings("unused")
	public void fooBar(
			Optional<String> firstName,
			Optional<Integer> age,
			Optional<Boolean> isTall,
			String lastName){
	}

	@Test
	public void testExtractParameterizedTypeFromOptionalParameter(){
		Parameter param1 = METHOD.getParameters()[0];
		Assert.assertEquals(MethodParameterExtractionTool.extractParameterizedTypeFromOptionalParameter(param1),
				String.class);

		Parameter param2 = METHOD.getParameters()[1];
		Assert.assertEquals(MethodParameterExtractionTool.extractParameterizedTypeFromOptionalParameter(param2),
				Integer.class);

		Parameter param3 = METHOD.getParameters()[2];
		Assert.assertEquals(MethodParameterExtractionTool.extractParameterizedTypeFromOptionalParameter(param3),
				Boolean.class);

		Parameter param4 = METHOD.getParameters()[3];
		Assert.assertThrows(IllegalArgumentException.class,
				() -> MethodParameterExtractionTool.extractParameterizedTypeFromOptionalParameter(param4));
	}

}

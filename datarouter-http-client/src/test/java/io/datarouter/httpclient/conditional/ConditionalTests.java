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
package io.datarouter.httpclient.conditional;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.httpclient.response.Conditional;

public class ConditionalTests{

	@Test
	public void testConditional(){
		String testString = "this is a test";
		Conditional<String> successConditional = Conditional.success(testString);
		Conditional<String> failureConditional = Conditional.failure(new RuntimeException());

		Assert.assertTrue(successConditional.isSuccess());
		Assert.assertFalse(successConditional.isFailure());

		Assert.assertTrue(failureConditional.isFailure());
		Assert.assertFalse(failureConditional.isSuccess());

		String mapString = successConditional
				.map(string -> string.concat(" plus more"))
				.orElse(null);
		Assert.assertEquals(mapString, testString + " plus more");

		String orElseString = failureConditional
				.map(string -> string.concat(" plus more"))
				.orElse(null);
		Assert.assertNull(orElseString);
	}

}

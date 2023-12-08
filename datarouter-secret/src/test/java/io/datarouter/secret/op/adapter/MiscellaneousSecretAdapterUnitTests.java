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
package io.datarouter.secret.op.adapter;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.secret.client.Secret;

public class MiscellaneousSecretAdapterUnitTests{

	@Test
	public void testNoOpAdapter(){
		NoOpAdapter<String> adapter = new NoOpAdapter<>();
		String test = "test";
		String adapted = adapter.adapt(test);
		Assert.assertEquals(adapted, test);
		Assert.assertSame(test, adapted);
	}

	@Test
	public void testValueExtractingAdapter(){
		String testString = "test";
		SecretValueExtractingAdapter<String> stringExtractor = new SecretValueExtractingAdapter<>();
		Assert.assertEquals(stringExtractor.adapt(new Secret("name", testString)), testString);

		Object testObj = 1L;
		SecretValueExtractingAdapter<Object> objectExtractor = new SecretValueExtractingAdapter<>();
		Assert.assertEquals(objectExtractor.adapt(new TypedSecret<>("name", testObj)), testObj);
	}

}

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
package io.datarouter.httpclient.request;

import org.testng.Assert;
import org.testng.annotations.Test;

public class HttpRequestMethodTests{

	@Test
	public void testFromPersistentString(){
		Assert.assertEquals(HttpRequestMethod.fromPersistentStringStatic("GET"), HttpRequestMethod.GET);
		Assert.assertEquals(HttpRequestMethod.fromPersistentStringStatic("Get"), HttpRequestMethod.GET);
		Assert.assertEquals(HttpRequestMethod.fromPersistentStringStatic("get"), HttpRequestMethod.GET);

		Assert.assertTrue(HttpRequestMethod.GET.matches("GET"));
		Assert.assertFalse(HttpRequestMethod.POST.matches("GET"));

		Assert.assertNull(HttpRequestMethod.fromPersistentStringStatic(null));
		Assert.assertNull(HttpRequestMethod.fromPersistentStringStatic(""));
	}


	@Test
	public void testAllEnumValues(){
		Assert.assertEquals(HttpRequestMethod.GET.toString(), "GET");
		Assert.assertEquals(HttpRequestMethod.GET.name(), "GET");
		Assert.assertEquals(HttpRequestMethod.GET.persistentString, "GET");
	}

}

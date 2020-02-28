/**
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
package io.datarouter.web.user.authenticate.config;

import org.testng.Assert;
import org.testng.annotations.Test;

public class BaseDatarouterAuthenticationConfigTests{

	@Test
	public void testNormalize(){
		Assert.assertEquals(BaseDatarouterAuthenticationConfig.normalizePath(""), "");
		Assert.assertEquals(BaseDatarouterAuthenticationConfig.normalizePath("/"), "/");
		Assert.assertEquals(BaseDatarouterAuthenticationConfig.normalizePath(" / "), "/");
		Assert.assertEquals(BaseDatarouterAuthenticationConfig.normalizePath("/caterpillar"), "/caterpillar");
		Assert.assertEquals(BaseDatarouterAuthenticationConfig.normalizePath("/caterpillar/"), "/caterpillar");
		// prob not valid
		Assert.assertEquals(BaseDatarouterAuthenticationConfig.normalizePath("/caterpillar//"), "/caterpillar/");
	}

	@Test
	public void testContains(){
		Assert.assertTrue(BaseDatarouterAuthenticationConfig.pathAContainsB("/fl", "/fl"));
		Assert.assertTrue(BaseDatarouterAuthenticationConfig.pathAContainsB("/fl", "/fl/"));
		Assert.assertTrue(BaseDatarouterAuthenticationConfig.pathAContainsB("/fl/", "/fl/"));
		Assert.assertTrue(BaseDatarouterAuthenticationConfig.pathAContainsB("/fl", "/fl/owbee"));
		Assert.assertFalse(BaseDatarouterAuthenticationConfig.pathAContainsB("/fl", "/flowbee"));
		Assert.assertFalse(BaseDatarouterAuthenticationConfig.pathAContainsB("/flowbee", "/fl"));
	}

}

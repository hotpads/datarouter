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
package io.datarouter.web.util.http;

import org.testng.Assert;
import org.testng.annotations.Test;

public class RequestToolTests{

	@Test
	public void testCheckDouble(){
		Assert.assertFalse(RequestTool.checkDouble(-0.01, false, false, false, false));
		Assert.assertTrue(RequestTool.checkDouble(-0.01, false, true, false, false));
		Assert.assertFalse(RequestTool.checkDouble(null, false, false, false, false));
		Assert.assertTrue(RequestTool.checkDouble(null, true, false, false, false));
		Assert.assertFalse(RequestTool.checkDouble(Double.NEGATIVE_INFINITY, false, false, false, false));
		Assert.assertFalse(RequestTool.checkDouble(Double.NEGATIVE_INFINITY, false, true, false, false));
		Assert.assertFalse(RequestTool.checkDouble(Double.NEGATIVE_INFINITY, false, false, true, false));
		Assert.assertTrue(RequestTool.checkDouble(Double.NEGATIVE_INFINITY, false, true, true, false));
		Assert.assertFalse(RequestTool.checkDouble(Double.POSITIVE_INFINITY, false, false, false, false));
		Assert.assertTrue(RequestTool.checkDouble(Double.POSITIVE_INFINITY, false, false, true, false));
		Assert.assertFalse(RequestTool.checkDouble(Double.NaN, false, false, false, false));
		Assert.assertTrue(RequestTool.checkDouble(Double.NaN, false, false, false, true));
	}

	@Test
	public void testGetRequestUriWithQueryString(){
		Assert.assertEquals(RequestTool.getRequestUriWithQueryString("example.com", null), "example.com");
		Assert.assertEquals(RequestTool.getRequestUriWithQueryString("example.com", "stuff=things"),
				"example.com?stuff=things");
	}

	@Test
	public void testMakeRedirectUriIfTrailingSlash(){
		Assert.assertNull(RequestTool.makeRedirectUriIfTrailingSlash("example.com", null));
		Assert.assertEquals(RequestTool.makeRedirectUriIfTrailingSlash("example.com/", null), "example.com");
		Assert.assertEquals(RequestTool.makeRedirectUriIfTrailingSlash("example.com/", "stuff=things"),
				"example.com?stuff=things");
		Assert.assertNull(RequestTool.makeRedirectUriIfTrailingSlash("example.com", "stuff=things"));
	}

	@Test
	public void testGetFullyQualifiedUrl(){
		Assert.assertEquals(RequestTool.getFullyQualifiedUrl("aurl", "http://x.com", "x.com", 80).toString(),
				"http://x.com/aurl");
		Assert.assertEquals(RequestTool.getFullyQualifiedUrl("/aurl", "http://x.com", "x.com", 80).toString(),
				"http://x.com/aurl");
		Assert.assertEquals(RequestTool.getFullyQualifiedUrl("/aurl", "https://x.com", "x.com", 80).toString(),
				"https://x.com:80/aurl");
		Assert.assertEquals(RequestTool.getFullyQualifiedUrl("/aurl", "https://x.com", "x.com", 443).toString(),
				"https://x.com/aurl");
		Assert.assertEquals(RequestTool.getFullyQualifiedUrl("/", "https://x.com", "x.com", 443).toString(), "https://x.com/");
		Assert.assertEquals(RequestTool.getFullyQualifiedUrl("", "https://x.com", "x.com", 443).toString(), "https://x.com/");
		Assert.assertEquals(RequestTool.getFullyQualifiedUrl("", "http://x.com:8080", "x.com", 8080).toString(),
				"http://x.com:8080/");
		Assert.assertEquals(RequestTool.getFullyQualifiedUrl("snack", "https://x.com", "x.com", 8443).toString(),
				"https://x.com:8443/snack");
	}

	@Test
	public void testIsAValidIpV4(){
		Assert.assertTrue(RequestTool.isAValidIpV4("1.0.1.1"));
		Assert.assertTrue(RequestTool.isAValidIpV4("0.0.0.0"));
		Assert.assertTrue(RequestTool.isAValidIpV4("124.159.0.18"));
		Assert.assertFalse(RequestTool.isAValidIpV4("256.159.0.18"));
		Assert.assertFalse(RequestTool.isAValidIpV4("blabla"));
		Assert.assertFalse(RequestTool.isAValidIpV4(""));
		Assert.assertFalse(RequestTool.isAValidIpV4(null));
	}

}

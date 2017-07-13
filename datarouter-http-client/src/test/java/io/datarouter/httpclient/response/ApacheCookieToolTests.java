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
package io.datarouter.httpclient.response;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.cookie.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.httpclient.response.ApacheCookieTool;

public class ApacheCookieToolTests{

	private final Cookie shortBreadCookie = new BasicClientCookie("shortBreadCookie", "ThumbPrint");
	private final Cookie sandwichCookie = new BasicClientCookie("sandwichCookie", "Oreo");

	@Test
	public void testGetCookie(){
		List<Cookie> cookies = new ArrayList<>();
		cookies.add(shortBreadCookie);
		cookies.add(sandwichCookie);

		Cookie cookie = ApacheCookieTool.getCookie(cookies, shortBreadCookie.getName());
		Assert.assertTrue(shortBreadCookie.getName().equals(cookie.getName()));
	}

	@Test
	public void testGetCookieValue(){
		List<Cookie> cookies = new ArrayList<>();
		cookies.add(shortBreadCookie);

		String cookieValue = ApacheCookieTool.getCookieValue(cookies, shortBreadCookie.getName());
		Assert.assertTrue(shortBreadCookie.getValue().equals(cookieValue));
	}

}
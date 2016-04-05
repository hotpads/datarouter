package com.hotpads.util.http.response;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.cookie.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ApacheCookieToolTests {

	private final Cookie shortBreadCookie = new BasicClientCookie("shortBreadCookie", "ThumbPrint");
	private final Cookie sandwichCookie = new BasicClientCookie("sandwichCookie", "Oreo");

	@Test
	public void testGetCookie() {
		List<Cookie> cookies = new ArrayList<>();
		cookies.add(shortBreadCookie);
		cookies.add(sandwichCookie);

		Cookie cookie = ApacheCookieTool.getCookie(cookies, shortBreadCookie.getName());
		Assert.assertTrue(shortBreadCookie.getName().equals(cookie.getName()));
	}

	@Test
	public void testGetCookieValue() {
		List<Cookie> cookies = new ArrayList<>();
		cookies.add(shortBreadCookie);

		String cookieValue = ApacheCookieTool.getCookieValue(cookies, shortBreadCookie.getName());
		Assert.assertTrue(shortBreadCookie.getValue().equals(cookieValue));
	}

}
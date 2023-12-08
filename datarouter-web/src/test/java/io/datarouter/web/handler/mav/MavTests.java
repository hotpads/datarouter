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
package io.datarouter.web.handler.mav;

import org.testng.Assert;
import org.testng.annotations.Test;

public class MavTests{

	public static final String URL = "url";
	public static final String VIEW_NAME = "viewName";

	@Test
	public void testIsRedirect(){
		//blocks are for namespacing safety
		{
			Mav mav = new Mav();
			Assert.assertFalse(mav.isRedirect());
			mav.setGlobalRedirectUrl(URL, false);
			Assert.assertTrue(mav.isRedirect());
			Assert.assertFalse(mav.shouldAppendModelQueryParams);
		}

		{
			Mav mav = new Mav();
			mav.setGlobalRedirectUrl(URL, true);
			Assert.assertTrue(mav.isRedirect());
			Assert.assertTrue(mav.shouldAppendModelQueryParams);
		}

		{
			//this constructor calls setViewName
			Mav mav = new Mav("");
			Assert.assertFalse(mav.isRedirect());
			Assert.assertFalse(mav.shouldAppendModelQueryParams);
			Assert.assertEquals(mav.getViewName(), mav.toJspFile(""));
		}

		{
			Mav mav = new Mav(Mav.REDIRECT);
			Assert.assertTrue(mav.isRedirect());
			Assert.assertTrue(mav.shouldAppendModelQueryParams);
			Assert.assertEquals(mav.getViewName(), "");
		}
	}

	@Test
	public void testGetRedirectUrl(){
		//no-arg constructor
		{
			Mav mav = new Mav();
			Assert.assertNull(mav.getRedirectUrl());
		}

		//1-arg/viewName constructor
		{
			Mav mav = new Mav(URL);
			Assert.assertNull(mav.getRedirectUrl());
		}

		{
			Mav mav = new Mav(Mav.REDIRECT + URL);
			Assert.assertEquals(mav.getRedirectUrl(), URL);
		}

		{
			Mav mav = new Mav(Mav.REDIRECT + URL + '.' + URL);
			Assert.assertEquals(mav.getRedirectUrl(), URL + '.' + URL);
		}

		{
			Mav mav = new Mav(Mav.REDIRECT + URL);
			mav.put("key", "value");
			Assert.assertEquals(mav.getRedirectUrl(), URL + "?key=value");
		}

		{
			Mav mav = new Mav(Mav.REDIRECT + URL);
			mav.put("key", "value");
			mav.put("key2", "value2");
			Assert.assertTrue(mav.getRedirectUrl().equals(URL + "?key=value&key2=value2")
					|| mav.getRedirectUrl().equals(URL + "?key2=value2&key=value"));
		}


		//setGlobalRedirect
		{
			Mav mav = new Mav();
			mav.setGlobalRedirectUrl(URL, false);
			Assert.assertEquals(mav.getRedirectUrl(), URL);
		}

		{
			Mav mav = new Mav();
			mav.setGlobalRedirectUrl(URL, false);
			mav.put("key", "value");
			Assert.assertEquals(mav.getRedirectUrl(), URL);
		}

		{
			Mav mav = new Mav();
			mav.setGlobalRedirectUrl(URL, true);
			Assert.assertEquals(mav.getRedirectUrl(), URL);
		}

		{
			Mav mav = new Mav();
			mav.setGlobalRedirectUrl(URL, true);
			mav.put("key", "value");
			Assert.assertEquals(mav.getRedirectUrl(), URL + "?key=value");
		}

		{
			Mav mav = new Mav();
			mav.setGlobalRedirectUrl(URL, true);
			mav.put("key", "value");
			mav.put("key2", "value2");
			Assert.assertTrue(mav.getRedirectUrl().equals(URL + "?key=value&key2=value2")
					|| mav.getRedirectUrl().equals(URL + "?key2=value2&key=value"));
		}

		//combination
		{
			Mav mav = new Mav(VIEW_NAME);
			Assert.assertNull(mav.getRedirectUrl());
			mav.setGlobalRedirectUrl(URL, false);
			mav.put("key", "value");
			Assert.assertEquals(mav.getRedirectUrl(), URL);
		}

		{
			Mav mav = new Mav(VIEW_NAME);
			Assert.assertNull(mav.getRedirectUrl());
			mav.setGlobalRedirectUrl(URL, true);
			mav.put("key", "value");
			Assert.assertEquals(mav.getRedirectUrl(), URL + "?key=value");
		}

		{
			Mav mav = new Mav(Mav.REDIRECT + VIEW_NAME);
			Assert.assertEquals(mav.getRedirectUrl(), VIEW_NAME);
			mav.setGlobalRedirectUrl(URL, true);
			mav.put("key", "value");
			Assert.assertEquals(mav.getRedirectUrl(), URL + "?key=value");
		}

		//query param encoding
		{
			Mav mav = new Mav(Mav.REDIRECT + URL);
			mav.put("&=", "&=");
			Assert.assertEquals(mav.getRedirectUrl(), URL + "?%26%3D=%26%3D");
		}

	}

}

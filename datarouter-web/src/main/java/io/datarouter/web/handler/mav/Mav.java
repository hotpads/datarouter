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
package io.datarouter.web.handler.mav;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.httpclient.path.PathNode;
import io.datarouter.httpclient.response.HttpStatusCode;
import io.datarouter.util.string.StringTool;

public class Mav{

	public static final String REDIRECT = "redirect:";
	private static final String UTF8 = "UTF-8";

	private boolean redirect = false;
	private boolean shouldAppendModelQueryParams = false;
	private String viewName;
	private String context;
	private String contentType = "text/html; charset=utf-8";
	private Map<String,Object> model = new HashMap<>();
	private String globalRedirectUrl;
	private int statusCode = HttpStatusCode.SC_200_OK.getStatusCode();

	/*---------------------------- constructors -----------------------------*/

	public Mav(){
	}

	public Mav(PathNode pathNode){
		this(pathNode.toSlashedString());
	}

	public Mav(String viewName){
		this.setViewName(viewName);
	}

	public Mav(String context, PathNode pathNode){
		this(context, pathNode.toSlashedString());
	}

	public Mav(String context, String viewName){
		this(viewName);
		this.context = context;
	}

	public Mav(PathNode pathNode, Map<String,Object> model){
		this(pathNode.toSlashedString(), model);
	}

	public Mav(String viewName, Map<String,Object> model){
		this(viewName);
		this.model = model;
	}

	/*---------------------------------- methods ----------------------------*/

	/**
	 * This method returns the value you give it to enable things like fetching an object from the database and getting
	 * a reference to it in one line.
	 */
	public <T> T put(String key, T value){
		model.put(key, value);
		return value;
	}

	public Map<? extends String,?> putAll(Map<? extends String,?> map){
		model.putAll(map);
		return map;
	}

	public boolean isRedirect(){
		return redirect;
	}

	public String getRedirectUrl(){
		if(!redirect){
			return null;
		}
		String queryParams = "";
		if(shouldAppendModelQueryParams && model.size() > 0){
			try{
				StringBuilder queryParamsBuilder = new StringBuilder("?");
				for(Entry<String,Object> entry : model.entrySet()){
					queryParamsBuilder
							.append(URLEncoder.encode(entry.getKey(), UTF8)).append('=')
							.append(URLEncoder.encode(entry.getValue().toString(), UTF8)).append('&');
				}
				queryParams = queryParamsBuilder.substring(0, queryParamsBuilder.length() - 1);//remove last &
			}catch(UnsupportedEncodingException e){
				throw new RuntimeException(e);
			}
		}
		return (StringTool.notEmpty(globalRedirectUrl) ? globalRedirectUrl : viewName) + queryParams;
	}

	public Mav setViewName(PathNode pathNode){
		return setViewName(pathNode.toSlashedString());
	}

	public Mav setViewName(String viewName){
		if(StringTool.nullSafe(viewName).startsWith(REDIRECT)){
			redirect = true;
			shouldAppendModelQueryParams = true;
			this.viewName = viewName.substring(REDIRECT.length());
		}else{
			if(viewName.contains(".")){
				this.viewName = viewName;
			}else{
				this.viewName = toJspFile(viewName);
			}
		}
		return this;
	}

	public Mav setGlobalRedirectUrl(String globalRedirectUrl, boolean shouldAppendModelQueryParams){
		redirect = true;
		this.globalRedirectUrl = globalRedirectUrl;
		this.shouldAppendModelQueryParams = shouldAppendModelQueryParams;
		return this;
	}

	private String toJspFile(String viewName){
		return "/WEB-INF/jsp" + viewName + ".jsp";
	}

	/*-------------------------------- get/set ------------------------------*/

	public Map<String,Object> getModel(){
		return model;
	}

	public String getViewName(){
		return viewName;
	}

	public String getContext(){
		return context;
	}

	public String getContentType(){
		return contentType;
	}

	public void setContentType(String contentType){
		this.contentType = contentType;
	}

	public int getStatusCode(){
		return statusCode;
	}

	public void setStatusCode(int statusCode){
		this.statusCode = statusCode;
	}

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
				Assert.assertEquals(mav.viewName, mav.toJspFile(""));
			}

			{
				Mav mav = new Mav(Mav.REDIRECT);
				Assert.assertTrue(mav.isRedirect());
				Assert.assertTrue(mav.shouldAppendModelQueryParams);
				Assert.assertEquals(mav.viewName, "");
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
				Mav mav = new Mav(REDIRECT + URL);
				Assert.assertEquals(mav.getRedirectUrl(), URL);
			}

			{
				Mav mav = new Mav(REDIRECT + URL + '.' + URL);
				Assert.assertEquals(mav.getRedirectUrl(), URL + '.' + URL);
			}

			{
				Mav mav = new Mav(REDIRECT + URL);
				mav.put("key", "value");
				Assert.assertEquals(mav.getRedirectUrl(), URL + "?key=value");
			}

			{
				Mav mav = new Mav(REDIRECT + URL);
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
				Assert.assertEquals(mav.getRedirectUrl(), null);
				mav.setGlobalRedirectUrl(URL, false);
				mav.put("key", "value");
				Assert.assertEquals(mav.getRedirectUrl(), URL);
			}

			{
				Mav mav = new Mav(VIEW_NAME);
				Assert.assertEquals(mav.getRedirectUrl(), null);
				mav.setGlobalRedirectUrl(URL, true);
				mav.put("key", "value");
				Assert.assertEquals(mav.getRedirectUrl(), URL + "?key=value");
			}

			{
				Mav mav = new Mav(REDIRECT + VIEW_NAME);
				Assert.assertEquals(mav.getRedirectUrl(), VIEW_NAME);
				mav.setGlobalRedirectUrl(URL, true);
				mav.put("key", "value");
				Assert.assertEquals(mav.getRedirectUrl(), URL + "?key=value");
			}

			//query param encoding
			{
				Mav mav = new Mav(REDIRECT + URL);
				mav.put("&=", "&=");
				Assert.assertEquals(mav.getRedirectUrl(), URL + "?%26%3D=%26%3D");
			}

		}
	}

}

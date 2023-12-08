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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.httpclient.response.HttpStatusCode;
import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.pathnode.PathNode;
import io.datarouter.util.string.StringTool;

public class Mav{
	private static final Logger logger = LoggerFactory.getLogger(Mav.class);

	public static final String REDIRECT = "redirect:";
	private static final String UTF8 = "UTF-8";

	private boolean redirect = false;
	protected boolean shouldAppendModelQueryParams = false;
	private String viewName;
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

	public Mav(PathNode pathNode, Map<String,Object> model){
		this(pathNode.toSlashedString(), model);
	}

	public Mav(String viewName, Map<String,Object> model){
		this(viewName);
		this.model = model;
	}

	/*---------------------------------- methods ----------------------------*/

	/**
	 * This method returns the value you give it to enable things like fetching an object and getting a reference to it
	 * in one line.
	 */
	public <T> T put(String key, T value){
		if(value instanceof BaseDatabean){
			logger.warn("Not a good practice to put databeans inside Mav objects. Databean={}", value.getClass()
					.getSimpleName(), new Throwable());
		}
		model.put(key, value);
		return value;
	}

	public void putAll(Map<? extends String,?> map){
		map.forEach(this::put);
	}

	public boolean isRedirect(){
		return redirect;
	}

	public String getRedirectUrl(){
		if(!redirect){
			return null;
		}
		String queryParams = "";
		if(shouldAppendModelQueryParams && !model.isEmpty()){
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
		}else if(viewName.contains(".")){
			this.viewName = viewName;
		}else{
			this.viewName = toJspFile(viewName);
		}
		return this;
	}

	public Mav setGlobalRedirectUrl(String globalRedirectUrl, boolean shouldAppendModelQueryParams){
		redirect = true;
		this.globalRedirectUrl = globalRedirectUrl;
		this.shouldAppendModelQueryParams = shouldAppendModelQueryParams;
		return this;
	}

	protected String toJspFile(String viewName){
		return "/WEB-INF/jsp" + viewName + ".jsp";
	}

	/*-------------------------------- get/set ------------------------------*/

	public Map<String,Object> getModel(){
		return model;
	}

	public String getViewName(){
		return viewName;
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

}

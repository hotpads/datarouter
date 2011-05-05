package com.hotpads.handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Preconditions;
import com.hotpads.util.core.BooleanTool;

public class Params{

	protected HttpServletRequest request;
	protected HttpServletResponse response;

	public Params(HttpServletRequest request, HttpServletResponse response){
		this.request = request;
		this.response = response;
	}
	
	public String required(String key){
		return Preconditions.checkNotNull(request.getParameter(key));
	}
	
	public String optional(String key, String defaultValue){
		String value = request.getParameter(key);
		return value==null?defaultValue:value;
	}
	
	public Boolean requiredBoolean(String key){
		return BooleanTool.isTrue(
				Preconditions.checkNotNull(request.getParameter(key)));
	}
	
	public Boolean optionalBoolean(String key, Boolean defaultValue){
		String value = request.getParameter(key);
		if(value==null){ return defaultValue; }
		return BooleanTool.isTrue(value);
	}
	
	public Long requiredLong(String key){
		return Long.valueOf(
				Preconditions.checkNotNull(request.getParameter(key)));
	}
	
	public Long optionalLong(String key, Long defaultValue){
		String value = request.getParameter(key);
		if(value==null){ return defaultValue; }
		return Long.valueOf(value);
	}
	
	//etc, etc...
	
	/**************************** fancier methods *************************/
	
	public String getContextPath(){
		return request.getContextPath();
	}
	
	
	/***************************** get/set **********************************/

	public HttpServletRequest getRequest(){
		return request;
	}

	public HttpServletResponse getResponse(){
		return response;
	}
	
	
}

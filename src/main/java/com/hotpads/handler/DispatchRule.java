package com.hotpads.handler;

import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

public class DispatchRule{
	private Pattern pattern;
	private Class<? extends BaseHandler> handlerClass;
	private String apiKey;
	
	public DispatchRule(String regex){
		this.pattern = Pattern.compile(regex);
	}
	
	/**** builder pattern methods *******/
	
	public DispatchRule withHandler(Class <? extends BaseHandler> handlerClass){
		this.handlerClass = handlerClass;
		return this;
	}
	
	public void withApiKey(String apiKey){
		this.apiKey = apiKey;
	}
	
	/**** getters *****/

	public Pattern getPattern(){
		return this.pattern;
	}

	public Class<? extends BaseHandler> getHandlerClass(){
		return handlerClass;
	}
	
	public boolean checkApiKey(HttpServletRequest request){
		return apiKey == null || apiKey.equals(request.getParameter("apiKey"));
	}

}

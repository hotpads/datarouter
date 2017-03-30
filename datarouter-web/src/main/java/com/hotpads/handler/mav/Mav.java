package com.hotpads.handler.mav;

import java.util.HashMap;
import java.util.Map;

import com.hotpads.datarouter.util.core.DrStringTool;

public class Mav{

	public static final String REDIRECT = "redirect:";

	/********************** fields *******************************************/

	private boolean redirect = false;
	private String viewName;
	private String context;
	private String contentType = "text/html";
	private Map<String,Object> model = new HashMap<>();
	private String globalRedirectUrl;
	private int statusCode = 200;

	/********************** constructors ****************************************/

	public Mav(){
	}

	public Mav(String viewName){
		this.setViewName(viewName);
	}

	public Mav(String context, String viewName){
		this.context = context;
		this.setViewName(viewName);
	}

	public Mav(String viewName, Map<String,Object> model){
		this.setViewName(viewName);
		this.model = model;
	}

	/********************** methods *******************************************/

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
		}else if(DrStringTool.notEmpty(globalRedirectUrl)){
			return globalRedirectUrl;
		}else{
			StringBuilder sb = new StringBuilder();
			sb.append(viewName);
			int numAppended = 0;
			for(String key : model.keySet()){
				if(numAppended > 0){
					sb.append("&");
				}
				sb.append(model.get(key).toString());
			}
			return sb.toString();
		}
	}

	public Mav setViewName(final String viewName){
		if(DrStringTool.nullSafe(viewName).startsWith(REDIRECT)){
			redirect = true;
			this.viewName = viewName.substring(REDIRECT.length());
		}else{
			if(viewName.contains(".")){
				this.viewName = viewName;
			}else{
				this.viewName = "/WEB-INF/jsp" + viewName + ".jsp";
			}
		}
		return this;
	}

	public Mav setGlobalRedirectUrl(String globalRedirectUrl){
		redirect = true;
		this.globalRedirectUrl = globalRedirectUrl;
		return this;
	}

	/************************** get/set ***********************************/

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

}

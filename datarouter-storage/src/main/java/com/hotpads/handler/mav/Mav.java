package com.hotpads.handler.mav;

import java.util.HashMap;
import java.util.Map;

import com.hotpads.datarouter.util.core.DrBooleanTool;
import com.hotpads.datarouter.util.core.DrStringTool;

public class Mav {
	
	public static final String REDIRECT = "redirect:";

	/********************** fields *******************************************/

	protected Boolean redirect = false;
	protected String viewName = null;
	protected String context;
	protected String contentType = "text/html";
	protected Map<String,Object> model = new HashMap<String,Object>();
	protected String globalRedirectUrl;
	protected int statusCode = 200;
	
	/********************** constructors ****************************************/
	
	public Mav(){
	}
	
	public Mav(String viewName){
		this.setViewName(viewName);
	}
	
	public Mav(String context, String viewName){
		this.setContext(context);
		this.setViewName(viewName);
	}
	
	public Mav(String viewName, Map<String,Object> model) {
		this.setViewName(viewName);
		this.setModel(model);
	}
	
//	public ModelAndView(String viewName, String key, Object value){
//		this.setViewName(viewName);
//		this.addObject(key, value);
//	}
	
//	public ModelAndView(String context, String viewName, String key, Object value){
//		this.setContext(context);
//		this.setViewName(viewName);
//		this.addObject(key, value);
//	}
	
	
	/********************** methods *******************************************/
	
	/**
	 * Backwards compatible method name for Spring framework.
	 */
	@Deprecated
	public Mav addObject(String key, Object value){
		model.put(key, value);
		return this;
	}
	
	/**
	 * This method returns the value you give it to enable things like fetching an object from the database and getting
	 * a reference to it in one line.
	 */
	public <T> T put(String key, T value){
		model.put(key, value);
		return value;
	}
	
	public boolean isRedirect(){
		return redirect;
	}
	
	public String getRedirectUrl(){
		if(DrBooleanTool.isFalse(this.redirect)){ 
			return null; 
		}else if(DrStringTool.notEmpty(this.globalRedirectUrl)){
			return this.globalRedirectUrl;
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
			this.redirect = true;
			this.viewName = viewName.substring(REDIRECT.length());
		}else{
			if(!viewName.contains(".")){
				this.viewName = "/WEB-INF/jsp" + viewName + ".jsp";
			}else{
				this.viewName = viewName;
			}
		}
		return this;
	}
	
	public Mav setViewName(String context, String viewName){
		this.setContext(context);
		return this.setViewName(viewName);
	}

	public void setGlobalRedirectUrl(String globalRedirectUrl) {
		this.redirect = true;
		this.globalRedirectUrl = globalRedirectUrl;
	}

	
	/************************** get/set ***********************************/
	

	public Map<String, Object> getModel() {
		return model;
	}

	public void setModel(Map<String, Object> model) {
		this.model = model;
	}

	public Boolean getRedirect() {
		return redirect;
	}

	public void setRedirect(Boolean redirect) {
		this.redirect = redirect;
	}

	public String getViewName() {
		return viewName;
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public String getGlobalRedirectUrl() {
		return globalRedirectUrl;
	}

	public String getContentType(){
		return contentType;
	}

	public void setContentType(String contentType){
		this.contentType = contentType;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}
	
	
	
}

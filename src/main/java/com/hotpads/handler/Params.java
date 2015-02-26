package com.hotpads.handler;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Preconditions;
import com.hotpads.datarouter.util.core.BooleanTool;
import com.hotpads.datarouter.util.core.ListTool;
import com.hotpads.datarouter.util.core.StringTool;
import com.hotpads.handler.user.session.DatarouterSession;

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
		if(value==null){ 
			return defaultValue; 
		}
		return BooleanTool.isTrue(value);
	}
	
	public Long requiredLong(String key){
		return Long.valueOf(
				Preconditions.checkNotNull(request.getParameter(key)));
	}
	
	public Long optionalLong(String key, Long defaultValue){
		String value = request.getParameter(key);
		if(value==null){ 
			return defaultValue; 
		}
		return Long.valueOf(value);
	}

	public Long optionalLongEmptySafe(String key, Long defaultValue){
		String value = request.getParameter(key);
		if(StringTool.isNullOrEmptyOrWhitespace(value)){ 
			return defaultValue; 
		}
		return Long.valueOf(value);
	}
	
	public Integer requiredInteger(String key){
		return Integer.valueOf(
				Preconditions.checkNotNull(request.getParameter(key)));
	}
	
	public Integer optionalInteger(String key, Integer defaultValue){
		String value = request.getParameter(key);
		if(value==null){ 
			return defaultValue; 
		}
		return Integer.valueOf(value);
	}
	
	public Double optionalDouble(String key, Double defaultValue){
		String value = request.getParameter(key);
		if(value==null){
			return defaultValue; 
		}
		return Double.valueOf(value);
	} 
	
	public Double requiredDouble(String key){
		return Double.valueOf(
				Preconditions.checkNotNull(request.getParameter(key)));
	}
	
	public List<String> optionalCsvList(String key, List<String> defaultValue){
		return optionalList(key, ",", defaultValue);
	}
	
	public List<String> optionalList(String key, String delimiter, List<String> defaultValue){
		String stringVal = request.getParameter(key);
		if(StringTool.isEmpty(stringVal)){
			return defaultValue;
		}
		return ListTool.nullSafeLinkedAddAll(null, stringVal.split(delimiter));
	}
	
	public Integer tryGetInteger(String key, Integer defaultValue) {
		Integer value = defaultValue;
		try {
			value = optionalInteger(key, defaultValue);
		} catch (NumberFormatException e) {
			// no-op
		}
		return value;
	}
	
	public Long tryGetLong(String key, Long defaultValue) {
		Long value = defaultValue;
		try {
			value = optionalLong(key, defaultValue);
		} catch (NumberFormatException e) {
			// no-op
		}
		return value;
	}
	
	//etc, etc...
	
	/**************************** fancier methods *************************/
	
	public String getContextPath(){
		return request.getContextPath();
	}
	
	public DatarouterSession getSession() {
		return (DatarouterSession) request.getAttribute("datarouterSession");
	}
	
	
	/***************************** get/set **********************************/

	public HttpServletRequest getRequest(){
		return request;
	}

	public HttpServletResponse getResponse(){
		return response;
	}
	
	
}

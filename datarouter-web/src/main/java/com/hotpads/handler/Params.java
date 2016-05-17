package com.hotpads.handler;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import com.google.common.base.Preconditions;
import com.hotpads.datarouter.util.core.DrBooleanTool;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.handler.user.session.DatarouterSession;
import com.hotpads.util.http.RequestTool;

public class Params{

	private final HttpServletRequest request;

	public Params(HttpServletRequest request){
		this.request = request;
	}

	public String required(String key){
		return Preconditions.checkNotNull(request.getParameter(key));
	}

	public Optional<String> optional(String key){
		return Optional.ofNullable(request.getParameter(key));
	}

	/**
	 * @deprecated inline it
	 */
	@Deprecated
	public String optional(String key, String defaultValue){
		return optional(key).orElse(defaultValue);
	}

	public Optional<String> optionalNotEmpty(String key){
		return optional(key).map(String::trim).filter(DrStringTool::notEmpty);
	}

	/**
	 * @deprecated inline it
	 */
	@Deprecated
	public String optionalNotEmpty(String key, String defaultValue) {
		return optionalNotEmpty(key).orElse(defaultValue);
	}

	public Boolean requiredBoolean(String key){
		return DrBooleanTool.isTrue(Preconditions.checkNotNull(request.getParameter(key)));
	}

	public Boolean optionalBoolean(String key, Boolean defaultValue){
		String value = request.getParameter(key);
		if(value==null){
			return defaultValue;
		}
		return DrBooleanTool.isTrue(value);
	}

	public Long requiredLong(String key){
		return Long.valueOf(Preconditions.checkNotNull(request.getParameter(key)));
	}

	public Long optionalLong(String key, Long defaultValue){
		String value = request.getParameter(key);
		if(value==null || "".equals(value)){
			return defaultValue;
		}
		return Long.valueOf(value);
	}

	public Long optionalLongEmptySafe(String key, Long defaultValue){
		String value = request.getParameter(key);
		if(DrStringTool.isNullOrEmptyOrWhitespace(value)){
			return defaultValue;
		}
		return Long.valueOf(value);
	}

	public Integer requiredInteger(String key){
		return Integer.valueOf(Preconditions.checkNotNull(request.getParameter(key)));
	}

	public Integer optionalInteger(String key, Integer defaultValue){
		String value = request.getParameter(key);
		if(DrStringTool.isNullOrEmptyOrWhitespace(value)){
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
		return Double.valueOf(Preconditions.checkNotNull(request.getParameter(key)));
	}

	public List<String> optionalCsvList(String key, List<String> defaultValue){
		return optionalList(key, ",", defaultValue);
	}

	public List<String> optionalList(String key, String delimiter, List<String> defaultValue){
		String stringVal = request.getParameter(key);
		if(DrStringTool.isEmpty(stringVal)){
			return defaultValue;
		}
		return Arrays.asList(stringVal.split(delimiter));
	}

	public Integer tryGetInteger(String key, Integer defaultValue) {
		try {
			return optionalInteger(key, defaultValue);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	public Long tryGetLong(String key, Long defaultValue) {
		try {
			return optionalLong(key, defaultValue);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	public Date tryGetLongAsDate(String key, Long defaultValue) {
		Long value = tryGetLong(key, defaultValue);
		return new Date(value);
	}

	public Map<String, String> toMap(){
		return RequestTool.getParamMap(request);
	}

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

}

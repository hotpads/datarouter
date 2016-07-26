package com.hotpads.handler;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;

import com.google.common.base.Preconditions;
import com.hotpads.datarouter.util.core.DrBooleanTool;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.handler.user.session.DatarouterSession;

public class Params{

	private final HttpServletRequest request;
	protected Map<String,String> paramsMap;

	public Params(HttpServletRequest request){
		this.request = request;
		paramsMap = new HashMap<>();
		Collections.list(request.getParameterNames())
				.forEach(key -> paramsMap.put(key, request.getParameter(key)));
	}

	public String required(String key){
		return Preconditions.checkNotNull(paramsMap.get(key));
	}

	public Optional<String> optional(String key){
		return Optional.ofNullable(paramsMap.get(key));
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
		return DrBooleanTool.isTrue(required(key));
	}

	public Boolean optionalBoolean(String key, Boolean defaultValue){
		return optional(key).map(DrBooleanTool::isTrue).orElse(defaultValue);
	}

	public Long requiredLong(String key){
		return Long.valueOf(required(key));
	}

	public Long optionalLong(String key, Long defaultValue){
		return optional(key).map(Long::valueOf).orElse(defaultValue);
	}

	public Long optionalLongEmptySafe(String key, Long defaultValue){
		return optional(key).filter(str -> !DrStringTool.isNullOrEmptyOrWhitespace(str)).map(Long::valueOf)
				.orElse(defaultValue);
	}

	public Integer requiredInteger(String key){
		return Integer.valueOf(required(key));
	}

	public Integer optionalInteger(String key, Integer defaultValue){
		return optional(key).filter(str -> !DrStringTool.isNullOrEmptyOrWhitespace(str)).map(Integer::valueOf)
				.orElse(defaultValue);
	}

	public Double requiredDouble(String key){
		return Double.valueOf(required(key));
	}

	public Double optionalDouble(String key, Double defaultValue){
		return optional(key).map(Double::valueOf).orElse(defaultValue);
	}

	public List<String> optionalCsvList(String key, List<String> defaultValue){
		return optionalList(key, ",", defaultValue);
	}

	public List<String> optionalList(String key, String delimiter, List<String> defaultValue){
		return optional(key).map(str -> str.split(delimiter)).map(Arrays::asList).orElse(defaultValue);
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

	public FileItem requiredFile(String key){
		throw new RuntimeException("not a multipart request");
	}

	public Optional<FileItem> optionalFile(String key){
		return Optional.empty();
	}

	public Map<String,String> toMap(){
		return paramsMap;
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

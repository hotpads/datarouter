package com.hotpads.handler.util;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.base.Joiner;
import com.hotpads.datarouter.util.core.DrBooleanTool;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrIdentityFunctor;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.datarouter.util.core.DrNumberTool;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.exception.analysis.HttpHeaders;
import com.hotpads.handler.Params;
import com.hotpads.handler.mav.Mav;
import com.hotpads.util.core.Functor;
import com.hotpads.util.core.Predicate;
import com.hotpads.util.core.collections.DefaultableMap;
import com.hotpads.util.datastructs.DefaultableHashMap;

public class RequestTool {

	public static final String SUBMIT_ACTION = "submitAction";
	
	public static String getSubmitAction(HttpServletRequest request){
		String submitAction = request.getParameter(SUBMIT_ACTION);
		if(submitAction != null){ 
			return submitAction; 
		}
		throw new NullPointerException("param "+SUBMIT_ACTION+" not found");
	}
	
	public static String getSubmitAction(HttpServletRequest request, String defaultAction){
		String action = get(request, SUBMIT_ACTION, defaultAction);
		return action;
	}

	public static List<String> getSlashedUriParts(HttpServletRequest request){
		return getSlashedUriParts(request.getRequestURI());
	}
	public static List<String> getSlashedUriParts(String uri){
		List<String> uriVars = Arrays.asList(uri.split("/"));
		uriVars = DrCollectionTool.filter( //get rid of blanks
					new Predicate<String>() {
						public boolean check(String t) {
							return t != null && !"".equals(t); 
						} 
					},
					uriVars);
		if(uriVars==null){ 
			uriVars = new LinkedList<String>();
		}
		return uriVars;
	}
	
	public static String getUriFromSlashedParts(HttpServletRequest request, List<String> slashedParts){
		String queryString = request.getQueryString();

		String uri;
		if(slashedParts == null || slashedParts.size()<1){
			uri = "/";
		}else{
			uri = "/"+Joiner.on("/").join(slashedParts);
		}
		
		return uri + (queryString==null?"":"?"+queryString);
	}
	
	public static String getStringParameterCheckOverrideVars(
			HttpServletRequest request, 
			Map<String, String> overrideVars, 
			String paramName, 
			String defaultValue){
		String stringVal = get(request, paramName, null);
		
		//override if there's something in aux
		if(overrideVars != null){
			String override = overrideVars.get(paramName);
			if( ! DrStringTool.isEmpty(override)){
				stringVal = override;
			}
		}
		
		if(DrStringTool.isEmpty(stringVal)){
			return defaultValue;
		}
		return stringVal;
	}
	
	public static String get(HttpServletRequest request, String paramName){
		String stringVal = request.getParameter(paramName);
		if(stringVal==null){
			throw new IllegalArgumentException("expected String:"+paramName+", but was null");
		}
		return stringVal;
	}
	
	public static String get(HttpServletRequest request, String paramName, String defaultValue){
		String stringVal = request.getParameter(paramName);
		return stringVal==null ? defaultValue : stringVal;
	}

	public static String getAndPut(HttpServletRequest request, String paramName, String defaultValue, Mav mav){
		String ret = RequestTool.get(request, paramName, defaultValue);
		if(ret != null){
			mav.put(paramName, ret);
		}
		return ret;
	}
	
	public static boolean exists(HttpServletRequest request, String paramName){
		return DrStringTool.notEmpty(get(request, paramName, null));
	}
	
	public static Boolean getBoolean(HttpServletRequest request, String paramName){
		String stringVal = request.getParameter(paramName);
		//checkboxes don't submit anything if unchecked, so assume null is false
//		if(StringTool.isEmpty(stringVal)){ throw new IllegalArgumentException("missing boolean parameter "+paramName); }
		return DrBooleanTool.isTrue(stringVal);
	}

	public static Boolean getBoolean(HttpServletRequest request, String paramName, Boolean defaultValue){
		String stringVal = get(request, paramName, null);
		if(DrStringTool.isEmpty(stringVal)){
			/* this has to be an explicit if rather than a ternary if to avoid a NPE when defaultValue==null */
			return defaultValue;
		}
		return DrBooleanTool.isTrue(stringVal);
	}
	
	public static Long getLong(HttpServletRequest request, String paramName){
		String stringVal = get(request, paramName, null);
		if(DrStringTool.isEmpty(stringVal)){
			throw new IllegalArgumentException("required Long "+paramName+" not found");
		}
		try{
			return Long.valueOf(stringVal);
		}catch(Exception e){
		}
		throw new IllegalArgumentException("required Long "+paramName+" is invalid");
	}
	
	public static Long getLong(HttpServletRequest request, String paramName, Long defaultValue){
		String stringVal = get(request, paramName, null);
		return DrNumberTool.getLongNullSafe(stringVal,defaultValue);
	}
	
	public static Integer getInteger(HttpServletRequest request, String paramName){
		String stringVal = get(request, paramName, null);
		if(DrStringTool.isEmpty(stringVal)){
			throw new IllegalArgumentException("required Integer "+paramName+" not found");
		}
		try{
			return Integer.valueOf(stringVal);
		}catch(Exception e){
		}
		throw new IllegalArgumentException("required Integer "+paramName+" is invalid");
	}
	
	public static Integer getInteger(HttpServletRequest request, String paramName, Integer defaultValue){
		String stringVal = get(request, paramName, null);
		return DrNumberTool.parseIntegerFromNumberString(stringVal,defaultValue);
	}
	
	/****** getters for Params   ******/
	public static Integer tryGetIntegerParam(Params params, String fieldName){	
		Integer value = null;
		try{
			value = params.optionalInteger(fieldName, null);			
		}catch(NumberFormatException e){
			return value;
		}
		return value;
	}	
	
	public static Long tryGetLongParam(Params params, String fieldName){
		Long value = null;
		try{
			value = params.optionalLong(fieldName, null);			
		}catch(NumberFormatException e){
			return value;
		}
		return value;
	}
	
	public static Integer getIntegerAndPut(HttpServletRequest request, String paramName, Integer defaultValue,
			Mav mav){
		String stringVal = RequestTool.get(request, paramName, null);
		Integer ret = DrNumberTool.parseIntegerFromNumberString(stringVal, defaultValue);
		mav.put(paramName, ret);
		return ret;
	}

	public static Double getDouble(HttpServletRequest request, String paramName, Double defaultValue){
		String stringVal = get(request, paramName, null);
		return DrNumberTool.getDoubleNullSafe(stringVal,defaultValue);
	}

	public static List<String> getCsvList(HttpServletRequest request, String paramName, List<String> defaultValue){
		return getList(request, paramName, ",", defaultValue);
	}
	
	public static List<String> getList(HttpServletRequest request, String paramName, String delimiter, 
			List<String> defaultValue){
		String stringVal = request.getParameter(paramName);
		if(DrStringTool.isEmpty(stringVal)){
			return defaultValue;
		}
		return DrListTool.nullSafeLinkedAddAll(null, stringVal.split(delimiter));
	}


	public static Double getDoubleParameterNullSafeCheckOverrideVars(
			HttpServletRequest request, 
			Map<String, String> overrideVars, 
			String paramName, 
			Double defaultValue){
		String stringVal = get(request, paramName, null);
//			ServletRequestUtils.getStringParameter(request, paramName, null);

		if(overrideVars != null){
			//override if there's something in aux
			String override = overrideVars.get(paramName);
			if( ! DrStringTool.isEmpty(override)){
				stringVal = override;
			}
		}
		
		try{
			return Double.valueOf(stringVal);
		}catch(Exception e){
			return defaultValue;
		}
	}
	
	public static Integer getIntegerParameterNullSafe(HttpServletRequest request, String paramName, 
			Integer defaultValue){
		String stringVal = get(request, paramName, null);
		return DrNumberTool.parseIntegerFromNumberString(stringVal,defaultValue);
	}
	
	public static Integer getIntegerParameterNullSafeCheckOverrideVars(
			HttpServletRequest request, 
			Map<String, String> overrideVars, 
			String paramName, 
			Integer defaultValue){
		String stringVal = get(request, paramName, null);

		if(overrideVars != null){
			//override if there's something in aux
			String override = overrideVars.get(paramName);
			if( ! DrStringTool.isEmpty(override)){
				stringVal = override;
			}
		}

		try{
			return Integer.valueOf(stringVal);
		}catch(Exception e){
			return defaultValue;
		}
	}
	
	public static Boolean getBooleanParameterNullSafeStandardJava(HttpServletRequest request, String paramName, 
			Boolean defaultValue){
		String stringVal = get(request, paramName, null);
		try{
			return Boolean.valueOf(stringVal);
		}catch(Exception e){
			return defaultValue;
		}
	}
	
	public static Boolean getBooleanParameterNullSafeStandardJavaCheckOverrideVars(
			HttpServletRequest request, 
			Map<String, String> overrideVars, 
			String paramName, 
			Boolean defaultValue){
		String stringVal = get(request, paramName, null);
		
		if(overrideVars != null){
			//override if there's something in aux
			String override = overrideVars.get(paramName);
			if( ! DrStringTool.isEmpty(override)){
				stringVal = override;
			}
		}

		try{
			return Boolean.valueOf(stringVal);
		}catch(Exception e){
			return defaultValue;
		}
	}
	
	public static Boolean getBooleanParameterNullSafeCustomValues(HttpServletRequest request, String paramName, 
			Boolean defaultValue){
		String stringVal = get(request, paramName, null);

		if(DrStringTool.isEmpty(stringVal)){
			return defaultValue;
		}else if(DrBooleanTool.isFalse(stringVal)){
			return false;
		}else if(DrBooleanTool.isTrue(stringVal)){
			return true;
		}else{
			return defaultValue;
		}
	}
	
	public static Boolean getBooleanParameterNullSafeCustomValuesCheckOverrideVars(
			HttpServletRequest request, 
			Map<String, String> overrideVars, 
			String paramName, 
			Boolean defaultValue){
		String stringVal = get(request, paramName, null);

		if(overrideVars != null){
			//override if there's something in aux
			String override = overrideVars.get(paramName);
			if( ! DrStringTool.isEmpty(override)){
				stringVal = override;
			}
		}
		
		if(DrStringTool.isEmpty(stringVal)){
			return defaultValue;
		}else if(DrBooleanTool.isFalse(stringVal)){
			return false;
		}else if(DrBooleanTool.isTrue(stringVal)){
			return true;
		}else{
			return defaultValue;
		}
	}
	
	public static void setAttributes(HttpServletRequest request, Map<String,Object> attributeMap){
		for(String key : DrCollectionTool.nullSafe(attributeMap.keySet())){
			request.setAttribute(key, attributeMap.get(key));
		}
	}

	/**
	 * get the parameters of a request in a DefaultableHashMap
	 * @param request
	 * @return
	 */
	public static DefaultableMap<String, String> getParamMap(HttpServletRequest request) {
		DefaultableHashMap<String, String> map = new DefaultableHashMap<String,String>();
		return getParameterMap(map,request);
	}
	public static NavigableMap<String, String> getMapOfParameters(HttpServletRequest request){
		NavigableMap<String, String> map = new TreeMap<String, String>();
		return getParameterMap(map,request);
	}
	
	public static <M extends Map<String,String>> M getParameterMap(M map, HttpServletRequest request){
		for(Object paramName : request.getParameterMap().keySet()){
			map.put((String)paramName, request.getParameter((String)paramName));
		}
		return map;
	}
	
	public static NavigableMap<String, String> getMapOfParametersMatchingPrefix(HttpServletRequest request, 
			String prefix){
		NavigableMap<String, String> allVars = getMapOfParameters(request);
		NavigableMap<String, String> matches = new TreeMap<String,String>();
		for(String key : allVars.keySet()){
			if(key.startsWith(prefix)){
				matches.put(key, allVars.get(key));
			}
		}
		return matches;
	}
	
	public static Map<String, String> getMapOfUrlStyleVars(String allVars){
		Map<String, String> map = new HashMap<String, String>();
		for (String pair : allVars.split("&")){
			if (pair.length() != 0) {
				int j = pair.indexOf('=');
				String key = (j != -1) ? pair.substring(0,j) : pair; 
				String val = (j != -1) ? pair.substring(j+1) : Boolean.TRUE.toString();
				try {
					key = URLDecoder.decode(key, "UTF-8");
					val = URLDecoder.decode(val, "UTF-8");
				} catch (UnsupportedEncodingException e) {}
				map.put(key, val);					
			}
		}
		return map;
	}
	
	public static boolean checkDouble(Double value, boolean allowNull,  boolean allowNegative){
		return checkDouble(value,allowNull,allowNegative,false,false);
	}
	
	public static boolean checkDouble(Double value, boolean allowNull, boolean allowNegative, boolean allowInfinity, 
			boolean allowNaN){
		if(value==null){
			return allowNull;
		}
		if(Double.isNaN(value)){
			return allowNaN;
		}
		return (allowInfinity || !Double.isInfinite(value))	&& (allowNegative || value>=0);
	}
	

	public static Map<String,String> getHeader(HttpServletRequest request){
		Map<String,String> headers = new HashMap<>();
		Enumeration<?> headersEnum = request.getHeaderNames();
		while(headersEnum!=null && headersEnum.hasMoreElements()){
			String h = (String)headersEnum.nextElement();
			if(h==null) {
				continue;
			}
			headers.put(h,request.getHeader(h));
		}
		return headers;
	}
	
	public static List<String> getCheckedBoxes(HttpServletRequest request, String prefix){
		return getCheckedBoxes(request,prefix,new DrIdentityFunctor<String>());
	}
	
	public static <T> List<T> getCheckedBoxes(HttpServletRequest request, String prefix, Functor<T,String> converter){
		Enumeration<String> paramNames = request.getParameterNames();
		List<T> selecteds = DrListTool.createArrayList();
		while(paramNames.hasMoreElements()){
			String name = paramNames.nextElement();
			if( ! name.startsWith(prefix)) {
				continue;
			}
			selecteds.add(converter.invoke(
							name.substring(
									prefix.length())));
		}
		return selecteds;
	}
	
	/**
	 * if uri ends with a /, strip that slash, otherwise return null
	 * includes query string in redirect uri
	 * @param request
	 * @return redirect uri
	 */
	public static String makeRedirectUriIfTrailingSlash(HttpServletRequest request){
		return makeRedirectUriIfTrailingSlash(request.getRequestURI(), request.getQueryString());
	}
	private static String makeRedirectUriIfTrailingSlash(String uri, String queryString){
		if( ! uri.endsWith("/")) {
			return null;
		}
		uri = uri.substring(0, uri.length()-1);
		return getRequestUriWithQueryString(uri, queryString);
	}
	
	/**
	 * recombine uri and query string
	 * @param request
	 * @return
	 */
	public static String getRequestURIWithQueryString(HttpServletRequest request){
		return getRequestUriWithQueryString(request.getRequestURI(),request.getQueryString());
	}
	private static String getRequestUriWithQueryString(String uri, String queryString){
		return uri + (queryString==null?"":"?"+queryString);
	}
	
	public static String getFileExtension(HttpServletRequest request){
		String uri = request.getRequestURI();
		Integer i = uri.lastIndexOf('.');
		if(i==-1){ 
			return null;
		}
		return uri.substring(i+1, uri.length());
	}
	

	public static URL getFullyQualifiedUrl(String path, HttpServletRequest similarRequest){
		if(!path.startsWith("/")){
			path = "/"+path;
		}
		String requestUrl = similarRequest.getRequestURL().toString();
		return getFullyQualifiedUrl(path, requestUrl, similarRequest.getServerName(), similarRequest.getServerPort());
	}
	
	public static URL getFullyQualifiedUrl(String path, String similarUrl, String serverName, int port){
		if(!path.startsWith("/")){
			path = "/"+path;
		}
		String protocol = similarUrl.split(":",2)[0];
		try{
			if((port==80 && "http".equals(protocol)) || (port==443 && "https".equals(protocol))){
				return new URL(protocol,serverName,path);
			}
			return new URL(protocol,serverName,port,path);
		}catch(MalformedURLException e){
			throw new IllegalArgumentException(e);
		}
	}
	
	public static String getReferer(HttpServletRequest request){
		return request.getHeader("referer");
	}
	
	public static String getUserAgent(HttpServletRequest request){
		return request.getHeader("user-agent");
	}
	
	public static String getIpAddress(HttpServletRequest request){
		if(request == null){
			return null;
		}
		String xForwardedFor = null;
		
		Enumeration<String> xForwards = request.getHeaders(HttpHeaders.X_FORWARDED_FOR);

		//haproxy adds x-forwarded-for headers to the http request for the originating ip.
		//if the client already had a x-forwarded-for header, 
		// java's request.getHeader() chooses the wrong header (the first one, which is the client's),
		// which can contain useless ips (like 127.0.0.1).  so instead use the last one that haproxy put on

		while (xForwards.hasMoreElements()) {
			xForwardedFor = xForwards.nextElement();
		}

		if (!DrStringTool.isNullOrEmptyOrWhitespace(xForwardedFor)){
			String[] proxyChain = xForwardedFor.split(", ");
			String clientIp = proxyChain[proxyChain.length - 1];
			if (isAValidIpV4(clientIp)) {
				return clientIp;
			}
			return request.getRemoteAddr();
		}
		String remoteAddr = request.getRemoteAddr();
		if ("127.0.0.1".equals(remoteAddr)) {//dev server
			remoteAddr = "98.204.67.1"; //FIXME why this adresse ?
		}
		return remoteAddr;
	}

	public static boolean isAValidIpV4(String dottedDecimal){
		String ipv4Pattern = "\\A(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}\\z";
		return dottedDecimal != null && dottedDecimal.matches(ipv4Pattern);
	}

	/** tests *****************************************************************/
	public static class Tests {
		@Test public void testCheckDouble(){
			Assert.assertFalse(checkDouble(-0.01,false,false,false,false));
			Assert.assertTrue(checkDouble(-0.01,false,true,false,false));
			Assert.assertFalse(checkDouble(null,false,false,false,false));
			Assert.assertTrue(checkDouble(null,true,false,false,false));
			Assert.assertFalse(checkDouble(Double.NEGATIVE_INFINITY, false,false,false,false));
			Assert.assertFalse(checkDouble(Double.NEGATIVE_INFINITY, false,true,false,false));
			Assert.assertFalse(checkDouble(Double.NEGATIVE_INFINITY, false,false,true,false));
			Assert.assertTrue(checkDouble(Double.NEGATIVE_INFINITY, false,true,true,false));
			Assert.assertFalse(checkDouble(Double.POSITIVE_INFINITY, false,false,false,false));
			Assert.assertTrue(checkDouble(Double.POSITIVE_INFINITY, false,false,true,false));
			Assert.assertFalse(checkDouble(Double.NaN,false,false,false,false));
			Assert.assertTrue(checkDouble(Double.NaN,false,false,false,true));
		}
		
		@Test public void testGetRequestUriWithQueryString(){
			Assert.assertEquals("example.com",
					getRequestUriWithQueryString("example.com",null));
			Assert.assertEquals("example.com?stuff=things",
					getRequestUriWithQueryString("example.com","stuff=things"));			
		}
		
		@Test public void testMakeRedirectUriIfTrailingSlash(){ 
			Assert.assertEquals(null, makeRedirectUriIfTrailingSlash("example.com",null));
			Assert.assertEquals("example.com", makeRedirectUriIfTrailingSlash("example.com/",null));
			Assert.assertEquals("example.com?stuff=things",
					makeRedirectUriIfTrailingSlash("example.com/","stuff=things"));
			Assert.assertEquals(null,
					makeRedirectUriIfTrailingSlash("example.com","stuff=things"));
		}
		
		@Test public void testGetFullyQualifiedUrl(){
			Assert.assertEquals("http://x.com/aurl",
					getFullyQualifiedUrl("aurl","http://x.com","x.com",80).toString());
			Assert.assertEquals("http://x.com/aurl",
					getFullyQualifiedUrl("/aurl","http://x.com","x.com",80).toString());
			Assert.assertEquals("https://x.com:80/aurl",
					getFullyQualifiedUrl("/aurl","https://x.com","x.com",80).toString());
			Assert.assertEquals("https://x.com/aurl",
					getFullyQualifiedUrl("/aurl","https://x.com","x.com",443).toString());
			Assert.assertEquals("https://x.com/", 
					getFullyQualifiedUrl("/","https://x.com","x.com",443).toString());
			Assert.assertEquals("https://x.com/",
					getFullyQualifiedUrl("","https://x.com","x.com",443).toString());
			Assert.assertEquals("http://x.com:8080/",
					getFullyQualifiedUrl("","http://x.com:8080","x.com",8080).toString());
			Assert.assertEquals("https://x.com:8443/snack",
					getFullyQualifiedUrl("snack","https://x.com","x.com",8443).toString());
		}

		@Test public void testIsAVildIpV4() {
			Assert.assertTrue(isAValidIpV4("1.0.1.1"));
			Assert.assertTrue(isAValidIpV4("0.0.0.0"));
			Assert.assertTrue(isAValidIpV4("124.159.0.18"));
			Assert.assertFalse(isAValidIpV4("256.159.0.18"));
			Assert.assertFalse(isAValidIpV4("blabla"));
			Assert.assertFalse(isAValidIpV4(""));
			Assert.assertFalse(isAValidIpV4(null));
		}
	}

	public static boolean isAjax(HttpServletRequest request){
		String xRequestedWith = request.getHeader(HttpHeaders.X_REQUESTED_WITH);
		return "XMLHttpRequest".equalsIgnoreCase(xRequestedWith);
	}

}
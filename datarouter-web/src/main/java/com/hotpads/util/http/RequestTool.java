package com.hotpads.util.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.junit.Assert;
import org.junit.Test;

import com.hotpads.datarouter.util.core.DrBooleanTool;
import com.hotpads.datarouter.util.core.DrCollectionTool;
import com.hotpads.datarouter.util.core.DrIdentityFunctor;
import com.hotpads.datarouter.util.core.DrListTool;
import com.hotpads.datarouter.util.core.DrNumberTool;
import com.hotpads.datarouter.util.core.DrStringTool;
import com.hotpads.handler.mav.Mav;
import com.hotpads.util.core.Functor;
import com.hotpads.util.core.collections.DefaultableMap;
import com.hotpads.util.datastructs.DefaultableHashMap;
import com.hotpads.util.http.security.UrlScheme;

public class RequestTool {
	private static final String INACCESSIBLE_BODY = "INACCESSIBLE BODY: ";
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
		//get rid of blanks
		return uriVars.stream().filter(DrStringTool::notEmpty).collect(Collectors.toList());
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

	public static Long getLongParameterNullSafe(HttpServletRequest request, String paramName,
			Long defaultValue){
		String stringVal = get(request, paramName, null);
		if(DrStringTool.isEmpty(stringVal)){
			return defaultValue;
		}
		return DrNumberTool.getLongNullSafe(stringVal,defaultValue);
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
	 */
	public static DefaultableMap<String, String> getParamMap(HttpServletRequest request) {
		DefaultableHashMap<String, String> map = new DefaultableHashMap<>();
		return getParameterMap(map,request);
	}
	public static NavigableMap<String, String> getMapOfParameters(HttpServletRequest request){
		NavigableMap<String, String> map = new TreeMap<>();
		return getParameterMap(map,request);
	}

	public static <M extends Map<String,String>> M getParameterMap(M map, HttpServletRequest request){
		for(Object paramName : request.getParameterMap().keySet()){
			map.put((String)paramName, request.getParameter((String)paramName));
		}
		return map;
	}

	public static Map<String, String> getMapOfUrlStyleVars(String allVars){
		Map<String, String> map = new HashMap<>();
		for (String pair : allVars.split("&")){
			if (pair.length() != 0) {
				int j = pair.indexOf('=');
				String key = j != -1 ? pair.substring(0,j) : pair;
				String val = j != -1 ? pair.substring(j+1) : Boolean.TRUE.toString();
				try {
					key = URLDecoder.decode(key, "UTF-8");
					val = URLDecoder.decode(val, "UTF-8");
				}catch (UnsupportedEncodingException e){
				}
				map.put(key, val);
			}
		}
		return map;
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
			String header = (String)headersEnum.nextElement();
			if(header==null) {
				continue;
			}
			headers.put(header,request.getHeader(header));
		}
		return headers;
	}

	public static List<String> getCheckedBoxes(HttpServletRequest request, String prefix){
		return getCheckedBoxes(request,prefix,new DrIdentityFunctor<String>());
	}

	public static <T> List<T> getCheckedBoxes(HttpServletRequest request, String prefix, Functor<T,String> converter){
		Enumeration<String> paramNames = request.getParameterNames();
		List<T> selecteds = new ArrayList<>();
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
	 */
	public static String getRequestURIWithQueryString(HttpServletRequest request){
		return getRequestUriWithQueryString(request.getRequestURI(),request.getQueryString());
	}
	private static String getRequestUriWithQueryString(String uri, String queryString){
		return uri + (queryString==null?"":"?"+queryString);
	}

	public static String getFileExtension(HttpServletRequest request){
		String uri = request.getRequestURI();
		Integer idx = uri.lastIndexOf('.');
		if(idx==-1){
			return null;
		}
		return uri.substring(idx+1, uri.length());
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
			if(port == UrlScheme.PORT_HTTP_STANDARD && "http".equals(protocol)
					|| port == UrlScheme.PORT_HTTPS_STANDARD && "https".equals(protocol)){
				return new URL(protocol, serverName, path);
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

	private static String getLastHeader(HttpServletRequest request, String headerName) {
		String header = null;

		Enumeration<String> headers = request.getHeaders(headerName);
		ArrayList<String> xs = new ArrayList<>();
		while (headers.hasMoreElements()) {
			xs.add(headers.nextElement());
		}
		if (xs.size() > 0) {
			//haproxy adds x-forwarded-for headers to the http request for the originating ip.
			//if the client already had a x-forwarded-for header,
			// java's request.getHeader() chooses the wrong header (the first one, which is the client's),
			// which can contain useless ips (like 127.0.0.1).  so instead use the last one that haproxy put on
			Iterator<String> it = xs.iterator();
			while(it.hasNext()) {
				header = it.next();
			}
		}

		return header;
	}

	public static String getIpAddress(HttpServletRequest request){
		if(request == null){
			return null;
		}

		String clientIp = getLastHeader(request, HttpHeaders.X_CLIENT_IP);

		//Node servers send in the original X-Forwarded-For as X-Client-IP
		if (!DrStringTool.isNullOrEmptyOrWhitespace(clientIp)) {
			String[] proxyChain = clientIp.split(", ");
			String ip = proxyChain[proxyChain.length - 1];
			if (isAValidIpV4(ip)) {
				return ip;
			}
		}


		String forwardedFor = getLastHeader(request, HttpHeaders.X_FORWARDED_FOR);
		//no x-client-ip present, check x-forwarded-for
		if (!DrStringTool.isNullOrEmptyOrWhitespace(forwardedFor)){
			String[] proxyChain = forwardedFor.split(", ");
			String ip = proxyChain[proxyChain.length - 1];
			if (isAValidIpV4(ip)) {
				return ip;
			}
		}

		//no x-forwarded-for, use ip straight from http request
		String remoteAddr = request.getRemoteAddr();
		if ("127.0.0.1".equals(remoteAddr)) {//dev server
			remoteAddr = "209.63.146.244"; //535 Mission st office's ip
		}
		return remoteAddr;
	}

	public static boolean isAValidIpV4(String dottedDecimal){
		String ipv4Pattern = "\\A(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}\\z";
		return dottedDecimal != null && dottedDecimal.matches(ipv4Pattern);
	}

	public static String getBodyAsString(ServletRequest request){
		StringBuilder builder = new StringBuilder();
		try(BufferedReader reader = request.getReader()){
			String line;
			while((line = reader.readLine()) != null){
				builder.append(line);
			}
		}catch(IOException e){
			throw new RuntimeException(e);
		}
		return builder.toString();
	}

	public static String partiallyTryGetBodyAsString(ServletRequest request){
		try{
			return getBodyAsString(request);
		}catch(RuntimeException e){
			Throwable cause = e.getCause();
			if(cause instanceof IllegalStateException){
				return INACCESSIBLE_BODY + cause.getMessage();
			}
			throw e;
		}
	}

	/** tests *****************************************************************/
	public static class Tests {
		@Test
		public void testCheckDouble(){
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

		@Test
		public void testGetRequestUriWithQueryString(){
			Assert.assertEquals("example.com", getRequestUriWithQueryString("example.com", null));
			Assert.assertEquals("example.com?stuff=things", getRequestUriWithQueryString("example.com",
					"stuff=things"));
		}

		@Test
		public void testMakeRedirectUriIfTrailingSlash(){
			Assert.assertEquals(null, makeRedirectUriIfTrailingSlash("example.com", null));
			Assert.assertEquals("example.com", makeRedirectUriIfTrailingSlash("example.com/", null));
			Assert.assertEquals("example.com?stuff=things", makeRedirectUriIfTrailingSlash("example.com/",
					"stuff=things"));
			Assert.assertEquals(null, makeRedirectUriIfTrailingSlash("example.com", "stuff=things"));
		}

		@Test
		public void testGetFullyQualifiedUrl(){
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

		@Test
		public void testIsAValidIpV4() {
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
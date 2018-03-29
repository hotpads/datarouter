/**
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
package io.datarouter.web.util.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.httpclient.security.UrlConstants;
import io.datarouter.util.BooleanTool;
import io.datarouter.util.collection.CollectionTool;
import io.datarouter.util.collection.ListTool;
import io.datarouter.util.net.IpTool;
import io.datarouter.util.number.NumberTool;
import io.datarouter.util.string.StringTool;
import io.datarouter.util.timer.ThreadSafePhaseTimer;
import io.datarouter.util.tuple.DefaultableMap;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.types.HttpRequestBuilder;

public class RequestTool{
	private static final Logger logger = LoggerFactory.getLogger(RequestTool.class);

	private static final String INACCESSIBLE_BODY = "INACCESSIBLE BODY: ";
	private static final String HEADER_VALUE_DELIMITER = ", ";
	private static final String[] PRIVATE_NETS = {"10.0.0.0/8", "172.16.0.0/12"};

	public static final String SUBMIT_ACTION = "submitAction";
	public static final String REQUEST_PHASE_TIMER = "requestPhaseTimer";

	public static String getPath(HttpServletRequest request){
		return request.getServletPath() + StringTool.nullSafe(request.getPathInfo());
	}

	public static String getSubmitAction(HttpServletRequest request){
		String submitAction = request.getParameter(SUBMIT_ACTION);
		if(submitAction != null){
			return submitAction;
		}
		throw new NullPointerException("param " + SUBMIT_ACTION + " not found");
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
		return uriVars.stream().filter(StringTool::notEmpty).collect(Collectors.toList());
	}

	public static String getStringParameterCheckOverrideVars(
			HttpServletRequest request,
			Map<String,String> overrideVars,
			String paramName,
			String defaultValue){
		String stringVal = get(request, paramName, null);

		//override if there's something in aux
		if(overrideVars != null){
			String override = overrideVars.get(paramName);
			if(!StringTool.isEmpty(override)){
				stringVal = override;
			}
		}

		if(StringTool.isEmpty(stringVal)){
			return defaultValue;
		}
		return stringVal;
	}

	public static String get(HttpServletRequest request, String paramName){
		String stringVal = request.getParameter(paramName);
		if(stringVal == null){
			throw new IllegalArgumentException("expected String:" + paramName + ", but was null");
		}
		return stringVal;
	}

	public static String get(HttpServletRequest request, String paramName, String defaultValue){
		String stringVal = request.getParameter(paramName);
		return stringVal == null ? defaultValue : stringVal;
	}

	public static boolean exists(HttpServletRequest request, String paramName){
		return StringTool.notEmpty(get(request, paramName, null));
	}

	public static Boolean getBoolean(HttpServletRequest request, String paramName){
		String stringVal = request.getParameter(paramName);
		//checkboxes don't submit anything if unchecked, so assume null is false
//		if(StringTool.isEmpty(stringVal)){ throw new IllegalArgumentException("missing boolean parameter "+paramName); }
		return BooleanTool.isTrue(stringVal);
	}

	public static Boolean getBoolean(HttpServletRequest request, String paramName, Boolean defaultValue){
		String stringVal = get(request, paramName, null);
		if(StringTool.isEmpty(stringVal)){
			/* this has to be an explicit if rather than a ternary if to avoid a NPE when defaultValue==null */
			return defaultValue;
		}
		return BooleanTool.isTrue(stringVal);
	}

	public static Long getLong(HttpServletRequest request, String paramName){
		String stringVal = get(request, paramName, null);
		if(StringTool.isEmpty(stringVal)){
			throw new IllegalArgumentException("required Long " + paramName + " not found");
		}
		try{
			return Long.valueOf(stringVal);
		}catch(Exception e){
			//swallow
		}
		throw new IllegalArgumentException("required Long " + paramName + " is invalid");
	}

	public static Long getLong(HttpServletRequest request, String paramName, Long defaultValue){
		String stringVal = get(request, paramName, null);
		return NumberTool.getLongNullSafe(stringVal, defaultValue);
	}

	public static Integer getInteger(HttpServletRequest request, String paramName){
		String stringVal = get(request, paramName, null);
		if(StringTool.isEmpty(stringVal)){
			throw new IllegalArgumentException("required Integer " + paramName + " not found");
		}
		try{
			return Integer.valueOf(stringVal);
		}catch(Exception e){
			//swallow
		}
		throw new IllegalArgumentException("required Integer " + paramName + " is invalid");
	}

	public static Integer getInteger(HttpServletRequest request, String paramName, Integer defaultValue){
		String stringVal = get(request, paramName, null);
		return NumberTool.parseIntegerFromNumberString(stringVal, defaultValue);
	}

	public static Integer getIntegerAndPut(HttpServletRequest request, String paramName, Integer defaultValue,
			Mav mav){
		String stringVal = RequestTool.get(request, paramName, null);
		Integer ret = NumberTool.parseIntegerFromNumberString(stringVal, defaultValue);
		mav.put(paramName, ret);
		return ret;
	}

	public static Double getDouble(HttpServletRequest request, String paramName, Double defaultValue){
		String stringVal = get(request, paramName, null);
		return NumberTool.getDoubleNullSafe(stringVal, defaultValue);
	}

	public static List<String> getCsvList(HttpServletRequest request, String paramName, List<String> defaultValue){
		return getList(request, paramName, ",", defaultValue);
	}

	public static List<String> getList(HttpServletRequest request, String paramName, String delimiter,
			List<String> defaultValue){
		String stringVal = request.getParameter(paramName);
		if(StringTool.isEmpty(stringVal)){
			return defaultValue;
		}
		return ListTool.nullSafeLinkedAddAll(null, stringVal.split(delimiter));
	}


	public static Double getDoubleParameterNullSafeCheckOverrideVars(
			HttpServletRequest request,
			Map<String,String> overrideVars,
			String paramName,
			Double defaultValue){
		String stringVal = get(request, paramName, null);
//			ServletRequestUtils.getStringParameter(request, paramName, null);

		if(overrideVars != null){
			//override if there's something in aux
			String override = overrideVars.get(paramName);
			if(!StringTool.isEmpty(override)){
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
		return NumberTool.parseIntegerFromNumberString(stringVal, defaultValue);
	}

	public static Integer getIntegerParameterNullSafeCheckOverrideVars(
			HttpServletRequest request,
			Map<String,String> overrideVars,
			String paramName,
			Integer defaultValue){
		String stringVal = get(request, paramName, null);

		if(overrideVars != null){
			//override if there's something in aux
			String override = overrideVars.get(paramName);
			if(!StringTool.isEmpty(override)){
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
		if(StringTool.isEmpty(stringVal)){
			return defaultValue;
		}
		return NumberTool.getLongNullSafe(stringVal, defaultValue);
	}

	public static Boolean getBooleanParameterNullSafeCustomValuesCheckOverrideVars(
			HttpServletRequest request,
			Map<String,String> overrideVars,
			String paramName,
			Boolean defaultValue){
		String stringVal = get(request, paramName, null);

		if(overrideVars != null){
			//override if there's something in aux
			String override = overrideVars.get(paramName);
			if(!StringTool.isEmpty(override)){
				stringVal = override;
			}
		}

		if(StringTool.isEmpty(stringVal)){
			return defaultValue;
		}else if(BooleanTool.isFalse(stringVal)){
			return false;
		}else if(BooleanTool.isTrue(stringVal)){
			return true;
		}else{
			return defaultValue;
		}
	}

	public static void setAttributes(HttpServletRequest request, Map<String,Object> attributeMap){
		for(String key : CollectionTool.nullSafe(attributeMap.keySet())){
			request.setAttribute(key, attributeMap.get(key));
		}
	}

	/**
	 * get the parameters of a request in a DefaultableHashMap
	 */
	public static DefaultableMap<String,String> getParamMap(HttpServletRequest request){
		DefaultableMap<String,String> map = new DefaultableMap<>(new LinkedHashMap<>());
		return getParameterMap(map, request);
	}

	public static NavigableMap<String,String> getMapOfParameters(HttpServletRequest request){
		NavigableMap<String,String> map = new TreeMap<>();
		return getParameterMap(map, request);
	}

	public static <M extends Map<String,String>> M getParameterMap(M map, HttpServletRequest request){
		for(Object paramName : request.getParameterMap().keySet()){
			map.put((String)paramName, request.getParameter((String)paramName));
		}
		return map;
	}

	public static String getParameterOrHeader(HttpServletRequest request, String key){
		String value = request.getParameter(key);
		return value != null ? value : request.getHeader(key);
	}

	public static Map<String,String> getMapOfUrlStyleVars(String allVars){
		Map<String,String> map = new HashMap<>();
		for(String pair : allVars.split("&")){
			if(pair.length() != 0){
				int equalsIndex = pair.indexOf('=');
				String key = equalsIndex != -1 ? pair.substring(0, equalsIndex) : pair;
				String val = equalsIndex != -1 ? pair.substring(equalsIndex + 1) : Boolean.TRUE.toString();
				try{
					key = URLDecoder.decode(key, "UTF-8");
					val = URLDecoder.decode(val, "UTF-8");
				}catch(UnsupportedEncodingException e){
					//swallow
				}
				map.put(key, val);
			}
		}
		return map;
	}

	public static boolean checkDouble(Double value, boolean allowNull, boolean allowNegative, boolean allowInfinity,
			boolean allowNaN){
		if(value == null){
			return allowNull;
		}
		if(Double.isNaN(value)){
			return allowNaN;
		}
		return (allowInfinity || !Double.isInfinite(value)) && (allowNegative || value >= 0);
	}


	public static Map<String,String> getHeader(HttpServletRequest request){
		Map<String,String> headers = new HashMap<>();
		Enumeration<?> headersEnum = request.getHeaderNames();
		while(headersEnum != null && headersEnum.hasMoreElements()){
			String header = (String)headersEnum.nextElement();
			if(header == null){
				continue;
			}
			headers.put(header, request.getHeader(header));
		}
		return headers;
	}

	public static String getHeaderOrParameter(HttpServletRequest request, String key){
		String value = request.getHeader(key);
		return value != null ? value : request.getParameter(key);
	}

	public static List<String> getCheckedBoxes(HttpServletRequest request, String prefix){
		return getCheckedBoxes(request, prefix, Function.identity());
	}

	public static <T> List<T> getCheckedBoxes(HttpServletRequest request, String prefix, Function<String,T> converter){
		Enumeration<String> paramNames = request.getParameterNames();
		List<T> selecteds = new ArrayList<>();
		while(paramNames.hasMoreElements()){
			String name = paramNames.nextElement();
			if(!name.startsWith(prefix)){
				continue;
			}
			selecteds.add(converter.apply(name.substring(prefix.length())));
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
		if(!uri.endsWith("/")){
			return null;
		}
		uri = uri.substring(0, uri.length() - 1);
		return getRequestUriWithQueryString(uri, queryString);
	}

	/**
	 * recombine uri and query string
	 */
	public static String getRequestUriWithQueryString(HttpServletRequest request){
		return getRequestUriWithQueryString(request.getRequestURI(), request.getQueryString());
	}

	private static String getRequestUriWithQueryString(String uri, String queryString){
		return uri + (queryString == null ? "" : "?" + queryString);
	}

	public static String getFileExtension(HttpServletRequest request){
		String uri = request.getRequestURI();
		Integer idx = uri.lastIndexOf('.');
		if(idx == -1){
			return null;
		}
		return uri.substring(idx + 1, uri.length());
	}


	public static URL getFullyQualifiedUrl(String path, HttpServletRequest similarRequest){
		if(!path.startsWith("/")){
			path = "/" + path;
		}
		String requestUrl = similarRequest.getRequestURL().toString();
		return getFullyQualifiedUrl(path, requestUrl, similarRequest.getServerName(), similarRequest.getServerPort());
	}

	public static URL getFullyQualifiedUrl(String path, String similarUrl, String serverName, int port){
		if(!path.startsWith("/")){
			path = "/" + path;
		}
		String protocol = similarUrl.split(":", 2)[0];
		try{
			if(port == UrlConstants.PORT_HTTP_STANDARD && "http".equals(protocol)
					|| port == UrlConstants.PORT_HTTPS_STANDARD && "https".equals(protocol)){
				return new URL(protocol, serverName, path);
			}
			return new URL(protocol, serverName, port, path);
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

	private static List<String> getAllHeaderValuesOrdered(HttpServletRequest request, String headerName){
		return Collections.list(request.getHeaders(headerName)).stream()
				.map(str -> str.split(HEADER_VALUE_DELIMITER))
				.flatMap(Arrays::stream)
				.collect(Collectors.toList());
	}

	public static String getIpAddress(HttpServletRequest request){
		if(request == null){
			return null;
		}

		//Node servers send in the original X-Forwarded-For as X-Client-IP
		List<String> clientIp = getAllHeaderValuesOrdered(request, HttpHeaders.X_CLIENT_IP);
		Optional<String> lastNonInternalIp = getLastNonInternalIp(clientIp);
		if(lastNonInternalIp.isPresent()){
			return lastNonInternalIp.get();
		}

		//no x-client-ip present, check x-forwarded-for
		List<String> forwardedFor = getAllHeaderValuesOrdered(request, HttpHeaders.X_FORWARDED_FOR);
		lastNonInternalIp = getLastNonInternalIp(forwardedFor);
		if(lastNonInternalIp.isPresent()){
			return lastNonInternalIp.get();
		}

		if(!clientIp.isEmpty() || !forwardedFor.isEmpty()){
			logger.error("Unusable IPs included, falling back to remoteAddr. "
					+ HttpHeaders.X_CLIENT_IP + ": [" + clientIp + "] "
					+ HttpHeaders.X_FORWARDED_FOR + ": [" + forwardedFor + "]");
		}

		//no x-forwarded-for, use ip straight from http request
		String remoteAddr = request.getRemoteAddr();
		if("127.0.0.1".equals(remoteAddr)){//dev server
			remoteAddr = "209.63.146.244"; //535 Mission st office's ip
		}
		return remoteAddr;
	}

	private static Optional<String> getLastNonInternalIp(List<String> headerValues){
		Collections.reverse(headerValues);
		return headerValues.stream()
				.filter(RequestTool::isAValidIpV4)
				.filter(RequestTool::isPublicNet)
				.findFirst();
	}

	private static boolean isPublicNet(String ip){
		return !IpTool.isIpAddressInSubnets(ip, PRIVATE_NETS);
	}

	public static boolean isAValidIpV4(String dottedDecimal){
		String ipv4Pattern = "\\A(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}\\z";
		return dottedDecimal != null && dottedDecimal.matches(ipv4Pattern);
	}

	public static String getBodyAsString(ServletRequest request){
		try(InputStream inputStream = request.getInputStream()){
			ByteArrayOutputStream result = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int length;
			while((length = inputStream.read(buffer)) != -1){
				result.write(buffer, 0, length);
			}
			String charsetName = Optional.ofNullable(request.getCharacterEncoding())
					.orElse(StandardCharsets.UTF_8.name());
			return result.toString(charsetName);
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}

	public static byte[] tryGetBodyAsByteArray(ServletRequest request){
		try{
			return IOUtils.toByteArray(request.getInputStream());
		}catch(Exception e){
			return (INACCESSIBLE_BODY + e.getMessage()).getBytes();
		}
	}

	public static ThreadSafePhaseTimer getOrSetPhaseTimer(HttpServletRequest request){
		ThreadSafePhaseTimer result = request == null ? null
				: (ThreadSafePhaseTimer)request.getAttribute(REQUEST_PHASE_TIMER);
		if(result == null){
			result = new ThreadSafePhaseTimer(REQUEST_PHASE_TIMER);
			if(request != null){
				request.setAttribute(REQUEST_PHASE_TIMER, result);
			}
		}
		return result;
	}

	public static boolean isAjax(HttpServletRequest request){
		String xRequestedWith = request.getHeader(HttpHeaders.X_REQUESTED_WITH);
		return "XMLHttpRequest".equalsIgnoreCase(xRequestedWith);
	}

	public static String getFormattedForwardedForWithAdditionalIp(HttpServletRequest httpServletRequest,
			String additionalIp){
		List<String> forwardedFor = getAllHeaderValuesOrdered(httpServletRequest, HttpHeaders.X_FORWARDED_FOR);
		Set<String> distinctForwardedFor = new LinkedHashSet<>(forwardedFor);
		if(isAValidIpV4(additionalIp)){
			distinctForwardedFor.add(additionalIp);
		}
		return String.join(HEADER_VALUE_DELIMITER, distinctForwardedFor);
	}

	/** tests *****************************************************************/
	public static class RequestToolTests{
		private static final String PRIVATE_IP = "10.95.188.27";
		private static final String PUBLIC_IP = "209.63.146.244";

		@Test
		public void testCheckDouble(){
			Assert.assertFalse(checkDouble(-0.01, false, false, false, false));
			Assert.assertTrue(checkDouble(-0.01, false, true, false, false));
			Assert.assertFalse(checkDouble(null, false, false, false, false));
			Assert.assertTrue(checkDouble(null, true, false, false, false));
			Assert.assertFalse(checkDouble(Double.NEGATIVE_INFINITY, false, false, false, false));
			Assert.assertFalse(checkDouble(Double.NEGATIVE_INFINITY, false, true, false, false));
			Assert.assertFalse(checkDouble(Double.NEGATIVE_INFINITY, false, false, true, false));
			Assert.assertTrue(checkDouble(Double.NEGATIVE_INFINITY, false, true, true, false));
			Assert.assertFalse(checkDouble(Double.POSITIVE_INFINITY, false, false, false, false));
			Assert.assertTrue(checkDouble(Double.POSITIVE_INFINITY, false, false, true, false));
			Assert.assertFalse(checkDouble(Double.NaN, false, false, false, false));
			Assert.assertTrue(checkDouble(Double.NaN, false, false, false, true));
		}

		@Test
		public void testGetRequestUriWithQueryString(){
			Assert.assertEquals(getRequestUriWithQueryString("example.com", null), "example.com");
			Assert.assertEquals(getRequestUriWithQueryString("example.com",
			"stuff=things"), "example.com?stuff=things");
		}

		@Test
		public void testMakeRedirectUriIfTrailingSlash(){
			Assert.assertEquals(makeRedirectUriIfTrailingSlash("example.com", null), null);
			Assert.assertEquals(makeRedirectUriIfTrailingSlash("example.com/", null), "example.com");
			Assert.assertEquals(makeRedirectUriIfTrailingSlash("example.com/",
			"stuff=things"), "example.com?stuff=things");
			Assert.assertEquals(makeRedirectUriIfTrailingSlash("example.com", "stuff=things"), null);
		}

		@Test
		public void testGetFullyQualifiedUrl(){
			Assert.assertEquals(getFullyQualifiedUrl("aurl", "http://x.com", "x.com", 80).toString(), "http://x.com/aurl");
			Assert.assertEquals(getFullyQualifiedUrl("/aurl", "http://x.com", "x.com", 80).toString(), "http://x.com/aurl");
			Assert.assertEquals(getFullyQualifiedUrl("/aurl", "https://x.com", "x.com", 80).toString(), "https://x.com:80/aurl");
			Assert.assertEquals(getFullyQualifiedUrl("/aurl", "https://x.com", "x.com", 443).toString(), "https://x.com/aurl");
			Assert.assertEquals(getFullyQualifiedUrl("/", "https://x.com", "x.com", 443).toString(), "https://x.com/");
			Assert.assertEquals(getFullyQualifiedUrl("", "https://x.com", "x.com", 443).toString(), "https://x.com/");
			Assert.assertEquals(getFullyQualifiedUrl("", "http://x.com:8080", "x.com", 8080).toString(), "http://x.com:8080/");
			Assert.assertEquals(getFullyQualifiedUrl("snack", "https://x.com", "x.com", 8443).toString(), "https://x.com:8443/snack");
		}

		@Test
		public void testIsAValidIpV4(){
			Assert.assertTrue(isAValidIpV4("1.0.1.1"));
			Assert.assertTrue(isAValidIpV4("0.0.0.0"));
			Assert.assertTrue(isAValidIpV4("124.159.0.18"));
			Assert.assertFalse(isAValidIpV4("256.159.0.18"));
			Assert.assertFalse(isAValidIpV4("blabla"));
			Assert.assertFalse(isAValidIpV4(""));
			Assert.assertFalse(isAValidIpV4(null));
		}

		@Test
		public void testIsInternalNet(){
			Assert.assertFalse(isPublicNet(PRIVATE_IP));
			Assert.assertTrue(isPublicNet(PUBLIC_IP));
		}

		@Test
		public void testGetIpAddress(){
			// haproxy -> node -> haproxy -> tomcat
			HttpServletRequest request = new HttpRequestBuilder()
					.withHeader(HttpHeaders.X_FORWARDED_FOR, PRIVATE_IP)
					.withHeader(HttpHeaders.X_CLIENT_IP, PUBLIC_IP)
					.build();
			// alb -> haproxy -> node -> haproxy -> tomcat
			Assert.assertEquals(getIpAddress(request), PUBLIC_IP);
			request = new HttpRequestBuilder()
					.withHeader(HttpHeaders.X_FORWARDED_FOR, PRIVATE_IP)
					.withHeader(HttpHeaders.X_CLIENT_IP, PUBLIC_IP + HEADER_VALUE_DELIMITER + PRIVATE_IP)
					.build();
			Assert.assertEquals(getIpAddress(request), PUBLIC_IP);
			// haproxy -> tomcat
			request = new HttpRequestBuilder()
					.withHeader(HttpHeaders.X_FORWARDED_FOR, PUBLIC_IP)
					.build();
			Assert.assertEquals(getIpAddress(request), PUBLIC_IP);
			// alb -> haproxy -> tomcat
			request = new HttpRequestBuilder()
					.withHeader(HttpHeaders.X_FORWARDED_FOR, PUBLIC_IP + HEADER_VALUE_DELIMITER + PRIVATE_IP)
					.build();
			Assert.assertEquals(getIpAddress(request), PUBLIC_IP);
			// alb -> haproxy -> tomcat with two separate headers
			request = new HttpRequestBuilder()
					.withHeader(HttpHeaders.X_FORWARDED_FOR, PUBLIC_IP)
					.withHeader(HttpHeaders.X_FORWARDED_FOR, PRIVATE_IP)
					.build();
			Assert.assertEquals(getIpAddress(request), PUBLIC_IP);
		}

		@Test
		public void testGetForwardedFor(){
			HttpServletRequest request = new HttpRequestBuilder()
					.withHeader(HttpHeaders.X_FORWARDED_FOR, PRIVATE_IP)
					.withHeader(HttpHeaders.X_FORWARDED_FOR, PUBLIC_IP)
					.build();
			Assert.assertEquals(getFormattedForwardedForWithAdditionalIp(request, null), PRIVATE_IP
					+ HEADER_VALUE_DELIMITER + PUBLIC_IP);

			request = new HttpRequestBuilder()
					.withHeader(HttpHeaders.X_FORWARDED_FOR, PUBLIC_IP)
					.withHeader(HttpHeaders.X_FORWARDED_FOR, PRIVATE_IP)
					.build();
			Assert.assertEquals(getFormattedForwardedForWithAdditionalIp(request, null), PUBLIC_IP
					+ HEADER_VALUE_DELIMITER + PRIVATE_IP);
			Assert.assertEquals(getFormattedForwardedForWithAdditionalIp(request, "invalid ip"), PUBLIC_IP
					+ HEADER_VALUE_DELIMITER + PRIVATE_IP);
			Assert.assertEquals(getFormattedForwardedForWithAdditionalIp(request, "0.0.0.0"), PUBLIC_IP
					+ HEADER_VALUE_DELIMITER + PRIVATE_IP + HEADER_VALUE_DELIMITER + "0.0.0.0");

			request = new HttpRequestBuilder().withHeader(HttpHeaders.X_FORWARDED_FOR, PRIVATE_IP)
					.withHeader(HttpHeaders.X_FORWARDED_FOR, "0.0.0.0")
					.build();
			Assert.assertEquals(getFormattedForwardedForWithAdditionalIp(request, "0.0.0.0"), PRIVATE_IP
					+ HEADER_VALUE_DELIMITER + "0.0.0.0");

			request = new HttpRequestBuilder().withHeader(HttpHeaders.X_FORWARDED_FOR, "0.0.0.0").withHeader(
					HttpHeaders.X_FORWARDED_FOR, PRIVATE_IP).build();
			Assert.assertEquals(getFormattedForwardedForWithAdditionalIp(request, "0.0.0.0"), "0.0.0.0"
					+ HEADER_VALUE_DELIMITER + PRIVATE_IP);
			Assert.assertEquals(getFormattedForwardedForWithAdditionalIp(request, PRIVATE_IP), "0.0.0.0"
					+ HEADER_VALUE_DELIMITER + PRIVATE_IP);
			Assert.assertEquals(getFormattedForwardedForWithAdditionalIp(request, "1.1.1.1"), "0.0.0.0"
					+ HEADER_VALUE_DELIMITER + PRIVATE_IP + HEADER_VALUE_DELIMITER + "1.1.1.1");

		}
	}
}

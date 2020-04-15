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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.httpclient.HttpHeaders;
import io.datarouter.httpclient.security.UrlConstants;
import io.datarouter.instrumentation.trace.TracerThreadLocal;
import io.datarouter.instrumentation.trace.TracerTool;
import io.datarouter.scanner.Scanner;
import io.datarouter.util.BooleanTool;
import io.datarouter.util.net.IpTool;
import io.datarouter.util.number.NumberTool;
import io.datarouter.util.string.StringTool;
import io.datarouter.util.tuple.DefaultableMap;

public class RequestTool{
	private static final Logger logger = LoggerFactory.getLogger(RequestTool.class);

	protected static final String HEADER_VALUE_DELIMITER = ", ";
	private static final String[] PRIVATE_NETS = {"10.0.0.0/8", "172.16.0.0/12"};

	public static final String INACCESSIBLE_BODY = "INACCESSIBLE BODY: ";
	public static final String REQUEST_PHASE_TIMER = "requestPhaseTimer";

	public static String getPath(HttpServletRequest request){
		return request.getServletPath() + StringTool.nullSafe(request.getPathInfo());
	}

	public static List<String> getSlashedUriParts(HttpServletRequest request){
		return getSlashedUriParts(request.getRequestURI());
	}

	public static List<String> getSlashedUriParts(String uri){
		List<String> uriVars = Arrays.asList(uri.split("/"));
		//get rid of blanks
		return Scanner.of(uriVars).include(StringTool::notEmpty).list();
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
			if(StringTool.notEmpty(override)){
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
		return Arrays.asList(stringVal.split(delimiter));
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
			if(StringTool.notEmpty(override)){
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
			if(StringTool.notEmpty(override)){
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
			if(StringTool.notEmpty(override)){
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
		for(Entry<String,String[]> param : request.getParameterMap().entrySet()){
			map.put(param.getKey(), param.getValue()[0]);
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

	protected static String makeRedirectUriIfTrailingSlash(String uri, String queryString){
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

	public static String getRequestUriWithQueryString(String uri, String queryString){
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
			logger.debug("Unusable IPs included, falling back to remoteAddr."
					+ " " + HttpHeaders.X_CLIENT_IP + "=" + clientIp
					+ " " + HttpHeaders.X_FORWARDED_FOR + "=" + forwardedFor
					+ " path=" + getPath(request));
			logger.debug("", new Exception());
		}
		return request.getRemoteAddr();
	}

	private static Optional<String> getLastNonInternalIp(List<String> headerValues){
		Collections.reverse(headerValues);
		return headerValues.stream()
				.filter(RequestTool::isAValidIpV4)
				.filter(RequestTool::isPublicNet)
				.findFirst();
	}

	protected static boolean isPublicNet(String ip){
		return !IpTool.isIpAddressInSubnets(ip, PRIVATE_NETS);
	}

	public static boolean isAValidIpV4(String dottedDecimal){
		String ipv4Pattern = "\\A(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}\\z";
		return dottedDecimal != null && dottedDecimal.matches(ipv4Pattern);
	}

	public static String getBodyAsString(HttpServletRequest request){
		try(InputStream inputStream = request.getInputStream();
				var $ = TracerTool.startSpan(TracerThreadLocal.get(), "RequestTool getBodyAsString")){
			if(inputStream == null){
				logger.warn("Request body is empty for uri={}, query={}, userAgent={}", request.getRequestURI(), request
						.getQueryString(), getUserAgent(request));
				return "";
			}
			ByteArrayOutputStream result = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int length;
			while((length = inputStream.read(buffer)) != -1){
				result.write(buffer, 0, length);
			}
			String charsetName = Optional.ofNullable(request.getCharacterEncoding())
					.orElse(StandardCharsets.UTF_8.name());
			String string = result.toString(charsetName);
			TracerTool.appendToSpanInfo("characters", string.length());
			return string;
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

	public static boolean isAjax(HttpServletRequest request){
		String xRequestedWith = request.getHeader(HttpHeaders.X_REQUESTED_WITH);
		return "XMLHttpRequest".equalsIgnoreCase(xRequestedWith);
	}

	public static String getRequestUriWithoutContextPath(HttpServletRequest request){
		return request.getRequestURI().substring(request.getContextPath().length());
	}

}

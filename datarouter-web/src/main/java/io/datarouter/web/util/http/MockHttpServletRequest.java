/*
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.ReadListener;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;

import io.datarouter.httpclient.HttpHeaders;

public class MockHttpServletRequest implements HttpServletRequest{

	private final Map<String,String[]> parameterMap;
	private final Reader reader;
	private final Map<String,Set<String>> headers;
	private final Map<String, Object> attributes;
	private final Cookie[] cookies;
	private final String serverName;
	private final String method;
	private String pathInfo;
	private String servletPath;

	public MockHttpServletRequest(Map<String, String[]> parameterMap, Reader reader, Map<String, Set<String>> headers,
			Map<String, Object> attributes, List<Cookie> cookies, String serverName, String method){
		this.parameterMap = parameterMap;
		this.reader = reader;
		this.headers = headers;
		this.attributes = attributes;
		this.cookies = cookies.toArray(new Cookie[0]);
		this.serverName = serverName;
		this.method = method;
	}

	@Override
	public Object getAttribute(String name){
		return attributes.get(name);
	}

	@Override
	public Enumeration<String> getAttributeNames(){
		return Collections.enumeration(attributes.keySet());
	}

	@Override
	public String getCharacterEncoding(){
		return StandardCharsets.UTF_8.toString();
	}

	@Override
	public void setCharacterEncoding(String env){
		throw new UnsupportedOperationException();
	}

	@Override
	public int getContentLength(){
		throw new UnsupportedOperationException();
	}

	@Override
	public String getContentType(){
		return getHeader(HttpHeaders.CONTENT_TYPE);
	}

	@Override
	public ServletInputStream getInputStream(){
		return new ServletInputStream(){
			@Override
			public int read() throws IOException{
				return reader.read();
			}

			@Override
			public boolean isFinished(){
				return false;
			}

			@Override
			public boolean isReady(){
				return true;
			}

			@Override
			public void setReadListener(ReadListener readListener){

			}
		};
	}

	@Override
	public String getParameter(String name){
		return getParameterMap().get(name)[0];
	}

	@Override
	public Enumeration<String> getParameterNames(){
		throw new UnsupportedOperationException();
	}

	@Override
	public String[] getParameterValues(String name){
		throw new UnsupportedOperationException();
	}

	@Override
	public Map<String,String[]> getParameterMap(){
		return parameterMap;
	}

	@Override
	public String getProtocol(){
		throw new UnsupportedOperationException();
	}

	@Override
	public String getScheme(){
		throw new UnsupportedOperationException();
	}

	@Override
	public String getServerName(){
		return serverName;
	}

	@Override
	public int getServerPort(){
		throw new UnsupportedOperationException();
	}

	@Override
	public BufferedReader getReader(){
		return new BufferedReader(reader);
	}

	@Override
	public String getRemoteAddr(){
		throw new UnsupportedOperationException();
	}

	@Override
	public String getRemoteHost(){
		throw new UnsupportedOperationException();
	}

	@Override
	public void setAttribute(String name, Object object){
		attributes.put(name, object);
	}

	@Override
	public void removeAttribute(String name){
		attributes.remove(name);
	}

	@Override
	public Locale getLocale(){
		throw new UnsupportedOperationException();
	}

	@Override
	public Enumeration<Locale> getLocales(){
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isSecure(){
		throw new UnsupportedOperationException();
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String path){
		throw new UnsupportedOperationException();
	}

	@Override
	public String getRealPath(String path){
		throw new UnsupportedOperationException();
	}

	@Override
	public int getRemotePort(){
		throw new UnsupportedOperationException();
	}

	@Override
	public String getLocalName(){
		throw new UnsupportedOperationException();
	}

	@Override
	public String getLocalAddr(){
		throw new UnsupportedOperationException();
	}

	@Override
	public int getLocalPort(){
		throw new UnsupportedOperationException();
	}

	@Override
	public ServletContext getServletContext(){
		throw new UnsupportedOperationException();
	}

	@Override
	public AsyncContext startAsync(){
		throw new UnsupportedOperationException();
	}

	@Override
	public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse){
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isAsyncStarted(){
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isAsyncSupported(){
		throw new UnsupportedOperationException();
	}

	@Override
	public AsyncContext getAsyncContext(){
		throw new UnsupportedOperationException();
	}

	@Override
	public DispatcherType getDispatcherType(){
		throw new UnsupportedOperationException();
	}

	@Override
	public String getAuthType(){
		throw new UnsupportedOperationException();
	}

	@Override
	public Cookie[] getCookies(){
		return cookies;
	}

	@Override
	public long getDateHeader(String name){
		throw new UnsupportedOperationException();
	}

	@Override
	public String getHeader(String name){
		return headers.getOrDefault(name, Set.of()).stream().findFirst().orElse(null);
	}

	@Override
	public Enumeration<String> getHeaders(String name){
		return Collections.enumeration(headers.getOrDefault(name, new HashSet<>()));
	}

	@Override
	public Enumeration<String> getHeaderNames(){
		return Collections.enumeration(headers.keySet());
	}

	@Override
	public int getIntHeader(String name){
		throw new UnsupportedOperationException();
	}

	@Override
	public String getMethod(){
		return method;
	}

	public void setPathInfo(String pathInfo){
		this.pathInfo = pathInfo;
	}

	@Override
	public String getPathInfo(){
		if(pathInfo != null){
			return pathInfo;
		}
		throw new UnsupportedOperationException();
	}

	@Override
	public String getPathTranslated(){
		throw new UnsupportedOperationException();
	}

	@Override
	public String getContextPath(){
		throw new UnsupportedOperationException();
	}

	@Override
	public String getQueryString(){
		throw new UnsupportedOperationException();
	}

	@Override
	public String getRemoteUser(){
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isUserInRole(String role){
		throw new UnsupportedOperationException();
	}

	@Override
	public Principal getUserPrincipal(){
		throw new UnsupportedOperationException();
	}

	@Override
	public String getRequestedSessionId(){
		throw new UnsupportedOperationException();
	}

	@Override
	public String getRequestURI(){
		throw new UnsupportedOperationException();
	}

	@Override
	public StringBuffer getRequestURL(){
		throw new UnsupportedOperationException();
	}

	public void setServletPath(String servletPath){
		this.servletPath = servletPath;
	}

	@Override
	public String getServletPath(){
		if(servletPath == null){
			throw new UnsupportedOperationException();
		}
		return servletPath;
	}

	@Override
	public HttpSession getSession(boolean create){
		throw new UnsupportedOperationException();
	}

	@Override
	public HttpSession getSession(){
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isRequestedSessionIdValid(){
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isRequestedSessionIdFromCookie(){
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isRequestedSessionIdFromURL(){
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isRequestedSessionIdFromUrl(){
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean authenticate(HttpServletResponse response){
		throw new UnsupportedOperationException();
	}

	@Override
	public void login(String username, String password){
		throw new UnsupportedOperationException();
	}

	@Override
	public void logout(){
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<Part> getParts(){
		throw new UnsupportedOperationException();
	}

	@Override
	public Part getPart(String name){
		throw new UnsupportedOperationException();
	}

	@Override
	public long getContentLengthLong(){
		return 0;
	}

	@Override
	public String changeSessionId(){
		return null;
	}

	@Override
	public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass){
		return null;
	}

}

package com.hotpads.handler.types;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

public class MockHttpRequest implements HttpServletRequest{

	private final Map<String,String[]> parameterMap;
	private final Reader reader;

	public MockHttpRequest(Map<String,String[]> parameterMap, Reader reader){
		this.parameterMap = parameterMap;
		this.reader = reader;
	}

	@Override
	public Object getAttribute(String name){
		throw new UnsupportedOperationException();
	}

	@Override
	public Enumeration<String> getAttributeNames(){
		throw new UnsupportedOperationException();
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
		throw new UnsupportedOperationException();
	}

	@Override
	public ServletInputStream getInputStream(){
		return new ServletInputStream(){
			@Override
			public int read() throws IOException{
				return reader.read();
			}
		};
	}

	@Override
	public String getParameter(String name){
		throw new UnsupportedOperationException();
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
		throw new UnsupportedOperationException();
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
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeAttribute(String name){
		throw new UnsupportedOperationException();
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
		throw new UnsupportedOperationException();
	}

	@Override
	public long getDateHeader(String name){
		throw new UnsupportedOperationException();
	}

	@Override
	public String getHeader(String name){
		throw new UnsupportedOperationException();
	}

	@Override
	public Enumeration<String> getHeaders(String name){
		throw new UnsupportedOperationException();
	}

	@Override
	public Enumeration<String> getHeaderNames(){
		throw new UnsupportedOperationException();
	}

	@Override
	public int getIntHeader(String name){
		throw new UnsupportedOperationException();
	}

	@Override
	public String getMethod(){
		throw new UnsupportedOperationException();
	}

	@Override
	public String getPathInfo(){
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

	@Override
	public String getServletPath(){
		throw new UnsupportedOperationException();
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

}

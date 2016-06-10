package com.hotpads.exception.analysis;

import java.util.Date;
import java.util.Map;

public class ExceptionDto{

	public Date date;
	public String serverName;
	public String stackTrace;
	public String errorClass;

	public String errorLocation;
	public Date receivedAt;
	public String methodName;
	public int lineNumber;

	public String httpMethod;
	public Map<String,String[]> httpParams;
	public String protocol;
	public String hostname;
	public int port;
	public String path;
	public String queryString;
	public String body;

	public String ip;
	public String userRoles;
	public Long userId;

	public String acceptCharset;
	public String acceptEncoding;
	public String acceptLanguage;
	public String accept;
	public String cacheControl;
	public String connection;
	public String contentEncoding;
	public String contentLanguage;
	public String contentLength;
	public String contentType;
	public String cookie;
	public String dnt;
	public String host;
	public String ifModifiedSince;
	public String origin;
	public String pragma;
	public String referer;
	public String userAgent;
	public String xForwardedFor;
	public String xRequestedWith;
	public Map<String,String[]> others;

}

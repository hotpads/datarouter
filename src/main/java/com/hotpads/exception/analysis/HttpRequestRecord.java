package com.hotpads.exception.analysis;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.serialize.fielder.Fielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.FieldTool;
import com.hotpads.datarouter.storage.field.imp.DateField;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.comparable.IntegerField;
import com.hotpads.datarouter.storage.field.imp.comparable.LongField;
import com.hotpads.datarouter.storage.key.multi.Lookup;
import com.hotpads.datarouter.storage.key.unique.BaseUniqueKey;
import com.hotpads.datarouter.util.UuidTool;
import com.hotpads.handler.exception.ExceptionRecord;
import com.hotpads.util.core.MapTool;
import com.hotpads.util.core.StringTool;

public class HttpRequestRecord extends BaseDatabean<HttpRequestRecordKey, HttpRequestRecord>{

	/******************* fields ************************/

	private HttpRequestRecordKey key;
	private Date created;

	private String exceptionRecordId;
	private String exceptionPlace;
	private String methodName;
	private int lineNumber;

	private String httpMethod;
	private String httpParams;

	private String protocol;
	private String hostname;
	private int port;
	private String contextPath;
	private String path;
	private String queryString;

	private String ip;
	private String userRoles;
	private Long userId;

	private String acceptCharset;
	private String acceptEncoding;
	private String acceptLanguage;
	private String accept;
	private String cacheControl;
	private String connection;
	private String contentEncoding;
	private String contentLanguage;
	private String contentLength;
	private String contentType;
	private String cookie;
	private String dnt;
	private String host;
	private String ifModifiedSince;
	private String origin;
	private String pragma;
	private String referer;
	private String userAgent;
	private String xForwardedFor;
	private String xRequestedWith;
	private String others;

	private static class F {
		public static String
		created = "created",

		exceptionRecordId = "exceptionRecordId",
		exceptionPlace = "exceptionPlace",
		methodName = "methodName",
		lineNumber = "lineNumber",

		httpMethod = "httpMethod",
		httpParams = "httpParams",

		protocolString = "protocol",
		hostname = "hostname",
		port = "port",
		contextPath = "contextPath",
		path = "path",
		queryString = "queryString",

		ip = "ip",
		userRoles="userRoles",
		userId = "userId",

		acceptCharset = "acceptCharset",
		acceptEncoding = "acceptEncoding",
		acceptLanguage = "acceptLanguage",
		accept = "accept",
		cacheControl = "cacheControl",
		connection = "connection",
		contentEncoding = "contentEncoding",
		contentLanguage = "contentLanguage",
		contentLength = "contentLength",
		contentType = "contentType",
		cookie = "cookie",
		dnt = "dnt",
		host = "host",
		ifModifiedSince = "ifModifiedSince",
		origin = "origin",
		pragma = "pragma",
		referer = "referer",
		userAgent = "userAgent",
		xForwardedFor = "xForwardedFor",
		xRequestedWith = "xRequestedWith",
		others = "others";
	}

	public static class HttpRequestRecordFielder extends BaseDatabeanFielder<HttpRequestRecordKey, HttpRequestRecord>{

		public HttpRequestRecordFielder() {}

		@Override
		public Class<? extends Fielder<HttpRequestRecordKey>> getKeyFielderClass() {
			return HttpRequestRecordKey.class;
		}

		//TODO adapt field size
		@Override
		public List<Field<?>> getNonKeyFields(HttpRequestRecord d) {
			return FieldTool.createList(
					new DateField(F.created, d.created),

					new StringField(F.exceptionRecordId, d.exceptionRecordId, MySqlColumnType.MAX_LENGTH_VARCHAR),
					new StringField(F.exceptionPlace, d.exceptionPlace, MySqlColumnType.MAX_LENGTH_VARCHAR),
					new StringField(F.methodName, d.methodName, MySqlColumnType.MAX_LENGTH_VARCHAR),
					new IntegerField(F.lineNumber, d.lineNumber),

					new StringField(F.httpMethod, d.httpMethod, 16),
					new StringField(F.httpParams, d.httpParams, MySqlColumnType.MAX_LENGTH_MEDIUMTEXT),

					new StringField(F.protocolString, d.protocol, 10),
					new StringField(F.hostname, d.hostname, MySqlColumnType.MAX_LENGTH_VARCHAR),
					new IntegerField(F.port, d.port),
					new StringField(F.contextPath, d.contextPath, MySqlColumnType.MAX_LENGTH_VARCHAR),
					new StringField(F.path, d.path, MySqlColumnType.MAX_LENGTH_VARCHAR),
					new StringField(F.queryString, d.queryString, MySqlColumnType.MAX_LENGTH_MEDIUMTEXT),

					new StringField(F.ip, d.ip, 39),
					new StringField(F.userRoles, d.userRoles, MySqlColumnType.MAX_LENGTH_VARCHAR),
					new LongField(F.userId, d.userId),
					
					new StringField(F.acceptCharset, d.acceptCharset, MySqlColumnType.MAX_LENGTH_VARCHAR),
					new StringField(F.acceptEncoding, d.acceptEncoding, MySqlColumnType.MAX_LENGTH_VARCHAR),
					new StringField(F.acceptLanguage, d.acceptLanguage, MySqlColumnType.MAX_LENGTH_VARCHAR),
					new StringField(F.accept, d.accept, MySqlColumnType.MAX_LENGTH_VARCHAR),
					new StringField(F.cacheControl, d.cacheControl, MySqlColumnType.MAX_LENGTH_VARCHAR),
					new StringField(F.connection, d.connection, MySqlColumnType.MAX_LENGTH_VARCHAR),
					new StringField(F.contentEncoding, d.contentEncoding, MySqlColumnType.MAX_LENGTH_VARCHAR),
					new StringField(F.contentLanguage, d.contentLanguage, MySqlColumnType.MAX_LENGTH_VARCHAR),
					new StringField(F.contentLength, d.contentLength, MySqlColumnType.MAX_LENGTH_VARCHAR),
					new StringField(F.contentType, d.contentType, MySqlColumnType.MAX_LENGTH_VARCHAR),
					new StringField(F.cookie, d.cookie, MySqlColumnType.MAX_LENGTH_MEDIUMTEXT),
					new StringField(F.dnt, d.dnt, MySqlColumnType.MAX_LENGTH_VARCHAR),
					new StringField(F.host, d.host, MySqlColumnType.MAX_LENGTH_VARCHAR),
					new StringField(F.ifModifiedSince, d.ifModifiedSince, MySqlColumnType.MAX_LENGTH_VARCHAR),
					new StringField(F.origin, d.origin, MySqlColumnType.MAX_LENGTH_VARCHAR),
					new StringField(F.pragma, d.pragma, MySqlColumnType.MAX_LENGTH_VARCHAR),
					new StringField(F.referer, d.referer, MySqlColumnType.MAX_LENGTH_VARCHAR),
					new StringField(F.userAgent, d.userAgent, MySqlColumnType.MAX_LENGTH_MEDIUMTEXT),
					new StringField(F.xForwardedFor, d.xForwardedFor, MySqlColumnType.MAX_LENGTH_VARCHAR),
					new StringField(F.xRequestedWith, d.xRequestedWith, MySqlColumnType.MAX_LENGTH_VARCHAR),
					new StringField(F.others, d.others, MySqlColumnType.MAX_LENGTH_VARCHAR)
					);
		}

	}

	/********************** construct ********************/

	HttpRequestRecord() {
		key = new HttpRequestRecordKey();
	}

	public HttpRequestRecord(String exceptionRecordId, String exceptionPlace, String methodName, int lineNumber,
			String httpMethod, String httpParams, String protocol, String hostname, int port, String contextPath,
			String path, String queryString, String ip, String sessionRoles, long userId, HeadersWrapper headersWrapper) {
		this.key = new HttpRequestRecordKey(UuidTool.generateUuid());
		this.created = new Date();

		this.exceptionRecordId = exceptionRecordId;
		this.exceptionPlace = exceptionPlace;
		this.methodName = methodName;
		this.lineNumber = lineNumber;

		this.httpMethod = httpMethod;
		this.httpParams = httpParams;

		this.protocol = protocol;
		this.hostname = hostname;
		this.port = port;
		this.contextPath = contextPath;
		this.path = path;
		this.queryString = queryString;

		this.ip = ip;
		this.userRoles = sessionRoles;
		this.userId = userId;

		this.acceptCharset = headersWrapper.getAcceptCharset();
		this.acceptEncoding = headersWrapper.getAcceptEncoding();
		this.acceptLanguage = headersWrapper.getAcceptLanguage();
		this.accept = headersWrapper.getAccept();
		this.cacheControl = headersWrapper.getCacheControl();
		this.connection = headersWrapper.getConnection();
		this.contentEncoding = headersWrapper.getContentEncoding();
		this.contentLanguage = headersWrapper.getContentLanguage();
		this.contentLength = headersWrapper.getContentLength();
		this.contentType = headersWrapper.getContentType();
		this.cookie = headersWrapper.getCookie();
		this.dnt = headersWrapper.getDnt();
		this.host = headersWrapper.getHost();
		this.ifModifiedSince = headersWrapper.getIfModifiedSince();
		this.origin = headersWrapper.getOrigin();
		this.pragma = headersWrapper.getPragma();
		this.referer = headersWrapper.getReferer();
		this.userAgent = headersWrapper.getUserAgent();
		this.xForwardedFor = headersWrapper.getXForwardedFor();
		this.xRequestedWith = headersWrapper.getXRequestedWith();

		this.others = headersWrapper.getOthers();
	}

	@Override
	public Class<HttpRequestRecordKey> getKeyClass() {
		return HttpRequestRecordKey.class;
	}

	/********************************Lookup*************************************/
	@SuppressWarnings("serial")
	public static class HttpRequestRecordByExceptionRecord extends BaseUniqueKey<HttpRequestRecordKey> implements Lookup<HttpRequestRecordKey> {

		private ExceptionRecord exceptionRecord;

		public HttpRequestRecordByExceptionRecord(ExceptionRecord exceptionRecord) {
			this.exceptionRecord = exceptionRecord;
		}

		@Override
		public List<Field<?>> getFields() {
			return FieldTool.createList(
					new StringField(F.exceptionRecordId, exceptionRecord.getKey().getId(), MySqlColumnType.MAX_LENGTH_VARCHAR)
					);
		}
	}

	/*************** getters / setters ******************/

	@Override
	public HttpRequestRecordKey getKey() {
		return key;
	}

	public void setKey(HttpRequestRecordKey key) {
		this.key = key;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public String getExceptionRecordId() {
		return exceptionRecordId;
	}

	public void setExceptionRecordId(String exceptionRecordId) {
		this.exceptionRecordId = exceptionRecordId;
	}

	public String getExceptionPlace() {
		return exceptionPlace;
	}

	public void setExceptionPlace(String exceptionPlace) {
		this.exceptionPlace = exceptionPlace;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}

	public String getHttpMethod() {
		return httpMethod;
	}

	public void setHttpMethod(String httpMethod) {
		this.httpMethod = httpMethod;
	}

	public String getHttpParams() {
		return httpParams;
	}

	public Map<String, String> getHttpParamsMap() {
		if (httpParams == null) { return null;}
		String[] tab = httpParams.split(",");
		String[] keyValue;
		Map<String, String> params = MapTool.create();
		for (String string : tab) {
			if (StringTool.notEmpty(string)) {
				keyValue = string.split(":");
				params.put(keyValue[0], keyValue[1].substring(1, keyValue[1].length() - 1));
			}
		}
		return params;
	}

	public void setHttpParams(String httpParams) {
		this.httpParams = httpParams;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getContextPath() {
		return contextPath;
	}

	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getQueryString() {
		return queryString;
	}

	public void setQueryString(String queryString) {
		this.queryString = queryString;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getUserRoles() {
		return userRoles;
	}

	public void setUserRoles(String userRoles) {
		this.userRoles = userRoles;
	}

	public String getShorterRoles() {
		return userRoles.substring(1, userRoles.length() - 1);
	}
	
	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Map<String, String> getHeaders() {
		Map<String, String> map = MapTool.createTreeMap();
		map.put("accept-charset", acceptCharset);
		map.put("accept-encoding", acceptEncoding);
		map.put("accept-language", acceptLanguage);
		map.put("accept", accept);
		map.put("cache-control", cacheControl);
		map.put("connection", connection);
		map.put("content-encoding", contentEncoding);
		map.put("content-language", contentLanguage);
		map.put("content-length", contentLength);
		map.put("content-type", contentType);
		map.put("dnt", dnt);
		map.put("host", host);
		map.put("if-modified-since", ifModifiedSince);
		map.put("origin", origin);
		map.put("pragma", pragma);
		map.put("referer", referer);
		map.put("userAgent", userAgent);
		map.put("x-forwarded-for", xForwardedFor);
		map.put("x-requested-with", xRequestedWith);
		return map;
	}

	public String getAcceptCharset() {
		return acceptCharset;
	}

	public void setAcceptCharset(String acceptCharset) {
		this.acceptCharset = acceptCharset;
	}

	public String getAcceptEncoding() {
		return acceptEncoding;
	}

	public void setAcceptEncoding(String acceptEncoding) {
		this.acceptEncoding = acceptEncoding;
	}

	public String getAcceptLanguage() {
		return acceptLanguage;
	}

	public void setAcceptLanguage(String acceptLanguage) {
		this.acceptLanguage = acceptLanguage;
	}

	public String getAccept() {
		return accept;
	}

	public void setAccept(String accept) {
		this.accept = accept;
	}

	public String getCacheControl() {
		return cacheControl;
	}

	public void setCacheControl(String cacheControl) {
		this.cacheControl = cacheControl;
	}

	public String getConnection() {
		return connection;
	}

	public void setConnection(String connection) {
		this.connection = connection;
	}

	public String getContentEncoding() {
		return contentEncoding;
	}

	public void setContentEncoding(String contentEncoding) {
		this.contentEncoding = contentEncoding;
	}

	public String getContentLanguage() {
		return contentLanguage;
	}

	public void setContentLanguage(String contentLanguage) {
		this.contentLanguage = contentLanguage;
	}

	public String getContentLength() {
		return contentLength;
	}

	public void setContentLength(String contentLength) {
		this.contentLength = contentLength;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getCookie() {
		return cookie;
	}

	public void setCookie(String cookie) {
		this.cookie = cookie;
	}

	public String getDnt() {
		return dnt;
	}

	public void setDnt(String dnt) {
		this.dnt = dnt;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getIfModifiedSince() {
		return ifModifiedSince;
	}

	public void setIfModifiedSince(String ifModifiedSince) {
		this.ifModifiedSince = ifModifiedSince;
	}

	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	public String getPragma() {
		return pragma;
	}

	public void setPragma(String pragma) {
		this.pragma = pragma;
	}

	public String getReferer() {
		return referer;
	}

	public void setReferer(String referer) {
		this.referer = referer;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public String getxForwardedFor() {
		return xForwardedFor;
	}

	public void setxForwardedFor(String xForwardedFor) {
		this.xForwardedFor = xForwardedFor;
	}

	public String getxRequestedWith() {
		return xRequestedWith;
	}

	public void setxRequestedWith(String xRequestedWith) {
		this.xRequestedWith = xRequestedWith;
	}

	public String getOthers() {
		return others;
	}

	public void setOthers(String others) {
		this.others = others;
	}

	public Map<String , String> getOthersHeaders() {
		if (StringTool.isEmpty(others)) {return null;}
		Map<String, String> map = MapTool.createTreeMap();
		String[] tab = others.split(", ");
		String[] keyVal;
		String val;
		for (String string : tab) {
			keyVal = string.split(": ");
			if (keyVal.length > 0 ) {
				val = (keyVal.length > 1 ? keyVal[1] : null);
				map.put(keyVal[0], val);
			}
		}
		return map;
	}

	public boolean isFromAjax() {
		return "XMLHttpRequest".equals(xRequestedWith);
	}

	public Map<String, String> getCookiesMap() {
		String[] tab = cookie.split("; ");
		String[] keyValue;
		Map<String, String> params = MapTool.create();
		for (String string : tab) {
			if (StringTool.notEmpty(string)) {
				keyValue = string.split("=");
				params.put(keyValue[0], keyValue[1].substring(1, keyValue[1].length() - 1));
			}
		}
		return params;
	}

	public String getUrl() {
		return getProtocol()+ "://" + hostname + ":" + port + contextPath + path + (queryString != null ? "?" + queryString : "");
	}
}

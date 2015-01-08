package com.hotpads.exception.analysis;

import static com.hotpads.exception.analysis.HttpHeaders.ACCEPT;
import static com.hotpads.exception.analysis.HttpHeaders.ACCEPT_CHARSET;
import static com.hotpads.exception.analysis.HttpHeaders.ACCEPT_ENCODING;
import static com.hotpads.exception.analysis.HttpHeaders.ACCEPT_LANGUAGE;
import static com.hotpads.exception.analysis.HttpHeaders.CACHE_CONTROL;
import static com.hotpads.exception.analysis.HttpHeaders.CONNECTION;
import static com.hotpads.exception.analysis.HttpHeaders.CONTENT_ENCODING;
import static com.hotpads.exception.analysis.HttpHeaders.CONTENT_LANGUAGE;
import static com.hotpads.exception.analysis.HttpHeaders.CONTENT_LENGTH;
import static com.hotpads.exception.analysis.HttpHeaders.CONTENT_TYPE;
import static com.hotpads.exception.analysis.HttpHeaders.DNT;
import static com.hotpads.exception.analysis.HttpHeaders.HOST;
import static com.hotpads.exception.analysis.HttpHeaders.IF_MODIFIED_SINCE;
import static com.hotpads.exception.analysis.HttpHeaders.ORIGIN;
import static com.hotpads.exception.analysis.HttpHeaders.PRAGMA;
import static com.hotpads.exception.analysis.HttpHeaders.REFERER;
import static com.hotpads.exception.analysis.HttpHeaders.USER_AGENT;
import static com.hotpads.exception.analysis.HttpHeaders.X_FORWARDED_FOR;
import static com.hotpads.exception.analysis.HttpHeaders.X_REQUESTED_WITH;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import com.google.common.base.Joiner;
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
import com.hotpads.datarouter.storage.field.imp.custom.LongDateField;
import com.hotpads.datarouter.storage.key.multi.BaseLookup;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.datarouter.util.UuidTool;
import com.hotpads.handler.exception.ExceptionRecord;
import com.hotpads.handler.util.RequestTool;
import com.hotpads.util.core.MapTool;

public class HttpRequestRecord extends BaseDatabean<HttpRequestRecordKey, HttpRequestRecord>{

	private static final int
		LENGTH_httpMethod = 16,
		LENGTH_protocol = 5,
		LENGTH_ip = 39;

	/******************* fields ************************/

	private HttpRequestRecordKey key;
	private Date created;
	private Date receivedAt;
	private Long duration;

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
	private String otherHeaders;

	private static class F {
		public static String
		created = "created",

		exceptionRecordId = "exceptionRecordId",
		exceptionPlace = "exceptionPlace",
		methodName = "methodName",
		lineNumber = "lineNumber",

		httpMethod = "httpMethod",
		httpParams = "httpParams",

		protocol = "protocol",
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
		others = "otherHeaders",
		receivedAt = "receivedAt",
		duration = "duration";
	}

	public static class HttpRequestRecordFielder extends BaseDatabeanFielder<HttpRequestRecordKey, HttpRequestRecord>{

		HttpRequestRecordFielder() {}

		@Override
		public Class<? extends Fielder<HttpRequestRecordKey>> getKeyFielderClass() {
			return HttpRequestRecordKey.class;
		}

		@Override
		public List<Field<?>> getNonKeyFields(HttpRequestRecord d) {
			return FieldTool.createList(
					new DateField(F.created, d.created),
					new LongDateField(F.receivedAt, d.receivedAt),
					new LongField(F.duration, d.duration),

					new StringField(F.exceptionRecordId, d.exceptionRecordId, MySqlColumnType.MAX_LENGTH_VARCHAR),
					new StringField(F.exceptionPlace, d.exceptionPlace, MySqlColumnType.MAX_LENGTH_VARCHAR),
					new StringField(F.methodName, d.methodName, MySqlColumnType.MAX_LENGTH_VARCHAR),
					new IntegerField(F.lineNumber, d.lineNumber),

					new StringField(F.httpMethod, d.httpMethod, LENGTH_httpMethod),
					new StringField(F.httpParams, d.httpParams, MySqlColumnType.MAX_LENGTH_MEDIUMTEXT),

					new StringField(F.protocol, d.protocol, LENGTH_protocol),
					new StringField(F.hostname, d.hostname, MySqlColumnType.MAX_LENGTH_VARCHAR),
					new IntegerField(F.port, d.port),
					new StringField(F.contextPath, d.contextPath, MySqlColumnType.MAX_LENGTH_VARCHAR),
					new StringField(F.path, d.path, MySqlColumnType.MAX_LENGTH_VARCHAR),
					new StringField(F.queryString, d.queryString, MySqlColumnType.MAX_LENGTH_MEDIUMTEXT),

					new StringField(F.ip, d.ip, LENGTH_ip),
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
					new StringField(F.referer, d.referer, MySqlColumnType.INT_LENGTH_LONGTEXT),
					new StringField(F.userAgent, d.userAgent, MySqlColumnType.MAX_LENGTH_MEDIUMTEXT),
					new StringField(F.xForwardedFor, d.xForwardedFor, MySqlColumnType.MAX_LENGTH_VARCHAR),
					new StringField(F.xRequestedWith, d.xRequestedWith, MySqlColumnType.MAX_LENGTH_VARCHAR),
					new StringField(F.others, d.otherHeaders, MySqlColumnType.MAX_LENGTH_MEDIUMTEXT)
					);
		}

		@Override
		public Map<String,List<Field<?>>> getIndexes(HttpRequestRecord databean){
			Map<String,List<Field<?>>> indexes = new TreeMap<>();
			indexes.put("index_exceptionRecord", new HttpRequestRecordByExceptionRecord().getFields());
			return indexes;
		}
	}

	/********************** construct ********************/

	HttpRequestRecord() {
		key = new HttpRequestRecordKey();
	}

	public HttpRequestRecord(Date receivedAt, String exceptionRecordId, String exceptionPlace,
			String methodName, int lineNumber, HttpServletRequest request, String sessionRoles, Long userId){
		this(
				receivedAt,
				exceptionRecordId,
				exceptionPlace,
				methodName,
				lineNumber,
				request.getMethod(),
				null,
				request.getScheme(),
				request.getServerName(),
				request.getServerPort(),
				request.getContextPath(),
				request.getRequestURI().substring(request.getContextPath().length()),
				request.getQueryString(),
				RequestTool.getIpAddress(request),
				sessionRoles,
				userId,
				new HttpHeaders(request)
				);
		
		StringBuilder paramStringBuilder = new StringBuilder();
		Joiner listJoiner = Joiner.on("; ");
		for (Entry<String, String[]> param : request.getParameterMap().entrySet()) {
			paramStringBuilder.append(param.getKey());
			paramStringBuilder.append(": ");
			paramStringBuilder.append(listJoiner.join(param.getValue()));
			paramStringBuilder.append(", ");
		}
		String paramString = paramStringBuilder.toString();
		this.httpParams = paramString;
	}
	
	private HttpRequestRecord(Date receivedAt, String exceptionRecordId, String exceptionPlace,
			String methodName, int lineNumber, String httpMethod, String httpParams, String protocol, String hostname,
			int port, String contextPath, String path, String queryString, String ip, String sessionRoles, Long userId,
			HttpHeaders headersWrapper){
	this.key = new HttpRequestRecordKey(UuidTool.generateV1Uuid());
		this.created = new Date();
		this.receivedAt = receivedAt;
		this.duration = created.getTime() - receivedAt.getTime();

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

		this.otherHeaders = headersWrapper.getOthers();
	}

	@Override
	public Class<HttpRequestRecordKey> getKeyClass() {
		return HttpRequestRecordKey.class;
	}

	/********************************Lookup*************************************/
	@SuppressWarnings("serial")
	public static class HttpRequestRecordByExceptionRecord extends BaseLookup<HttpRequestRecordKey> implements UniqueKey<HttpRequestRecordKey> {

		private String exceptionRecordId;

		private HttpRequestRecordByExceptionRecord(){}
		
		public HttpRequestRecordByExceptionRecord(ExceptionRecord exceptionRecord) {
			this.exceptionRecordId = exceptionRecord.getKey().getId();
		}

		@Override
		public List<Field<?>> getFields() {
			return FieldTool.createList(
					new StringField(F.exceptionRecordId, exceptionRecordId, MySqlColumnType.MAX_LENGTH_VARCHAR)
					);
		}
	}

	/******* tools *****/
	public Map<String, String> getHeaders() {
		Map<String, String> map = MapTool.createTreeMap();
		map.put(ACCEPT_CHARSET, acceptCharset);
		map.put(ACCEPT_ENCODING, acceptEncoding);
		map.put(ACCEPT_LANGUAGE, acceptLanguage);
		map.put(ACCEPT, accept);
		map.put(CACHE_CONTROL, cacheControl);
		map.put(CONNECTION, connection);
		map.put(CONTENT_ENCODING, contentEncoding);
		map.put(CONTENT_LANGUAGE, contentLanguage);
		map.put(CONTENT_LENGTH, contentLength);
		map.put(CONTENT_TYPE, contentType);
		map.put(DNT, dnt);
		map.put(HOST, host);
		map.put(IF_MODIFIED_SINCE, ifModifiedSince);
		map.put(ORIGIN, origin);
		map.put(PRAGMA, pragma);
		map.put(REFERER, referer);
		map.put(USER_AGENT, userAgent);
		map.put(X_FORWARDED_FOR, xForwardedFor);
		map.put(X_REQUESTED_WITH, xRequestedWith);
		return map;
	}

	public Map<String, String> getOtherHeadersMap() {
		return MapTool.getMapFromString(otherHeaders, ", ", ": ");
	}

	public Map<String, String> getHttpParamsMap() {
		return MapTool.getMapFromString(httpParams, ", ", ": ");
	}

	public Map<String, String> getCookiesMap() {
		return MapTool.getMapFromString(cookie, "; ", "=");
	}

	public boolean isFromAjax() {
		return "XMLHttpRequest".equals(xRequestedWith);
	}

	public String getUrl() {
		return getProtocol()+ "://" + hostname + ":" + port + contextPath + path + (queryString != null ? "?" + queryString : "");
	}

	public static HttpRequestRecord createEmptyForTesting(){
		return new HttpRequestRecord(null, null, null, null, 0, null, null, null, null, 0, null, null, null, null,
				null, 0l, new HttpHeaders(null));
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

	public String getOtherHeaders() {
		return otherHeaders;
	}

	public void setOtherHeaders(String otherHeaders) {
		this.otherHeaders = otherHeaders;
	}

	public Long getDuration(){
		return duration;
	}

	public Date getReceivedAt(){
		return receivedAt;
	}

}

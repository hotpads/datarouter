package com.hotpads.exception.analysis;

import static com.hotpads.util.http.HttpHeaders.ACCEPT;
import static com.hotpads.util.http.HttpHeaders.ACCEPT_CHARSET;
import static com.hotpads.util.http.HttpHeaders.ACCEPT_ENCODING;
import static com.hotpads.util.http.HttpHeaders.ACCEPT_LANGUAGE;
import static com.hotpads.util.http.HttpHeaders.CACHE_CONTROL;
import static com.hotpads.util.http.HttpHeaders.CONNECTION;
import static com.hotpads.util.http.HttpHeaders.CONTENT_ENCODING;
import static com.hotpads.util.http.HttpHeaders.CONTENT_LANGUAGE;
import static com.hotpads.util.http.HttpHeaders.CONTENT_LENGTH;
import static com.hotpads.util.http.HttpHeaders.CONTENT_TYPE;
import static com.hotpads.util.http.HttpHeaders.DNT;
import static com.hotpads.util.http.HttpHeaders.HOST;
import static com.hotpads.util.http.HttpHeaders.IF_MODIFIED_SINCE;
import static com.hotpads.util.http.HttpHeaders.ORIGIN;
import static com.hotpads.util.http.HttpHeaders.PRAGMA;
import static com.hotpads.util.http.HttpHeaders.REFERER;
import static com.hotpads.util.http.HttpHeaders.USER_AGENT;
import static com.hotpads.util.http.HttpHeaders.X_FORWARDED_FOR;
import static com.hotpads.util.http.HttpHeaders.X_REQUESTED_WITH;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hotpads.datarouter.client.imp.jdbc.ddl.domain.MySqlColumnType;
import com.hotpads.datarouter.serialize.fielder.BaseDatabeanFielder;
import com.hotpads.datarouter.storage.databean.BaseDatabean;
import com.hotpads.datarouter.storage.field.Field;
import com.hotpads.datarouter.storage.field.imp.DateField;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.array.ByteArrayField;
import com.hotpads.datarouter.storage.field.imp.array.ByteArrayFieldKey;
import com.hotpads.datarouter.storage.field.imp.comparable.IntegerField;
import com.hotpads.datarouter.storage.field.imp.comparable.LongField;
import com.hotpads.datarouter.storage.field.imp.custom.LongDateField;
import com.hotpads.datarouter.storage.key.multi.BaseLookup;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.datarouter.util.UuidTool;
import com.hotpads.datarouter.util.core.DrMapTool;
import com.hotpads.handler.exception.ExceptionRecord;
import com.hotpads.util.http.HttpHeaders;
import com.hotpads.util.http.RequestTool;

public class HttpRequestRecord extends BaseDatabean<HttpRequestRecordKey, HttpRequestRecord>{

	private static final Gson gson = new Gson();

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
	private byte[] binaryBody;

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

	private static class FieldKeys{
		private static final ByteArrayFieldKey binaryBody = new ByteArrayFieldKey("binaryBody")
				.withSize(MySqlColumnType.MAX_LENGTH_LONGBLOB);
	}

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
		body = "body",

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

		public HttpRequestRecordFielder() {
			super(HttpRequestRecordKey.class);
		}

		@Override
		public List<Field<?>> getNonKeyFields(HttpRequestRecord record){
			return Arrays.asList(
					new DateField(F.created, record.created),
					new LongDateField(F.receivedAt, record.receivedAt),
					new LongField(F.duration, record.duration),

					new StringField(F.exceptionRecordId, record.exceptionRecordId, MySqlColumnType.MAX_LENGTH_VARCHAR),
					new StringField(F.exceptionPlace, record.exceptionPlace, MySqlColumnType.MAX_LENGTH_VARCHAR),
					new StringField(F.methodName, record.methodName, MySqlColumnType.MAX_LENGTH_VARCHAR),
					new IntegerField(F.lineNumber, record.lineNumber),

					new StringField(F.httpMethod, record.httpMethod, LENGTH_httpMethod),
					new StringField(F.httpParams, record.httpParams, MySqlColumnType.INT_LENGTH_LONGTEXT),

					new StringField(F.protocol, record.protocol, LENGTH_protocol),
					new StringField(F.hostname, record.hostname, MySqlColumnType.MAX_LENGTH_VARCHAR),
					new IntegerField(F.port, record.port),
					new StringField(F.contextPath, record.contextPath, MySqlColumnType.MAX_LENGTH_VARCHAR),
					new StringField(F.path, record.path, MySqlColumnType.MAX_LENGTH_VARCHAR),
					new StringField(F.queryString, record.queryString, MySqlColumnType.INT_LENGTH_LONGTEXT),
					new ByteArrayField(FieldKeys.binaryBody, record.binaryBody),

					new StringField(F.ip, record.ip, LENGTH_ip),
					new StringField(F.userRoles, record.userRoles, MySqlColumnType.MAX_LENGTH_VARCHAR),
					new LongField(F.userId, record.userId),

					new StringField(F.acceptCharset, record.acceptCharset, MySqlColumnType.MAX_LENGTH_VARCHAR),
					new StringField(F.acceptEncoding, record.acceptEncoding, MySqlColumnType.MAX_LENGTH_VARCHAR),
					new StringField(F.acceptLanguage, record.acceptLanguage, MySqlColumnType.MAX_LENGTH_VARCHAR),
					new StringField(F.accept, record.accept, MySqlColumnType.MAX_LENGTH_VARCHAR),
					new StringField(F.cacheControl, record.cacheControl, MySqlColumnType.MAX_LENGTH_VARCHAR),
					new StringField(F.connection, record.connection, MySqlColumnType.MAX_LENGTH_VARCHAR),
					new StringField(F.contentEncoding, record.contentEncoding, MySqlColumnType.MAX_LENGTH_VARCHAR),
					new StringField(F.contentLanguage, record.contentLanguage, MySqlColumnType.MAX_LENGTH_VARCHAR),
					new StringField(F.contentLength, record.contentLength, MySqlColumnType.MAX_LENGTH_VARCHAR),
					new StringField(F.contentType, record.contentType, MySqlColumnType.MAX_LENGTH_VARCHAR),
					new StringField(F.cookie, record.cookie, MySqlColumnType.INT_LENGTH_LONGTEXT),
					new StringField(F.dnt, record.dnt, MySqlColumnType.MAX_LENGTH_VARCHAR),
					new StringField(F.host, record.host, MySqlColumnType.MAX_LENGTH_VARCHAR),
					new StringField(F.ifModifiedSince, record.ifModifiedSince, MySqlColumnType.MAX_LENGTH_VARCHAR),
					new StringField(F.origin, record.origin, MySqlColumnType.MAX_LENGTH_VARCHAR),
					new StringField(F.pragma, record.pragma, MySqlColumnType.MAX_LENGTH_VARCHAR),
					new StringField(F.referer, record.referer, MySqlColumnType.INT_LENGTH_LONGTEXT),
					new StringField(F.userAgent, record.userAgent, MySqlColumnType.INT_LENGTH_LONGTEXT),
					new StringField(F.xForwardedFor, record.xForwardedFor, MySqlColumnType.MAX_LENGTH_VARCHAR),
					new StringField(F.xRequestedWith, record.xRequestedWith, MySqlColumnType.MAX_LENGTH_VARCHAR),
					new StringField(F.others, record.otherHeaders, MySqlColumnType.INT_LENGTH_LONGTEXT)
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
				gson.toJson(request.getParameterMap()),
				request.getScheme(),
				request.getServerName(),
				request.getServerPort(),
				request.getContextPath(),
				request.getRequestURI().substring(request.getContextPath().length()),
				request.getQueryString(),
				RequestTool.tryGetBodyAsByteArray(request),
				RequestTool.getIpAddress(request),
				sessionRoles,
				userId,
				new HttpHeaders(request)
				);
	}

	private HttpRequestRecord(Date receivedAt, String exceptionRecordId, String exceptionPlace, String methodName,
			int lineNumber, String httpMethod, String httpParams, String protocol, String hostname, int port,
			String contextPath, String path, String queryString, byte[] binaryBody, String ip, String sessionRoles,
			Long userId, HttpHeaders headersWrapper){
		this.key = new HttpRequestRecordKey(UuidTool.generateV1Uuid());
		this.created = new Date();
		this.receivedAt = receivedAt;
		if(receivedAt != null) {
			this.duration = created.getTime() - receivedAt.getTime();
		}

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
		this.binaryBody = binaryBody;

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

	public HttpRequestRecord(ExceptionDto exceptionDto, String exceptionRecordId){
		this.key = new HttpRequestRecordKey(UuidTool.generateV1Uuid());
		this.created = new Date(exceptionDto.dateMs);
		this.receivedAt = exceptionDto.receivedAt;
		if(receivedAt != null) {
			this.duration = created.getTime() - receivedAt.getTime();
		}

		this.exceptionRecordId = exceptionRecordId;
		this.exceptionPlace = exceptionDto.errorLocation;
		this.methodName = exceptionDto.methodName;
		this.lineNumber = exceptionDto.lineNumber;

		this.httpMethod = exceptionDto.httpMethod;
		this.httpParams = gson.toJson(exceptionDto.httpParams);

		this.protocol = exceptionDto.protocol;
		this.hostname = exceptionDto.hostname;
		this.port = exceptionDto.port;
		this.path = exceptionDto.path;
		this.queryString = exceptionDto.queryString;
		if(exceptionDto.body != null){
			this.binaryBody = exceptionDto.body.getBytes();
		}

		this.ip = exceptionDto.ip;
		this.userRoles = exceptionDto.userRoles;
		this.userId = exceptionDto.userId;

		this.acceptCharset = exceptionDto.acceptCharset;
		this.acceptEncoding = exceptionDto.acceptEncoding;
		this.acceptLanguage = exceptionDto.acceptLanguage;
		this.accept = exceptionDto.accept;
		this.cacheControl = exceptionDto.cacheControl;
		this.connection = exceptionDto.connection;
		this.contentEncoding = exceptionDto.contentEncoding;
		this.contentLanguage = exceptionDto.contentLanguage;
		this.contentLength = exceptionDto.contentLength;
		this.contentType = exceptionDto.contentType;
		this.cookie = exceptionDto.cookie;
		this.dnt = exceptionDto.dnt;
		this.host = exceptionDto.host;
		this.ifModifiedSince = exceptionDto.ifModifiedSince;
		this.origin = exceptionDto.origin;
		this.pragma = exceptionDto.pragma;
		this.referer = exceptionDto.referer;
		this.userAgent = exceptionDto.userAgent;
		this.xForwardedFor = exceptionDto.forwardedFor;
		this.xRequestedWith = exceptionDto.requestedWith;

		this.otherHeaders = gson.toJson(exceptionDto.others);
	}

	@Override
	public Class<HttpRequestRecordKey> getKeyClass() {
		return HttpRequestRecordKey.class;
	}

	/********************************Lookup*************************************/
	@SuppressWarnings("serial")
	public static class HttpRequestRecordByExceptionRecord extends BaseLookup<HttpRequestRecordKey>
	implements UniqueKey<HttpRequestRecordKey> {

		private String exceptionRecordId;

		private HttpRequestRecordByExceptionRecord(){}

		public HttpRequestRecordByExceptionRecord(ExceptionRecord exceptionRecord) {
			this.exceptionRecordId = exceptionRecord.getKey().getId();
		}

		@Override
		public List<Field<?>> getFields() {
			return Arrays.asList(
					new StringField(F.exceptionRecordId, exceptionRecordId, MySqlColumnType.MAX_LENGTH_VARCHAR));
		}
	}

	/******* tools *****/
	public Map<String, String> getHeaders() {
		Map<String, String> map = new TreeMap<>();
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

	public Map<String, String[]> getOtherHeadersMap() {
		return gson.fromJson(otherHeaders, new TypeToken<Map<String, String[]>>(){}.getType());
	}

	public Map<String, String[]> getHttpParamsMap() {
		return gson.fromJson(httpParams, new TypeToken<Map<String, String[]>>(){}.getType());
	}

	public Map<String, String> getCookiesMap() {
		return DrMapTool.getMapFromString(cookie, "; ", "=");
	}

	public boolean isFromAjax() {
		return "XMLHttpRequest".equals(xRequestedWith);
	}

	public String getUrl(){
		return getProtocol() + "://" + hostname + ":" + port + (contextPath == null ? "" : contextPath) + path
				+ (queryString != null ? "?" + queryString : "");
	}

	public static HttpRequestRecord createEmptyForTesting(){
		return new HttpRequestRecord(null, null, null, null, 0, null, null, null, null, 0, null, null, null, null,
				null, null, 0L, new HttpHeaders(null));
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

	public String getStringBody(){
		if(binaryBody != null){
			return new String(binaryBody);
		}
		return null;
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

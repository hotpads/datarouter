package com.hotpads.datarouter.monitoring.exception;

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
import com.hotpads.datarouter.storage.field.imp.DateFieldKey;
import com.hotpads.datarouter.storage.field.imp.StringField;
import com.hotpads.datarouter.storage.field.imp.StringFieldKey;
import com.hotpads.datarouter.storage.field.imp.array.ByteArrayField;
import com.hotpads.datarouter.storage.field.imp.array.ByteArrayFieldKey;
import com.hotpads.datarouter.storage.field.imp.comparable.IntegerField;
import com.hotpads.datarouter.storage.field.imp.comparable.IntegerFieldKey;
import com.hotpads.datarouter.storage.field.imp.comparable.LongField;
import com.hotpads.datarouter.storage.field.imp.comparable.LongFieldKey;
import com.hotpads.datarouter.storage.field.imp.custom.LongDateField;
import com.hotpads.datarouter.storage.field.imp.custom.LongDateFieldKey;
import com.hotpads.datarouter.storage.key.multi.BaseLookup;
import com.hotpads.datarouter.storage.key.unique.UniqueKey;
import com.hotpads.datarouter.util.UuidTool;
import com.hotpads.datarouter.util.core.DrMapTool;
import com.hotpads.handler.exception.ExceptionRecord;
import com.hotpads.handler.exception.ExceptionRecordKey;
import com.hotpads.util.http.HttpHeaders;
import com.hotpads.util.http.RequestTool;

public class HttpRequestRecord extends BaseDatabean<HttpRequestRecordKey, HttpRequestRecord>{

	private static final Gson gson = new Gson();

	private HttpRequestRecordKey key;
	private Date created;
	private Date receivedAt;
	private Long duration;

	private String exceptionRecordId;
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

	public static class FieldKeys{
		public static final DateFieldKey created = new DateFieldKey("created");
		public static final LongDateFieldKey receivedAt = new LongDateFieldKey("receivedAt");
		public static final LongFieldKey duration = new LongFieldKey("duration");
		public static final StringFieldKey exceptionRecordId = new StringFieldKey("exceptionRecordId");
		public static final StringFieldKey methodName = new StringFieldKey("methodName");
		public static final IntegerFieldKey lineNumber = new IntegerFieldKey("lineNumber");
		public static final StringFieldKey httpMethod = new StringFieldKey("httpMethod").withSize(16);
		public static final StringFieldKey httpParams = new StringFieldKey("httpParams")
				.withSize(MySqlColumnType.INT_LENGTH_LONGTEXT);
		public static final StringFieldKey protocol = new StringFieldKey("protocol").withSize(5);
		public static final StringFieldKey hostname = new StringFieldKey("hostname");
		public static final IntegerFieldKey port = new IntegerFieldKey("port");
		public static final StringFieldKey contextPath = new StringFieldKey("contextPath");
		public static final StringFieldKey path = new StringFieldKey("path");
		public static final StringFieldKey queryString = new StringFieldKey("queryString")
				.withSize(MySqlColumnType.INT_LENGTH_LONGTEXT);
		public static final ByteArrayFieldKey binaryBody = new ByteArrayFieldKey("binaryBody")
				.withSize(MySqlColumnType.MAX_LENGTH_LONGBLOB);
		public static final StringFieldKey ip = new StringFieldKey("ip").withSize(39);
		public static final StringFieldKey userRoles = new StringFieldKey("userRoles");
		public static final LongFieldKey userId = new LongFieldKey("userId");
		public static final StringFieldKey acceptCharset = new StringFieldKey("acceptCharset");
		public static final StringFieldKey acceptEncoding = new StringFieldKey("acceptEncoding");
		public static final StringFieldKey acceptLanguage = new StringFieldKey("acceptLanguage");
		public static final StringFieldKey accept = new StringFieldKey("accept");
		public static final StringFieldKey cacheControl = new StringFieldKey("cacheControl");
		public static final StringFieldKey connection = new StringFieldKey("connection");
		public static final StringFieldKey contentEncoding = new StringFieldKey("contentEncoding");
		public static final StringFieldKey contentLanguage = new StringFieldKey("contentLanguage");
		public static final StringFieldKey contentLength = new StringFieldKey("contentLength");
		public static final StringFieldKey contentType = new StringFieldKey("contentType");
		public static final StringFieldKey cookie = new StringFieldKey("cookie")
				.withSize(MySqlColumnType.INT_LENGTH_LONGTEXT);
		public static final StringFieldKey dnt = new StringFieldKey("dnt");
		public static final StringFieldKey host = new StringFieldKey("host");
		public static final StringFieldKey ifModifiedSince = new StringFieldKey("ifModifiedSince");
		public static final StringFieldKey origin = new StringFieldKey("origin");
		public static final StringFieldKey pragma = new StringFieldKey("pragma");
		public static final StringFieldKey referer = new StringFieldKey("referer")
				.withSize(MySqlColumnType.INT_LENGTH_LONGTEXT);
		public static final StringFieldKey userAgent = new StringFieldKey("userAgent")
				.withSize(MySqlColumnType.INT_LENGTH_LONGTEXT);
		public static final StringFieldKey xForwardedFor = new StringFieldKey("xForwardedFor");
		public static final StringFieldKey xRequestedWith = new StringFieldKey("xRequestedWith");
		public static final StringFieldKey otherHeaders = new StringFieldKey("otherHeaders")
				.withSize(MySqlColumnType.INT_LENGTH_LONGTEXT);
	}

	public static class HttpRequestRecordFielder extends BaseDatabeanFielder<HttpRequestRecordKey, HttpRequestRecord>{

		public HttpRequestRecordFielder(){
			super(HttpRequestRecordKey.class);
		}

		@Override
		public List<Field<?>> getNonKeyFields(HttpRequestRecord record){
			return Arrays.asList(
					new DateField(FieldKeys.created, record.created),
					new LongDateField(FieldKeys.receivedAt, record.receivedAt),
					new LongField(FieldKeys.duration, record.duration),

					new StringField(FieldKeys.exceptionRecordId, record.exceptionRecordId),
					new StringField(FieldKeys.methodName, record.methodName),
					new IntegerField(FieldKeys.lineNumber, record.lineNumber),

					new StringField(FieldKeys.httpMethod, record.httpMethod),
					new StringField(FieldKeys.httpParams, record.httpParams),

					new StringField(FieldKeys.protocol, record.protocol),
					new StringField(FieldKeys.hostname, record.hostname),
					new IntegerField(FieldKeys.port, record.port),
					new StringField(FieldKeys.contextPath, record.contextPath),
					new StringField(FieldKeys.path, record.path),
					new StringField(FieldKeys.queryString, record.queryString),
					new ByteArrayField(FieldKeys.binaryBody, record.binaryBody),

					new StringField(FieldKeys.ip, record.ip),
					new StringField(FieldKeys.userRoles, record.userRoles),
					new LongField(FieldKeys.userId, record.userId),

					new StringField(FieldKeys.acceptCharset, record.acceptCharset),
					new StringField(FieldKeys.acceptEncoding, record.acceptEncoding),
					new StringField(FieldKeys.acceptLanguage, record.acceptLanguage),
					new StringField(FieldKeys.accept, record.accept),
					new StringField(FieldKeys.cacheControl, record.cacheControl),
					new StringField(FieldKeys.connection, record.connection),
					new StringField(FieldKeys.contentEncoding, record.contentEncoding),
					new StringField(FieldKeys.contentLanguage, record.contentLanguage),
					new StringField(FieldKeys.contentLength, record.contentLength),
					new StringField(FieldKeys.contentType, record.contentType),
					new StringField(FieldKeys.cookie, record.cookie),
					new StringField(FieldKeys.dnt, record.dnt),
					new StringField(FieldKeys.host, record.host),
					new StringField(FieldKeys.ifModifiedSince, record.ifModifiedSince),
					new StringField(FieldKeys.origin, record.origin),
					new StringField(FieldKeys.pragma, record.pragma),
					new StringField(FieldKeys.referer, record.referer),
					new StringField(FieldKeys.userAgent, record.userAgent),
					new StringField(FieldKeys.xForwardedFor, record.xForwardedFor),
					new StringField(FieldKeys.xRequestedWith, record.xRequestedWith),
					new StringField(FieldKeys.otherHeaders, record.otherHeaders));
		}

		@Override
		public Map<String,List<Field<?>>> getIndexes(HttpRequestRecord databean){
			Map<String,List<Field<?>>> indexes = new TreeMap<>();
			indexes.put("index_exceptionRecord", new HttpRequestRecordByExceptionRecord().getFields());
			return indexes;
		}

	}

	/********************** construct ********************/

	public HttpRequestRecord(){
		this.key = new HttpRequestRecordKey();
	}

	public HttpRequestRecord(Date receivedAt, String exceptionRecordId, String methodName, int lineNumber,
			HttpServletRequest request, String sessionRoles, Long userId){
		this(
				receivedAt,
				exceptionRecordId,
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
				new HttpHeaders(request));
	}

	private HttpRequestRecord(Date receivedAt, String exceptionRecordId, String methodName, int lineNumber,
			String httpMethod, String httpParams, String protocol, String hostname, int port, String contextPath,
			String path, String queryString, byte[] binaryBody, String ip, String sessionRoles, Long userId,
			HttpHeaders headersWrapper){
		this.key = new HttpRequestRecordKey(UuidTool.generateV1Uuid());
		this.created = new Date();
		this.receivedAt = receivedAt;
		if(receivedAt != null){
			this.duration = created.getTime() - receivedAt.getTime();
		}

		this.exceptionRecordId = exceptionRecordId;
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
		if(receivedAt != null){
			this.duration = created.getTime() - receivedAt.getTime();
		}

		this.exceptionRecordId = exceptionRecordId;
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
	public Class<HttpRequestRecordKey> getKeyClass(){
		return HttpRequestRecordKey.class;
	}

	/********************************Lookup*************************************/
	public static class HttpRequestRecordByExceptionRecord extends BaseLookup<HttpRequestRecordKey>
	implements UniqueKey<HttpRequestRecordKey>{

		private String exceptionRecordId;

		private HttpRequestRecordByExceptionRecord(){
		}

		public HttpRequestRecordByExceptionRecord(ExceptionRecord exceptionRecord){
			this.exceptionRecordId = exceptionRecord.getKey().getId();
		}

		@Override
		public List<Field<?>> getFields(){
			return Arrays.asList(
					new StringField(FieldKeys.exceptionRecordId, exceptionRecordId));
		}
	}

	/******* tools *****/
	public Map<String,String> getHeaders(){
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

	public Map<String,String[]> getOtherHeadersMap(){
		return gson.fromJson(otherHeaders, new TypeToken<Map<String,String[]>>(){}.getType());
	}

	public Map<String,String[]> getHttpParamsMap(){
		return gson.fromJson(httpParams, new TypeToken<Map<String,String[]>>(){}.getType());
	}

	public Map<String,String> getCookiesMap(){
		return DrMapTool.getMapFromString(cookie, "; ", "=");
	}

	public boolean isFromAjax(){
		return "XMLHttpRequest".equals(xRequestedWith);
	}

	public String getUrl(){
		return getProtocol() + "://" + hostname + ":" + port + (contextPath == null ? "" : contextPath) + path
				+ (queryString != null ? "?" + queryString : "");
	}

	public static HttpRequestRecord createEmptyForTesting(){
		return new HttpRequestRecord(null, null, null, 0, null, null, null, null, 0, null, null, null, null, null, null,
				0L, new HttpHeaders(null));
	}

	/*************** getters / setters ******************/
	@Override
	public HttpRequestRecordKey getKey(){
		return key;
	}

	public Date getCreated(){
		return created;
	}

	public void setCreated(Date created){
		this.created = created;
	}

	public ExceptionRecordKey getExceptionRecordKey(){
		return new ExceptionRecordKey(exceptionRecordId);
	}

	public String getExceptionRecordId(){
		return exceptionRecordId;
	}

	public void setExceptionRecordId(String exceptionRecordId){
		this.exceptionRecordId = exceptionRecordId;
	}

	public String getMethodName(){
		return methodName;
	}

	public int getLineNumber(){
		return lineNumber;
	}

	public String getHttpMethod(){
		return httpMethod;
	}

	public String getHttpParams(){
		return httpParams;
	}

	public String getProtocol(){
		return protocol;
	}

	public String getHostname(){
		return hostname;
	}

	public int getPort(){
		return port;
	}

	public String getContextPath(){
		return contextPath;
	}

	public String getPath(){
		return path;
	}

	public String getQueryString(){
		return queryString;
	}

	public String getStringBody(){
		if(binaryBody != null){
			return new String(binaryBody);
		}
		return null;
	}

	public String getIp(){
		return ip;
	}

	public String getUserRoles(){
		return userRoles;
	}

	public String getShorterRoles(){
		return userRoles.substring(1, userRoles.length() - 1);
	}

	public Long getUserId(){
		return userId;
	}

	public String getAcceptCharset(){
		return acceptCharset;
	}

	public String getAcceptEncoding(){
		return acceptEncoding;
	}

	public String getAcceptLanguage(){
		return acceptLanguage;
	}

	public String getAccept(){
		return accept;
	}

	public String getCacheControl(){
		return cacheControl;
	}

	public String getConnection(){
		return connection;
	}

	public String getContentEncoding(){
		return contentEncoding;
	}

	public String getContentLanguage(){
		return contentLanguage;
	}

	public String getContentLength(){
		return contentLength;
	}

	public String getContentType(){
		return contentType;
	}

	public String getCookie(){
		return cookie;
	}

	public String getDnt(){
		return dnt;
	}

	public String getHost(){
		return host;
	}

	public String getIfModifiedSince(){
		return ifModifiedSince;
	}

	public String getOrigin(){
		return origin;
	}

	public String getPragma(){
		return pragma;
	}

	public String getReferer(){
		return referer;
	}

	public String getUserAgent(){
		return userAgent;
	}

	public String getxForwardedFor(){
		return xForwardedFor;
	}

	public String getxRequestedWith(){
		return xRequestedWith;
	}

	public String getOtherHeaders(){
		return otherHeaders;
	}

	public Long getDuration(){
		return duration;
	}

	public Date getReceivedAt(){
		return receivedAt;
	}

}

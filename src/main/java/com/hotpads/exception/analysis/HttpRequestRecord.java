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
import com.hotpads.datarouter.storage.field.imp.comparable.BooleanField;
import com.hotpads.datarouter.storage.field.imp.comparable.IntegerField;
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
	private String userAgent;
	private boolean fromAjax;
	private String httpReferer;
	private String cookies;
	private String sessionRoles;

	public static class F {
		public static String
			id = "id",
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

			ip="ip",
			userAgent="userAgent",
			fromAjax="fromAjax",
			httpReferer="httpReferer",
			cookies="cookies",
			sessionRoles="sessionRoles";
	}

	public static class HttpRequestRecordFielder extends BaseDatabeanFielder<HttpRequestRecordKey, HttpRequestRecord>{

		public HttpRequestRecordFielder() {}

		@Override
		public Class<? extends Fielder<HttpRequestRecordKey>> getKeyFielderClass() {
			return HttpRequestRecordKey.class;
		}

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
					new StringField(F.userAgent, d.userAgent, MySqlColumnType.MAX_LENGTH_MEDIUMTEXT),
					new BooleanField(F.fromAjax, d.fromAjax),
					new StringField(F.httpReferer, d.httpReferer, MySqlColumnType.MAX_LENGTH_VARCHAR),
					new StringField(F.cookies, d.cookies, MySqlColumnType.MAX_LENGTH_MEDIUMTEXT),
					new StringField(F.sessionRoles, d.sessionRoles, MySqlColumnType.MAX_LENGTH_VARCHAR)
					);
		}

	}

	/********************** construct ********************/

	HttpRequestRecord() {
		key = new HttpRequestRecordKey();
	}

	public HttpRequestRecord(String exceptionRecordId, String exceptionPlace, String methodName, int lineNumber,
			String httpMethod, String httpParams, String protocol, String hostname, int port, String contextPath,
			String path, String queryString, String ip, String userAgent, boolean fromAjax, String httpReferer,
			String cookies, String sessionRoles) {
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
		this.userAgent = userAgent;
		this.fromAjax = fromAjax;
		this.httpReferer = httpReferer;
		this.cookies = cookies;
		this.sessionRoles = sessionRoles;
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
		String[] tab = httpParams.substring(1, httpParams.length() - 1).split(",");
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

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public boolean isFromAjax() {
		return fromAjax;
	}

	public void setFromAjax(boolean fromAjax) {
		this.fromAjax = fromAjax;
	}

	public String getHttpReferer() {
		return httpReferer;
	}

	public void setHttpReferer(String httpReferer) {
		this.httpReferer = httpReferer;
	}

	public String getCookies() {
		return cookies;
	}
	
	public Map<String, String> getCokkiesMap() {
		String[] tab = cookies.substring(1, cookies.length() - 1).split(",");
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

	public void setCookies(String cookies) {
		this.cookies = cookies;
	}

	public String getSessionRoles() {
		return sessionRoles;
	}

	public String getSorterRoles() {
		return sessionRoles.substring(1, sessionRoles.length() - 1);
	}

	public void setSessionRoles(String sessionRoles) {
		this.sessionRoles = sessionRoles;
	}

	public String getUrl() {
		return getProtocol()+ "://" + hostname + ":" + port + contextPath + path + (queryString != null ? "?" + queryString : "");
	}
}

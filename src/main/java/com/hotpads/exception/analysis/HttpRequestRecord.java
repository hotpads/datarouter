package com.hotpads.exception.analysis;

import java.util.Date;
import java.util.List;

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
import com.hotpads.datarouter.util.UuidTool;

public class HttpRequestRecord extends BaseDatabean<HttpRequestRecordKey, HttpRequestRecord>{

	/******************* fields ************************/

	private HttpRequestRecordKey key;
	private Date created;
	
	private String exceptionRecordId;
	private String exceptionPlace;
	private String methodName;
	private int lineNumber;

	private String urlRequested;
	private String httpMethod;
	private String httpParams;

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
			lineNumber="lineNumber",

			urlRequested="urlRequested",
			httpMethod="httpMethod",
			httpParams="httpParams",

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

					new StringField(F.urlRequested, d.urlRequested, MySqlColumnType.MAX_LENGTH_MEDIUMTEXT),
					new StringField(F.httpMethod, d.httpMethod, 16),
					new StringField(F.httpParams, d.httpParams, MySqlColumnType.MAX_LENGTH_MEDIUMTEXT),

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
			String urlRequested, String httpMethod, String httpParams, String ip, String userAgent, boolean fromAjax,
			String httpReferer, String cookies, String sessionRoles) {
		this.key = new HttpRequestRecordKey(UuidTool.generateUuid());
		this.created = new Date();
		this.exceptionRecordId = exceptionRecordId;
		this.exceptionPlace = exceptionPlace;
		this.methodName = methodName;
		this.lineNumber = lineNumber;
		this.urlRequested = urlRequested;
		this.httpMethod = httpMethod;
		this.httpParams = httpParams;
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

	public String getUrlRequested() {
		return urlRequested;
	}

	public void setUrlRequested(String urlRequested) {
		this.urlRequested = urlRequested;
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

	public void setCookies(String cookies) {
		this.cookies = cookies;
	}

	public String getSessionRoles() {
		return sessionRoles;
	}

	public void setSessionRoles(String sessionRoles) {
		this.sessionRoles = sessionRoles;
	}

}

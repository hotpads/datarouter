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
package io.datarouter.exception.storage.httprecord;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.exception.storage.exceptionrecord.BaseExceptionRecord;
import io.datarouter.exception.storage.exceptionrecord.BaseExceptionRecordKey;
import io.datarouter.httpclient.HttpHeaders;
import io.datarouter.instrumentation.exception.HttpRequestRecordDto;
import io.datarouter.model.databean.BaseDatabean;
import io.datarouter.model.field.Field;
import io.datarouter.model.field.imp.DateField;
import io.datarouter.model.field.imp.DateFieldKey;
import io.datarouter.model.field.imp.StringField;
import io.datarouter.model.field.imp.StringFieldKey;
import io.datarouter.model.field.imp.array.ByteArrayField;
import io.datarouter.model.field.imp.array.ByteArrayFieldKey;
import io.datarouter.model.field.imp.comparable.IntegerField;
import io.datarouter.model.field.imp.comparable.IntegerFieldKey;
import io.datarouter.model.field.imp.comparable.LongField;
import io.datarouter.model.field.imp.comparable.LongFieldKey;
import io.datarouter.model.field.imp.custom.LongDateField;
import io.datarouter.model.field.imp.custom.LongDateFieldKey;
import io.datarouter.model.key.unique.BaseUniqueKey;
import io.datarouter.model.serialize.fielder.BaseDatabeanFielder;
import io.datarouter.model.serialize.fielder.Fielder;
import io.datarouter.model.util.CommonFieldSizes;
import io.datarouter.util.array.ArrayTool;
import io.datarouter.util.serialization.GsonTool;
import io.datarouter.util.string.StringTool;
import io.datarouter.web.monitoring.exception.ExceptionDto;
import io.datarouter.web.util.http.RecordedHttpHeaders;

public abstract class BaseHttpRequestRecord<
		PK extends BaseHttpRequestRecordKey<PK>,
		D extends BaseHttpRequestRecord<PK,D>>
extends BaseDatabean<PK,D>{
	private static final Logger logger = LoggerFactory.getLogger(BaseHttpRequestRecord.class);

	private Date created;
	private Date receivedAt;
	private Long duration;

	private String exceptionRecordId;
	private String traceId;
	private String parentId;

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
	private String userToken;

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
		public static final StringFieldKey traceId = new StringFieldKey("traceId");
		public static final StringFieldKey parentId = new StringFieldKey("parentId");
		public static final StringFieldKey httpMethod = new StringFieldKey("httpMethod").withSize(16);
		public static final StringFieldKey httpParams = new StringFieldKey("httpParams")
				.withSize(CommonFieldSizes.INT_LENGTH_LONGTEXT);
		public static final StringFieldKey protocol = new StringFieldKey("protocol").withSize(5);
		public static final StringFieldKey hostname = new StringFieldKey("hostname");
		public static final IntegerFieldKey port = new IntegerFieldKey("port");
		public static final StringFieldKey contextPath = new StringFieldKey("contextPath");
		public static final StringFieldKey path = new StringFieldKey("path");
		public static final StringFieldKey queryString = new StringFieldKey("queryString")
				.withSize(CommonFieldSizes.INT_LENGTH_LONGTEXT);
		public static final ByteArrayFieldKey binaryBody = new ByteArrayFieldKey("binaryBody")
				.withSize(CommonFieldSizes.MAX_LENGTH_LONGBLOB);
		public static final StringFieldKey ip = new StringFieldKey("ip").withSize(39);
		public static final StringFieldKey userRoles = new StringFieldKey("userRoles")
				.withSize(CommonFieldSizes.INT_LENGTH_LONGTEXT);
		public static final StringFieldKey userToken = new StringFieldKey("userToken");
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
				.withSize(CommonFieldSizes.INT_LENGTH_LONGTEXT);
		public static final StringFieldKey dnt = new StringFieldKey("dnt");
		public static final StringFieldKey host = new StringFieldKey("host");
		public static final StringFieldKey ifModifiedSince = new StringFieldKey("ifModifiedSince");
		public static final StringFieldKey origin = new StringFieldKey("origin");
		public static final StringFieldKey pragma = new StringFieldKey("pragma");
		public static final StringFieldKey referer = new StringFieldKey("referer")
				.withSize(CommonFieldSizes.INT_LENGTH_LONGTEXT);
		public static final StringFieldKey userAgent = new StringFieldKey("userAgent")
				.withSize(CommonFieldSizes.INT_LENGTH_LONGTEXT);
		public static final StringFieldKey xForwardedFor = new StringFieldKey("xForwardedFor");
		public static final StringFieldKey xRequestedWith = new StringFieldKey("xRequestedWith");
		public static final StringFieldKey otherHeaders = new StringFieldKey("otherHeaders")
				.withSize(CommonFieldSizes.INT_LENGTH_LONGTEXT);
	}

	public abstract static class BaseHttpRequestRecordFielder<
			PK extends BaseHttpRequestRecordKey<PK>,
			D extends BaseHttpRequestRecord<PK,D>>
	extends BaseDatabeanFielder<PK,D>{

		protected BaseHttpRequestRecordFielder(Class<? extends Fielder<PK>> primaryKeyFielderClass){
			super(primaryKeyFielderClass);
		}

		@Override
		public List<Field<?>> getNonKeyFields(D record){
			return List.of(
					new DateField(FieldKeys.created, record.getCreated()),
					new LongDateField(FieldKeys.receivedAt, record.getReceivedAt()),
					new LongField(FieldKeys.duration, record.getDuration()),

					new StringField(FieldKeys.exceptionRecordId, record.getExceptionRecordId()),
					new StringField(FieldKeys.traceId, record.getTraceId()),
					new StringField(FieldKeys.parentId, record.getParentId()),

					new StringField(FieldKeys.httpMethod, record.getHttpMethod()),
					new StringField(FieldKeys.httpParams, record.getHttpParams()),

					new StringField(FieldKeys.protocol, record.getProtocol()),
					new StringField(FieldKeys.hostname, record.getHostname()),
					new IntegerField(FieldKeys.port, record.getPort()),
					new StringField(FieldKeys.contextPath, record.getContextPath()),
					new StringField(FieldKeys.path, record.getPath()),
					new StringField(FieldKeys.queryString, record.getQueryString()),
					new ByteArrayField(FieldKeys.binaryBody, record.getBinaryBody()),

					new StringField(FieldKeys.ip, record.getIp()),
					new StringField(FieldKeys.userRoles, record.getUserRoles()),
					new StringField(FieldKeys.userToken, record.getUserToken()),

					new StringField(FieldKeys.acceptCharset, record.getAcceptCharset()),
					new StringField(FieldKeys.acceptEncoding, record.getAcceptEncoding()),
					new StringField(FieldKeys.acceptLanguage, record.getAcceptLanguage()),
					new StringField(FieldKeys.accept, record.getAccept()),
					new StringField(FieldKeys.cacheControl, record.getCacheControl()),
					new StringField(FieldKeys.connection, record.getConnection()),
					new StringField(FieldKeys.contentEncoding, record.getContentEncoding()),
					new StringField(FieldKeys.contentLanguage, record.getContentLanguage()),
					new StringField(FieldKeys.contentLength, record.getContentLength()),
					new StringField(FieldKeys.contentType, record.getContentType()),
					new StringField(FieldKeys.cookie, record.getCookie()),
					new StringField(FieldKeys.dnt, record.getDnt()),
					new StringField(FieldKeys.host, record.getHost()),
					new StringField(FieldKeys.ifModifiedSince, record.getIfModifiedSince()),
					new StringField(FieldKeys.origin, record.getOrigin()),
					new StringField(FieldKeys.pragma, record.getPragma()),
					new StringField(FieldKeys.referer, record.getReferer()),
					new StringField(FieldKeys.userAgent, record.getUserAgent()),
					new StringField(FieldKeys.xForwardedFor, record.getxForwardedFor()),
					new StringField(FieldKeys.xRequestedWith, record.getxRequestedWith()),

					new StringField(FieldKeys.otherHeaders, record.getOtherHeaders()));
		}

	}

	public BaseHttpRequestRecord(PK key){
		super(key);
	}

	public BaseHttpRequestRecord(
			PK key,
			Date receivedAt,
			String exceptionRecordId,
			String traceId,
			String parentId,
			String httpMethod,
			String httpParams,
			String protocol,
			String hostname,
			int port,
			String contextPath,
			String path,
			String queryString,
			byte[] binaryBody,
			String ip,
			String sessionRoles,
			String userToken,
			RecordedHttpHeaders headersWrapper){
		super(key);
		this.created = new Date();
		this.receivedAt = receivedAt;
		if(receivedAt != null){
			this.duration = created.getTime() - receivedAt.getTime();
		}

		this.exceptionRecordId = exceptionRecordId;
		this.traceId = traceId;
		this.parentId = parentId;

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
		this.userToken = userToken;

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

	public BaseHttpRequestRecord(PK key, HttpRequestRecordDto dto){
		super(key);
		this.created = dto.created;
		this.receivedAt = dto.receivedAt;
		this.duration = dto.duration;
		this.exceptionRecordId = dto.exceptionRecordId;
		this.traceId = dto.traceId;
		this.parentId = dto.parentId;
		this.httpMethod = dto.httpMethod;
		this.httpParams = dto.httpParams;
		this.protocol = dto.protocol;
		this.hostname = dto.hostname;
		this.port = dto.port;
		this.contextPath = dto.contextPath;
		this.path = dto.path;
		this.queryString = dto.queryString;
		this.binaryBody = dto.binaryBody;
		this.ip = dto.ip;
		this.userRoles = dto.userRoles;
		this.userToken = dto.userToken;
		this.acceptCharset = dto.acceptCharset;
		this.acceptEncoding = dto.acceptEncoding;
		this.acceptLanguage = dto.acceptLanguage;
		this.accept = dto.accept;
		this.cacheControl = dto.cacheControl;
		this.connection = dto.connection;
		this.contentEncoding = dto.contentEncoding;
		this.contentLanguage = dto.contentLanguage;
		this.contentLength = dto.contentLength;
		this.contentType = dto.contentType;
		this.cookie = dto.cookie;
		this.dnt = dto.dnt;
		this.host = dto.host;
		this.ifModifiedSince = dto.ifModifiedSince;
		this.origin = dto.origin;
		this.pragma = dto.pragma;
		this.referer = dto.referer;
		this.userAgent = dto.userAgent;
		this.xForwardedFor = dto.xForwardedFor;
		this.xRequestedWith = dto.xRequestedWith;
		this.otherHeaders = dto.otherHeaders;
	}

	public BaseHttpRequestRecord(PK key, ExceptionDto dto, String exceptionRecordId){
		super(key);
		this.created = new Date(dto.dateMs);
		if(dto.receivedAtMs != null){
			this.receivedAt = new Date(dto.receivedAtMs);
			this.duration = dto.dateMs - dto.receivedAtMs;
		}
		this.exceptionRecordId = exceptionRecordId;
		this.httpMethod = dto.httpMethod;
		this.httpParams = GsonTool.GSON.toJson(dto.httpParams);
		this.protocol = dto.protocol;
		this.hostname = dto.hostname;
		this.port = dto.port;
		this.path = dto.path;
		this.queryString = dto.queryString;
		this.binaryBody = dto.body == null ? null : dto.body.getBytes();
		this.ip = dto.ip;
		this.userRoles = dto.userRoles;
		this.userToken = dto.userToken;

		this.acceptCharset = dto.acceptCharset;
		this.acceptEncoding = dto.acceptEncoding;
		this.acceptLanguage = dto.acceptLanguage;
		this.accept = dto.accept;
		this.cacheControl = dto.cacheControl;
		this.connection = dto.connection;
		this.contentEncoding = dto.contentEncoding;
		this.contentLanguage = dto.contentLanguage;
		this.contentLength = dto.contentLength;
		this.contentType = dto.contentType;
		this.cookie = dto.cookie;
		this.dnt = dto.dnt;
		this.host = dto.host;
		this.ifModifiedSince = dto.ifModifiedSince;
		this.origin = dto.origin;
		this.pragma = dto.pragma;
		this.referer = dto.referer;
		this.userAgent = dto.userAgent;
		this.xForwardedFor = dto.forwardedFor;
		this.xRequestedWith = dto.requestedWith;

		this.otherHeaders = GsonTool.GSON.toJson(dto.others);
	}

	public abstract static class BaseHttpRequestRecordByExceptionRecord<
			PK extends BaseHttpRequestRecordKey<PK>,
			D extends BaseHttpRequestRecord<PK,D>,
			IK extends BaseExceptionRecordKey<IK>,
			I extends BaseExceptionRecord<IK,I>>
	extends BaseUniqueKey<PK>{

		private String exceptionRecordId;

		public BaseHttpRequestRecordByExceptionRecord(D httpRequestRecord){
			this.exceptionRecordId = httpRequestRecord.getExceptionRecordId();
		}

		public BaseHttpRequestRecordByExceptionRecord(I exceptionRecord){
			this.exceptionRecordId = exceptionRecord.getKey().getId();
		}

		@Override
		public List<Field<?>> getFields(){
			return List.of(new StringField(FieldKeys.exceptionRecordId, exceptionRecordId));
		}

	}

	public abstract static class BaseHttpRequestRecordByTraceContext<
			PK extends BaseHttpRequestRecordKey<PK>,
			D extends BaseHttpRequestRecord<PK,D>>
	extends BaseUniqueKey<PK>{

		private String traceId;
		private String parentId;

		public BaseHttpRequestRecordByTraceContext(D httpRequestRecord){
			this.traceId = httpRequestRecord.getTraceId();
			this.parentId = httpRequestRecord.getParentId();
		}

		public BaseHttpRequestRecordByTraceContext(String traceId, String parentId){
			this.traceId = traceId;
			this.parentId = parentId;
		}

		@Override
		public List<Field<?>> getFields(){
			return List.of(new StringField(FieldKeys.traceId, traceId),
					new StringField(FieldKeys.parentId, parentId));
		}

	}

 	public Map<String,String> getHeaders(){
		Map<String,String> map = new LinkedHashMap<>();
		map.put(HttpHeaders.ACCEPT_CHARSET, acceptCharset);
		map.put(HttpHeaders.ACCEPT_ENCODING, acceptEncoding);
		map.put(HttpHeaders.ACCEPT_LANGUAGE, acceptLanguage);
		map.put(HttpHeaders.ACCEPT, accept);
		map.put(HttpHeaders.CACHE_CONTROL, cacheControl);
		map.put(HttpHeaders.CONNECTION, connection);
		map.put(HttpHeaders.CONTENT_ENCODING, contentEncoding);
		map.put(HttpHeaders.CONTENT_LANGUAGE, contentLanguage);
		map.put(HttpHeaders.CONTENT_LENGTH, contentLength);
		map.put(HttpHeaders.CONTENT_TYPE, contentType);
		map.put(HttpHeaders.COOKIE, cookie);
		map.put(HttpHeaders.DNT, dnt);
		map.put(HttpHeaders.HOST, host);
		map.put(HttpHeaders.IF_MODIFIED_SINCE, ifModifiedSince);
		map.put(HttpHeaders.ORIGIN, origin);
		map.put(HttpHeaders.PRAGMA, pragma);
		map.put(HttpHeaders.REFERER, referer);
		map.put(HttpHeaders.USER_AGENT, userAgent);
		map.put(HttpHeaders.X_FORWARDED_FOR, xForwardedFor);
		map.put(HttpHeaders.X_REQUESTED_WITH, xRequestedWith);
		return map;
	}

	public Date getCreated(){
		return created;
	}

	public void setCreated(Date created){
		this.created = created;
	}


	public String getExceptionRecordId(){
		return exceptionRecordId;
	}

	public void setExceptionRecordId(String exceptionRecordId){
		this.exceptionRecordId = exceptionRecordId;
	}

	public String getTraceId(){
		return traceId;
	}

	public String getParentId(){
		return parentId;
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

	public String getUserToken(){
		return userToken;
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

	public byte[] getBinaryBody(){
		return binaryBody;
	}

	public void trimBinaryBody(int size){
		int originalLength = binaryBody.length;
		if(originalLength > size){
			binaryBody = ArrayTool.trimToSize(binaryBody, size);
			logger.warn("Trimmed binary body to {} from {} for sqs, exceptionRecordId={}", size, originalLength,
					exceptionRecordId);
		}
	}

	public void trimContentType(){
		contentType = trimField(FieldKeys.contentType, contentType);
	}

	public void trimAcceptCharset(){
		acceptCharset = trimField(FieldKeys.acceptCharset, acceptCharset);
	}

	public void trimXForwardedFor(){
		xForwardedFor = trimField(FieldKeys.xForwardedFor, xForwardedFor);
	}

	public void trimPath(){
		path = trimField(FieldKeys.path, path);
	}

	public void trimAcceptLanguage(){
		acceptLanguage = trimField(FieldKeys.acceptLanguage, acceptLanguage);
	}

	public void trimPragma(){
		pragma = trimField(FieldKeys.pragma, pragma);
	}

	private String trimField(StringFieldKey fieldKey, String field){
		if(field == null){
			return field;
		}
		int fieldSize = fieldKey.getSize();
		int fieldValueLength = field.length();
		if(fieldValueLength > fieldSize){
			logger.warn("Trimmed {} to {} from {}, exceptionRecordId={}", fieldKey.getName(), fieldSize,
					fieldValueLength, exceptionRecordId);
			return StringTool.trimToSize(field, fieldSize);
		}
		return field;
	}

}

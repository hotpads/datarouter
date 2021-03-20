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
package io.datarouter.instrumentation.exception;

import java.util.Date;

public class HttpRequestRecordDto{

	public static final byte[] CONFIDENTIALITY_MSG_BYTES = "body omitted for confidentiality".getBytes();
	public static final int BINARY_BODY_MAX_SIZE = 10_000;

	public final String id;
	public final Date created;
	public final Date receivedAt;
	public final Long duration;

	public final String exceptionRecordId;
	public final String traceId;
	public final String parentId;

	public final String httpMethod;
	public final String httpParams;

	public final String protocol;
	public final String hostname;
	public final int port;
	public final String contextPath;
	public final String path;
	public final String queryString;
	public final byte[] binaryBody;

	public final String ip;
	public final String userRoles;
	public final String userToken;

	public final String acceptCharset;
	public final String acceptEncoding;
	public final String acceptLanguage;
	public final String accept;
	public final String cacheControl;
	public final String connection;
	public final String contentEncoding;
	public final String contentLanguage;
	public final String contentLength;
	public final String contentType;
	public final String cookie;
	public final String dnt;
	public final String host;
	public final String ifModifiedSince;
	public final String origin;
	public final String pragma;
	public final String referer;
	public final String userAgent;
	public final String xForwardedFor;
	public final String xRequestedWith;
	public final String otherHeaders;

	public HttpRequestRecordDto(
			String id,
			Date created,
			Date receivedAt,
			Long duration,
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
			String userRoles,
			String userToken,
			String acceptCharset,
			String acceptEncoding,
			String acceptLanguage,
			String accept,
			String cacheControl,
			String connection,
			String contentEncoding,
			String contentLanguage,
			String contentLength,
			String contentType,
			String cookie,
			String dnt,
			String host,
			String ifModifiedSince,
			String origin,
			String pragma,
			String referer,
			String userAgent,
			String xForwardedFor,
			String xRequestedWith,
			String otherHeaders){
		this.id = id;
		this.created = created;
		this.receivedAt = receivedAt;
		this.duration = duration;

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
		this.userRoles = userRoles;
		this.userToken = userToken;
		this.acceptCharset = acceptCharset;
		this.acceptEncoding = acceptEncoding;
		this.acceptLanguage = acceptLanguage;
		this.accept = accept;
		this.cacheControl = cacheControl;
		this.connection = connection;
		this.contentEncoding = contentEncoding;
		this.contentLanguage = contentLanguage;
		this.contentLength = contentLength;
		this.contentType = contentType;
		this.cookie = cookie;
		this.dnt = dnt;
		this.host = host;
		this.ifModifiedSince = ifModifiedSince;
		this.origin = origin;
		this.pragma = pragma;
		this.referer = referer;
		this.userAgent = userAgent;
		this.xForwardedFor = xForwardedFor;
		this.xRequestedWith = xRequestedWith;
		this.otherHeaders = otherHeaders;
	}

}

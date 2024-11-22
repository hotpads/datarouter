/*
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
package io.datarouter.exception.dto;

import java.time.Instant;
import java.util.Date;

import io.datarouter.binarydto.codec.BinaryDtoIndexedCodec;
import io.datarouter.binarydto.dto.BinaryDto;
import io.datarouter.binarydto.dto.BinaryDtoField;
import io.datarouter.instrumentation.exception.HttpRequestRecordDto;
import io.datarouter.util.Require;

public class HttpRequestRecordBinaryDto
extends BinaryDto<HttpRequestRecordBinaryDto>
implements TaskExecutionRecordBinaryDto<HttpRequestRecordDto>{

	@BinaryDtoField(index = 0)
	public final String serviceName;
	@BinaryDtoField(index = 1)
	public final String id;
	@BinaryDtoField(index = 2)
	public final Instant created;
	@BinaryDtoField(index = 3)
	public final Instant receivedAt;
	@BinaryDtoField(index = 4)
	public final Long duration;
	@BinaryDtoField(index = 5)
	public final String exceptionRecordId;
	@BinaryDtoField(index = 6)
	public final String traceId;
	@BinaryDtoField(index = 7)
	public final String parentId;
	@BinaryDtoField(index = 8)
	public final String httpMethod;
	@BinaryDtoField(index = 9)
	public final String httpParams;
	@BinaryDtoField(index = 10)
	public final String protocol;
	@BinaryDtoField(index = 11)
	public final String hostname;
	@BinaryDtoField(index = 12)
	public final int port;
	@BinaryDtoField(index = 13)
	public final String contextPath;
	@BinaryDtoField(index = 14)
	public final String path;
	@BinaryDtoField(index = 15)
	public final String queryString;
	@BinaryDtoField(index = 16)
	public final byte[] binaryBody;
	@BinaryDtoField(index = 17)
	public final String ip;
	@BinaryDtoField(index = 18)
	public final String userRoles;
	@BinaryDtoField(index = 19)
	public final String userToken;
	@BinaryDtoField(index = 20)
	public final String acceptCharset;
	@BinaryDtoField(index = 21)
	public final String acceptEncoding;
	@BinaryDtoField(index = 22)
	public final String acceptLanguage;
	@BinaryDtoField(index = 23)
	public final String accept;
	@BinaryDtoField(index = 24)
	public final String cacheControl;
	@BinaryDtoField(index = 25)
	public final String connection;
	@BinaryDtoField(index = 26)
	public final String contentEncoding;
	@BinaryDtoField(index = 27)
	public final String contentLanguage;
	@BinaryDtoField(index = 28)
	public final String contentLength;
	@BinaryDtoField(index = 29)
	public final String contentType;
	@BinaryDtoField(index = 30)
	public final String cookie;
	@BinaryDtoField(index = 31)
	public final String dnt;
	@BinaryDtoField(index = 32)
	public final String host;
	@BinaryDtoField(index = 33)
	public final String ifModifiedSince;
	@BinaryDtoField(index = 34)
	public final String origin;
	@BinaryDtoField(index = 35)
	public final String pragma;
	@BinaryDtoField(index = 36)
	public final String referer;
	@BinaryDtoField(index = 37)
	public final String userAgent;
	@BinaryDtoField(index = 38)
	public final String xForwardedFor;
	@BinaryDtoField(index = 39)
	public final String xRequestedWith;
	@BinaryDtoField(index = 40)
	public final String otherHeaders;
	@BinaryDtoField(index = 41)
	public final String environment;

	public HttpRequestRecordBinaryDto(
			String serviceName,
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
			String otherHeaders,
			String environment){
		this.serviceName = serviceName;
		this.id = id;
		this.created = created == null ? null : created.toInstant();
		this.receivedAt = receivedAt == null ? null : receivedAt.toInstant();
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
		this.environment = environment;
	}

	public HttpRequestRecordBinaryDto(HttpRequestRecordDto record, String serviceName){
		this(
				Require.notBlank(serviceName),
				record.id(),
				record.created(),
				record.receivedAt(),
				record.duration(),
				record.exceptionRecordId(),
				record.traceId(),
				record.parentId(),
				record.httpMethod(),
				record.httpParams(),
				record.protocol(),
				record.hostname(),
				record.port(),
				record.contextPath(),
				record.path(),
				record.queryString(),
				record.binaryBody(),
				record.ip(),
				record.userRoles(),
				record.userToken(),
				record.acceptCharset(),
				record.acceptEncoding(),
				record.acceptLanguage(),
				record.accept(),
				record.cacheControl(),
				record.connection(),
				record.contentEncoding(),
				record.contentLanguage(),
				record.contentLength(),
				record.contentType(),
				record.cookie(),
				record.dnt(),
				record.host(),
				record.ifModifiedSince(),
				record.origin(),
				record.pragma(),
				record.referer(),
				record.userAgent(),
				record.xForwardedFor(),
				record.xRequestedWith(),
				record.otherHeaders(),
				record.environment());
	}

	@Override
	public HttpRequestRecordDto toDto(){
		return new HttpRequestRecordDto(
				id,
				created == null ? null : Date.from(created),
				receivedAt == null ? null : Date.from(receivedAt),
				duration,
				exceptionRecordId,
				traceId,
				parentId,
				httpMethod,
				httpParams,
				protocol,
				hostname,
				port,
				contextPath,
				path,
				queryString,
				binaryBody,
				ip,
				userRoles,
				userToken,
				acceptCharset,
				acceptEncoding,
				acceptLanguage,
				accept,
				cacheControl,
				connection,
				contentEncoding,
				contentLanguage,
				contentLength,
				contentType,
				cookie,
				dnt,
				host,
				ifModifiedSince,
				origin,
				pragma,
				referer,
				userAgent,
				xForwardedFor,
				xRequestedWith,
				otherHeaders,
				environment);
	}

	public static HttpRequestRecordBinaryDto decode(byte[] bytes){
		return BinaryDtoIndexedCodec.of(HttpRequestRecordBinaryDto.class).decode(bytes);
	}

	@Override
	public String getServiceName(){
		return serviceName;
	}

}

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
package io.datarouter.exception.storage.httprecord;

import java.util.Date;
import java.util.Optional;
import java.util.function.Supplier;

import javax.servlet.http.HttpServletRequest;

import io.datarouter.exception.storage.exceptionrecord.ExceptionRecordKey;
import io.datarouter.gson.GsonTool;
import io.datarouter.instrumentation.exception.HttpRequestRecordDto;
import io.datarouter.instrumentation.trace.W3TraceContext;
import io.datarouter.types.Ulid;
import io.datarouter.util.string.StringTool;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.monitoring.exception.ExceptionAndHttpRequestDto;
import io.datarouter.web.util.RequestAttributeTool;
import io.datarouter.web.util.http.RecordedHttpHeaders;
import io.datarouter.web.util.http.RequestTool;

public class HttpRequestRecord extends BaseHttpRequestRecord<HttpRequestRecordKey,HttpRequestRecord>{

	public static class HttpRequestRecordFielder
	extends BaseHttpRequestRecordFielder<HttpRequestRecordKey,HttpRequestRecord>{

		public HttpRequestRecordFielder(){
			super(HttpRequestRecordKey::new);
		}

	}

	public HttpRequestRecord(){
		super(new HttpRequestRecordKey());
	}

	public HttpRequestRecord(String exceptionRecordId, Optional<W3TraceContext> traceContext,
			HttpServletRequest request, String sessionRoles, String userToken, boolean omitPayload){
		this(RequestAttributeTool.get(request, BaseHandler.REQUEST_RECEIVED_AT).orElse(new Date()),
				exceptionRecordId,
				traceContext.map(W3TraceContext::getTraceId).orElse(null),
				traceContext.map(W3TraceContext::getParentId).orElse(null),
				request.getMethod(),
				GsonTool.withUnregisteredEnums().toJson(request.getParameterMap()),
				request.getScheme(),
				request.getServerName(),
				request.getServerPort(),
				request.getContextPath(),
				getRequestPath(request),
				request.getQueryString(),
				omitPayload ? HttpRequestRecordDto.CONFIDENTIALITY_MSG_BYTES
						: RequestTool.tryGetBodyAsByteArray(request),
				RequestTool.getIpAddress(request),
				sessionRoles,
				userToken,
				new RecordedHttpHeaders(request));
	}

	private static String getRequestPath(HttpServletRequest request){
		String requestUri = request.getRequestURI();
		if(requestUri == null){
			return "";
		}
		return requestUri.substring(StringTool.nullSafe(request.getContextPath()).length());
	}

	public HttpRequestRecord(
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
		super(new HttpRequestRecordKey(new Ulid()), receivedAt, exceptionRecordId, traceId, parentId,
				httpMethod, httpParams, protocol, hostname, port, contextPath, path, queryString, binaryBody, ip,
				sessionRoles, userToken, headersWrapper);
	}

	public HttpRequestRecord(ExceptionAndHttpRequestDto exceptionDto, String exceptionRecordId){
		super(new HttpRequestRecordKey(new Ulid()), exceptionDto, exceptionRecordId);
	}

	public HttpRequestRecordDto toDto(){
		return new HttpRequestRecordDto(
				getKey().getId().value(),
				getCreated(),
				getReceivedAt(),
				getDuration(),

				getExceptionRecordId(),
				getTraceId(),
				getParentId(),

				getHttpMethod(),
				getHttpParams(),

				getProtocol(),
				getHostname(),
				getPort(),
				getContextPath(),
				getPath(),
				getQueryString(),
				getBinaryBody(),

				getIp(),
				getUserRoles(),
				getUserToken(),

				getAcceptCharset(),
				getAcceptEncoding(),
				getAcceptLanguage(),
				getAccept(),
				getCacheControl(),
				getConnection(),
				getContentEncoding(),
				getContentLanguage(),
				getContentLength(),
				getContentType(),
				getCookie(),
				getDnt(),
				getHost(),
				getIfModifiedSince(),
				getOrigin(),
				getPragma(),
				getReferer(),
				getUserAgent(),
				getxForwardedFor(),
				getxRequestedWith(),
				getOtherHeaders());
	}

	public static HttpRequestRecord createEmptyForTesting(){
		return new HttpRequestRecord(null, null, null, null, null, null, null, null, 0, null, null, null, null, null,
				null, null, new RecordedHttpHeaders((HttpServletRequest)null));
	}

	@Override
	public Supplier<HttpRequestRecordKey> getKeySupplier(){
		return HttpRequestRecordKey::new;
	}

	public ExceptionRecordKey getExceptionRecordKey(){
		return new ExceptionRecordKey(getExceptionRecordId());
	}

}

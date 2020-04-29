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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import io.datarouter.exception.storage.exceptionrecord.ExceptionRecord;
import io.datarouter.exception.storage.exceptionrecord.ExceptionRecordKey;
import io.datarouter.instrumentation.exception.HttpRequestRecordDto;
import io.datarouter.model.field.Field;
import io.datarouter.util.UuidTool;
import io.datarouter.util.serialization.GsonTool;
import io.datarouter.util.string.StringTool;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.monitoring.exception.ExceptionDto;
import io.datarouter.web.util.RequestAttributeTool;
import io.datarouter.web.util.http.RecordedHttpHeaders;
import io.datarouter.web.util.http.RequestTool;

public class HttpRequestRecord extends BaseHttpRequestRecord<HttpRequestRecordKey,HttpRequestRecord>{

	private static final byte[] CONFIDENTIALITY_MSG_BYTES = "body omitted for confidentiality".getBytes();

	public static class HttpRequestRecordFielder
	extends BaseHttpRequestRecordFielder<HttpRequestRecordKey,HttpRequestRecord>{

		public HttpRequestRecordFielder(){
			super(HttpRequestRecordKey.class);
		}

		@Override
		public Map<String,List<Field<?>>> getUniqueIndexes(HttpRequestRecord record){
			Map<String,List<Field<?>>> indexes = new TreeMap<>();
			indexes.put("unique_exceptionRecord", new HttpRequestRecordByExceptionRecord(record).getFields());
			return indexes;
		}

	}

	public HttpRequestRecord(){
		super(new HttpRequestRecordKey());
	}

	public HttpRequestRecord(String exceptionRecordId, HttpServletRequest request, String sessionRoles,
			String userToken, boolean omitPayload){
		this(RequestAttributeTool.get(request, BaseHandler.REQUEST_RECEIVED_AT).orElse(new Date()),
				exceptionRecordId,
				request.getMethod(),
				GsonTool.GSON.toJson(request.getParameterMap()),
				request.getScheme(),
				request.getServerName(),
				request.getServerPort(),
				request.getContextPath(),
				getRequestPath(request),
				request.getQueryString(),
				omitPayload ? CONFIDENTIALITY_MSG_BYTES : RequestTool.tryGetBodyAsByteArray(request),
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
		super(new HttpRequestRecordKey(UuidTool.generateV1Uuid()), receivedAt, exceptionRecordId, httpMethod,
				httpParams, protocol, hostname, port, contextPath, path, queryString, binaryBody, ip, sessionRoles,
				userToken, headersWrapper);
	}

	public HttpRequestRecord(ExceptionDto exceptionDto, String exceptionRecordId){
		super(new HttpRequestRecordKey(UuidTool.generateV1Uuid()), exceptionDto, exceptionRecordId);
	}

	public HttpRequestRecordDto toDto(){
		return new HttpRequestRecordDto(
				getKey().getId(),
				getCreated(),
				getReceivedAt(),
				getDuration(),

				getExceptionRecordId(),

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

	public static class HttpRequestRecordByExceptionRecord
	extends BaseHttpRequestRecordByExceptionRecord<
			HttpRequestRecordKey,
			HttpRequestRecord,ExceptionRecordKey,
			ExceptionRecord>{

		public HttpRequestRecordByExceptionRecord(HttpRequestRecord httpRequestRecord){
			super(httpRequestRecord);
		}

		public HttpRequestRecordByExceptionRecord(ExceptionRecord exceptionRecord){
			super(exceptionRecord);
		}

	}

	public static HttpRequestRecord createEmptyForTesting(){
		return new HttpRequestRecord(null, null, null, null, null, null, 0, null, null, null, null, null, null, null,
				new RecordedHttpHeaders((HttpServletRequest)null));
	}

	@Override
	public Class<HttpRequestRecordKey> getKeyClass(){
		return HttpRequestRecordKey.class;
	}

	public ExceptionRecordKey getExceptionRecordKey(){
		return new ExceptionRecordKey(getExceptionRecordId());
	}

}

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
package io.datarouter.instrumentation.exception;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.instrumentation.validation.DatarouterInstrumentationValidationConstants.ExceptionInstrumentationConstants;
import io.datarouter.instrumentation.validation.DatarouterInstrumentationValidationTool;

public record HttpRequestRecordDto(
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
		String environment)
implements TaskExecutionRecordDto{
	private static final Logger logger = LoggerFactory.getLogger(HttpRequestRecordDto.class);

	public static final byte[] CONFIDENTIALITY_MSG_BYTES = "body omitted for confidentiality".getBytes();

	public HttpRequestRecordDto trimmed(){
		return new HttpRequestRecordDto(
				id,
				created,
				receivedAt,
				duration,
				exceptionRecordId,
				traceId,
				parentId,
				httpMethod,
				trim(httpParams, ExceptionInstrumentationConstants.MAX_SIZE_HTTP_PARAMS),
				protocol,
				hostname,
				port,
				contextPath,
				trim(path, ExceptionInstrumentationConstants.MAX_SIZE_PATH),
				queryString,
				binaryBody,
				ip,
				userRoles,
				userToken,
				trim(acceptCharset, ExceptionInstrumentationConstants.MAX_SIZE_ACCEPT_CHARSET),
				acceptEncoding,
				trim(acceptLanguage, ExceptionInstrumentationConstants.MAX_SIZE_ACCEPT_LANGUAGE),
				trim(accept, ExceptionInstrumentationConstants.MAX_SIZE_ACCEPT),
				cacheControl,
				connection,
				contentEncoding,
				contentLanguage,
				contentLength,
				trim(contentType, ExceptionInstrumentationConstants.MAX_SIZE_CONTENT_TYPE),
				cookie,
				dnt,
				host,
				ifModifiedSince,
				trim(origin, ExceptionInstrumentationConstants.MAX_SIZE_ORIGIN),
				trim(pragma, ExceptionInstrumentationConstants.MAX_SIZE_PRAGMA),
				referer,
				userAgent,
				trim(xForwardedFor, ExceptionInstrumentationConstants.MAX_SIZE_X_FORWARDED_FOR),
				xRequestedWith,
				otherHeaders,
				environment);
	}

	private String trim(String string, int size){
		return DatarouterInstrumentationValidationTool.trimToSizeAndLog(string, size, logger, id);
	}
}

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

import io.datarouter.instrumentation.DatarouterInstrumentationStringTrimmingTool;

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
		String otherHeaders)
implements TaskExecutionRecordDto{
	private static final Logger logger = LoggerFactory.getLogger(HttpRequestRecordDto.class);

	public static final byte[] CONFIDENTIALITY_MSG_BYTES = "body omitted for confidentiality".getBytes();
	public static final int BINARY_BODY_MAX_SIZE = 10_000;

	public static final int MAX_LENGTH_CONTENT_TYPE = 255;
	public static final int MAX_LENGTH_ACCEPT_CHARSET = 255;
	public static final int MAX_LENGTH_X_FORWARDED_FOR = 255;
	public static final int MAX_LENGTH_PATH = 255;
	public static final int MAX_LENGTH_ACCEPT_LANGUAGE = 5_000;
	public static final int MAX_LENGTH_ORIGIN = 255;
	public static final int MAX_LENGTH_PRAGMA = 255;
	public static final int MAX_LENGTH_ACCEPT = 255;
	public static final int MAX_LENGTH_HTTP_PARAMS = 5_000;

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
				trim(httpParams, MAX_LENGTH_HTTP_PARAMS),
				protocol,
				hostname,
				port,
				contextPath,
				trim(path, MAX_LENGTH_PATH),
				queryString,
				binaryBody,
				ip,
				userRoles,
				userToken,
				trim(acceptCharset, MAX_LENGTH_ACCEPT_CHARSET),
				acceptEncoding,
				trim(acceptLanguage, MAX_LENGTH_ACCEPT_LANGUAGE),
				trim(accept, MAX_LENGTH_ACCEPT),
				cacheControl,
				connection,
				contentEncoding,
				contentLanguage,
				contentLength,
				trim(contentType, MAX_LENGTH_CONTENT_TYPE),
				cookie,
				dnt,
				host,
				ifModifiedSince,
				trim(origin, MAX_LENGTH_ORIGIN),
				trim(pragma, MAX_LENGTH_PRAGMA),
				referer,
				userAgent,
				trim(xForwardedFor, MAX_LENGTH_X_FORWARDED_FOR),
				xRequestedWith,
				otherHeaders);
	}

	private String trim(String string, int size){
		return DatarouterInstrumentationStringTrimmingTool.trimToSizeAndLog(string, size, logger, id);
	}
}

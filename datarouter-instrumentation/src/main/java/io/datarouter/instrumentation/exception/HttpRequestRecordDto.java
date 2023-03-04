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

	public static final byte[] CONFIDENTIALITY_MSG_BYTES = "body omitted for confidentiality".getBytes();
	public static final int BINARY_BODY_MAX_SIZE = 10_000;
	public static final int MAX_LENGTH_HTTP_PARAMS = 5_000;
}

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
package io.datarouter.web.monitoring.exception;

import java.util.Map;

public class ExceptionDto{

	public long dateMs;
	public String appName;
	public String serverName;
	public String stackTrace;
	public String errorClass;

	public String appVersion;
	public String errorLocation;
	public Long receivedAtMs;
	public String methodName;
	public Integer lineNumber;
	public String callOrigin;

	public String httpMethod;
	public Map<String,String[]> httpParams;
	public String protocol;
	public String hostname;
	public int port;
	public String path;
	public String queryString;
	public String body;

	public String ip;
	public String userRoles;
	public String userToken;

	public String acceptCharset;
	public String acceptEncoding;
	public String acceptLanguage;
	public String accept;
	public String cacheControl;
	public String connection;
	public String contentEncoding;
	public String contentLanguage;
	public String contentLength;
	public String contentType;
	public String cookie;
	public String dnt;
	public String host;
	public String ifModifiedSince;
	public String origin;
	public String pragma;
	public String referer;
	public String userAgent;
	public String forwardedFor; // X-Forwarded-For header
	public String requestedWith; // X-Requested-With header
	public Map<String,String[]> others;

}

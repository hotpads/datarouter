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
package io.datarouter.httpclient;

public class HttpHeaders{

	public static final String ACCEPT_CHARSET = "accept-charset";
	public static final String ACCEPT_ENCODING = "accept-encoding";
	public static final String ACCEPT_LANGUAGE = "accept-language";
	public static final String ACCEPT = "accept";
	public static final String CACHE_CONTROL = "cache-control";
	public static final String CONNECTION = "connection";
	public static final String CONTENT_ENCODING = "content-encoding";
	public static final String CONTENT_DISPOSITION = "content-disposition";
	public static final String CONTENT_LANGUAGE = "content-language";
	public static final String CONTENT_LENGTH = "content-length";
	public static final String CONTENT_SECURITY_POLICY = "content-security-policy";
	public static final String CONTENT_TYPE = "content-type";
	public static final String COOKIE = "cookie";
	public static final String DNT = "dnt";
	public static final String HOST = "host";
	public static final String IF_MODIFIED_SINCE = "if-modified-since";
	public static final String ORIGIN = "origin";
	public static final String PRAGMA = "pragma";
	public static final String REFERER = "referer";
	public static final String UPGRADE = "upgrade";
	public static final String USER_AGENT = "user-agent";
	public static final String SEC_WEBSOCKET_VERSION = "sec-websocket-version";
	public static final String X_FORWARDED_FOR = "x-forwarded-for";
	public static final String X_FRAME_OPTIONS = "x-frame-options";
	public static final String X_REQUESTED_WITH = "x-requested-with";

	// specific ones
	public static final String X_CLIENT_IP = IpExtractor.X_CLIENT_IP;
	public static final String X_EXCEPTION_ID = "x-eid";

}

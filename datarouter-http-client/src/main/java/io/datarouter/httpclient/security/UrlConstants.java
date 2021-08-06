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
package io.datarouter.httpclient.security;

public class UrlConstants{

	public static final int PORT_HTTP_STANDARD = 80;
	public static final int PORT_HTTPS_STANDARD = 443;
	public static final int PORT_HTTP_DEV = 8080;
	public static final int PORT_HTTPS_DEV = 8443;
	public static final String LOCAL_HOST = "localhost";
	public static final String LOCAL_DEV_SERVER = LOCAL_HOST + ":" + PORT_HTTP_DEV;
	public static final String LOCAL_DEV_SERVER_HTTPS = LOCAL_HOST + ":" + PORT_HTTPS_DEV;
	public static final String LOCAL_DEV_SERVER_URL = UrlScheme.HTTP.getStringRepresentation() + "://" + LOCAL_HOST
			+ ":" + PORT_HTTP_DEV;
	public static final String LOCAL_DEV_SERVER_HTTPS_URL = UrlScheme.HTTPS.getStringRepresentation() + "://"
			+ LOCAL_HOST + ":" + PORT_HTTPS_DEV;

}

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
package io.datarouter.httpclient.response;

public enum HttpStatusCode{
	SC_200_OK(200, "Ok"),
	SC_201_CREATED(201, "Created"),
	SC_202_ACCEPTED(202, "Accepted"),
	SC_301_MOVED_PERMANENTLY(301, "Moved Permanently"),
	SC_400_BAD_REQUEST(400, "Bad Request"),
	SC_401_UNATHORIZED(401, "Unauthorized"),
	SC_403_FORBIDDEN(403, "Forbidden"),
	SC_404_NOT_FOUND(404, "Not Found"),
	SC_405_METHOD_NOT_ALLOWED(405, "Method Not Allowed"),
	SC_408_REQUEST_TIMEOUT(408, "Request Timeout"),
	SC_409_CONFLICT(409, "Conflict"),
	SC_410_GONE(410, "Gone"),
	SC_422_UNPROCESSABLE_ENTITY(422, "Unprocessable Entity"),
	SC_424_FAILED_DEPENDENCY(424, "Failed Dependency"),
	SC_429_TOO_MANY_REQUESTS(429, "Too Many Requests"),
	SC_500_INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
	SC_502_BAD_GATEWAY(502, "Bad Gateway"),
	SC_503_SERVICE_UNAVAILABLE(503, "Service Unavailable"),
	SC_504_GATEWAY_TIMEOUT(504, "Gateway Timeout"),
	;

	private final int statusCode;
	private final String message;

	HttpStatusCode(int statusCode, String message){
		this.statusCode = statusCode;
		this.message = message;
	}

	public int getStatusCode(){
		return statusCode;
	}

	public String getMessage(){
		return message;
	}

}

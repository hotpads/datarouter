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
package io.datarouter.web.handler;

import java.util.Collections;
import java.util.List;

import io.datarouter.httpclient.HttpHeaders;
import io.datarouter.web.util.http.RequestTool;

public class IpDetectionHandler extends BaseHandler{

	@Handler(defaultHandler = true)
	public IpDetectionDto debugIpDetection(){
		IpDetectionDto ipDetectionDto = new IpDetectionDto();
		ipDetectionDto.clientIpHeaders = Collections.list(request.getHeaders(HttpHeaders.X_CLIENT_IP));
		ipDetectionDto.forwardedForHeaders = Collections.list(request.getHeaders(HttpHeaders.X_FORWARDED_FOR));
		ipDetectionDto.remoteAddr = request.getRemoteAddr();
		ipDetectionDto.detectedIp = RequestTool.getIpAddress(request);
		return ipDetectionDto;
	}

	@SuppressWarnings("unused") // used by serialization reflection
	private static class IpDetectionDto{
		private List<String> clientIpHeaders;
		private List<String> forwardedForHeaders;
		private String remoteAddr;
		public String detectedIp;
	}

}

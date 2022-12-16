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
package io.datarouter.web.port;

import io.datarouter.httpclient.security.UrlConstants;

public record PortIdentificationResult(
		int httpPort,
		int httpsPort,
		String errorMessage){

	public static PortIdentificationResult success(int httpPort, int httpsPort){
		return new PortIdentificationResult(httpPort, httpsPort, "∅");
	}

	public static PortIdentificationResult errorWithdefaults(String errorMessage){
		return new PortIdentificationResult(UrlConstants.PORT_HTTP_DEV, UrlConstants.PORT_HTTPS_DEV, errorMessage);
	}

}

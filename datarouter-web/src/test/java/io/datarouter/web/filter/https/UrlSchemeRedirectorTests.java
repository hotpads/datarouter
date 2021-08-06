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
package io.datarouter.web.filter.https;

import java.net.URL;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.httpclient.security.UrlConstants;
import io.datarouter.httpclient.security.UrlScheme;
import io.datarouter.web.port.PortIdentifier.DefaultPortIdentifier;

public class UrlSchemeRedirectorTests{

	private final UrlSchemeRedirector urlSchemeRedirector = new UrlSchemeRedirector(new DefaultPortIdentifier());

	private final String urlHttp = "http://x.com", urlHttps = "https://x.com", param = "/y?z=0",
			urlWithHttpPort = urlHttp + ":" + UrlConstants.PORT_HTTP_DEV + param, urlWithHttpsPort = urlHttps + ":"
					+ UrlConstants.PORT_HTTPS_DEV + param;

	@Test
	public void testGetRedirectUrlPortStringWithColon(){
		Assert.assertEquals(urlSchemeRedirector.getRedirectUrlPortStringWithColon(80, UrlScheme.HTTP), "");
		Assert.assertEquals(urlSchemeRedirector.getRedirectUrlPortStringWithColon(UrlConstants.PORT_HTTP_DEV,
				UrlScheme.HTTP), ":" + UrlConstants.PORT_HTTP_DEV);
		Assert.assertEquals(urlSchemeRedirector.getRedirectUrlPortStringWithColon(UrlConstants.PORT_HTTP_DEV,
				UrlScheme.HTTPS), ":" + UrlConstants.PORT_HTTPS_DEV);
		Assert.assertEquals(urlSchemeRedirector.getRedirectUrlPortStringWithColon(UrlConstants.PORT_HTTPS_STANDARD,
				UrlScheme.HTTP), "");
	}

	@Test
	public void testGetUriWithScheme() throws Exception{
		Assert.assertEquals(urlSchemeRedirector.getUriWithScheme(UrlScheme.HTTP, new URL(urlHttp + param)), urlHttp
				+ param);
		Assert.assertEquals(urlSchemeRedirector.getUriWithScheme(UrlScheme.HTTP, new URL(urlWithHttpPort)),
				urlWithHttpPort);
		Assert.assertEquals(urlSchemeRedirector.getUriWithScheme(UrlScheme.HTTPS, new URL(urlWithHttpPort)),
				urlWithHttpsPort);
		Assert.assertEquals(urlSchemeRedirector.getUriWithScheme(UrlScheme.HTTPS, new URL(urlHttp + param)), urlHttps
				+ param);
		Assert.assertEquals(urlSchemeRedirector.getUriWithScheme(UrlScheme.HTTPS, new URL(urlHttp)), urlHttps);
		Assert.assertEquals(urlSchemeRedirector.getUriWithScheme(UrlScheme.HTTP, new URL(urlHttp)), urlHttp);
	}

}

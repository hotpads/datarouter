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
package io.datarouter.web.util.http;

import javax.servlet.http.HttpServletRequest;

import org.testng.Assert;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import io.datarouter.httpclient.HttpHeaders;
import io.datarouter.web.test.DatarouterWebTestNgModuleFactory;
import jakarta.inject.Inject;

@Guice(moduleFactory = DatarouterWebTestNgModuleFactory.class)
public class IpAddressServiceTests{

	private static final String PRIVATE_IP = RequestToolTests.PRIVATE_IP;
	private static final String PUBLIC_IP = RequestToolTests.PUBLIC_IP;

	@Inject
	private IpAddressService ipAddressService;

	@Test
	public void testGetIpAddress(){
		// haproxy -> node -> haproxy -> tomcat
		HttpServletRequest request = new MockHttpServletRequestBuilder()
				.withHeader(HttpHeaders.X_FORWARDED_FOR, PRIVATE_IP)
				.withHeader(HttpHeaders.X_CLIENT_IP, PUBLIC_IP)
				.build();
		// alb -> haproxy -> node -> haproxy -> tomcat
		Assert.assertEquals(ipAddressService.getIpAddress(request, false), PUBLIC_IP);
		request = new MockHttpServletRequestBuilder()
				.withHeader(HttpHeaders.X_FORWARDED_FOR, PRIVATE_IP)
				.withHeader(HttpHeaders.X_CLIENT_IP, PUBLIC_IP + RequestTool.HEADER_VALUE_DELIMITER + PRIVATE_IP)
				.build();
		Assert.assertEquals(ipAddressService.getIpAddress(request, false), PUBLIC_IP);
		// haproxy -> tomcat
		request = new MockHttpServletRequestBuilder()
				.withHeader(HttpHeaders.X_FORWARDED_FOR, PUBLIC_IP)
				.build();
		Assert.assertEquals(ipAddressService.getIpAddress(request, false), PUBLIC_IP);
		// alb -> haproxy -> tomcat
		request = new MockHttpServletRequestBuilder()
				.withHeader(HttpHeaders.X_FORWARDED_FOR, PUBLIC_IP + RequestTool.HEADER_VALUE_DELIMITER + PRIVATE_IP)
				.build();
		Assert.assertEquals(ipAddressService.getIpAddress(request, false), PUBLIC_IP);
		// alb -> haproxy -> tomcat with two separate headers
		request = new MockHttpServletRequestBuilder()
				.withHeader(HttpHeaders.X_FORWARDED_FOR, PUBLIC_IP)
				.withHeader(HttpHeaders.X_FORWARDED_FOR, PRIVATE_IP)
				.build();
		Assert.assertEquals(ipAddressService.getIpAddress(request, false), PUBLIC_IP);

		// GCP Added LB forwarding IP
		request = new MockHttpServletRequestBuilder()
				.withHeader(HttpHeaders.X_FORWARDED_FOR, PUBLIC_IP + RequestTool.HEADER_VALUE_DELIMITER
						+ "34.120.253.44")
				.build();
		Assert.assertEquals(ipAddressService.getIpAddress(request, true), PUBLIC_IP);
	}

}

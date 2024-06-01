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
package io.datarouter.web.security;

import java.time.Duration;
import java.util.regex.Pattern;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.httpclient.security.DefaultCsrfGenerator;
import io.datarouter.httpclient.security.SecurityParameters;
import io.datarouter.web.util.http.MockHttpServletRequestBuilder;

public class DefaultCsrfValidatorTests{

	private static final DefaultCsrfGenerator CSRF_GENERATOR = new DefaultCsrfGenerator(() -> "foobar");
	public static final DefaultCsrfValidator CSRF_VALIDATOR = new DefaultCsrfValidator(
			CSRF_GENERATOR,
			Duration.ofMillis(500));

	@Test
	public void test(){
		validate(new MockHttpServletRequestBuilder(),
				false, "csrfToken not found in http request");
		validate(new MockHttpServletRequestBuilder()
						.withParameter(SecurityParameters.CSRF_TOKEN, "123"),
				false, "csrfIv not found in http request");
		validate(new MockHttpServletRequestBuilder()
						.withParameter(SecurityParameters.CSRF_TOKEN, "123")
						.withParameter(SecurityParameters.CSRF_IV, "123"),
				false, Pattern.quote("Bad key?"));

		String csrfIv = CSRF_GENERATOR.generateCsrfIv();
		String csrfToken = CSRF_GENERATOR.generateCsrfToken(csrfIv);
		validate(new MockHttpServletRequestBuilder()
						.withParameter(SecurityParameters.CSRF_TOKEN, csrfToken)
						.withParameter(SecurityParameters.CSRF_IV, csrfIv),
				true, null);

		csrfIv = CSRF_GENERATOR.generateCsrfIv();
		// expired token
		csrfToken = CSRF_GENERATOR.generateCsrfToken(csrfIv, System.currentTimeMillis() - 1000);
		validate(new MockHttpServletRequestBuilder()
						.withParameter(SecurityParameters.CSRF_TOKEN, csrfToken)
						.withParameter(SecurityParameters.CSRF_IV, csrfIv),
				false, ".*too old.*");
		}

	private void validate(
			MockHttpServletRequestBuilder mockHttpServletRequestBuilder,
			boolean success,
			String errorMessageRegex){
		CsrfValidationResult result = CSRF_VALIDATOR.check(mockHttpServletRequestBuilder.build());
		Assert.assertEquals(result.success(), success);
		if(!success){
			Assert.assertTrue(Pattern.matches(errorMessageRegex, result.errorMessage()));
		}
	}

}

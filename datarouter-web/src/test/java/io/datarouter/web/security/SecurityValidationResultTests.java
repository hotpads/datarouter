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

import javax.servlet.http.HttpServletRequest;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.web.util.http.MockHttpServletRequestBuilder;

public class SecurityValidationResultTests{

	@Test
	public void testCombinedWith(){
		SignatureValidator goodValidator = SecurityValidationResult::success;
		SignatureValidator badValidator = SecurityValidationResult::failure;
		SignatureValidator skippedValidator = _ -> {
			throw new RuntimeException();
		};

		HttpServletRequest request = new MockHttpServletRequestBuilder().build();
		SecurityValidationResult result = SecurityValidationResult.of(goodValidator::validate, request)
				.combinedWith(badValidator::validate)
				.combinedWith(skippedValidator::validate);
		Assert.assertFalse(result.isSuccess());
	}

	@Test
	public void testCreateMethods(){
		HttpServletRequest request = new MockHttpServletRequestBuilder().build();
		Assert.assertTrue(SecurityValidationResult.success(request).isSuccess());
		Assert.assertSame(SecurityValidationResult.success(request).getWrappedRequest(), request);
		Assert.assertNull(SecurityValidationResult.success(request).getFailureMessage());
		Assert.assertFalse(SecurityValidationResult.failure(request).isSuccess());
	}

}

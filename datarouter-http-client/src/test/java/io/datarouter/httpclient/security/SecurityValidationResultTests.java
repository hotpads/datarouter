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
package io.datarouter.httpclient.security;

import javax.servlet.http.HttpServletRequest;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SecurityValidationResultTests{

	@Test
	public void testCombinedWith(){
		SignatureValidator goodValidator = Mockito.mock(SignatureValidator.class);
		SignatureValidator badValidator = Mockito.mock(SignatureValidator.class);
		SignatureValidator skippedValidator = Mockito.mock(SignatureValidator.class);
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);

		Mockito.doReturn(SecurityValidationResult.success(request)).when(goodValidator).validate(request);
		Mockito.doReturn(SecurityValidationResult.failure(request)).when(badValidator).validate(request);
		Mockito.doReturn(SecurityValidationResult.success(request)).when(skippedValidator).validate(request);

		SecurityValidationResult.of(goodValidator::validate, request)
				.combinedWith(badValidator::validate)
				.combinedWith(skippedValidator::validate);

		Mockito.verify(goodValidator).validate(request);
		Mockito.verify(badValidator).validate(request);
		Mockito.verify(skippedValidator, Mockito.never()).validate(request);
	}

	@Test
	public void testCreateMethods(){
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		Assert.assertTrue(SecurityValidationResult.success(request).isSuccess());
		Assert.assertSame(SecurityValidationResult.success(request).getWrappedRequest(), request);
		Assert.assertNull(SecurityValidationResult.success(request).getFailureMessage());
		Assert.assertFalse(SecurityValidationResult.failure(request).isSuccess());
	}

}

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
package io.datarouter.auth.service;

import java.util.Optional;
import java.util.function.Supplier;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import io.datarouter.auth.storage.account.DatarouterAccountCredential;
import io.datarouter.httpclient.security.DefaultCsrfGenerator;
import io.datarouter.httpclient.security.SecurityParameters;
import io.datarouter.web.security.CsrfValidator;
import io.datarouter.web.security.DefaultCsrfValidator;
import io.datarouter.web.util.http.RequestTool;

public class DatarouterAccountCsrfValidator implements CsrfValidator{

	private final Long requestTimeoutMs;
	private final DatarouterAccountService datarouterAccountService;

	@Singleton
	public static class DatarouterAccountCsrfValidatorFactory{

		@Inject
		private DatarouterAccountService datarouterAccountService;

		public DatarouterAccountCsrfValidator create(Long requestTimeoutMs){
			return new DatarouterAccountCsrfValidator(requestTimeoutMs, datarouterAccountService);
		}
	}

	private DatarouterAccountCsrfValidator(Long requestTimeoutMs, DatarouterAccountService accountService){
		this.requestTimeoutMs = requestTimeoutMs;
		this.datarouterAccountService = accountService;
	}

	@Override
	public boolean check(HttpServletRequest request){
		return getCsrfValidatorForAccountWithApiKey(request)
				.map(csrfValidator -> csrfValidator.check(request))
				.orElse(false);
	}

	@Override
	public Long getRequestTimeMs(HttpServletRequest request){
		return getCsrfValidatorForAccountWithApiKey(request)
				.map(csrfValidator -> csrfValidator.getRequestTimeMs(request))
				.orElse(null);
	}

	private Optional<DefaultCsrfValidator> getCsrfValidatorForAccountWithApiKey(HttpServletRequest request){
		String apiKey = RequestTool.getParameterOrHeader(request, SecurityParameters.API_KEY);
		return datarouterAccountService.findAccountCredentialForApiKeyAuth(apiKey)
				.map(DatarouterAccountCredential::getSecretKey)
				.map(secretKey -> (Supplier<String>)(() -> secretKey))
				.map(secretKey -> new DefaultCsrfValidator(new DefaultCsrfGenerator(secretKey), requestTimeoutMs));
	}

}

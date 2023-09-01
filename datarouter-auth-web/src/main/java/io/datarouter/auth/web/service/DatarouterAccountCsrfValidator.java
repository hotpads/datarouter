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
package io.datarouter.auth.web.service;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Supplier;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.httpclient.security.DefaultCsrfGenerator;
import io.datarouter.httpclient.security.SecurityParameters;
import io.datarouter.web.security.CsrfValidator;
import io.datarouter.web.security.DefaultCsrfValidator;
import io.datarouter.web.util.http.RequestTool;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

public class DatarouterAccountCsrfValidator implements CsrfValidator{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterAccountCsrfValidator.class);

	private final Long requestTimeoutMs;
	private final String apiKeyFieldName;
	private final DatarouterAccountCredentialService datarouterAccountCredentialService;

	@Singleton
	public static class DatarouterAccountCsrfValidatorFactory{

		@Inject
		private DatarouterAccountCredentialService datarouterAccountCredentialService;

		/**
		 * {@link Deprecated} use create(Duration)
		 */
		@Deprecated
		public DatarouterAccountCsrfValidator create(Long requestTimeoutMs){
			return create(requestTimeoutMs, SecurityParameters.API_KEY);
		}

		public DatarouterAccountCsrfValidator create(Duration requestTimeout){
			return create(requestTimeout.toMillis(), SecurityParameters.API_KEY);
		}

		public DatarouterAccountCsrfValidator create(Long requestTimeoutMs, String apiKeyFieldName){
			return new DatarouterAccountCsrfValidator(requestTimeoutMs, apiKeyFieldName,
					datarouterAccountCredentialService);
		}

	}

	private DatarouterAccountCsrfValidator(Long requestTimeoutMs,
			String apiKeyFieldName,
			DatarouterAccountCredentialService datarouterAccountApiKeyService){
		this.requestTimeoutMs = requestTimeoutMs;
		this.apiKeyFieldName = apiKeyFieldName;
		this.datarouterAccountCredentialService = datarouterAccountApiKeyService;
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
		String apiKey = RequestTool.getParameterOrHeader(request, apiKeyFieldName);
		Optional<String> optionalSecretKey = datarouterAccountCredentialService.findSecretKeyForApiKeyAuth(apiKey);
		if(optionalSecretKey.isEmpty()){
			logger.warn("Missing account for apiKey={}", apiKey);
		}
		return optionalSecretKey
				.map(secretKey -> (Supplier<String>)() -> secretKey)
				.map(secretKey -> new DefaultCsrfValidator(new DefaultCsrfGenerator(secretKey), requestTimeoutMs));
	}

}

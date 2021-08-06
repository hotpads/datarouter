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
package io.datarouter.auth.service;

import java.util.Optional;
import java.util.function.Supplier;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.httpclient.security.DefaultSignatureGenerator;
import io.datarouter.httpclient.security.SecurityParameters;
import io.datarouter.web.security.DefaultSignatureValidator;
import io.datarouter.web.security.SecurityValidationResult;
import io.datarouter.web.security.SignatureValidator;
import io.datarouter.web.util.http.RequestTool;

@Singleton
public class DatarouterAccountSignatureValidator implements SignatureValidator{
	private static final Logger logger = LoggerFactory.getLogger(DatarouterAccountSignatureValidator.class);

	@Inject
	private DatarouterAccountCredentialService datarouterAccountCredentialService;

	@Override
	public SecurityValidationResult validate(HttpServletRequest request){
		String apiKey = RequestTool.getParameterOrHeader(request, SecurityParameters.API_KEY);
		Optional<String> optionalSecretKey = datarouterAccountCredentialService.findSecretKeyForApiKeyAuth(apiKey);
		if(optionalSecretKey.isEmpty()){
			logger.warn("Missing account for apiKey={}", apiKey);
		}
		return optionalSecretKey
				.map(secretKey -> (Supplier<String>)(() -> secretKey))
				.map(DefaultSignatureGenerator::new)
				.map(DefaultSignatureValidator::new)
				.map(signatureValidator -> signatureValidator.validate(request))
				.orElse(SecurityValidationResult.failure(request));
	}

}

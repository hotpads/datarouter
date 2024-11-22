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
import java.util.Base64;

import javax.crypto.Cipher;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.httpclient.security.DefaultCsrfGenerator;
import io.datarouter.httpclient.security.SecurityParameters;
import io.datarouter.util.duration.DatarouterDuration;

public class DefaultCsrfValidator implements CsrfValidator{
	private static final Logger logger = LoggerFactory.getLogger(DefaultCsrfValidator.class);

	private static final Duration DEFAULT_REQUEST_TIMEOUT = Duration.ofSeconds(10);

	private final DefaultCsrfGenerator generator;
	private final DatarouterDuration requestTimeout;

	public DefaultCsrfValidator(DefaultCsrfGenerator generator){
		this(generator, DEFAULT_REQUEST_TIMEOUT);
	}

	public DefaultCsrfValidator(DefaultCsrfGenerator generator, Duration requestTimeout){
		this.generator = generator;
		this.requestTimeout = new DatarouterDuration(requestTimeout);
	}

	@Override
	public CsrfValidationResult check(HttpServletRequest request){
		String csrfToken = getParameterOrHeader(request, SecurityParameters.CSRF_TOKEN);
		if(csrfToken == null){
			return new CsrfValidationResult(false, "csrfToken not found in http request");
		}
		byte[] csrfTokenbytes = Base64.getDecoder().decode(csrfToken);
		String cipherIv = getParameterOrHeader(request, SecurityParameters.CSRF_IV);
		if(cipherIv == null){
			return new CsrfValidationResult(false, "csrfIv not found in http request");
		}
		long requestTimeMs;
		try{
			Cipher cipher = generator.getCipher(Cipher.DECRYPT_MODE, cipherIv);
			requestTimeMs = Long.parseLong(new String(cipher.doFinal(csrfTokenbytes)));
		}catch(Exception e){
			logger.error("could not decrypt csrf token", e);
			return new CsrfValidationResult(false, "Bad key?");
		}
		DatarouterDuration age = DatarouterDuration.ageMs(requestTimeMs);
		boolean success = age.isShorterThan(requestTimeout);
		String errorMessages = null;
		if(!success){
			errorMessages = "CSRF token age too old: " + age + " (" + requestTimeMs + ")";
		}
		return new CsrfValidationResult(success, errorMessages);
	}

	private static String getParameterOrHeader(HttpServletRequest request, String key){
		String value = request.getParameter(key);
		return value != null ? value : request.getHeader(key);
	}

}

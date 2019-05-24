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
package io.datarouter.web.security;

import java.security.GeneralSecurityException;
import java.time.Duration;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.httpclient.security.DefaultCsrfGenerator;
import io.datarouter.httpclient.security.SecurityParameters;

public class DefaultCsrfValidator implements CsrfValidator{
	private static final Logger logger = LoggerFactory.getLogger(DefaultCsrfValidator.class);

	private static final Long DEFAULT_REQUEST_TIMEOUT_IN_MS = Duration.ofSeconds(10).toMillis();

	private final DefaultCsrfGenerator generator;
	private final long requestTimeoutMs;

	public DefaultCsrfValidator(DefaultCsrfGenerator generator){
		this(generator, DEFAULT_REQUEST_TIMEOUT_IN_MS);
	}

	public DefaultCsrfValidator(DefaultCsrfGenerator generator, Long requestTimeoutMs){
		this.generator = generator;
		this.requestTimeoutMs = requestTimeoutMs;
	}

	@Override
	public boolean check(HttpServletRequest request){
		Long requestTime = null;
		try{
			requestTime = getRequestTimeMs(request);
		}catch(Exception e){
			logger.warn("DefaultCsrfValidator failed check. Bad key?", e);
		}
		if(requestTime == null){
			return false;
		}
		return System.currentTimeMillis() < requestTime + requestTimeoutMs;
	}

	@Override
	public Long getRequestTimeMs(HttpServletRequest request){
		String csrfToken = getParameterOrHeader(request, SecurityParameters.CSRF_TOKEN);
		String cipherIv = getParameterOrHeader(request, SecurityParameters.CSRF_IV);
		try{
			Cipher aes = generator.getCipher(Cipher.DECRYPT_MODE, cipherIv);
			return Long.parseLong(new String(aes.doFinal(Base64.getDecoder().decode(csrfToken))));
		}catch(GeneralSecurityException e){
			throw new RuntimeException(e);
		}
	}

	private static String getParameterOrHeader(HttpServletRequest request, String key){
		String value = request.getParameter(key);
		return value != null ? value : request.getHeader(key);
	}

}

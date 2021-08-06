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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datarouter.httpclient.security.DefaultSignatureGenerator;
import io.datarouter.httpclient.security.RequestSignatureDto;
import io.datarouter.httpclient.security.SecurityParameters;
import io.datarouter.web.util.http.CachingHttpServletRequest;

public class DefaultSignatureValidator implements SignatureValidator{
	private static final Logger logger = LoggerFactory.getLogger(DefaultSignatureValidator.class);

	private final DefaultSignatureGenerator signatureGenerator;

	public DefaultSignatureValidator(DefaultSignatureGenerator signatureGenerator){
		this.signatureGenerator = signatureGenerator;
	}

	public boolean checkHexSignature(Map<String,String> params, HttpEntity entity, String candidateSignature){
		RequestSignatureDto hexSignatureReorder = signatureGenerator.getHexSignature(params, entity);
		if(hexSignatureReorder.signature.equals(candidateSignature)){
			return true;
		}
		RequestSignatureDto hexSignatureNoReorder = signatureGenerator.getHexSignatureWithoutSettingParameterOrder(
				params, entity);
		boolean match = hexSignatureNoReorder.signature.equals(candidateSignature);
		if(match){
			if(!params.isEmpty()){
				logger.warn("signature was not generated with ordered param");
			}
		}else{
			logger.warn("got={} expectedNoReorder={} expectedReorder={} partsNoReorder={} partsReorder={}",
					candidateSignature, hexSignatureNoReorder.signature, hexSignatureReorder.signature,
					hexSignatureNoReorder.originalSignatureParts, hexSignatureReorder.originalSignatureParts);
		}
		return match;
	}

	private boolean checkHexSignatureMulti(HttpServletRequest request, HttpEntity entity){
		String parameter = getParameterOrHeader(request, SecurityParameters.SIGNATURE);
		Map<String,String> params = multiToSingle(request.getParameterMap());
		return checkHexSignature(params, entity, parameter);
	}

	@Override
	public SecurityValidationResult validate(HttpServletRequest request){
		if(isFormPost(request) || "GET".equalsIgnoreCase(request.getMethod())){
			boolean result = checkHexSignatureMulti(request, null);
			return new SecurityValidationResult(request, result, null);
		}

		HttpEntity entity;
		try{
			Optional<CachingHttpServletRequest> cachingRequestOptional = CachingHttpServletRequest.get(request);
			if(cachingRequestOptional.isEmpty()){
				cachingRequestOptional = Optional.of(CachingHttpServletRequest.getOrCreate(request));
				request = cachingRequestOptional.get();
			}
			entity = new ByteArrayEntity(cachingRequestOptional.get().getContent());
		}catch(IOException e){
			throw new RuntimeException(e);
		}

		boolean result = checkHexSignatureMulti(request, entity);
		return new SecurityValidationResult(request, result, null);
	}

	private static String getParameterOrHeader(HttpServletRequest request, String key){
		String value = request.getParameter(key);
		return value != null ? value : request.getHeader(key);
	}

	private boolean isFormPost(HttpServletRequest request){
		String contentType = request.getContentType();
		boolean isFormContent = contentType != null
				&& contentType.contains(ContentType.APPLICATION_FORM_URLENCODED.getMimeType());
		return isFormContent && "POST".equalsIgnoreCase(request.getMethod());
	}

	private Map<String,String> multiToSingle(Map<String,String[]> data){
		Map<String,String> map = new HashMap<>();
		for(Entry<String,String[]> entry : data.entrySet()){
			map.put(entry.getKey(), entry.getValue()[0]);
		}
		return map;
	}

}

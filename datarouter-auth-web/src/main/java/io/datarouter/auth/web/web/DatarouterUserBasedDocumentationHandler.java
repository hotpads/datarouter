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
package io.datarouter.auth.web.web;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.http.entity.StringEntity;

import io.datarouter.auth.service.DatarouterAccountUserService;
import io.datarouter.auth.web.service.DatarouterAccountCredentialService;
import io.datarouter.httpclient.security.DefaultCsrfGenerator;
import io.datarouter.httpclient.security.DefaultSignatureGenerator;
import io.datarouter.httpclient.security.SecurityParameters;
import io.datarouter.util.string.StringTool;
import io.datarouter.web.config.DatarouterWebFiles;
import io.datarouter.web.dispatcher.RouteSet;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.documentation.ApiDocService;
import io.datarouter.web.handler.documentation.DocumentedEndpointJspDto;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.util.http.RequestTool;
import jakarta.inject.Inject;

public abstract class DatarouterUserBasedDocumentationHandler extends BaseHandler{

	@Inject
	private DatarouterAccountCredentialService datarouterAccountCredentialService;
	@Inject
	private DatarouterAccountUserService datarouterAccountUserService;
	@Inject
	private ApiDocService apiDocService;
	@Inject
	private DatarouterWebFiles files;

	protected Mav createDocumentationMav(String apiName, String apiUrlContext, List<RouteSet> routeSets){
		List<DocumentedEndpointJspDto> endpoints = apiDocService.buildDocumentation(apiUrlContext, routeSets);
		Mav model = new Mav(files.jsp.docs.dispatcherDocsJsp);
		model.put("endpoints", endpoints);
		model.put("apiName", apiName);
		model.put("hideAuth", false);

		getSessionInfo().getSession().ifPresent(session -> {
			model.put("apiKeyParameterName", SecurityParameters.API_KEY);
			datarouterAccountUserService.findFirstAccountCredentialForUser(session)
					.ifPresent(credential -> model.put("apiKey", credential.getKey().getApiKey()));
		});
		model.put("hideAuth", true);
		return model;
	}

	@Handler
	public Map<String,String> getCsrfIv(){
		String apiKeyFieldName = request.getHeader("X-apiKeyFieldName");
		Map<String,String> result = RequestTool.getParamMap(request);
		Optional<String> secret = datarouterAccountCredentialService.findSecretKeyForApiKeyAuth(result.get(
				apiKeyFieldName));
		if(secret.isEmpty()){
			return result;
		}
		var csrfGenerator = new DefaultCsrfGenerator(secret::get);
		String csrfIv = csrfGenerator.generateCsrfIv();
		String csrfToken = csrfGenerator.generateCsrfToken(csrfIv);
		result.put("csrfIv", csrfIv);
		result.put("csrfToken", csrfToken);
		return result;
	}

	@Handler
	public Map<String,String> getSignature(){
		String apiKeyFieldName = request.getHeader("X-apiKeyFieldName");
		Map<String,String> params = RequestTool.getParamMap(request);
		String body = RequestTool.getBodyAsString(request);
		Optional<String> secret = datarouterAccountCredentialService.findSecretKeyForApiKeyAuth(params.get(
				apiKeyFieldName));
		if(secret.isEmpty()){
			return params;
		}
		var signatureGenerator = new DefaultSignatureGenerator(secret::get);
		if(StringTool.isNullOrEmpty(body)){
			params.put("signature", signatureGenerator.getHexSignature(params).signature);
		}else{
			StringEntity entity = new StringEntity(body, StandardCharsets.UTF_8);
			params.put("signature", signatureGenerator.getHexSignature(params, entity).signature);
		}
		return params;
	}

}

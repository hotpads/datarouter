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
import java.util.Map;
import java.util.Optional;

import org.apache.http.entity.StringEntity;

import io.datarouter.auth.web.link.DatarouterDocsLink;
import io.datarouter.auth.web.service.DatarouterAccountCredentialService;
import io.datarouter.httpclient.endpoint.link.DatarouterLinkClient;
import io.datarouter.httpclient.security.DefaultCsrfGenerator;
import io.datarouter.httpclient.security.DefaultSignatureGenerator;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.properties.ServiceName;
import io.datarouter.util.string.StringTool;
import io.datarouter.web.config.RouteSetRegistry;
import io.datarouter.web.handler.documentation.DocumentationRouteSet;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.handler.mav.imp.GlobalRedirectMav;
import io.datarouter.web.util.http.RequestTool;
import jakarta.inject.Inject;

public class DatarouterDocumentationHandler extends DatarouterUserBasedDocumentationHandler{

	@Inject
	private RouteSetRegistry routeSetRegistry;
	@Inject
	private ServiceName serviceName;
	@Inject
	private DatarouterAccountCredentialService datarouterAccountCredentialService;
	@Inject
	private DatarouterLinkClient datarouterLinkClient;

	@Handler
	public Mav docs(Optional<String> endpoint){
		if(endpoint.isPresent()){
			return Scanner.of(routeSetRegistry.get())
					.include(clazz -> clazz instanceof DocumentationRouteSet)
					.listTo(routeSets -> createApiMav(serviceName.get(), endpoint.get(), routeSets));
		}
		return Scanner.of(routeSetRegistry.get())
				.include(clazz -> clazz instanceof DocumentationRouteSet)
				.listTo(routeSets -> createDocumentationMav(serviceName.get(), routeSets));
	}

	@Handler
	public Mav docsV2(Optional<String> endpoint){
		return new GlobalRedirectMav(datarouterLinkClient.toInternalUrl(new DatarouterDocsLink()
						.withOptEndpoint(endpoint)));
	}

	@Handler
	public Mav schema(){
		return Scanner.of(routeSetRegistry.get())
				.include(clazz -> clazz instanceof DocumentationRouteSet)
				.listTo(routeSets -> createSchemaMav(serviceName.get(), "", routeSets));
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

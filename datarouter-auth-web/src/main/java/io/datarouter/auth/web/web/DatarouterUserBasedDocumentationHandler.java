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

import static j2html.TagCreator.script;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import io.datarouter.auth.service.DatarouterAccountUserService;
import io.datarouter.httpclient.endpoint.link.DatarouterLinkClient;
import io.datarouter.httpclient.security.SecurityParameters;
import io.datarouter.instrumentation.web.ContextName;
import io.datarouter.scanner.Scanner;
import io.datarouter.web.dispatcher.RouteSet;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.documentation.ApiDocSchemaDto;
import io.datarouter.web.handler.documentation.ApiDocSchemaService;
import io.datarouter.web.handler.documentation.ApiDocSchemaTool;
import io.datarouter.web.handler.documentation.ApiDocService;
import io.datarouter.web.handler.documentation.DatarouterJsonApiSchemaHtml;
import io.datarouter.web.handler.documentation.DocumentedEndpointJspDto;
import io.datarouter.web.handler.mav.Mav;
import io.datarouter.web.html.j2html.bootstrap4.Bootstrap4PageFactory;
import jakarta.inject.Inject;

public abstract class DatarouterUserBasedDocumentationHandler extends BaseHandler{

	@Inject
	private DatarouterAccountUserService datarouterAccountUserService;
	@Inject
	private ApiDocService apiDocService;
	@Inject
	private ApiDocSchemaService apiDocSchemaService;
	@Inject
	private DatarouterLinkClient datarouterLinkClient;
	@Inject
	private Bootstrap4PageFactory pageFactory;
	@Inject
	private ContextName contextName;

	protected Mav createDocumentationMav(String apiName, List<RouteSet> routeSets){
		Map<String,List<DocumentedEndpointJspDto>> endpointsByDispatchType =
				apiDocService.buildDocumentation("", routeSets);
		var builder = pageFactory.startBuilder(request)
				.withTitle(apiName + " API Documentation")
				.withContent(DatarouterApiIndexPageHtml.makeApiIndexContent(endpointsByDispatchType));
		return builder.buildMav();
	}

	protected Mav createSchemaMav(String apiName, String apiUrlContext, List<RouteSet> routeSets){
		List<ApiDocSchemaDto> schemas = apiDocSchemaService.buildSchemas(apiUrlContext, routeSets);
		var builder = pageFactory.startBuilder(request)
				.withTitle(apiName + " Schema Documentation")
				.withContent(DatarouterJsonApiSchemaHtml.makeSchemaContent(schemas, apiName));
		return builder.buildMav();
	}

	protected Mav createApiMav(String apiName, String apiPath, List<RouteSet> routeSets){
		Map<String,List<DocumentedEndpointJspDto>> endpointsByDispatchType =
				apiDocService.buildDocumentation("", routeSets);
		AtomicReference<String> apiKeyParameterName = new AtomicReference<>("apiKey");
		AtomicReference<String> apiKey = new AtomicReference<>();
		getSessionInfo().getSession().ifPresent(session -> {
			apiKeyParameterName.set(SecurityParameters.API_KEY);
			datarouterAccountUserService.findFirstAccountCredentialForUser(session)
					.ifPresent(credential -> apiKey.set(credential.getKey().getApiKey()));
		});
		var html = new DatarouterApiDocumentationPageHtml(
				apiName,
				endpointsByDispatchType,
				true,
				apiKeyParameterName.get(),
				apiKey.get(),
				contextName.getContextPath(),
				buildSchemas(apiPath, endpointsByDispatchType),
				datarouterLinkClient)
				.build(apiPath);
		var builder = pageFactory.startBuilder(request)
				.withTitle(apiName + " API Documentation")
				.withScript(script().withSrc(contextName.getContextPath() + "/js/docs/apiDocs.js"))
				.withContent(html);
		return builder.buildMav();
	}

	private List<ApiDocSchemaDto> buildSchemas(String apiPath, Map<String,
			List<DocumentedEndpointJspDto>> endpointsByDispatchType){
		Map<String,ApiDocSchemaDto> schemas = new HashMap<>();
		for(List<DocumentedEndpointJspDto> endpoints : endpointsByDispatchType.values()){
			for(DocumentedEndpointJspDto endpoint : endpoints){
				if(!endpoint.getUrl().equals(apiPath)){
					continue;
				}
				endpoint.getParameters().forEach(param -> {
					if(param.getSchema() != null){
						ApiDocSchemaTool.buildAllSchemas(param.getSchema(), schemas);
					}
				});
				Optional.ofNullable(endpoint.getResponse().getSchema())
						.ifPresent(schema -> ApiDocSchemaTool.buildAllSchemas(schema, schemas));
			}
		}
		return Scanner.of(schemas.values())
				.sort(Comparator.comparing(ApiDocSchemaDto::toSimpleClassName))
				.list();
	}
}

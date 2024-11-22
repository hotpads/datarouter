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

import java.util.List;
import java.util.Map;

import io.datarouter.auth.service.DatarouterAccountUserService;
import io.datarouter.httpclient.security.SecurityParameters;
import io.datarouter.web.config.DatarouterWebFiles;
import io.datarouter.web.dispatcher.RouteSet;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.documentation.ApiDocService;
import io.datarouter.web.handler.documentation.DocumentedEndpointJspDto;
import io.datarouter.web.handler.mav.Mav;
import jakarta.inject.Inject;

public abstract class DatarouterUserBasedDocumentationHandler extends BaseHandler{

	@Inject
	private DatarouterAccountUserService datarouterAccountUserService;
	@Inject
	private ApiDocService apiDocService;
	@Inject
	private DatarouterWebFiles files;

	protected Mav createDocumentationMav(String apiName, String apiUrlContext, List<RouteSet> routeSets){
		Map<String,List<DocumentedEndpointJspDto>> endpointsByDispatchType =
				apiDocService.buildDocumentation(apiUrlContext, routeSets);
		Mav model = new Mav(files.jsp.docs.dispatcherDocsJsp);
		model.put("endpointsByDispatchType", endpointsByDispatchType);
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

}

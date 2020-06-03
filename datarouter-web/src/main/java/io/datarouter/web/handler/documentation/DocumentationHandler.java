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
package io.datarouter.web.handler.documentation;

import java.util.List;

import javax.inject.Inject;

import io.datarouter.web.config.DatarouterWebFiles;
import io.datarouter.web.dispatcher.BaseRouteSet;
import io.datarouter.web.handler.BaseHandler;
import io.datarouter.web.handler.mav.Mav;

public class DocumentationHandler extends BaseHandler{

	@Inject
	private ApiDocService service;
	@Inject
	private DatarouterWebFiles files;

	protected Mav createDocumentationMav(String apiName, String apiUrlContext, List<BaseRouteSet> routeSets){
		List<DocumentedEndpointJspDto> endpoints = service.buildDocumentation(apiUrlContext, routeSets);
		Mav model = new Mav(files.jsp.docs.dispatcherDocsJsp);
		model.put("endpoints", endpoints);
		model.put("apiName", apiName);
		model.put("hideAuth", false);
		return model;
	}

}

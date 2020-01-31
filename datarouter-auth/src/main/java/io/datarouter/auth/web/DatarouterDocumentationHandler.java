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
package io.datarouter.auth.web;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.datarouter.httpclient.client.DatarouterService;
import io.datarouter.web.config.RootRouteSetsSupplier;
import io.datarouter.web.dispatcher.BaseRouteSet;
import io.datarouter.web.handler.mav.Mav;

public class DatarouterDocumentationHandler extends DatarouterUserBasedDocumentationHandler{

	@Inject
	private RootRouteSetsSupplier routeSetsSupplier;
	@Inject
	private DatarouterService datarouterService;

	@Handler(defaultHandler = true)
	public Mav viewDocumentation(){
		List<BaseRouteSet> routeSets = routeSetsSupplier.get().stream()
				.filter(clazz -> clazz instanceof DocumentationRouteSet)
				.collect(Collectors.toList());
		return createDocumentationMav(datarouterService.getName(), "", routeSets);
	}

}

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

import io.datarouter.scanner.Scanner;
import io.datarouter.storage.config.properties.ServiceName;
import io.datarouter.web.config.RouteSetRegistry;
import io.datarouter.web.handler.documentation.DocumentationRouteSet;
import io.datarouter.web.handler.mav.Mav;
import jakarta.inject.Inject;

public class DatarouterDocumentationHandler extends DatarouterUserBasedDocumentationHandler{

	@Inject
	private RouteSetRegistry routeSetRegistry;
	@Inject
	private ServiceName serviceName;

	@Handler
	public Mav docs(){
		return Scanner.of(routeSetRegistry.get())
				.include(clazz -> clazz instanceof DocumentationRouteSet)
				.listTo(routeSets -> createDocumentationMav(serviceName.get(), "", routeSets));
	}

}

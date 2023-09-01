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
package io.datarouter.auth.web.config;

import io.datarouter.scanner.Scanner;
import io.datarouter.web.config.RouteSetRegistry;
import io.datarouter.web.handler.documentation.DocumentationRouteSet;
import io.datarouter.web.navigation.AppNavBarCategory;
import io.datarouter.web.navigation.DynamicNavBarItem;
import io.datarouter.web.navigation.NavBarCategory.NavBarItemType;
import io.datarouter.web.navigation.NavBarItem;
import jakarta.inject.Inject;

public class ApiDocsNavBarItem implements DynamicNavBarItem{

	@Inject
	private DatarouterAuthPaths paths;
	@Inject
	private RouteSetRegistry routeSetRegistry;

	@Override
	public NavBarItem getNavBarItem(){
		return new NavBarItem(AppNavBarCategory.DOCS, paths.docs.join("/", "/", "/"), "Api Docs");
	}

	@Override
	public Boolean shouldDisplay(){
		return Scanner.of(routeSetRegistry.get())
				.anyMatch(clazz -> clazz instanceof DocumentationRouteSet);
	}

	@Override
	public NavBarItemType getType(){
		return NavBarItemType.APP;
	}

}

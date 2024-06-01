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
package io.datarouter.graphql.config;

import io.datarouter.graphql.listener.GraphQlAppListener;
import io.datarouter.web.config.BaseWebPlugin;
import io.datarouter.web.navigation.AppNavBarCategory;

public class DatarouterGraphQlPlugin extends BaseWebPlugin{

	private static final DatarouterGraphQlPaths PATHS = new DatarouterGraphQlPaths();

	public DatarouterGraphQlPlugin(){
		addSettingRoot(DatarouterGraphQlSettingsRoot.class);
		addAppListener(GraphQlAppListener.class);
		addRouteSet(DatarouterGraphQlRouteSet.class);
		addAppNavBarItem(AppNavBarCategory.DOCS, PATHS.graphql.playground.join("/", "/", "/"), "GraphQl Playground");
		addTestable(DatarouterGraphQlSchemaIntegrationService.class);
	}

}

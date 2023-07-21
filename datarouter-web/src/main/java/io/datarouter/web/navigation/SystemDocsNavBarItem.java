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
package io.datarouter.web.navigation;

import io.datarouter.web.config.DatarouterWebPaths;
import io.datarouter.web.navigation.NavBarCategory.NavBarItemType;
import io.datarouter.web.service.DocumentationNamesAndLinksSupplier;
import jakarta.inject.Inject;

public class SystemDocsNavBarItem implements DynamicNavBarItem{

	@Inject
	private DocumentationNamesAndLinksSupplier docNameAndLinksSupplier;
	@Inject
	private DatarouterWebPaths paths;

	@Override
	public NavBarItem getNavBarItem(){
		return new NavBarItem(AppNavBarCategory.DOCS, paths.documentation.systemDocs, "System Docs");
	}

	@Override
	public Boolean shouldDisplay(){
		return docNameAndLinksSupplier.getSystemDocs().size() != 0;
	}

	@Override
	public NavBarItemType getType(){
		return NavBarItemType.APP;
	}

}

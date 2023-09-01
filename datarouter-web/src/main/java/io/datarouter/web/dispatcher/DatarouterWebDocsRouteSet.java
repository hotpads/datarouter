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
package io.datarouter.web.dispatcher;

import io.datarouter.auth.role.DatarouterUserRole;
import io.datarouter.storage.tag.Tag;
import io.datarouter.web.browse.ViewReadmesHandler;
import io.datarouter.web.browse.ViewSystemDocsHandler;
import io.datarouter.web.config.DatarouterWebPaths;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterWebDocsRouteSet extends BaseRouteSet{

	@Inject
	public DatarouterWebDocsRouteSet(DatarouterWebPaths paths){
		handle(paths.documentation.readmes).withHandler(ViewReadmesHandler.class);
		handle(paths.documentation.systemDocs).withHandler(ViewSystemDocsHandler.class);
	}

	@Override
	protected DispatchRule applyDefault(DispatchRule rule){
		return rule
				.allowRoles(
						DatarouterUserRole.USER,
						DatarouterUserRole.DOC_USER,
						DatarouterUserRole.DATAROUTER_ADMIN)
				.withTag(Tag.DATAROUTER);
	}

}

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
package io.datarouter.changelog.config;

import io.datarouter.auth.role.DatarouterUserRoleRegistry;
import io.datarouter.changelog.web.EditChangelogHandler;
import io.datarouter.changelog.web.ManualChangelogHandler;
import io.datarouter.changelog.web.ViewChangelogForDateRangeHandler;
import io.datarouter.changelog.web.ViewChangelogHandler;
import io.datarouter.changelog.web.ViewExactChangelogHandler;
import io.datarouter.storage.tag.Tag;
import io.datarouter.web.dispatcher.BaseRouteSet;
import io.datarouter.web.dispatcher.DispatchRule;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterChangelogRouteSet extends BaseRouteSet{

	@Inject
	public DatarouterChangelogRouteSet(DatarouterChangelogPaths paths){
		handle(paths.datarouter.changelog.edit).withHandler(EditChangelogHandler.class);
		handle(paths.datarouter.changelog.insert).withHandler(ManualChangelogHandler.class);
		handle(paths.datarouter.changelog.viewExact).withHandler(ViewExactChangelogHandler.class);
		handle(paths.datarouter.changelog.viewAll).withHandler(ViewChangelogHandler.class);
		handle(paths.datarouter.changelog.viewForDateRange).withHandler(ViewChangelogForDateRangeHandler.class);
	}

	@Override
	protected DispatchRule applyDefault(DispatchRule rule){
		return rule
				.allowRoles(
						DatarouterUserRoleRegistry.DATAROUTER_ADMIN,
						DatarouterUserRoleRegistry.DATAROUTER_MONITORING)
				.withTag(Tag.DATAROUTER);
	}

}

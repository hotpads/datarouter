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
package io.datarouter.changelog.config;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.changelog.web.ManualChangelogHandler;
import io.datarouter.changelog.web.ViewChangelogForDateRangeHandler;
import io.datarouter.changelog.web.ViewChangelogHandler;
import io.datarouter.changelog.web.ViewExactChangelogHandler;
import io.datarouter.web.dispatcher.BaseRouteSet;
import io.datarouter.web.dispatcher.DispatchRule;
import io.datarouter.web.user.role.DatarouterUserRole;

@Singleton
public class DatarouterChangelogRouteSet extends BaseRouteSet{

	@Inject
	public DatarouterChangelogRouteSet(DatarouterChangelogPaths paths){
		super(paths.datarouter.changelog);
		handle(paths.datarouter.changelog.viewExact).withHandler(ViewExactChangelogHandler.class);
		handleDir(paths.datarouter.changelog.insert).withHandler(ManualChangelogHandler.class);
		handle(paths.datarouter.changelog.viewAll).withHandler(ViewChangelogHandler.class);
		handle(paths.datarouter.changelog.viewForDateRange).withHandler(ViewChangelogForDateRangeHandler.class);
	}

	@Override
	protected DispatchRule applyDefault(DispatchRule rule){
		return rule
				.allowRoles(DatarouterUserRole.DATAROUTER_ADMIN, DatarouterUserRole.DATAROUTER_MONITORING)
				.withIsSystemDispatchRule(true);
	}

}

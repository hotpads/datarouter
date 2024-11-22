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
package io.datarouter.webappinstance.config;

import io.datarouter.auth.role.DatarouterUserRoleRegistry;
import io.datarouter.storage.tag.Tag;
import io.datarouter.web.dispatcher.BaseRouteSet;
import io.datarouter.web.dispatcher.DispatchRule;
import io.datarouter.webappinstance.web.WebappInstanceHistoryHandler;
import io.datarouter.webappinstance.web.WebappInstanceRunningHandler;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterWebappInstanceRouteSet extends BaseRouteSet{

	@Inject
	public DatarouterWebappInstanceRouteSet(DatarouterWebappInstancePaths paths){
		handle(paths.datarouter.webappInstances.running).withHandler(WebappInstanceRunningHandler.class);
		handle(paths.datarouter.webappInstances.history).withHandler(WebappInstanceHistoryHandler.class);
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

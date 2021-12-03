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
package io.datarouter.plugin.copytable.config;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.datarouter.plugin.copytable.web.JobletCopyTableHandler;
import io.datarouter.plugin.copytable.web.JobletTableProcessorHandler;
import io.datarouter.plugin.copytable.web.SingleThreadCopyTableHandler;
import io.datarouter.plugin.copytable.web.SingleThreadTableProcessorHandler;
import io.datarouter.web.dispatcher.BaseRouteSet;
import io.datarouter.web.dispatcher.DispatchRule;
import io.datarouter.web.user.role.DatarouterUserRole;

@Singleton
public class DatarouterCopyTableRouteSet extends BaseRouteSet{

	@Inject
	public DatarouterCopyTableRouteSet(DatarouterCopyTablePaths paths){
		super(paths.datarouter);
		handleDir(paths.datarouter.copyTableJoblets).withHandler(JobletCopyTableHandler.class);
		handleDir(paths.datarouter.copyTableSingleThread).withHandler(SingleThreadCopyTableHandler.class);
		handleDir(paths.datarouter.tableProcessorJoblets).withHandler(JobletTableProcessorHandler.class);
		handleDir(paths.datarouter.tableProcessorSingleThread).withHandler(SingleThreadTableProcessorHandler.class);
	}

	@Override
	protected DispatchRule applyDefault(DispatchRule rule){
		return rule
				.allowRoles(DatarouterUserRole.DATAROUTER_ADMIN)
				.withIsSystemDispatchRule(true);
	}

}

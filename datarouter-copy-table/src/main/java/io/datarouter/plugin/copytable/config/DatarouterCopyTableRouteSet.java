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

import io.datarouter.auth.role.DatarouterUserRoleRegistry;
import io.datarouter.plugin.copytable.web.JobletCopyTableHandler;
import io.datarouter.plugin.copytable.web.JobletTableProcessorHandler;
import io.datarouter.plugin.copytable.web.MigrateSystemTablesHandler;
import io.datarouter.plugin.copytable.web.MigrateSystemTablesMetadataHandler;
import io.datarouter.plugin.copytable.web.SingleThreadCopyTableHandler;
import io.datarouter.plugin.copytable.web.SingleThreadTableProcessorHandler;
import io.datarouter.plugin.copytable.web.SystemTablesCopyHandler;
import io.datarouter.plugin.copytable.web.SystemTablesListHandler;
import io.datarouter.storage.tag.Tag;
import io.datarouter.web.dispatcher.BaseRouteSet;
import io.datarouter.web.dispatcher.DispatchRule;
import io.datarouter.web.handler.encoder.DatarouterDefaultHandlerCodec;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DatarouterCopyTableRouteSet extends BaseRouteSet{

	@Inject
	public DatarouterCopyTableRouteSet(DatarouterCopyTablePaths paths){
		registerHandler(JobletCopyTableHandler.class);
		registerHandler(SingleThreadCopyTableHandler.class);
		registerHandler(JobletTableProcessorHandler.class);
		registerHandler(SingleThreadTableProcessorHandler.class);
		registerHandler(SystemTablesCopyHandler.class);
		registerHandler(SystemTablesListHandler.class);
		registerHandler(MigrateSystemTablesHandler.class);
		registerHandler(MigrateSystemTablesMetadataHandler.class);
	}

	@Override
	protected DispatchRule applyDefault(DispatchRule rule){
		return rule
				.allowRoles(DatarouterUserRoleRegistry.DATAROUTER_ADMIN)
				.withDefaultHandlerCodec(DatarouterDefaultHandlerCodec.INSTANCE)
				.withTag(Tag.DATAROUTER);
	}

}
